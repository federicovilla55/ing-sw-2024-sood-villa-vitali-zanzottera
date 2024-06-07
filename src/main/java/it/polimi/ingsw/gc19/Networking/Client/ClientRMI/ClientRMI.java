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
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;

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
 * This class represents the RMI client interface. It extends {@link UnicastRemoteObject}
 * in order to let server invoke method on it.
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

    /**
     * This method is used by client to connect to {@link MainServerRMI}.
     * If client is disconnected, then the request is not performed.
     * @param nickname the nickname of the player to register
     */
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
                signalPossibleNetworkProblem();
            }

        });
    }

    /**
     * This method invokes {@link MainServerRMI#disconnect(VirtualClient, String)} to notify server
     * that player wants to create a game. If client is disconnected, then the request is not performed.
     * If client is disconnected, then the request is not performed.
     * @param gameName the name of the game that will be created.
     * @param numPlayers the number of players to play with.
     */
    @Override
    public void createGame(String gameName, int numPlayers){
        this.virtualServerMethodsInvoker.submit(() -> {
            if(this.clientController.isDisconnected()){
                return;
            }
            try {
                this.virtualGameServer = this.virtualMainServer.createGame(this, gameName, this.nickname, numPlayers);
            } catch (RemoteException e) {
                signalPossibleNetworkProblem();
            }
        });
    }

    /**
     * This method invokes {@link MainServerRMI#createGame(VirtualClient, String, String, int, long)} to ask server to create a game
     * with the specified seed. If client is disconnected, then the request is not performed.
     * @param gameName the name of the game that will be created.
     * @param numPlayers the number of players for the game.
     * @param seed the seed for the game.
     */
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
                    signalPossibleNetworkProblem();
                }
            }
        });
    }

    /**
     * This method invokes {@link MainServerRMI#joinGame(VirtualClient, String, String)} to
     * ask server to join the specified game. If client is disconnected,
     * then the request is not performed.
     * @param gameName the name of the game to join.
     */
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
                    signalPossibleNetworkProblem();
                }
            }
        });
    }

    /**
     * This method invokes {@link MainServerRMI#joinFirstAvailableGame(VirtualClient, String)} to ask server
     * join first available game. If client is disconnected, then the request is not performed.
     */
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
                    signalPossibleNetworkProblem();
                }
            }
        });
    }

    /**
     * This method is used when client has to reconnect to server. It
     * works in all situations, including rebooting the machine.
     * @throws IllegalStateException when an error happened managing configuration
     */
    @Override
    public void reconnect() throws RuntimeException{
        Configuration clientConfig;
        String nick;

        try{
            clientConfig = ConfigurationManager.retrieveConfiguration(this.nickname);
        }
        catch (IllegalStateException | IOException e){
            throw new IllegalStateException("[EXCEPTION]: could not reconnect due to: " + e);
        }

        try {
            virtualMainServer = (VirtualMainServer) this.registry.lookup(ClientSettings.MAIN_SERVER_RMI_NAME);

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

    /**
     * This method is used when client has to log out fom game
     * @throws RuntimeException if a {@link RemoteException} happens
     * while invoking {@link MainServerRMI#disconnectFromGame(VirtualClient, String)} method.
     */
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

    /**
     * This method is used by client to explicitly disconnect from
     * server. It deletes current configuration.
     * @throws RuntimeException if an error occurs while invoking method
     * {@link MainServerRMI#disconnect(VirtualClient, String)} of {@link MainServerRMI}.
     */
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

    /**
     * This method is used to signal to {@link ClientController} that
     * maybe a network error has occurred.
     */
    @Override
    public void signalPossibleNetworkProblem() {
        if(!this.clientController.isDisconnected()) {
            this.clientController.signalPossibleNetworkProblem();
        }
        this.heartBeatManager.stopHeartBeatManager();
    }

    /**
     * This method is used by {@link HeartBeatManager} to notify
     * {@link MainServerRMI} that client is alive.
     * @throws RuntimeException if an error occurs while performing the requested action.
     */
    @Override
    public void sendHeartBeat() throws RuntimeException{
        try {
            this.virtualMainServer.heartBeat(this);
        }
        catch (RemoteException remoteException){
            throw new RuntimeException("Could not send heartbeat due to exception: " + remoteException.getClass());
        }
    }

    /**
     * This method is used to signal to {@link ClientRMI} to start
     * {@link HeartBeatManager}.
     */
    public void startSendingHeartbeat(){
        this.heartBeatManager.startHeartBeatManager();
    }

    /**
     * This method is used to signal to {@link ClientRMI} to stop
     * {@link HeartBeatManager}
     */
    public void stopSendingHeartbeat() {
        this.heartBeatManager.stopHeartBeatManager();
    }

    /**
     * This method is used when {@link ClientRMI} need to be stopped.
     * It shuts down {@link HeartBeatManager} and un-export registry.
     */
    public void stopClient(){
        stopSendingHeartbeat();

        this.virtualServerMethodsInvoker.shutdownNow();

        try{
            UnicastRemoteObject.unexportObject(this.registry, true);
        }
        catch (NoSuchObjectException ignored){ };
    }

    /**
     * This method is used to notify {@link ClientHandlerRMI} that a card need to be placed.
     * If client is disconnected, then the request is not performed.
     * @param cardToInsert the card that needs to be placed.
     * @param anchorCard the card from which to place the card.
     * @param directionToInsert the direction from the anchorCard in which player wants to place the card.
     * @param orientation the orientation in which player wants to place cardToInsert.
     */
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
                    signalPossibleNetworkProblem();
                }
            }
        });
    }

    /**
     * This method is used to notify send a chat message.
     * If client is disconnected, then the request is not performed.
     * @param UsersToSend a list containing the nickname of the users to whom player want to send the message.
     * @param messageToSend the message player wants to send.
     */
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
                    signalPossibleNetworkProblem();
                }
            }
        });
    }

    /**
     * This method invoke {@link ClientHandlerRMI#placeInitialCard(CardOrientation)} to notify
     * server that player has placed its initial card. If client is disconnected,
     * then the request is not performed.
     * @param cardOrientation the orientation in which player wants to place the initial card.
     */
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
                    signalPossibleNetworkProblem();
                }
            }
        });
    }

    /**
     * This method invoke {@link ClientHandlerRMI#pickCardFromTable(PlayableCardType, int)} to
     * notify server that player wants to pick a card from table. If client is disconnected,
     * then the request is not performed.
     * @param type the type of card player wants to pick (RESOURCE/GOLD).
     * @param position the position in the table player wants to take the card from.
     */
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
                    signalPossibleNetworkProblem();
                }
            }
        });
    }

    /**
     * This method invoke {@link ClientHandlerRMI#pickCardFromDeck(PlayableCardType)} to
     * notify server that player wants to pick a card from deck
     * @param type the type of card player wants to pick (RESOURCE/GOLD).
     */
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
                    signalPossibleNetworkProblem();
                }
            }
        });
    }

    /**
     * This method invoke {@link ClientHandlerRMI#chooseColor(Color)} to notify server that player
     * has chosen its color. If client is disconnected, then the request is not performed.
     * @param color the selected color.
     */
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
                    signalPossibleNetworkProblem();
                }
            }
        });
    }

    /**
     * This method invokes {@link ClientHandlerRMI#choosePrivateGoalCard(int)} to notify
     * server that player has chosen its private goal card. If client is disconnected,
     * then the request is not performed.
     * @param cardIdx which of the two proposed goal card we want to choose.
     */
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
                    signalPossibleNetworkProblem();
                }
            }
        });
    }

    /**
     * This method invoke {@link MainServerRMI#requestAvailableGames(VirtualClient, String)} to
     * ask server about available games. If client is disconnected,
     * then the request is not performed.
     */
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
                    signalPossibleNetworkProblem();
                }
            }
        });
    }

    /**
     * Getter for {@link MessageHandler} associated to the object
     * @return the {@link MessageHandler} associated with the object
     */
    public MessageHandler getMessageHandler(){
        return this.messageHandler;
    }

    /**
     * Getter for nickname stored in the class
     * @return the nickname of the player that owns this object
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * This method is used by {@link MainServerRMI} or {@link ClientHandlerRMI} to
     * push new {@link MessageToClient}.
     * @param message the {@link MessageToClient} to send to RMI client
     * @throws RemoteException if an error occurs while performing the action.
     */
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

    /**
     * This method is used to configure the "interface" (e.g. storing on
     * a separate JSON file the configuration) after {@link CreatedPlayerMessage}
     * has arrived
     * @param nick the nickname of the player
     * @param token the token associated to the client.
     */
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