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

    private void run() throws RemoteException {
        this.server.NewConnection(this, "Aryan");
    }

    public static void main(String[] args)  throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry("127.0.0.1", 12122);
        VirtualServer server = (VirtualServer) registry.lookup("RMIServer");

        new ClientRmiTest(server).run();
    }
    @Override
    public void GetMessage(MessageToClient message) {

    }
}
