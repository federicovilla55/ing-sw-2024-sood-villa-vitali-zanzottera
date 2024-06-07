package it.polimi.ingsw.gc19.Networking.Client.ClientFactory;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;

import java.io.IOException;

/**
 * This interface is used to implement factory method
 * design pattern for client creation.
 */
public interface ClientFactory {

    /**
     * This is the factory method for client creation
     * @param clientController the {@link ClientController} that needs to be inserted in "network interface"
     * @return the {@link ClientInterface} that has been built
     * @throws IOException if an IO-connected error occurs during the execution
     * @throws RuntimeException if a generic error occurs during the execution
     */
    ClientInterface createClient(ClientController clientController) throws IOException, RuntimeException;

}