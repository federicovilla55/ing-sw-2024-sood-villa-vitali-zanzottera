package it.polimi.ingsw.gc19.Networking.Server.ServerRmi;

import it.polimi.ingsw.gc19.Controller.Controller;
import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.HandleClient;
import it.polimi.ingsw.gc19.Networking.Server.VirtualServer;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServerRmi implements VirtualServer{

    private List<HandleClient> ListClient;

    private Map<String, HandleClient> MapClientToHandle;
    final Controller MasterController;
    public ServerRmi(List<HandleClient> ActiveList, Controller MasterController){
        this.ListClient = ActiveList;
        this.MasterController = MasterController;
    }

    @Override
    public void NewConnection(VirtualClient clientRmi, String nickName) throws RemoteException{
        boolean check = MasterController.NewClient(nickName);
        if(!check) {
            throw new RemoteException("Nickname already present");
        }
        else {
            HandleClient newClient = new ClientHandleRmi(clientRmi, nickName);
            ListClient.add(newClient);
            MapClientToHandle.put(nickName, newClient);
        }
    }

    @Override
    public void NewUser(String nickname) throws RemoteException {
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
    public void PlaceCard() throws RemoteException {

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
    public void SendChatTo(String nickName, ArrayList<String> UsersToSend) throws RemoteException {

    }
}
