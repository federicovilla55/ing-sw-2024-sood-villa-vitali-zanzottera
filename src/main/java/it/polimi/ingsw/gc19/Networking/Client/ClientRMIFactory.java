package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.ClientRMI;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageHandler;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;

import java.rmi.RemoteException;

public class ClientRMIFactory implements ClientFactory {
    @Override
    public ClientInterface createClient(ClientController clientController) throws RemoteException {
        MessageHandler messageHandler = new MessageHandler(clientController);
        ClientInterface clientInterface =  new ClientRMI(messageHandler);
        messageHandler.setClient(clientInterface);
        messageHandler.start();

        return clientInterface;
    }
}
