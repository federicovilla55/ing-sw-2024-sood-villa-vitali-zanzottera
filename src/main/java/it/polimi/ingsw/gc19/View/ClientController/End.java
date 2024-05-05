package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.DisconnectFromGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;

import java.util.ArrayList;

/**
 * The game ended. The client can still write in chat or try to connect
 * to new games.
 */
class End extends ClientState {

    public End(ClientController clientController, ClientInterface clientInterface) {
        super(clientController, clientInterface);
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
        return ViewState.END;
    }

}
