package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;

/**
 * This state represent the moment a client opens the game and is not connected
 * to the server. The client needs to select a nickname and ask for a connection request.
 */
public class NotPlayer extends ClientState {

    public NotPlayer(ClientController clientController) {
        super(clientController);
    }

    /**
     * Getter for {@link ViewState} associated to this state
     * @return the {@link ViewState} associated to this state.
     */
    @Override
    public ViewState getState() {
        return ViewState.NOT_PLAYER;
    }

}