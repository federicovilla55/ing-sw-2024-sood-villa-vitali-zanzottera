package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.StartPlayingGameMessage;
import it.polimi.ingsw.gc19.View.Listeners.ListenersManager;

/**
 * The client is connected in a game that is in the SETUP state.
 */
class Setup extends ClientState {

    public Setup(ClientController clientController, ClientInterface clientInterface, ListenersManager listenersManager) {
        super(clientController, clientInterface, listenersManager);
    }

    @Override
    public void nextState(StartPlayingGameMessage message) {
        if (message.getNickFirstPlayer().equals(clientController.getNickname())) {
            clientController.setNextState(new Place(clientController, clientInterface, listenersManager));
        }
        else {
            clientController.setNextState(new OtherTurn(clientController, clientInterface, listenersManager));
        }
        super.nextState(message);
    }

    @Override
    public ViewState getState() {
        return ViewState.SETUP;
    }

}
