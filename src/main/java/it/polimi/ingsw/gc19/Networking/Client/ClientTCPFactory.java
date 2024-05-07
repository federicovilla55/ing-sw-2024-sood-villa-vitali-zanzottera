package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Networking.Client.ClientTCP.ClientTCP;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageHandler;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;

import java.io.IOException;
import java.rmi.RemoteException;

public class ClientTCPFactory implements ClientFactory {
    @Override
    public ClientInterface createClient() throws IOException {
        return new ClientTCP(new MessageHandler(new ClientController()));
    }
}
