package it.polimi.ingsw.gc19.Networking.Server.ServerRMI;

import it.polimi.ingsw.gc19.Networking.Server.VirtualMainServer;

import java.rmi.RemoteException;

public interface VirtualMainServerForTests extends VirtualMainServer{
    void resetMainServer() throws RemoteException;
}
