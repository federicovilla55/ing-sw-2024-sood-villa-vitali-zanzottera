package it.polimi.ingsw.gc19.Networking.Client.ClientRMI;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Networking.Client.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageHandler;
import it.polimi.ingsw.gc19.Networking.Client.NetworkManagement.HeartBeatManager;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.ConfigurationManager;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.Configuration;
import it.polimi.ingsw.gc19.Networking.Server.Message.HeartBeat.ServerHeartBeatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.VirtualGameServer;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.VirtualMainServer;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;

import java.io.*;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Represents a client using RMI for communication with the server.
 */
public class ClientRMI extends UnicastRemoteObject implements VirtualClient, ClientInterface {

    private final Registry registry;
    private VirtualMainServer virtualMainServer;
    private VirtualGameServer virtualGameServer;
    private final Object virtualGameServerLock;

    private final HeartBeatManager heartBeatManager;

    private String nickname;

    private final MessageHandler messageHandler;
    private final ClientController clientController;
    private final ExecutorService virtualServerMethodsInvoker;

    public ClientRMI(MessageHandler messageHandler) throws RuntimeException, RemoteException{
        super();

        try {
            this.registry = LocateRegistry.getRegistry(ClientSettings.RMI_SERVER_IP, ClientSettings.SERVER_RMI_PORT);
            virtualMainServer = (VirtualMainServer) registry.lookup(ClientSettings.MAIN_SERVER_RMI_NAME);
        }
        catch (RemoteException remoteException){
            throw new RuntimeException("[Remote Exception]: could not locate registry.");
        }
        catch (NotBoundException notBoundException){
            throw new RuntimeException("[Not Bound Exception]: could not lookup on registry.");
        }

        this.heartBeatManager = new HeartBeatManager(this);
        this.virtualMainServer.registerClient(this);
        this.heartBeatManager.startHeartBeatManager();

        this.virtualGameServer = null;
        this.virtualGameServerLock = new Object();

        this.messageHandler = messageHandler;
        this.clientController = messageHandler.getClientController();

        this.virtualServerMethodsInvoker = Executors.newSingleThreadExecutor();
    }

    @Override
    public void connect(String nickname){
        this.virtualServerMethodsInvoker.submit(() -> {
            if(this.clientController.isDisconnected()){
                return;
            }
            try{
                this.virtualMainServer.newConnection(this, nickname);
            }
            catch (RemoteException e) {
                this.clientController.disconnect();
            }

        });
    }

    @Override
    public void createGame(String gameName, int numPlayers){
        this.virtualServerMethodsInvoker.submit(() -> {
            if(this.clientController.isDisconnected()){
                return;
            }
            try {
                this.virtualGameServer = this.virtualMainServer.createGame(this, gameName, this.nickname, numPlayers);
            } catch (RemoteException e) {
                this.clientController.disconnect();
            }
        });
    }

    @Override
    public void createGame(String gameName, int numPlayers, int seed){
        this.virtualServerMethodsInvoker.submit(() -> {
            if(this.clientController.isDisconnected()){
                return;
            }
            synchronized (this.virtualGameServerLock) {
                try {
                    this.virtualGameServer = this.virtualMainServer.createGame(this, gameName, this.nickname, numPlayers, seed);
                }
                catch (RemoteException e) {
                    this.clientController.disconnect();
                }
            }
        });
    }

    @Override
    public void joinGame(String gameName){
        this.virtualServerMethodsInvoker.submit(() -> {
            if(this.clientController.isDisconnected()){
                return;
            }
            synchronized (this.virtualGameServerLock) {
                try {
                    this.virtualGameServer = this.virtualMainServer.joinGame(this, gameName, this.nickname);
                }
                catch (RemoteException e) {
                    this.clientController.disconnect();
                }
            }
        });
    }

    @Override
    public void joinFirstAvailableGame(){
        this.virtualServerMethodsInvoker.submit(() -> {
            if(this.clientController.isDisconnected()){
                return;
            }
            synchronized (this.virtualGameServerLock) {
                try {
                    this.virtualGameServer = this.virtualMainServer.joinFirstAvailableGame(this, this.nickname);
                }
                catch (RemoteException e) {
                    this.clientController.disconnect();
                }
            }
        });
    }

