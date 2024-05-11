package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.StartPlayingGameMessage;

/**
 * The client is connected in a game that is in the SETUP state.
 */
class Setup extends ClientState {

    public Setup(ClientController clientController) {
        super(clientController);
    }

    @Override
    public void nextState(StartPlayingGameMessage message) {
        if (message.getNickFirstPlayer().equals(clientController.getNickname())) {
            clientController.setNextState(new Place(clientController));
        }
        else {
            clientController.setNextState(new OtherTurn(clientController));
        }
        super.nextState(message);
    }

    @Override
    public ViewState getState() {
        return ViewState.SETUP;
    }

}
