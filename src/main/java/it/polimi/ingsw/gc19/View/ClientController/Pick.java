package it.polimi.ingsw.gc19.View.ClientController;

/**
 * The client can pick a card from one of the two decks or from
 * one of the four cards in the table.
 */
class Pick extends ClientState {

    public Pick(ClientController clientController) {
        super(clientController);
    }

    @Override
    public ViewState getState() {
        return ViewState.PICK;
    }

}
