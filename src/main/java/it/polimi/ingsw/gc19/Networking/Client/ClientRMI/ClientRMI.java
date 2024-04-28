package it.polimi.ingsw.gc19.Networking.Client.ClientRMI;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Client.MessageHandler;
import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.VirtualGameServer;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.VirtualMainServer;
import it.polimi.ingsw.gc19.Networking.Server.Settings;
import it.polimi.ingsw.gc19.View.GameLocalView.ActionParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    private VirtualMainServer virtualMainServer;
    private VirtualGameServer virtualGameServer;
    private final Object virtualGameServerLock;
    private ScheduledExecutorService heartbeatScheduler;
    private String nickname;
    private final MessageHandler messageHandler;
    private final ActionParser actionParser;

    public ClientRMI(String nickname, MessageHandler messageHandler, ActionParser actionParser) throws RemoteException {
        super();
        this.nickname = nickname;

        this.virtualGameServer = null;
        this.virtualGameServerLock = new Object();

        this.messageHandler = messageHandler;
        this.actionParser = actionParser;
    }

    public void setVirtualMainServer(VirtualMainServer virtualMainServer){
        this.virtualMainServer = virtualMainServer;
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
            }
            catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    @Override
    public void createGame(String gameName, int numPlayers, int seed){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                this.virtualGameServer = this.virtualMainServer.createGame(this, gameName, this.nickname, numPlayers, seed);
            }
            catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    @Override
    public void joinGame(String gameName){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                this.virtualGameServer = this.virtualMainServer.joinGame(this, gameName, this.nickname);
            }
            catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    public void joinFirstAvailableGame(){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                this.virtualGameServer = this.virtualMainServer.joinFirstAvailableGame(this, this.nickname);
            }
            catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    public void reconnect() throws RuntimeException{
        Scanner tokenScanner;
        String token;

        File tokenFile = new File("src/main/java/it/polimi/ingsw/gc19/Networking/Client/ClientTCP/TokenFile" + "_" + this.nickname);

        if(tokenFile.exists() && tokenFile.isFile()) {
            try {
                tokenScanner = new Scanner(tokenFile);
                token = tokenScanner.nextLine();
                tokenScanner.close();
            }
            catch (IOException ioException) {
                throw new IllegalStateException("Reconnection is not possible because token file has not been found!");
            }
        }
        else{
            throw new IllegalStateException("Reconnection is not possible because token file has not been found!");
        }

        synchronized (this.virtualGameServerLock) {
            try {
                this.virtualGameServer = this.virtualMainServer.reconnect(this, this.nickname, token);
            }
            catch (RemoteException e) {
                throw new RuntimeException("Could not perform reconnection due to Remote Exception:" + e);
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
            }
            catch (RemoteException e) {
                //@TODO: this case can be dangerous because thread have no lock. Skip?
                this.actionParser.disconnect();
            }
        }, 0, 1000 * Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS / 10, TimeUnit.MILLISECONDS);
    }

    public void stopSendingHeartbeat() {
        if (heartbeatScheduler != null && !heartbeatScheduler.isShutdown()) {
            heartbeatScheduler.shutdown();
        }
    }

    public void placeCard(String cardToInsert, String anchorCard, Direction directionToInsert, CardOrientation orientation){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                if (this.virtualGameServer != null) {
                    this.virtualGameServer.placeCard(cardToInsert, anchorCard, directionToInsert, orientation);
                }
            }
            catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    public void sendChatMessage(ArrayList<String> UsersToSend, String messageToSend){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                if (this.virtualGameServer != null) {
                    this.virtualGameServer.sendChatMessage(UsersToSend, messageToSend);
                }
            } catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    public void placeInitialCard(CardOrientation cardOrientation){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                if (this.virtualGameServer != null) {
                    this.virtualGameServer.placeInitialCard(cardOrientation);
                }
            } catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    public void pickCardFromTable(PlayableCardType type, int position){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                if(this.virtualGameServer != null) {
                    this.virtualGameServer.pickCardFromTable(type, position);
                }
            }
            catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    public void pickCardFromDeck(PlayableCardType type){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                if(this.virtualGameServer != null) {
                    this.virtualGameServer.pickCardFromDeck(type);
                }
            }
            catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    public void chooseColor(Color color){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                if(this.virtualGameServer != null) {
                    this.virtualGameServer.chooseColor(color);
                }
            }
            catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    public void choosePrivateGoalCard(int cardIdx){
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                if(this.virtualGameServer != null) {
                    this.virtualGameServer.choosePrivateGoalCard(cardIdx);
                }
            }
            catch (RemoteException e) {
                this.actionParser.disconnect();
            }
        }
    }

    @Override
    public void availableGames() {
        if(this.actionParser.isDisconnected()){
            return;
        }
        synchronized (this.virtualGameServerLock) {
            try {
                if(this.virtualGameServer != null) {
                    this.virtualMainServer.requestAvailableGames(this, this.nickname);
                }
            }
            catch (RemoteException e) {
                this.actionParser.disconnect();
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
            if(tokenFile.setReadOnly()){
                System.err.println("[TOKEN]: token file wrote and set read only.");
            }
        }
        catch (IOException ignored){ };
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

    @Override
    public void pushUpdate(MessageToClient message) throws RemoteException {
        this.messageHandler.update(message);
    }

}