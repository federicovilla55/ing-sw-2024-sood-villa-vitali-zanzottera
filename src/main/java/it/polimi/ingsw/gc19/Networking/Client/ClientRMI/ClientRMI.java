package it.polimi.ingsw.gc19.Networking.Client.ClientRMI;

import it.polimi.ingsw.gc19.Networking.Server.VirtualGameServer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

public class ClientRMI extends UnicastRemoteObject implements Remote{

    private final VirtualGameServer virtualMainServer;
    private VirtualGameServer virtualGameServer;

    public ClientRMI(VirtualGameServer virtualGameServer) throws RemoteException {
        this.virtualMainServer = virtualGameServer;
        this.virtualGameServer = null;
    }

    public ClientRMI(int port, VirtualGameServer virtualGameServer) throws RemoteException {
        super(port);
        this.virtualMainServer = virtualGameServer;
        this.virtualGameServer = null;
    }

    public ClientRMI(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf, VirtualGameServer virtualGameServer) throws RemoteException {
        super(port, csf, ssf);
        this.virtualMainServer = virtualGameServer;
        this.virtualGameServer = null;
    }

    public void start(){

    }

}
