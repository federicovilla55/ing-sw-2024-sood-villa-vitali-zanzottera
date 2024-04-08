package it.polimi.ingsw.gc19.Networking.Server.ServerRMI;

import it.polimi.ingsw.gc19.Costants.ImportantConstants;
import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Server;
import it.polimi.ingsw.gc19.Networking.Server.VirtualMainServer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GameServerRMI extends Server implements VirtualMainServer, Remote{

    private static GameServerRMI gameServerRMI = null;
    private final HashMap<VirtualClient, ClientHandlerRMI> connectedClients;
    private final HashMap<VirtualClient, Long> lastHeartBeatOfClients;

    public static GameServerRMI getInstance(){
        if(gameServerRMI == null){
            gameServerRMI = new GameServerRMI();
        }
        return gameServerRMI;
    }

    private GameServerRMI(){
        super();
        this.connectedClients = new HashMap<>();
        this.lastHeartBeatOfClients = new HashMap<>();
    }

    @Override
    public void newConnection(VirtualClient clientRMI, String nickName) throws RemoteException{
        ClientHandlerRMI clientHandlerRMI;
        synchronized(this.connectedClients){
            if(this.connectedClients.containsKey(clientRMI)){
                clientRMI.pushUpdate(new GameHandlingError(Error.CLIENT_ALREADY_CONNECTED_TO_SERVER, "Your virtual client is already registered in server!"));
                return;
            }
        }
        clientHandlerRMI = new ClientHandlerRMI(clientRMI, nickName);
        synchronized(this.connectedClients){
            this.connectedClients.put(clientRMI, clientHandlerRMI);
        }
        this.mainController.createClient(clientHandlerRMI);
    }

    @Override
    public void createGame(VirtualClient clientRMI, String gameName, int numPlayer) throws RemoteException{
        ClientHandlerRMI clientHandlerRMI;
        synchronized(this.connectedClients){
            if(!this.connectedClients.containsKey(clientRMI)){
                clientRMI.pushUpdate(new GameHandlingError(Error.CLIENT_NOT_REGISTERED_TO_SERVER, "Your virtual client is not registered to server! Please register..."));
                return;
            }
            clientHandlerRMI = this.connectedClients.get(clientRMI);
        }
        if(this.mainController.createGame(gameName, numPlayer, clientHandlerRMI)){
            synchronized(this.lastHeartBeatOfClients){
                this.lastHeartBeatOfClients.put(clientRMI, new Date().getTime()); //@TODO: other methods to handle heartbeats?
            }
            Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> runHeartBeatTester(clientRMI),
                                                                    0, ImportantConstants.MAX_DELTA_TIME_BETWEEN_HEARTBEATS / 10, TimeUnit.SECONDS);
        }
    }

    @Override
    public void joinGame(VirtualClient clientRMI, String gameName) throws RemoteException {
        ClientHandlerRMI clientHandlerRMI;
        synchronized(this.connectedClients){
            if(!this.connectedClients.containsKey(clientRMI)){
                clientRMI.pushUpdate(new GameHandlingError(Error.CLIENT_NOT_REGISTERED_TO_SERVER, "Your virtual client is not registered to server! Please register..."));
                return;
            }
            clientHandlerRMI = this.connectedClients.get(clientRMI);
        }
        if(this.mainController.registerToGame(clientHandlerRMI, gameName)){
            synchronized(this.lastHeartBeatOfClients){
                this.lastHeartBeatOfClients.put(clientRMI, new Date().getTime()); //@TODO: other methods to handle heartbeats?
            }
            Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> runHeartBeatTester(clientRMI),
                                                                    0, ImportantConstants.MAX_DELTA_TIME_BETWEEN_HEARTBEATS / 10, TimeUnit.SECONDS);
        }
    }

    @Override
    public void reconnect(VirtualClient clientRMI, String gameName, String nickName) throws RemoteException {

    }

    @Override
    public void disconnect(VirtualClient clientRMI, String nickname) throws RemoteException {

    }

    public void runHeartBeatTester(VirtualClient virtualClient){
        boolean removePlayer = false;
        String playerName;
        synchronized(this.lastHeartBeatOfClients){
            if(new Date().getTime() - this.lastHeartBeatOfClients.get(virtualClient) > ImportantConstants.MAX_DELTA_TIME_BETWEEN_HEARTBEATS){
                removePlayer = true;
                this.connectedClients.remove(virtualClient);
            }
        }
        if(removePlayer) {
            synchronized (this.connectedClients) {
                playerName = this.connectedClients.get(virtualClient).getName();
                this.connectedClients.remove(virtualClient);
            }
            this.mainController.setPlayerInactive(playerName);
        }
    }

    @Override
    public void heartBeat(VirtualClient virtualClient) throws RemoteException{
        synchronized(this.lastHeartBeatOfClients){
            this.lastHeartBeatOfClients.put(virtualClient, new Date().getTime());
        }
    }

    public void kickPlayersOff(String nickname){
        VirtualClient toRemove = null;
        synchronized(this.connectedClients){
            for(Map.Entry<VirtualClient, ClientHandlerRMI> e : this.connectedClients.entrySet()){
                if(e.getValue().getName().equals(nickname)){
                    toRemove = e.getKey();
                }
            }
        }
        synchronized (this.connectedClients){
            this.connectedClients.remove(toRemove);
        }
        synchronized (this.lastHeartBeatOfClients){
            this.connectedClients.remove(toRemove);
        }
    }

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
