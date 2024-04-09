package it.polimi.ingsw.gc19.Networking.Client.ClientRMI;

import it.polimi.ingsw.gc19.Networking.Server.VirtualGameServer;
import it.polimi.ingsw.gc19.Networking.Server.VirtualMainServer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

public class ClientRMI extends UnicastRemoteObject implements Remote{

    private final VirtualMainServer virtualMainServer;
    private VirtualGameServer virtualGameServer;

    public ClientRMI(VirtualMainServer virtualGameServer) throws RemoteException {
        this.virtualMainServer = virtualGameServer;
        this.virtualGameServer = null;
    }

    public void start(){

    }

}
