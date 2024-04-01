package it.polimi.ingsw.gc19.Networking.Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualServer extends Remote {
    public void NewUser() throws RemoteException;
    public void CreateGame() throws RemoteException;
    public void JoinGame() throws RemoteException;
    public void PlaceCard() throws RemoteException;
    public void HeartBeat() throws RemoteException;
    public void Reconnect() throws RemoteException;
}
