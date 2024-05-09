package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.View.ClientController.ClientController;

import java.io.IOException;

public interface ClientFactory {
    ClientInterface createClient(ClientController clientController) throws IOException;
}
