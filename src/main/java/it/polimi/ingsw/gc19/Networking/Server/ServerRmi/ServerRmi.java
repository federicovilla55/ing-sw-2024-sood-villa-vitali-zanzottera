package it.polimi.ingsw.gc19.Networking.Server.ServerRmi;

import it.polimi.ingsw.gc19.Controller.Controller;
import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.HandleClient;
import it.polimi.ingsw.gc19.Networking.Server.VirtualServer;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerRmi implements VirtualServer{

    private List<HandleClient> ListClient;

    private Map<String, HandleClient> MapClientToHandle;
    final Controller MasterController;
    public ServerRmi(List<HandleClient> ActiveList, Controller MasterController){
        this.ListClient = ActiveList;
        this.MasterController = MasterController;
        MapClientToHandle = new HashMap<>();
    }

    @Override
    public void NewConnection(VirtualClient clientRmi,String nickName) throws RemoteException{
        boolean check = MasterController.NewClient(nickName);
        if(!check) {
            System.err.println("Name already connected");
            throw new RemoteException("Nickname already present");
        }
        else {
            HandleClient newClient = new ClientHandleRmi(clientRmi, nickName);
            System.err.println("new client connected: "+nickName);
            ListClient.add(newClient);
            MapClientToHandle.put(nickName, newClient);
        }
    }

    @Override
    public void NewUser(String nickname) throws RemoteException {
        System.err.println("new client connected");
    }

    @Override
    public void CreateGame(String nickName, String gameName, int numPlayer) throws RemoteException {
        try {
            MasterController.createGame(nickName, gameName, numPlayer, MapClientToHandle.get(nickName));
        } catch (IOException e) {
            throw new RemoteException("Game name already present");
        }
    }

    @Override
    public void JoinGame(String nickName, String GameName) throws RemoteException {
        MasterController.joinGame(nickName, GameName, MapClientToHandle.get(nickName));
    }

    @Override
    public void PlaceCard(String nickName, String cardToInsert, String anchorCard, Direction directionToInsert) throws RemoteException {
        MasterController.makeMove(nickName,cardToInsert,anchorCard,directionToInsert);
    }

    @Override
    public void HeartBeat(String nickName) throws RemoteException {
        for(HandleClient client : ListClient ) {
            client.UpdateHeartBeat();
        }
    }

    @Override
    public void Reconnect() throws RemoteException {

    }

    @Override
    public void SendChatTo(String nickName, ArrayList<String> UsersToSend, String messageToSend) throws RemoteException {
        MasterController.SendChatMessage(nickName, UsersToSend, messageToSend);

    }
    @Override
    public void SetInitialCard(String nickName, CardOrientation cardOrientation) throws RemoteException {
        MasterController.setInitialCard(nickName,cardOrientation);
    }

    @Override
    public void DrawFromTable(String nickname, PlayableCardType type, int position) {
        MasterController.DrawCardFromTable(nickname, type, position);
    }

    @Override
    public void DrawFromDeck(String nickname, PlayableCardType type) {
        MasterController.DrawCardFromDeck(nickname,type);
    }
}
