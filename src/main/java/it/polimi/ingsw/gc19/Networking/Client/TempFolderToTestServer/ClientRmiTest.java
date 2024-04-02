package it.polimi.ingsw.gc19.Networking.Client.TempFolderToTestServer;

import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientRmiTest extends UnicastRemoteObject implements VirtualClient {
    protected ClientRmiTest() throws RemoteException {
    }

    @Override
    public void GetMessage(MessageToClient message) {

    }
}
