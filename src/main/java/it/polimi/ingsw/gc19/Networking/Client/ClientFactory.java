package it.polimi.ingsw.gc19.Networking.Client;

import java.io.IOException;
import java.rmi.RemoteException;

public interface ClientFactory {
    ClientInterface createClient() throws IOException;
}
