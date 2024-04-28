package it.polimi.ingsw.gc19.Networking.Server.ServerRMI;

import it.polimi.ingsw.gc19.Controller.MainController;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.AvailableGamesMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.DisconnectFromServer;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.*;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.*;

public class MainServerRMI extends Server implements VirtualMainServer{

    private final HashMap<VirtualClient, Tuple<ClientHandlerRMI, String>> connectedClients;
    private final ConcurrentHashMap<VirtualClient, Long> lastHeartBeatOfClients;
    private final HashMap<VirtualClient, Long> pendingVirtualClientToKill;

    private final ScheduledExecutorService heartBeatTesterExecutor;
    private final ScheduledExecutorService inactiveClientKillerExecutor;

    public MainServerRMI(){
        super();
        this.connectedClients = new HashMap<>();
        this.lastHeartBeatOfClients = new ConcurrentHashMap<>();
        this.pendingVirtualClientToKill = new HashMap<>();

        this.heartBeatTesterExecutor = new ScheduledThreadPoolExecutor(1);
        this.inactiveClientKillerExecutor = new ScheduledThreadPoolExecutor(1);
        this.heartBeatTesterExecutor.scheduleAtFixedRate(this::runHeartBeatTesterForClient, 0, Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS * 1000 / 10, TimeUnit.MILLISECONDS);
        this.inactiveClientKillerExecutor.scheduleAtFixedRate(this::runInactiveClientKiller, 0, Settings.TIME_TO_WAIT_BEFORE_CLIENT_HANDLER_KILL * 1000 / 10, TimeUnit.MILLISECONDS);
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
     */
    @Override
    public void newConnection(VirtualClient clientRMI, String nickName) throws RemoteException{
        ClientHandlerRMI clientHandlerRMI;
        synchronized(this.connectedClients){
            if(this.connectedClients.containsKey(clientRMI)){
                clientRMI.pushUpdate(new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER,
                                                           "Your virtual client is already registered in server!")
                                             .setHeader(nickName));
                return;
            }
        }

        clientHandlerRMI = new ClientHandlerRMI(clientRMI, nickName);
        clientHandlerRMI.start();
        String hashedMessage = super.computeHashOfClientHandler(clientHandlerRMI, nickName);

        if(this.mainController.createClient(clientHandlerRMI)) {
            synchronized (this.connectedClients) {
                this.connectedClients.put(clientRMI, new Tuple<>(clientHandlerRMI, hashedMessage));
            }
            this.lastHeartBeatOfClients.put(clientRMI, new Date().getTime());

            clientHandlerRMI.update(new CreatedPlayerMessage(clientHandlerRMI.getUsername(), hashedMessage).setHeader(clientHandlerRMI.getUsername()));
        }
    }

