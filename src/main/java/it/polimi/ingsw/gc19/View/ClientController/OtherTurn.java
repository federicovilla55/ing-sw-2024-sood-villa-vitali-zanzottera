package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.EndGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;

/**
 * This state represent the moment a client is playing his game
 * and another player is the active player.
 */
class OtherTurn extends ClientState {

    public OtherTurn(ClientController clientController) {
        super(clientController);
    }

    /**
     * This method handles {@link EndGameMessage}. It sets {@link ViewState} of
     * {@link ClientController} to {@link End}.
     * @param message the {@link EndGameMessage} to be handled
     */
    @Override
    public void nextState(EndGameMessage message) {
        super.nextState(message);
        clientController.setNextState(new End(clientController), true);
    }

    /**
     * This method handles {@link TurnStateMessage}. It sets {@link ViewState} of
     * {@link ClientController} to {@link Pick} or {@link OtherTurn} depending on {@param message}
     * @param message the {@link TurnStateMessage} to be handled
     */
    @Override
    public void nextState(TurnStateMessage message) {
        if (message.getNick().equals(clientController.getNickname()) && message.getTurnState() == TurnState.PLACE) {
            clientController.setNextState(new Place(clientController), true);
        }
        super.nextState(message);
    }

    /**
     * Getter for {@link ViewState} associated to this state
     * @return the {@link ViewState} associated to this state.
     */
    @Override
    public ViewState getState() {
        return ViewState.OTHER_TURN;
    }

}