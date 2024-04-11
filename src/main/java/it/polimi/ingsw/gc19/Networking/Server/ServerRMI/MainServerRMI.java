package it.polimi.ingsw.gc19.Networking.Server.ServerRMI;

import it.polimi.ingsw.gc19.Model.Tuple;
import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Controller.MainController;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class MainServerRMI extends Server implements VirtualMainServer{

    private final HashMap<VirtualClient, Tuple<ClientHandlerRMI, String>> connectedClients;
    private final HashMap<VirtualClient, RMIAgentForHeartBeat> rmiAgentsForHeart;

    public MainServerRMI(){
        super();
        this.connectedClients = new HashMap<>();
        this.rmiAgentsForHeart = new HashMap<>();
    }

    /**
     * This method is used to establish a new RMI connection between client and server.
     * We must check if clientRMI is already connected to server. If no, server generates a token
     * (hash of {@param nickName} and {@link ClientHandlerRMI}) and inserts it in {@link CreatedPlayerMessage}.
     * Token will be used during reconnection. Then, it registers first heartbeat in <code>lastHeartBeatOfClient</code> and
     * starts thread for checking {@param clientRMI}.
     * @param clientRMI virtual client of player who wants to create a connection
     * @param nickName nickname chosen by the player
     * @throws RemoteException exception thrown if something goes wrong
     * @return {@link VirtualHeartBeatServer} for heartbeat notification
     */
    @Override
    public VirtualHeartBeatServer newConnection(VirtualClient clientRMI, String nickName) throws RemoteException{
        ClientHandlerRMI clientHandlerRMI;
        synchronized(this.connectedClients){
            if(this.connectedClients.containsKey(clientRMI)){
                clientRMI.pushUpdate(new GameHandlingError(Error.CLIENT_ALREADY_CONNECTED_TO_SERVER,
                                                           "Your virtual client is already registered in server!")
                                             .setHeader(nickName));
                return null;
            }
        }

        clientHandlerRMI = new ClientHandlerRMI(clientRMI, nickName);
        String hashedMessage = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5"); // 128 bits
            // @TODO: figure out if it's preferable to use Base64 for bytes to String conversion
            hashedMessage = Arrays.toString(digest.digest((nickName + clientHandlerRMI.toString()).getBytes()));
        } catch (NoSuchAlgorithmException ignored){ };

        if(this.mainController.createClient(clientHandlerRMI)) {
            synchronized (this.connectedClients) {
                this.connectedClients.put(clientRMI, new Tuple<>(clientHandlerRMI, hashedMessage));
            }
            clientHandlerRMI.update(new CreatedPlayerMessage(clientHandlerRMI.getName(), hashedMessage).setHeader(clientHandlerRMI.getName()));

            RMIAgentForHeartBeat rmiAgentForHeartBeat = new RMIAgentForHeartBeat(this, clientRMI);

            return (VirtualHeartBeatServer) UnicastRemoteObject.exportObject(rmiAgentForHeartBeat, 12122);
        }

        return null;
    }

    /**
     * This method is the entry point for creating a game. If {@link MainController#createGame(String, int, ClientHandler)}
     * returns <code>true</code> then builds all is needed server-side for managing the client.
     * Player has to be registered to server in order to create a new game.
     * @param clientRMI virtual client of the player
     * @param gameName name of the game to build
     * @param nickName nickname of the player
     * @param numPlayer number of player for the game
     * @return a {@link VirtualGameServer} remote object from which the client can access the game.
     * If a game cannot be built it returns <code>null</code>.
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public VirtualGameServer createGame(VirtualClient clientRMI, String gameName, String nickName, int numPlayer) throws RemoteException{
        ClientHandlerRMI clientHandlerRMI = getClientHandlerForVirtualClient(clientRMI, nickName);
        if(clientHandlerRMI == null){
            return null;
        }
        if(this.mainController.createGame(gameName, numPlayer, clientHandlerRMI)){
            return (VirtualGameServer) UnicastRemoteObject.exportObject(clientHandlerRMI, new Random(1).nextInt(10000, 64000));
        }
        return null;
    }

    /**
     * This method acts the same as {@link MainServerRMI#createGame(VirtualClient, String, String, int, long)}
     * but lets the user choose the seed on which build the game
     * @param clientRMI virtual client of player creating the game
     * @param gameName game name chosen by the player
     * @param nickName nickname of the player
     * @param numPlayer number of player for the specified game
     * @param randomSeed random seed for the game
     * @return a {@link VirtualGameServer} remote object from which the client can access the game.
     * If a game cannot be built it returns <code>null</code>.
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public VirtualGameServer createGame(VirtualClient clientRMI, String gameName, String nickName, int numPlayer, long randomSeed) throws RemoteException{
        ClientHandlerRMI clientHandlerRMI = getClientHandlerForVirtualClient(clientRMI, nickName);
        if(clientHandlerRMI == null){
            return null;
        }
        if(this.mainController.createGame(gameName, numPlayer, clientHandlerRMI, randomSeed)){
            return (VirtualGameServer) UnicastRemoteObject.exportObject(clientHandlerRMI, new Random(2).nextInt(10000, 64000));
        }
        return null;
    }

    /**
     * This method build a {@link ClientHandler} object with which server can communicate with client.
     * If client is not registered it returns null.
     * @param clientRMI virtual client of the requesting action player
     * @param nickName nickname of the player
     * @return {@link ClientHandlerRMI} if the player has been registered in server, otherwise <code>null</code>
     * @throws RemoteException exception thrown if something goes wrong
     */
    private ClientHandlerRMI getClientHandlerForVirtualClient(VirtualClient clientRMI, String nickName) throws RemoteException {
        ClientHandlerRMI clientHandlerRMI;
        synchronized(this.connectedClients){
            if(!this.connectedClients.containsKey(clientRMI)){
                clientRMI.pushUpdate(new GameHandlingError(Error.CLIENT_NOT_REGISTERED_TO_SERVER,
                                                           "Your virtual client is not registered to server! Please register...")
                                             .setHeader(nickName));
                return null;
            }
            clientHandlerRMI = this.connectedClients.get(clientRMI).x();
        }
        return clientHandlerRMI;
    }

    /**
     * This method is the entry point for client who wants to join a game. If {@link MainController#registerToGame(ClientHandler, String)}
     * goes fine it exports {@link ClientHandlerRMI} object and returns a reference to it.
     * @param clientRMI virtual client associated to the requesting player
     * @param gameName name of the game
     * @param nickName nickname of the requesting player
     * @return a {@link VirtualGameServer} remote object from which the client can access the game.
     * If a game cannot be built it returns <code>null</code>.
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public VirtualGameServer joinGame(VirtualClient clientRMI, String gameName, String nickName) throws RemoteException {
        ClientHandlerRMI clientHandlerRMI = getClientHandlerForVirtualClient(clientRMI, nickName);
        if(this.mainController.registerToGame(clientHandlerRMI, gameName)){
            return (VirtualGameServer) UnicastRemoteObject.exportObject(clientHandlerRMI, new Random(3).nextInt(10000, 64000));
        }
        return null;
    }

    /**
     * This method is the entry point for client who wants to join the first available game. If {@link MainController#registerToGame(ClientHandler, String)}
     * goes fine it exports {@link ClientHandlerRMI} object and returns a reference to it.
     * @param clientRMI virtual client associated to the requesting player
     * @param nickName nickname of the requesting player
     * @return a {@link VirtualGameServer} remote object from which the client can access the game.
     * If a game cannot be built it returns <code>null</code>.
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public VirtualGameServer joinFirstAvailableGame(VirtualClient clientRMI, String nickName) throws RemoteException {
        ClientHandlerRMI clientHandlerRMI = getClientHandlerForVirtualClient(clientRMI, nickName);
        String gameName = this.mainController.registerToFirstAvailableGame(clientHandlerRMI);
        if(gameName != null){
            return (VirtualGameServer) UnicastRemoteObject.exportObject(clientHandlerRMI, new Random(4).nextInt(10000, 64000));
        }
        return null;
    }

    //public VirtualHeartBeatServer getYourVirtualHeartBeatServer

    /**
     * This method support advanced functionality resilience to disconnection. It checks if client is already
     * connected to server (in other words if it correctly sends heartbeats). If yes, reconnection is no needed.
     * Otherwise, it is necessary to check tokens. If there is another {@link VirtualClient} with the same token
     * in <code>connectedClients</code> then reconnection can take place. Otherwise, requesting player is faking to be
     * some other and it returns null. Lastly, it exports the new game server and insert first heartbeat inside
     * <code>lastHeartBeatOfClients</code>.
     * @param clientRMI virtual client of the player who wants to reconnect
     * @param nicName nickname of client
     * @param token private token of the client
     * @return {@link VirtualGameServer} remote object of the game server
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public Tuple<VirtualGameServer, VirtualHeartBeatServer> reconnect(VirtualClient clientRMI, String nicName, String token) throws RemoteException {
        VirtualClient clientRMIBefore = null;
        ClientHandlerRMI clientHandlerRMI = null;
        boolean found = false;

        synchronized (this.connectedClients){
            if(this.connectedClients.containsKey(clientRMI)){
                clientRMI.pushUpdate(new GameHandlingError(Error.CLIENT_ALREADY_CONNECTED_TO_SERVER,
                                                           "You cannot reconnect to server because you are already connected!")
                                             .setHeader(nicName));
                return null;
            }
        }

        synchronized (this.connectedClients) {
            for (var v : this.connectedClients.entrySet()) {
                if (v.getValue().y().equals(token)) {
                    clientRMIBefore = v.getKey();
                    clientHandlerRMI = new ClientHandlerRMI(clientRMI, v.getValue().x());
                    this.connectedClients.put(clientRMI, new Tuple<>(clientHandlerRMI, v.getValue().y()));
                    found = true;
                    break;
                }
            }
        }

        if(found){
            synchronized (this.lastHeartBeatOfClients){
                this.lastHeartBeatOfClients.remove(clientRMIBefore);
                this.lastHeartBeatOfClients.put(clientRMI, new Date().getTime());
            }
            if(this.mainController.reconnect(clientHandlerRMI)){
                return (VirtualGameServer) UnicastRemoteObject.exportObject(clientHandlerRMI, new Random(5).nextInt(10000,64000));
            }
        }
        else {
            synchronized (this.connectedClients){
                if(!this.connectedClients.containsKey(clientRMI)) {
                    clientRMI.pushUpdate(new GameHandlingError(Error.CLIENT_NOT_REGISTERED_TO_SERVER,
                                                               "You are not registered to server! Please register")
                                                 .setHeader(nicName));
                }
            }
        }
        return null;
    }

    /**
     * This method disconnects one client from the RMI server. Client must explicitly tell to server that
     * he wants to be disconnected. It removes {@param clientRMI} from both <code>connectedClients</code>
     * and <code>lastHeartBeatOfClients</code>. Finally, it tells to {@link MainController} to disconnect
     * the player.
     * @param clientRMI virtual client of the plyer who wants to be disconnected
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public void disconnect(VirtualClient clientRMI, String nickName) throws RemoteException {
        ClientHandlerRMI clientHandlerRMI;
        synchronized (this.connectedClients){
            if(!this.connectedClients.containsKey(clientRMI)){
                clientRMI.pushUpdate(new GameHandlingError(Error.CLIENT_NOT_REGISTERED_TO_SERVER,
                                                           "Your virtual client is not registered to server! Please register...")
                                             .setHeader(nickName));
                return;
            }
            else{
                clientHandlerRMI = this.connectedClients.remove(clientRMI).x();
            }
        }
        synchronized (this.lastHeartBeatOfClients){
            this.lastHeartBeatOfClients.remove(clientRMI);
        }
        this.mainController.disconnect(clientHandlerRMI);
    }


    public void notifyVirtualClientIsInactive(VirtualClient virtualClient){
        String playerName;
        synchronized (this.connectedClients){
            playerName = this.connectedClients.get(virtualClient).x().getName();
            //System.err.println("removing " + playerName);
            if(playerName != null) {
                this.mainController.setPlayerInactive(playerName);
            }
        }
    }

    /*
    String playerName;
        VirtualClient virtualClientToRemove = null;
        synchronized(this.lastHeartBeatOfClients) {
            for (VirtualClient virtualClient : this.lastHeartBeatOfClients.keySet()) {
                System.err.println(lastHeartBeatOfClients.entrySet());
                if (new Date().getTime() - this.lastHeartBeatOfClients.get(virtualClient) > 1000 * Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS) {
                    System.err.println("found something " + virtualClient);
                    this.lastHeartBeatOfClients.remove(virtualClient);
                    virtualClientToRemove = virtualClient;
                    synchronized (this.connectedClients) {
                        playerName = this.connectedClients.get(virtualClientToRemove).x().getName();
                        System.err.println("removing " + playerName);
                    }
                    if(playerName != null) {
                        this.mainController.setPlayerInactive(playerName);
                    }
                }
            }
        }
        /*if(virtualClientToRemove != null){
            synchronized (this.connectedClients) {
                playerName = this.connectedClients.get(virtualClientToRemove).x().getName();
                System.out.println("removing " + playerName);
            }
            if(playerName != null) {
                this.mainController.setPlayerInactive(playerName);
            }
        }
     */

    /**
     * This method notifies to the server that client is still alive.
     * @param virtualClient virtual client of the player
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public void heartBeat(VirtualClient virtualClient) throws RemoteException{
        synchronized(this.lastHeartBeatOfClients){
            System.out.println("received heartbeat from " + virtualClient);
            if(this.lastHeartBeatOfClients.containsKey(virtualClient)) {
                //System.out.println("stored heartbeat from " + virtualClient);
                this.lastHeartBeatOfClients.put(virtualClient, new Date().getTime());
            }
        }
    }

    /**
     * This method is used when someone wants to reset the server.
     * Used only for testing purposes.
     */
    @Override
    public void resetMainServer() {
        synchronized (connectedClients) {
            this.connectedClients.clear();
        }
        synchronized (lastHeartBeatOfClients) {
            this.lastHeartBeatOfClients.clear();
        }
        this.mainController.resetMainController();
    }

}