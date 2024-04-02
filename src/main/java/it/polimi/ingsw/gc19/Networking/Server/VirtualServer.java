package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface VirtualServer extends Remote {
    public void NewConnection(VirtualClient ClientRmi, String nickName) throws RemoteException;
    public void NewUser(String nickname) throws RemoteException;
    public void CreateGame(String nickName, String gameName, int numPlayer) throws RemoteException;
    public void JoinGame(String nickName, String GameName) throws RemoteException;
    public void PlaceCard() throws RemoteException;
    public void HeartBeat(String nickName) throws RemoteException;
    public void Reconnect() throws RemoteException;
    public void SendChatTo(String nickName,ArrayList<String> UsersToSend) throws RemoteException;
}
