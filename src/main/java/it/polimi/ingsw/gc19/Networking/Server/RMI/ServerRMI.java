package it.polimi.ingsw.gc19.Networking.Server.RMI;

import it.polimi.ingsw.gc19.Controller.Controller;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandle;
import it.polimi.ingsw.gc19.Networking.Server.VirtualServer;

import java.rmi.RemoteException;
import java.util.List;

public class ServerRMI implements VirtualServer{

    private List<ClientHandle> ActiveList;
    final Controller MasterController;

    public ServerRMI(List<ClientHandle> ActiveList, Controller MasterController)
    {
        this.ActiveList = ActiveList;
        this.MasterController = MasterController;
    }
    public void Run()
    {

    }

    @Override
    public void NewUser() throws RemoteException {

    }

    @Override
    public void CreateGame() throws RemoteException {

    }

    @Override
    public void JoinGame() throws RemoteException {

    }

    @Override
    public void PlaceCard() throws RemoteException {

    }

    @Override
    public void HeartBeat() throws RemoteException {

    }

    @Override
    public void Reconnect() throws RemoteException {

    }
}
