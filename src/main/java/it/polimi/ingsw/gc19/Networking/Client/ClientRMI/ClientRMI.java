package it.polimi.ingsw.gc19.Networking.Client.ClientRMI;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Tuple;
import it.polimi.ingsw.gc19.Networking.Client.MessageHandler;
import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.VirtualGameServer;
import it.polimi.ingsw.gc19.Networking.Server.VirtualMainServer;
import it.polimi.ingsw.gc19.Networking.Client.MessageHandler;

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
public class ClientRMI extends UnicastRemoteObject implements Remote, VirtualClient {

    private final VirtualMainServer virtualMainServer;
    private VirtualGameServer virtualGameServer;
    private ScheduledExecutorService heartbeatScheduler;

    private final Deque<MessageToClient> incomingMessages;

    private String nickname;
    private String token;

    private final MessageHandler messageHandler;

    public ClientRMI(VirtualMainServer virtualMainServer) throws RemoteException {
        this.virtualMainServer = virtualMainServer;
        this.virtualGameServer = null;
        this.messageHandler = new MessageHandler();
        this.incomingMessages = new ArrayDeque<>();
    }

    public void start(){

    }

    public void connect(String nickname){
        try{
            this.virtualMainServer.newConnection(this, nickname);
            startSendingHeartbeat();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void createGame(String gameName, int numPlayers){
        try {
            this.virtualGameServer =
                    this.virtualMainServer.createGame(
                            this, gameName, this.nickname, numPlayers);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void joinGame(String gameName){
        try {
            this.virtualGameServer =
                    this.virtualMainServer.joinGame(
                            this, gameName, this.nickname);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void joinFirstAvailableGame(){
        try {
            this.virtualGameServer =
                    this.virtualMainServer.joinFirstAvailableGame(
                            this, this.nickname);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void reconnect(){
        try {
            this.virtualGameServer =
                    this.virtualMainServer.reconnect(
                            this, this.nickname, this.token
                    );
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void disconnect(){
        stopSendingHeartbeat();
        try {
            this.virtualMainServer.disconnect(
                    this, this.nickname);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
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
        }, 0, 100, TimeUnit.MILLISECONDS);

    }

    public void stopSendingHeartbeat() {
        if (heartbeatScheduler != null && !heartbeatScheduler.isShutdown()) {
            heartbeatScheduler.shutdown();
        }
    }

    public void placeCard(String cardToInsert, String anchorCard, Direction directionToInsert, CardOrientation orientation){
        try {
            this.virtualGameServer.placeCard(cardToInsert, anchorCard, directionToInsert, orientation);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendChatMessage(ArrayList<String> UsersToSend, String messageToSend){
        try {
            this.virtualGameServer.sendChatMessage(UsersToSend, messageToSend);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void placeInitialCard(CardOrientation cardOrientation){
        try {
            this.virtualGameServer.placeInitialCard(cardOrientation);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void pickCardFromTable(PlayableCardType type, int position){
        try {
            this.virtualGameServer.pickCardFromTable(type, position);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void pickCardFromDeck(PlayableCardType type){
        try {
            this.virtualGameServer.pickCardFromDeck(type);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    void chooseColor(Color color){
        try {
            this.virtualGameServer.chooseColor(color);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    void choosePrivateGoalCard(int cardIdx){
        try {
            this.virtualGameServer.choosePrivateGoalCard(cardIdx);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void pushUpdate(MessageToClient message) throws RemoteException {
        message.accept(this.messageHandler);
        synchronized (this.incomingMessages){
            this.incomingMessages.add(message);
        }

    }


}