package it.polimi.ingsw.gc19.Networking.Client.ClientRMI;

import it.polimi.ingsw.gc19.Networking.Server.VirtualServer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

public class ClientRMI extends UnicastRemoteObject implements Remote{

    private final VirtualServer virtualMainServer;
    private VirtualServer virtualGameServer;

    public ClientRMI(VirtualServer virtualServer) throws RemoteException {
        this.virtualMainServer = virtualServer;
        this.virtualGameServer = null;
    }

    public ClientRMI(int port, VirtualServer virtualServer) throws RemoteException {
        super(port);
        this.virtualMainServer = virtualServer;
        this.virtualGameServer = null;
    }

    public ClientRMI(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf, VirtualServer virtualServer) throws RemoteException {
        super(port, csf, ssf);
        this.virtualMainServer = virtualServer;
        this.virtualGameServer = null;
    }

    public void start(){

    }

}
