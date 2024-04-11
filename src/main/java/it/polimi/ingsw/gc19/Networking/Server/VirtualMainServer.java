package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualMainServer extends Remote {
    void newConnection(VirtualClient clientRmi, String nickName) throws RemoteException;
    VirtualGameServer createGame(VirtualClient clientRMI, String gameName, String nickname, int numPlayer) throws RemoteException;
    VirtualGameServer createGame(VirtualClient clientRMI, String gameName, String nickname, int numPlayer, long randomSeed) throws RemoteException;
    VirtualGameServer joinGame(VirtualClient clientRMI, String GameName, String nickname) throws RemoteException;
    VirtualGameServer joinFirstAvailableGame(VirtualClient clientRMI, String nickName) throws RemoteException;
    VirtualGameServer reconnect(VirtualClient clientRMI, String nickName, String token) throws RemoteException;
    void disconnect(VirtualClient clientRMI, String nickName) throws RemoteException;
    void heartBeat(VirtualClient clientRMI) throws RemoteException;
    void resetMainServer() throws RemoteException;

}
