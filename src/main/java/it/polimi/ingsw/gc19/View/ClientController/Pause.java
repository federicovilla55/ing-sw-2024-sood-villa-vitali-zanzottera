package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;

/**
 * Because all the other players in the game left, the game is in pause
 * and therefore the client can't make any action in the game.
 */
class Pause extends ClientState {

    public Pause(ClientController clientController) {
        super(clientController);
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

    @Override
    public ViewState getState() {
        return ViewState.PAUSE;
    }

}