    @Override
    public void reconnect() throws IllegalStateException{
        Configuration clientConfig;
        String nick;

        try{
            clientConfig = ConfigurationManager.retriveConfiguration(this.nickname);
        }
        catch (IllegalStateException | IOException e){
            throw new IllegalStateException("[EXCEPTION]: could not reconnect due to: " + e);
        }

        //int numOfTry = 0;

        try {
            virtualMainServer = (VirtualMainServer) this.registry.lookup(ClientSettings.MAIN_SERVER_RMI_NAME);

            /*while(!Thread.currentThread().isInterrupted() && numOfTry < 10) {
                if(this.nickname != null){
                    nick = this.nickname;
                }
                else{
                    nick = clientConfig.getNick();
                }

                try {
                    this.virtualGameServer = this.virtualMainServer.reconnect(this, nick, clientConfig.getToken());
                    return;
                }
                catch (RemoteException remoteException){
                    numOfTry++;

                    try{
                        TimeUnit.MILLISECONDS.sleep(250);
                    }
                    catch (InterruptedException interruptedException){
                        Thread.currentThread().interrupt(); //This operation can be dangerous?
                        return;
                    }
                }
            }*/

            if(this.nickname != null){
                nick = this.nickname;
            }
            else{
                nick = clientConfig.getNick();
            }

            try {
                this.virtualGameServer = this.virtualMainServer.reconnect(this, nick, clientConfig.getToken());
            }
            catch (RemoteException remoteException){
                throw new RuntimeException("[Remote Exception]: could not invoke remote method of RMI server.");
            }

        }
        catch (NotBoundException e) {
            throw new RuntimeException("[Not Bound Exception]: could not lookup on RMI registry.");
        }
        catch (RemoteException remoteException){
            throw new RuntimeException("[Remote Exception]: could not lookup on RMI registry.");
        }
    }

    @Override
    public void logoutFromGame() throws RuntimeException{
        try {
            this.virtualMainServer.disconnectFromGame(this, this.nickname);
        }
        catch (RemoteException e) {
            throw new RuntimeException("Cannot disconnect from server because of Remote Exception: " + e);
        }
        synchronized (this.virtualGameServerLock){
            this.virtualGameServer = null;
        }
    }

    @Override
    public void disconnect() throws RuntimeException{
        ConfigurationManager.deleteConfiguration(this.nickname);

        try {
            this.virtualMainServer.disconnect(this, this.nickname);
        }
        catch (RemoteException e) {
            throw new RuntimeException("Cannot disconnect from server because of Remote Exception: " + e);
        }

        stopClient();
    }

    @Override
    public void signalPossibleNetworkProblem() {
        if(!this.clientController.isDisconnected()) {
            this.clientController.disconnect();
        }
        this.heartBeatManager.stopHeartBeatManager();
    }

    @Override
    public void sendHeartBeat() throws RuntimeException{
        try {
            this.virtualMainServer.heartBeat(this);
        }
        catch (RemoteException remoteException){
            throw new RuntimeException("Could not send heartbeat due to exception: " + remoteException.getClass());
        }
    }

    public void startSendingHeartbeat(){
        this.heartBeatManager.startHeartBeatManager();
    }

    public void stopSendingHeartbeat() {
        this.heartBeatManager.stopHeartBeatManager();
    }

    public void stopClient(){
        stopSendingHeartbeat();

        this.virtualServerMethodsInvoker.shutdownNow();

        try{
            UnicastRemoteObject.unexportObject(this.registry, true);
        }
        catch (NoSuchObjectException ignored){ };
    }

    @Override
    public void placeCard(String cardToInsert, String anchorCard, Direction directionToInsert, CardOrientation orientation){
        this.virtualServerMethodsInvoker.submit(() -> {
            if(this.clientController.isDisconnected()){
                return;
            }
            synchronized (this.virtualGameServerLock) {
                try {
                    if (this.virtualGameServer != null) {
                        this.virtualGameServer.placeCard(cardToInsert, anchorCard, directionToInsert, orientation);
                    }
                }
                catch (RemoteException e) {
                    this.clientController.disconnect();
                }
            }
        });
    }

