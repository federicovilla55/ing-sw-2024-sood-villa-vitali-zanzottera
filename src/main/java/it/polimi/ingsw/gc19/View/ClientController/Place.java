package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.View.Listeners.ListenersManager;

/**
 * The clients can place a card in its station.
 */
class Place extends ClientState {

    public Place(ClientController clientController, ClientInterface clientInterface, ListenersManager listenersManager) {
        super(clientController, clientInterface, listenersManager);
    }

    @Override
    public ViewState getState() {
        return ViewState.PLACE;
    }

    @Override
    public void nextState(GameHandlingErrorMessage message) {
        clientController.handleError(message);
    }

    @Override
    public void nextState(NetworkHandlingErrorMessage message) {
        clientController.handleError(message);
    }

    @Override
    public void nextState(RefusedActionMessage message) {
        clientController.handleError(message);
    }

}
