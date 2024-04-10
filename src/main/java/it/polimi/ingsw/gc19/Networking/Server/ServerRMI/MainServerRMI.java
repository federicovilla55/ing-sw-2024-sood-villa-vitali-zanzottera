package it.polimi.ingsw.gc19.Networking.Server.ServerRMI;

import it.polimi.ingsw.gc19.Costants.ImportantConstants;
import it.polimi.ingsw.gc19.Model.Tuple;
import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.ClientRMI;
import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Server;
import it.polimi.ingsw.gc19.Networking.Server.Settings;
import it.polimi.ingsw.gc19.Networking.Server.VirtualGameServer;
import it.polimi.ingsw.gc19.Networking.Server.VirtualMainServer;

import java.net.NetworkInterface;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainServerRMI extends Server implements VirtualMainServer, Remote{

    private final HashMap<VirtualClient, Tuple<ClientHandlerRMI, String>> connectedClients;
    private final HashMap<VirtualClient, Long> lastHeartBeatOfClients;

    public MainServerRMI(){
        super();
        this.connectedClients = new HashMap<>();
        this.lastHeartBeatOfClients = new HashMap<>();
    }

    @Override
    public void newConnection(VirtualClient clientRMI, String nickName) throws RemoteException{
        ClientHandlerRMI clientHandlerRMI;
        synchronized(this.connectedClients){
            if(this.connectedClients.containsKey(clientRMI)){
                clientRMI.pushUpdate(new GameHandlingError(Error.CLIENT_ALREADY_CONNECTED_TO_SERVER,
                                                           "Your virtual client is already registered in server!")
                                             .setHeader(nickName));
                return;
            }
        }

        clientHandlerRMI = new ClientHandlerRMI(clientRMI, nickName);
        String hashedMessage = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5"); // 128 bits
            // @todo: figure out if it's preferable to use Base64 for bytes to String conversion
            hashedMessage = Arrays.toString(digest.digest((nickName + clientHandlerRMI.toString()).getBytes()));
        } catch (NoSuchAlgorithmException ignored){ };

        if(this.mainController.createClient(clientHandlerRMI)) {
            synchronized (this.connectedClients) {
                this.connectedClients.put(clientRMI, new Tuple<>(clientHandlerRMI, hashedMessage));
            }
            synchronized(this.lastHeartBeatOfClients){
                this.lastHeartBeatOfClients.put(clientRMI, new Date().getTime());
            }

            clientHandlerRMI.update(new CreatedPlayerMessage(clientHandlerRMI.getName(), hashedMessage).setHeader(clientHandlerRMI.getName()));

            Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> runHeartBeatTesterForClient(clientRMI),
                                                                    0, 1000 * Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS / 50, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public VirtualGameServer createGame(VirtualClient clientRMI, String gameName, String nickName, int numPlayer) throws RemoteException{
        ClientHandlerRMI clientHandlerRMI = buildClientHandler(clientRMI, nickName);
        if(clientHandlerRMI == null){
            return null;
        }
        if(this.mainController.createGame(gameName, numPlayer, clientHandlerRMI)){
            return buildGameServerAndStartHeartBeatThread(clientRMI, gameName, clientHandlerRMI);
        }
        return null;
    }

    @Override
    public VirtualGameServer createGame(VirtualClient clientRMI, String gameName, String nickName, int numPlayer, long randomSeed) throws RemoteException{
        ClientHandlerRMI clientHandlerRMI = buildClientHandler(clientRMI, nickName);
        if(clientHandlerRMI == null){
            return null;
        }
        if(this.mainController.createGame(gameName, numPlayer, clientHandlerRMI, randomSeed)){
            return buildGameServerAndStartHeartBeatThread(clientRMI, gameName, clientHandlerRMI);
        }
        return null;
    }

    private ClientHandlerRMI buildClientHandler(VirtualClient clientRMI, String nickName) throws RemoteException {
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

    @Override
    public VirtualGameServer joinGame(VirtualClient clientRMI, String gameName, String nickName) throws RemoteException {
        ClientHandlerRMI clientHandlerRMI = buildClientHandler(clientRMI, nickName);
        if(this.mainController.registerToGame(clientHandlerRMI, gameName)){
            return buildGameServerAndStartHeartBeatThread(clientRMI, gameName, clientHandlerRMI);
        }
        return null;
    }

    @Override
    public VirtualGameServer joinFirstAvailableGame(VirtualClient clientRMI, String nickName) throws RemoteException {
        ClientHandlerRMI clientHandlerRMI = buildClientHandler(clientRMI, nickName);
        String gameName = this.mainController.registerToFirstAvailableGame(clientHandlerRMI);
        if(gameName != null){
            return buildGameServerAndStartHeartBeatThread(clientRMI, gameName, clientHandlerRMI);
        }
        return null;
    }

    private VirtualGameServer buildGameServerAndStartHeartBeatThread(VirtualClient clientRMI, String gameName, ClientHandlerRMI clientHandlerRMI) throws RemoteException {
        VirtualGameServer playerVirtualGameServer = (VirtualGameServer) UnicastRemoteObject.exportObject(clientHandlerRMI, 12122);

        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> runHeartBeatTesterForClient(clientRMI),
                                                                0, 1000 * Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS / 50, TimeUnit.MILLISECONDS);
        return playerVirtualGameServer;
    }

    @Override
    public VirtualGameServer reconnect(VirtualClient clientRMI, String nicName, String token) throws RemoteException {
        VirtualClient clientRMIBefore = null;
        ClientHandlerRMI clientHandlerRMI = null;
        boolean found = false;

        synchronized (this.lastHeartBeatOfClients){
            if(this.lastHeartBeatOfClients.containsKey(clientRMI)){
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
                VirtualGameServer playerVirtualGameServer = (VirtualGameServer) UnicastRemoteObject.exportObject(clientHandlerRMI, 12122);

                Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> runHeartBeatTesterForClient(clientRMI),
                                                                        0, 1000 * Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS / 50, TimeUnit.MILLISECONDS);

                return playerVirtualGameServer;
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
     * This method disconnects one client from the RMI server. This method is called from client
     * only when the application shuts down. It removes virtual client from the hashmap of connected clients
     * and the heartbeat hashmap
     * @param clientRMI
     * @throws RemoteException
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

    /**
     * This method checks if a player has sent the heartbeat correctly.
     * If delta time between heartbeat is greater than max permitted it removes virtual client
     * from the hash of heartbeat to check, but it keeps virtual client inside hashmap of connected clients
     * so that token is still available for reconnection.
     * @param virtualClient
     */
    public void runHeartBeatTesterForClient(VirtualClient virtualClient){ //@TODO: one thread for single player or one for all?
        boolean removePlayer = false;
        String playerName;
        synchronized(this.lastHeartBeatOfClients){
            if(new Date().getTime() - this.lastHeartBeatOfClients.get(virtualClient) > 1000 * Settings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS){
                //System.out.println(new Date().getTime() - this.lastHeartBeatOfClients.get(virtualClient));
                removePlayer = true;
                this.lastHeartBeatOfClients.remove(virtualClient);
            }
        }
        if(removePlayer) {
            synchronized (this.connectedClients) {
                playerName = this.connectedClients.get(virtualClient).x().getName();
            }
            this.mainController.setPlayerInactive(playerName);
        }
    }

    /**
     * This method notifies to the server that client is still alive.
     * @param virtualClient
     * @throws RemoteException
     */
    @Override
    public void heartBeat(VirtualClient virtualClient) throws RemoteException{
        synchronized(this.lastHeartBeatOfClients){
            //System.out.println("Ricevuto hearthbeat");
            if(this.lastHeartBeatOfClients.containsKey(virtualClient)) {
                this.lastHeartBeatOfClients.put(virtualClient, new Date().getTime());
            }
        }
    }

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

    //un player può disconnetersi se fa un comando esplicito, oppure se non manda più heartbeat
    //in entrmbi i casi lo disconnetto dal server
    //quando un gioco termina il main controller mette il player nella lobby, quindi dal punto di vista del server non dovrebbe cambiare niente

}

/*private static GameServerRMI serverRMI = null;

    private static final long MAX_DELAY_BETWEEN_HEARTBEAT = 20;

    private final HashMap<VirtualClient, ClientHandlerRMI> connectedClients;
    private final HashMap<VirtualClient, Long> lastHeartBeatOfClients;

    public static GameServerRMI getServerRMI(){
        if(serverRMI == null){
            serverRMI = new GameServerRMI();
            return serverRMI;
        }
        return serverRMI;
    }

    public GameServerRMI(){
        super();
        this.connectedClients = new HashMap<>();
        this.lastHeartBeatOfClients = new HashMap<>();
        Runnable checkHeartBeat = new Runnable(){
            @Override
            public void run() {
                synchronized(GameServerRMI.serverRMI.lastHeartBeatOfClients){
                    for(var e : GameServerRMI.serverRMI.lastHeartBeatOfClients.entrySet()){
                        if(new Date().getTime() - e.getValue() > MAX_DELAY_BETWEEN_HEARTBEAT){
                            GameServerRMI.serverRMI.mainController.setPlayerInactive(GameServerRMI.serverRMI.connectedClients.get(e.getKey()).getName());
                            GameServerRMI.serverRMI.connectedClients.remove(e.getKey());
                            GameServerRMI.serverRMI.lastHeartBeatOfClients.remove(e.getKey());
                        }
                    }
                }
            }
        };

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(checkHeartBeat, 0, 5, TimeUnit.SECONDS);
    }

    private String getNicknameFromVirtualClient(VirtualClient clientRMI){
        synchronized(this.connectedClients){
            return this.connectedClients.get(clientRMI).getName();
        }
    }

    @Override
    public void newConnection(VirtualClient clientRMI, String nickName) throws RemoteException{
        ClientHandlerRMI newClient = new ClientHandlerRMI(clientRMI, nickName);
        if(this.getController().createClient(newClient, nickName)){
            System.err.println("new client connected: " + nickName);
            connectedClients.put(clientRMI, newClient);
        }
        else {
            System.err.println("Name already connected");
            newClient.update(new GameHandlingError(Error.PLAYER_NAME_ALREADY_IN_USE, "Player " + nickName + " is already connected to server!"));
        }
    }

    @Override
    public void disconnect(VirtualClient clientRMI, String nickName) throws RemoteException{
        if(!this.connectedClients.containsKey(clientRMI)){
            throw new RemoteException("You are not registered to server!"); //GUARDARE MEGLIO
        }
        this.getController().setPlayerInactive(nickName);
        this.connectedClients.remove(clientRMI);
        this.lastHeartBeatOfClients.remove(clientRMI);
    }

    @Override
    public void createGame(VirtualClient clientRMI, String gameName, int numPlayer) throws RemoteException {
        if(!this.connectedClients.containsKey(clientRMI)){
            throw new RemoteException("You are not registered to server!"); //GUARDARE MEGLIO
        }
        ClientHandlerRMI clientToAdd = this.connectedClients.get(clientRMI);
        this.lastHeartBeatOfClients.put(clientRMI, new Date().getTime());
        this.getController().createGame(gameName, numPlayer, clientToAdd);
    }

    @Override
    public void joinGame(VirtualClient clientRMI, String gameName) throws RemoteException {
        if(!this.connectedClients.containsKey(clientRMI)){
            throw new RemoteException("You are not registered to server!"); //GUARDARE MEGLIO
        }
        //@TODO: handle exception!
        ClientHandlerRMI clientToAdd = this.connectedClients.get(clientRMI);
        this.lastHeartBeatOfClients.put(clientRMI, new Date().getTime());
        this.getController().registerToGame(clientToAdd, gameName);
    }

    @Override
    public void placeCard(VirtualClient clientRMI, String cardToInsert, String anchorCard, Direction directionToInsert, CardOrientation cardOrientation) throws RemoteException {
        if(!this.connectedClients.containsKey(clientRMI)){
            throw new RemoteException("You are not registered to server!");
        }
        //@TODO: handle exception!
        this.getController().placeCard(this.connectedClients.get(clientRMI), cardToInsert, anchorCard, directionToInsert, cardOrientation);
    }

    @Override
    public void heartBeat(VirtualClient clientRMI) throws RemoteException {
        synchronized(this.lastHeartBeatOfClients){
            this.lastHeartBeatOfClients.put(clientRMI, new Date().getTime());
        }
    }

    @Override
    public void reconnect(VirtualClient clientRMI, String gameName, String nickName) throws RemoteException{
        if(this.connectedClients.containsKey(clientRMI)){
            throw new RemoteException("You are already connected!");
        }
        ClientHandlerRMI reconnectedClient = new ClientHandlerRMI(clientRMI, nickName);
        this.connectedClients.put(clientRMI, reconnectedClient);
        this.mainController.reconnect(reconnectedClient, gameName);
        this.lastHeartBeatOfClients.put(clientRMI, new Date().getTime());
    }

    @Override
    public void sendChatMessage(VirtualClient clientRMI, ArrayList<String> UsersToSend, String messageToSend) throws RemoteException {
        if(!this.connectedClients.containsKey(clientRMI)){
            throw new RemoteException("You are not registered to server!");
        }
        //System.out.println(UsersToSend.toString());
        this.getController().sendChatMessage(this.connectedClients.get(clientRMI), UsersToSend, messageToSend);

    }
    @Override
    public void placeInitialCard(VirtualClient clientRMI, CardOrientation cardOrientation) throws RemoteException {
        if(!this.connectedClients.containsKey(clientRMI)){
            throw new RemoteException("You are not registered to server!");
        }
        this.getController().placeInitialCard(this.connectedClients.get(clientRMI), cardOrientation);
    }

    @Override
    public void pickCardFromTable(VirtualClient clientRMI, PlayableCardType type, int position) throws RemoteException{
        if(!this.connectedClients.containsKey(clientRMI)){
            throw new RemoteException("You are not registered to server!");
        }
        this.getController().pickCardFromTable(this.connectedClients.get(clientRMI), type, position);
    }

    @Override
    public void pickCardFromDeck(VirtualClient clientRMI, PlayableCardType type) throws RemoteException{
        if(!this.connectedClients.containsKey(clientRMI)){
            throw new RemoteException("You are not registered to server!");
        }
        this.getController().pickCardFromDeck(this.connectedClients.get(clientRMI), type);
    }

 */
