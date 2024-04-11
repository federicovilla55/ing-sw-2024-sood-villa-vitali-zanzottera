package it.polimi.ingsw.gc19.Networking.Server.ServerRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualHeartBeatServer extends Remote{
    void heartBeat() throws RemoteException;
}
