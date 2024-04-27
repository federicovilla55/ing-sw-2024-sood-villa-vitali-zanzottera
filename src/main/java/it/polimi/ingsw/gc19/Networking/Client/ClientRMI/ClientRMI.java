package it.polimi.ingsw.gc19.Networking.Client.ClientRMI;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.ReconnectToServerMessage;
import it.polimi.ingsw.gc19.Networking.Client.MessageHandler;
import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Settings;
import it.polimi.ingsw.gc19.Networking.Server.VirtualGameServer;
import it.polimi.ingsw.gc19.Networking.Server.VirtualMainServer;
import it.polimi.ingsw.gc19.Networking.Client.MessageHandler;
import it.polimi.ingsw.gc19.ObserverPattern.ObservableMessageToClient;
import it.polimi.ingsw.gc19.ObserverPattern.ObservableMessageToServer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Represents a client using RMI for communication with the server.
 */
public class ClientRMI extends UnicastRemoteObject implements VirtualClient, ClientInterface{

    private final VirtualMainServer virtualMainServer;
    private VirtualGameServer virtualGameServer;
    private final Object virtualGameServerLock;
    private ScheduledExecutorService heartbeatScheduler;

    private String nickname;

    private final MessageHandler messageHandler;

    public ClientRMI(VirtualMainServer virtualMainServer, MessageHandler messageHandler, String nickname) throws RemoteException {
        super();
        this.nickname = nickname;

        this.virtualMainServer = virtualMainServer;
        this.virtualGameServer = null;
        this.virtualGameServerLock = new Object();

        this.messageHandler = messageHandler;
    }


    @Override
    public void connect(){
        try{
            this.virtualMainServer.newConnection(this, nickname);
            startSendingHeartbeat();
        } catch (RemoteException e) {
            throw new RuntimeException(e); //@TODO: handle every single of these exception
        }
    }

    @Override
    public void createGame(String gameName, int numPlayers){
        synchronized (this.virtualGameServerLock) {
            try {
                this.virtualGameServer = this.virtualMainServer.createGame(this, gameName, this.nickname, numPlayers);
            } catch (RemoteException e) {
                //@TODO: invoke correct method of message handler
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void createGame(String gameName, int numPlayers, int seed){
        synchronized (this.virtualGameServerLock) {
            try {
                this.virtualGameServer = this.virtualMainServer.createGame(this, gameName, this.nickname, numPlayers, seed);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void joinGame(String gameName){
        synchronized (this.virtualGameServerLock) {
            try {
                this.virtualGameServer = this.virtualMainServer.joinGame(this, gameName, this.nickname);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void joinFirstAvailableGame(){
        synchronized (this.virtualGameServerLock) {
            try {
                this.virtualGameServer = this.virtualMainServer.joinFirstAvailableGame(this, this.nickname);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void reconnect(){
        synchronized (this.virtualGameServerLock) {
            Scanner tokenScanner = null;
            try {
                File tokenFile = new File("src/main/java/it/polimi/ingsw/gc19/Networking/Client/ClientTCP/TokenFile" + "_" + this.nickname);
                try {
                    tokenScanner = new Scanner(tokenFile);
                }
                catch (IOException ignored){
                    System.err.println(ignored.getMessage());
                    //@TODO: notify view or Client App
                    return;
                };

                this.virtualGameServer = this.virtualMainServer.reconnect(this, this.nickname, tokenScanner.nextLine());
                tokenScanner.close();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void logout(){
        this.stopSendingHeartbeat();
    }

    @Override
    public void disconnect(){
        stopSendingHeartbeat();
        try {
            this.virtualMainServer.disconnect(this, this.nickname); //@TODO: what happens when client disconnects himself? We set virtual main server to null?
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        File tokenFile = new File("src/main/java/it/polimi/ingsw/gc19/Networking/Client/ClientTCP/TokenFile" + "_" + this.nickname);
        if(tokenFile.delete()){
            System.err.println("Token file deleted...");
        }
    }

    public void startSendingHeartbeat(){
        this.heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
        this.heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                virtualMainServer.heartBeat(this);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }, 0, 1000 * Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS / 10, TimeUnit.MILLISECONDS);
    }

    public void stopSendingHeartbeat() {
        if (heartbeatScheduler != null && !heartbeatScheduler.isShutdown()) {
            heartbeatScheduler.shutdown();
        }
    }

    public void placeCard(String cardToInsert, String anchorCard, Direction directionToInsert, CardOrientation orientation){
        synchronized (this.virtualGameServerLock) {
            try {
                if(this.virtualGameServer != null) {
                    this.virtualGameServer.placeCard(cardToInsert, anchorCard, directionToInsert, orientation);
                }
                else{
                    //@TODO: handle this else
                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendChatMessage(ArrayList<String> UsersToSend, String messageToSend){
        synchronized (this.virtualGameServerLock) {
            try {
                if(this.virtualGameServer != null) {
                    this.virtualGameServer.sendChatMessage(UsersToSend, messageToSend);
                }
                else {

                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void placeInitialCard(CardOrientation cardOrientation){
        synchronized (this.virtualGameServerLock) {
            try {
                if(this.virtualGameServer != null) {
                    this.virtualGameServer.placeInitialCard(cardOrientation);
                }
                else{

                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void pickCardFromTable(PlayableCardType type, int position){
        synchronized (this.virtualGameServerLock) {
            try {
                if(this.virtualGameServer != null) {
                    this.virtualGameServer.pickCardFromTable(type, position);
                }
                else{

                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void pickCardFromDeck(PlayableCardType type){
        synchronized (this.virtualGameServerLock) {
            try {
                if(this.virtualGameServer != null) {
                    this.virtualGameServer.pickCardFromDeck(type);
                }
                else{

                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void chooseColor(Color color){
        synchronized (this.virtualGameServerLock) {
            try {
                if(this.virtualGameServer != null) {
                    this.virtualGameServer.chooseColor(color);
                }
                else{

                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void choosePrivateGoalCard(int cardIdx){
        synchronized (this.virtualGameServerLock) {
            try {
                if(this.virtualGameServer != null) {
                    this.virtualGameServer.choosePrivateGoalCard(cardIdx);
                }
                else{

                }
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void setToken(String token) {
        File tokenFile = new File("src/main/java/it/polimi/ingsw/gc19/Networking/Client/ClientTCP/TokenFile" + "_" + this.nickname);
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tokenFile));
            bufferedWriter.write(token);
            bufferedWriter.close();
            tokenFile.setReadOnly();
        }
        catch (IOException ignored){
            System.err.println(ignored.getMessage());
        };
    }

    public MessageHandler getMessageHandler(){
        return this.messageHandler;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void endGame(){
        synchronized (this.virtualGameServerLock) {
            this.virtualGameServer = null;
        }
    }

    @Override
    public void pushUpdate(MessageToClient message) throws RemoteException {
        this.messageHandler.update(message);
    }

}