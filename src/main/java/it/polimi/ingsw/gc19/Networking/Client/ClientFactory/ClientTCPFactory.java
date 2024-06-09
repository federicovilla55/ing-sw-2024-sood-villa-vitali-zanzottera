package it.polimi.ingsw.gc19.Networking.Client.ClientFactory;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.ClientTCP.ClientTCP;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageHandler;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;

import java.io.IOException;

/**
 * Factory method for TCP client
 */
public class ClientTCPFactory implements ClientFactory {

    /**
     * Tries to build a {@link ClientTCP}
     * @param clientController the {@link ClientController} that needs to be inserted in "network interface"
     * @return the {@link ClientInterface} built
     * @throws IOException if an IO-related error occurs during the process
     */
    @Override
    public ClientInterface createClient(ClientController clientController) throws IOException {
        MessageHandler messageHandler = new MessageHandler(clientController);
        ClientInterface clientInterface =  new ClientTCP(messageHandler);
        messageHandler.start();

        return clientInterface;
    }
}