package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;

import java.rmi.RemoteException;

public interface VirtualMainServer{

    public void newConnection(VirtualClient clientRmi, String nickName) throws RemoteException;
    public void createGame(VirtualClient clientRMI, String gameName, int numPlayer) throws RemoteException;
    public void joinGame(VirtualClient clientRMI, String GameName) throws RemoteException;
    public void reconnect(VirtualClient clientRMI, String gameName, String nickName) throws RemoteException;
    void disconnect(VirtualClient clientRMI, String nickname) throws RemoteException;

}
