package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface VirtualClient  extends Remote{

    public void GetMessage(MessageToClient message) throws RemoteException;
}