    @Override
    public void sendChatMessage(ArrayList<String> UsersToSend, String messageToSend){
        this.virtualServerMethodsInvoker.submit(() -> {
            if(this.clientController.isDisconnected()){
                return;
            }
            synchronized (this.virtualGameServerLock) {
                try {
                    if (this.virtualGameServer != null) {
                        this.virtualGameServer.sendChatMessage(UsersToSend, messageToSend);
                    }
                } catch (RemoteException e) {
                    this.clientController.disconnect();
                }
            }
        });
    }

    @Override
    public void placeInitialCard(CardOrientation cardOrientation){
        this.virtualServerMethodsInvoker.submit(() -> {
            if(this.clientController.isDisconnected()){
                return;
            }
            synchronized (this.virtualGameServerLock) {
                try {
                    if (this.virtualGameServer != null) {
                        this.virtualGameServer.placeInitialCard(cardOrientation);
                    }
                } catch (RemoteException e) {
                    this.clientController.disconnect();
                }
            }
        });
    }

    @Override
    public void pickCardFromTable(PlayableCardType type, int position){
        this.virtualServerMethodsInvoker.submit(() -> {
            if(this.clientController.isDisconnected()){
                return;
            }
            synchronized (this.virtualGameServerLock) {
                try {
                    if(this.virtualGameServer != null) {
                        this.virtualGameServer.pickCardFromTable(type, position);
                    }
                }
                catch (RemoteException e) {
                    this.clientController.disconnect();
                }
            }
        });
    }

    @Override
    public void pickCardFromDeck(PlayableCardType type){
        this.virtualServerMethodsInvoker.submit(() -> {
            if(this.clientController.isDisconnected()){
                return;
            }
            synchronized (this.virtualGameServerLock) {
                try {
                    if(this.virtualGameServer != null) {
                        this.virtualGameServer.pickCardFromDeck(type);
                    }
                }
                catch (RemoteException e) {
                    this.clientController.disconnect();
                }
            }
        });
    }

    @Override
    public void chooseColor(Color color){
        this.virtualServerMethodsInvoker.submit(() -> {
            if(this.clientController.isDisconnected()){
                return;
            }
            synchronized (this.virtualGameServerLock) {
                try {
                    if(this.virtualGameServer != null) {
                        this.virtualGameServer.chooseColor(color);
                    }
                }
                catch (RemoteException e) {
                    this.clientController.disconnect();
                }
            }
        });
    }

    @Override
    public void choosePrivateGoalCard(int cardIdx){
        this.virtualServerMethodsInvoker.submit(() -> {
            if(this.clientController.isDisconnected()){
                return;
            }
            synchronized (this.virtualGameServerLock) {
                try {
                    if(this.virtualGameServer != null) {
                        this.virtualGameServer.choosePrivateGoalCard(cardIdx);
                    }
                }
                catch (RemoteException e) {
                    this.clientController.disconnect();
                }
            }
        });
    }

    @Override
    public void availableGames() {
        this.virtualServerMethodsInvoker.submit(() -> {
            if(this.clientController.isDisconnected()){
                return;
            }
            synchronized (this.virtualGameServerLock) {
                try {
                    this.virtualMainServer.requestAvailableGames(this, this.nickname);
                }
                catch (RemoteException e) {
                    this.clientController.disconnect();
                }
            }
        });
    }

    public MessageHandler getMessageHandler(){
        return this.messageHandler;
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public void pushUpdate(MessageToClient message) throws RemoteException {
        if(message instanceof ServerHeartBeatMessage){
            this.heartBeatManager.heartBeat();
        }
        else{
            if(this.nickname == null || message.getHeader() == null || message.getHeader().contains(this.nickname)){
                this.messageHandler.update(message);
            }
        }
    }

    @Override
    public void configure(String nick, String token) {
        this.nickname = nick;

        try {
            ConfigurationManager.saveConfiguration(new Configuration(nick, token, Configuration.ConnectionType.RMI));
        }
        catch (RuntimeException runtimeException){
            System.err.println("[CONFIG]: could not write config file. Skipping...");
        }
    }

}