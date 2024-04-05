package it.polimi.ingsw.gc19.Networking.Server.ServerRmi;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.Server;
import it.polimi.ingsw.gc19.Networking.Server.VirtualServer;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerRMI extends Server implements VirtualServer{

    private static ServerRMI serverRMI = null;

    private static final long MAX_DELAY_BETWEEN_HEARTBEAT = 20;

    private final HashMap<VirtualClient, ClientHandlerRMI> connectedClients;
    private final HashMap<VirtualClient, Long> lastHeartBeatOfClients;

    public static ServerRMI getServerRMI(){
        if(serverRMI == null){
            serverRMI = new ServerRMI();
            return serverRMI;
        }
        return serverRMI;
    }

    public ServerRMI(){
        super();
        this.connectedClients = new HashMap<>();
        this.lastHeartBeatOfClients = new HashMap<>();
        Runnable checkHeartBeat = new Runnable(){
            @Override
            public void run() {
                synchronized(ServerRMI.serverRMI.lastHeartBeatOfClients){
                    for(var e : ServerRMI.serverRMI.lastHeartBeatOfClients.entrySet()){
                        if(new Date().getTime() - e.getValue() > MAX_DELAY_BETWEEN_HEARTBEAT){
                            ServerRMI.serverRMI.controller.setPlayerInactive(ServerRMI.serverRMI.connectedClients.get(e.getKey()).getName());
                            ServerRMI.serverRMI.connectedClients.remove(e.getKey());
                            ServerRMI.serverRMI.lastHeartBeatOfClients.remove(e.getKey());
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
        if(this.getController().createClient(newClient)){
            connectedClients.put(clientRMI, newClient);
            this.lastHeartBeatOfClients.put(clientRMI, new Date().getTime());
        }
    }

    @Override
    public void disconnect(VirtualClient clientRMI, String nickName) throws RemoteException{
        if(!this.connectedClients.containsKey(clientRMI)){
            throw new RemoteException("You are not registered to server!");
        }
        this.getController().setPlayerInactive(nickName);
        this.connectedClients.remove(clientRMI);
        this.lastHeartBeatOfClients.remove(clientRMI);
    }

    @Override
    public void createGame(VirtualClient clientRMI, String gameName, int numPlayer) throws RemoteException {
        if(!this.connectedClients.containsKey(clientRMI)){
            throw new RemoteException("You are not registered to server!");
        }
        ClientHandlerRMI clientToAdd = this.connectedClients.get(clientRMI);
        this.getController().createGame(gameName, numPlayer, clientToAdd);
    }

    @Override
    public void joinGame(VirtualClient clientRMI, String gameName) throws RemoteException {
        if(!this.connectedClients.containsKey(clientRMI)){
            throw new RemoteException("You are not registered to server!");
        }
        //@TODO: handle exception!
        ClientHandlerRMI clientToAdd = this.connectedClients.get(clientRMI);
        this.getController().registerToGame(clientToAdd, gameName);
    }

    @Override
    public void placeCard(VirtualClient clientRMI, String cardToInsert, String anchorCard, Direction directionToInsert) throws RemoteException {
        if(!this.connectedClients.containsKey(clientRMI)){
            throw new RemoteException("You are not registered to server!");
        }
        //@TODO: handle exception!
        this.getController().makeMove(this.connectedClients.get(clientRMI), cardToInsert, anchorCard, directionToInsert);
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
        this.controller.reconnect(reconnectedClient, gameName);
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
        this.getController().setInitialCard(this.connectedClients.get(clientRMI), cardOrientation);
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

}