    /**
     * This method is used to close RMI connection (and consequently freeing name)
     * with clients that are inactive for more than <code>Settings.TIME_TO_WAIT_BEFORE_CLIENT_HANDLER_KILL</code>
     * seconds. It notifies {@link MainController} that the player has to be
     * disconnected and removes its {@link VirtualClient} from all the hashmaps.
     */
    private void runInactiveClientKiller(){
        synchronized (this.pendingVirtualClientToKill){
            for(VirtualClient client : this.pendingVirtualClientToKill.keySet()){
                if(new Date().getTime() - this.pendingVirtualClientToKill.get(client) > 1000 * Settings.TIME_TO_WAIT_BEFORE_CLIENT_HANDLER_KILL){
                    if(this.connectedClients.containsKey(client)){
                        connectedClients.get(client).x().interrupt();
                        this.mainController.disconnect(this.connectedClients.remove(client).x());
                    }
                    this.lastHeartBeatOfClients.remove(client);
                    synchronized (this.pendingVirtualClientToKill) {
                        this.pendingVirtualClientToKill.remove(client);
                    }
                }
            }
        }
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
            try {
                return (VirtualGameServer) UnicastRemoteObject.exportObject(clientHandlerRMI, 12122);
            }
            catch (RemoteException remoteException){
                try {
                    UnicastRemoteObject.unexportObject(clientHandlerRMI, true);
                }
                catch (NoSuchElementException ignored) { };

                return (VirtualGameServer) UnicastRemoteObject.exportObject(clientHandlerRMI, 12122);
            }
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
            try {
                return (VirtualGameServer) UnicastRemoteObject.exportObject(clientHandlerRMI, 12122);
            }
            catch (RemoteException remoteException){
                try {
                    UnicastRemoteObject.unexportObject(clientHandlerRMI, true);
                }
                catch (NoSuchElementException ignored) { };

                return (VirtualGameServer) UnicastRemoteObject.exportObject(clientHandlerRMI, 12122);
            }
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
                clientRMI.pushUpdate(new NetworkHandlingErrorMessage(NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER,
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
            try {
                return (VirtualGameServer) UnicastRemoteObject.exportObject(clientHandlerRMI, 12122);
            }
            catch (RemoteException remoteException){
                try {
                    UnicastRemoteObject.unexportObject(clientHandlerRMI, true);
                }
                catch (NoSuchElementException ignored) { };

                return (VirtualGameServer) UnicastRemoteObject.exportObject(clientHandlerRMI, 12122);
            }
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
        if(this.mainController.registerToFirstAvailableGame(clientHandlerRMI)){
            try {
                return (VirtualGameServer) UnicastRemoteObject.exportObject(clientHandlerRMI, 12122);
            }
            catch (RemoteException remoteException){
                try {
                    UnicastRemoteObject.unexportObject(clientHandlerRMI, true);
                }
                catch (NoSuchElementException ignored) { };

                return (VirtualGameServer) UnicastRemoteObject.exportObject(clientHandlerRMI, 12122);
            }
        }
        return null;
    }

    /**
     * This method support advanced functionality resilience to disconnection. It checks if client is already
     * connected to server (in other words if it correctly sends heartbeats). If yes, reconnection is no needed.
     * Otherwise, it is necessary to check tokens. If there is another {@link VirtualClient} with the same token
     * in <code>connectedClients</code> then reconnection can take place. Otherwise, requesting player is faking to be
     * some other and it returns null. Lastly, it exports the new game server and insert first heartbeat inside
     * <code>lastHeartBeatOfClients</code>.
     * @param clientRMI virtual client of the player who wants to reconnect
     * @param nickName nickname of client
     * @param token private token of the client
     * @return {@link VirtualGameServer} remote object of the game server
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public VirtualGameServer reconnect(VirtualClient clientRMI, String nickName, String token) throws RemoteException {
        VirtualClient clientRMIBefore = null;
        ClientHandlerRMI clientHandlerRMI = null;
        boolean found = false;
        if(this.lastHeartBeatOfClients.containsKey(clientRMI)){
            clientRMI.pushUpdate(new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER,
                                                       "You cannot reconnect to server because you are already connected!")
                                         .setHeader(nickName));
            return null;
        }


        synchronized (this.connectedClients) {
            for (var v : this.connectedClients.entrySet()) {
                if (v.getValue().y().equals(token)) {
                    clientRMIBefore = v.getKey();
                    v.getValue().x().interruptClientHandler();
                    clientHandlerRMI = new ClientHandlerRMI(clientRMI, v.getValue().x());
                    clientHandlerRMI.start();
                    clientHandlerRMI.setGameController(v.getValue().x().getGameController()); //Setting new game controller inside Client RMI Handler
                    this.connectedClients.put(clientRMI, new Tuple<>(clientHandlerRMI, v.getValue().y()));
                    found = true;
                    break;
                }
            }
        }

        if(found){
            this.lastHeartBeatOfClients.remove(clientRMIBefore);
            this.lastHeartBeatOfClients.put(clientRMI, new Date().getTime());

            if(this.mainController.reconnect(clientHandlerRMI)){
                try {
                    return (VirtualGameServer) UnicastRemoteObject.exportObject(clientHandlerRMI, 12122);
                }
                catch (RemoteException remoteException){
                    try {
                        UnicastRemoteObject.unexportObject(clientHandlerRMI, true);
                    }
                    catch (NoSuchElementException ignored) { };

                    return (VirtualGameServer) UnicastRemoteObject.exportObject(clientHandlerRMI, 12122);
                }
            }
        }
        else {
            synchronized (this.connectedClients){
                if(!this.connectedClients.containsKey(clientRMI)) {
                    clientRMI.pushUpdate(new NetworkHandlingErrorMessage(NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER,
                                                                         "You are not registered to server! Please register")
                                                 .setHeader(nickName));
                }
                else{
                    clientRMI.pushUpdate(new NetworkHandlingErrorMessage(NetworkError.COULD_NOT_RECONNECT,
                                                               "Could not reconnect to server!")
                                                 .setHeader(nickName));
                }
            }
        }
        return null;
    }

    /**
     * This method disconnects one client from the RMI server. Client must explicitly tell to server that
     * he wants to be disconnected. It removes {@param clientRMI} from both <code>connectedClients</code>
     * and <code>lastHeartBeatOfClients</code>. Finally, it tells to {@link MainController} to disconnect
     * the player and and send to player a {@link DisconnectFromServer}.
     * @param clientRMI virtual client of the player who wants to be disconnected
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public void disconnect(VirtualClient clientRMI, String nickName) throws RemoteException {
        synchronized (this.connectedClients){
            if(!this.connectedClients.containsKey(clientRMI)){
                clientRMI.pushUpdate(new NetworkHandlingErrorMessage(NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER,
                                                                     "Your virtual client is not registered to server! Please register...")
                                             .setHeader((nickName == null) ? "" : nickName));
                return;
                //@TODO: check if it is possible to have errors here? If client has already disconnected what happens?
            }
            else{
                var clientToDisconnect = this.connectedClients.remove(clientRMI);
                clientToDisconnect.x().sendMessageToClient(new DisconnectFromServer().setHeader(nickName));
                clientToDisconnect.x().interrupt();

                try{
                    UnicastRemoteObject.unexportObject(clientToDisconnect.x(), true);
                }
                catch (NoSuchObjectException ignored){ };

                this.mainController.disconnect(clientToDisconnect.x());
            }
        }
        this.lastHeartBeatOfClients.remove(clientRMI);
    }

    @Override
    protected void runHeartBeatTesterForClient(){
        String playerName;
        for (VirtualClient virtualClient : this.lastHeartBeatOfClients.keySet()) {
            if (new Date().getTime() - this.lastHeartBeatOfClients.get(virtualClient) > 1000 * Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS) {
                this.lastHeartBeatOfClients.remove(virtualClient);
                synchronized (this.connectedClients) {
                    playerName = this.connectedClients.get(virtualClient).x().getUsername();
                }
                this.mainController.setPlayerInactive(playerName);

                synchronized (this.pendingVirtualClientToKill){
                    this.pendingVirtualClientToKill.put(virtualClient, new Date().getTime());
                }
            }
        }
    }

    /**
     * This method notifies to the server that client is still alive.
     * @param virtualClient virtual client of the player
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public void heartBeat(VirtualClient virtualClient) throws RemoteException{
        if(this.lastHeartBeatOfClients.containsKey(virtualClient)) {
            this.lastHeartBeatOfClients.put(virtualClient, new Date().getTime());
            synchronized (this.pendingVirtualClientToKill){
                this.pendingVirtualClientToKill.remove(virtualClient);
            }
        }
    }

    /**
     * This method is used by RMI clients to ask server about games
     * they can freely join.
     * @param clientRMI is the {@link VirtualClient} of the sender client
     * @param nickName is the nickname of the player
     * @throws RemoteException if something goes wrong while doing the requested action
     */
    @Override
    public void requestAvailableGames(VirtualClient clientRMI, String nickName) throws RemoteException {
        ClientHandlerRMI clientHandlerRMI;
        synchronized(this.connectedClients){
            if(!this.connectedClients.containsKey(clientRMI)){
                clientRMI.pushUpdate(new NetworkHandlingErrorMessage(NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER,
                                                                     "Your virtual client is not registered to server! Please register...")
                                             .setHeader(nickName));
                return;
            }
            clientHandlerRMI = this.connectedClients.get(clientRMI).x();
            clientHandlerRMI.sendMessageToClient(new AvailableGamesMessage(mainController.findAvailableGames())
                                                         .setHeader(nickName));
        }
    }

    /**
     * This method is used by RMI clients that want to exit the game where they are
     * actually in. To do that, it uses {@link MainController#disconnectPlayerFromGame(ClientHandler)}.
     * @param clientRMI is the {@link VirtualClient} of the sender client
     * @param nickname is the nickname of the player
     * @throws RemoteException if something goes wrong while doing the requested action
     */
    @Override
    public void disconnectFromGame(VirtualClient clientRMI, String nickname) throws RemoteException {
        ClientHandlerRMI clientHandlerRMI;
        synchronized (this.connectedClients){
            if(!this.connectedClients.containsKey(clientRMI)){
                clientRMI.pushUpdate(new NetworkHandlingErrorMessage(NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER,
                                                                     "Your virtual client is not registered to server! Please register...")
                                             .setHeader((nickname == null) ? "" : nickname));
                return;
            }
            else{
                clientHandlerRMI = this.connectedClients.get(clientRMI).x();
                try{
                    UnicastRemoteObject.unexportObject(clientHandlerRMI, true);
                }
                catch (NoSuchObjectException ignored){ };
            }
        }
        this.mainController.disconnectPlayerFromGame(clientHandlerRMI);
    }

    /**
     * This method is used when someone wants to reset the server.
     * Used only for testing purposes.
     */
    @Override
    public void resetServer() {
        this.heartBeatTesterExecutor.shutdownNow();
        this.inactiveClientKillerExecutor.shutdownNow();

        synchronized (connectedClients) {
            this.connectedClients.clear();
        }
        synchronized (lastHeartBeatOfClients) {
            this.lastHeartBeatOfClients.clear();
        }

        this.mainController.resetMainController();
    }

    /**
     * This method is used to kill {@link ClientHandlerRMI} connected
     * to server.
     */
    @Override
    public void killClientHandlers(){
        synchronized (this.connectedClients){
            for(var c : this.connectedClients.entrySet()){
                c.getValue().x().interruptClientHandler();
            }
        }
    }

}