package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;

import java.util.ArrayList;

/**
 * This state represent the moment a client opens the game and is not connected
 * to the server. The client needs to select a nickname and ask for a connection request.
 */
class NotPlayer extends ClientState {
    @Override
    public void nextState(NetworkHandlingErrorMessage message) {
        clientController.handleError(message);
    }


    public NotPlayer(ClientController clientController, ClientInterface clientInterface) {
        super(clientController, clientInterface);
    }

    @Override
    public ViewState getState() {
        return ViewState.NOT_PLAYER;
    }

}
