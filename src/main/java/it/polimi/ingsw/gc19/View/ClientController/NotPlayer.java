package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.View.Listeners.ListenersManager;

/**
 * This state represent the moment a client opens the game and is not connected
 * to the server. The client needs to select a nickname and ask for a connection request.
 */
class NotPlayer extends ClientState {

    public NotPlayer(ClientController clientController, ClientInterface clientInterface, ListenersManager listenersManager) {
        super(clientController, clientInterface, listenersManager);
    }

    @Override
    public void nextState(NetworkHandlingErrorMessage message) {
        clientController.handleError(message);
    }

    @Override
    public ViewState getState() {
        return ViewState.NOT_PLAYER;
    }

}
