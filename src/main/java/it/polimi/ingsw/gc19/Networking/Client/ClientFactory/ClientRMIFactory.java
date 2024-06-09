package it.polimi.ingsw.gc19.Networking.Client.ClientFactory;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.ClientRMI;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageHandler;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;

import java.rmi.RemoteException;

/**
 * Factory method for RMI client
 */
public class ClientRMIFactory implements ClientFactory {

    /**
     * Create an RMI client
     * @param clientController the {@link ClientController} that needs to be inserted in "network interface"
     * @return the built {@link ClientInterface}
     * @throws RemoteException if RMI - error occurs during creation
     * @throws RuntimeException if a generic error occurs during creation
     */
    @Override
    public ClientInterface createClient(ClientController clientController) throws RemoteException, RuntimeException {
        MessageHandler messageHandler = new MessageHandler(clientController);
        ClientInterface clientInterface =  new ClientRMI(messageHandler);
        messageHandler.start();

        return clientInterface;
    }
}