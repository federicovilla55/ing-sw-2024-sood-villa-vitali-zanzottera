package it.polimi.ingsw.gc19.Networking.Client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Client extends Remote{

    void update() throws RemoteException;
}
