package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;


/**
 * The clients can place a card in its station.
 */
class Place extends ClientState {

    public Place(ClientController clientController) {
        super(clientController);
    }

    /**
     * Getter for {@link ViewState} associated to this state
     * @return the {@link ViewState} associated to this state.
     */
    @Override
    public ViewState getState() {
        return ViewState.PLACE;
    }

}