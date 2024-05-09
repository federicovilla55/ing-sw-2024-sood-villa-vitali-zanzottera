package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.View.Listeners.ListenersManager;

/**
 * The game ended. The client can still write in chat or try to connect
 * to new games.
 */
class End extends ClientState {

    public End(ClientController clientController, ClientInterface clientInterface, ListenersManager listenersManager) {
        super(clientController, clientInterface, listenersManager);
    }

    @Override
    public ViewState getState() {
        return ViewState.END;
    }

}
