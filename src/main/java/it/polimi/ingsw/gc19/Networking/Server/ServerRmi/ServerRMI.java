package it.polimi.ingsw.gc19.Networking.Server.ServerRmi;

import it.polimi.ingsw.gc19.Controller.Controller;
import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Server;
import it.polimi.ingsw.gc19.Networking.Server.VirtualServer;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ServerRMI extends Server implements VirtualServer{

    private static ServerRMI serverRMI = null;

    private final HashMap<VirtualClient, ClientHandlerRMI> connectedClients;
    private final Set<ClientHandlerRMI> activeClients;
    private final Set<ClientHandlerRMI> inactiveClients;

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
        this.activeClients = new HashSet<>();
        this.inactiveClients = new HashSet<>();
    }

    @Override
    public void newConnection(VirtualClient clientRMI, String nickName) throws RemoteException{
        ClientHandlerRMI newClient = new ClientHandlerRMI(clientRMI, nickName);
        if(this.getController().createClient(nickName, newClient)){
            System.err.println("new client connected: " + nickName);
            connectedClients.put(clientRMI, newClient);
        }
        else {
            System.err.println("Name already connected");
            throw new RemoteException("Nickname already present");
        }
    }

    @Override
    public void createGame(VirtualClient clientRMI, String gameName, int numPlayer) throws RemoteException {
        if(!this.connectedClients.containsKey(clientRMI)){
            throw new RemoteException("You are not registered to server!");
        }
        ClientHandlerRMI clientToAdd = this.connectedClients.get(clientRMI);
        this.getController().createGame(gameName, numPlayer);
        this.getController().registerToGame(clientToAdd, gameName);
        this.activeClients.add(clientToAdd);
    }

    @Override
    public void joinGame(VirtualClient clientRMI, String gameName) throws RemoteException {
        if(!this.connectedClients.containsKey(clientRMI)){
            throw new RemoteException("You are not registered to server!");
        }
        //@TODO: handle exception!
        ClientHandlerRMI clientToAdd = this.connectedClients.get(clientRMI);
        this.getController().registerToGame(clientToAdd, gameName);
        this.activeClients.add(clientToAdd);
    }

    @Override
    public void PlaceCard(String nickName, String cardToInsert, String anchorCard, Direction directionToInsert) throws RemoteException {
        this.getController().makeMove(nickName, cardToInsert, anchorCard, directionToInsert);
    }

    @Override
    public void HeartBeat(String nickName) throws RemoteException {
        /*for(HandleClient client : ListClient ) {
            client.UpdateHeartBeat();
        }*/
    }

    @Override
    public void Reconnect() throws RemoteException {

    }

    @Override
    public void sendChatMessage(VirtualClient clientRMI, ArrayList<String> UsersToSend, String messageToSend) throws RemoteException {
        if(!this.connectedClients.containsKey(clientRMI)){
            throw new RemoteException("You are not registered to server!");
        }
        System.out.println(UsersToSend.toString());
        this.getController().sendChatMessage(this.connectedClients.get(clientRMI).getName(), UsersToSend, messageToSend);

    }
    @Override
    public void SetInitialCard(String nickName, CardOrientation cardOrientation) throws RemoteException {
        //MasterController.setInitialCard(nickName,cardOrientation);
    }

    @Override
    public void DrawFromTable(String nickname, PlayableCardType type, int position) {
        //MasterController.DrawCardFromTable(nickname, type, position);
    }

    @Override
    public void DrawFromDeck(String nickname, PlayableCardType type) {
        //MasterController.DrawCardFromDeck(nickname,type);
    }
}

