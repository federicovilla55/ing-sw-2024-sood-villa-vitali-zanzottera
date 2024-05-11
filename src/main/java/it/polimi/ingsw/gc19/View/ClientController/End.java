package it.polimi.ingsw.gc19.View.ClientController;

/**
 * The game ended. The client can still write in chat or try to connect
 * to new games.
 */
class End extends ClientState {

    public End(ClientController clientController) {
        super(clientController);
    }

    @Override
    public ViewState getState() {
        return ViewState.END;
    }

}
