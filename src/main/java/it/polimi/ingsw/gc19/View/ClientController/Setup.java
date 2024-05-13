package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.StartPlayingGameMessage;

/**
 * This class represents a state represents where a client
 * is connected and is in a game currently {@link ViewState#SETUP} state.
 */
class Setup extends ClientState {

    public Setup(ClientController clientController) {
        super(clientController);
    }

    /**
     * This method handles {@link StartPlayingGameMessage}. Depending on
     * {@param message}, it sets next state of {@link ClientController} to {@link Place}
     * or {@link OtherTurn}
     * @param message is the {@link StartPlayingGameMessage} to be handled
     */
    @Override
    public void nextState(StartPlayingGameMessage message) {
        if (message.getNickFirstPlayer().equals(clientController.getNickname())) {
            clientController.setNextState(new Place(clientController), true);
        }
        else {
            clientController.setNextState(new OtherTurn(clientController), true);
        }
        super.nextState(message);
    }

    /**
     * Getter for {@link ViewState} associated to this state.
     * @return the {@link ViewState} associated to this state
     */
    @Override
    public ViewState getState() {
        return ViewState.SETUP;
    }

}