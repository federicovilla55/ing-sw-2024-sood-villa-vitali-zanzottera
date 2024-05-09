package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.View.Listeners.ListenersManager;

/**
 * The client can pick a card from one of the two decks or from
 * one of the four cards in the table.
 */
class Pick extends ClientState {

    public Pick(ClientController clientController, ClientInterface clientInterface, ListenersManager listenersManager) {
        super(clientController, clientInterface, listenersManager);
    }

    @Override
    public ViewState getState() {
        return ViewState.PICK;
    }

}
