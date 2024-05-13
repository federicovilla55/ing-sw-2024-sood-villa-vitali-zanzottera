package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.ClientRMI;
import it.polimi.ingsw.gc19.Networking.Client.ClientTCP.ClientTCP;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageHandler;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;

import java.io.IOException;

public class ClientTCPFactory implements ClientFactory {
    @Override
    public ClientInterface createClient(ClientController clientController) throws IOException {
        MessageHandler messageHandler = new MessageHandler(clientController);
        ClientInterface clientInterface =  new ClientTCP(messageHandler);
        messageHandler.start();

        return clientInterface;
    }
}
