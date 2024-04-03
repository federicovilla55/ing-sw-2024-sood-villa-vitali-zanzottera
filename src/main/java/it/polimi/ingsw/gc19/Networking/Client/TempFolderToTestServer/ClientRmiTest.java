package it.polimi.ingsw.gc19.Networking.Client.TempFolderToTestServer;

import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.VirtualServer;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ClientRmiTest extends UnicastRemoteObject implements VirtualClient {
    final VirtualServer server;
    protected ClientRmiTest(VirtualServer server) throws RemoteException {
        this.server = server;
    }

    private void run() {
        try {
            this.server.newConnection(this, "Aryan");
        } catch (RemoteException e) {
            System.err.println("Name Already present");
        }
    }

    public static void main(String[] args)  throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(args[0], 12122);
        VirtualServer server = (VirtualServer) registry.lookup("RMIServer");

        new ClientRmiTest(server).run();
    }

    @Override
    public void GetMessage(MessageToClient message) throws RemoteException{

    }
}