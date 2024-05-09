package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.EndGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
import it.polimi.ingsw.gc19.View.Listeners.ListenersManager;

class OtherTurn extends ClientState {

    public OtherTurn(ClientController clientController, ClientInterface clientInterface, ListenersManager listenersManager) {
        super(clientController, clientInterface, listenersManager);
    }

    @Override
    public void nextState(EndGameMessage message) {
        super.nextState(message);
        clientController.setNextState(new End(clientController, clientInterface, listenersManager));
    }

    @Override
    public void nextState(TurnStateMessage message) {
        if (message.getNick().equals(clientController.getNickname()) && message.getTurnState() == TurnState.PLACE) {
            clientController.setNextState(new Place(clientController, clientInterface, listenersManager));
        }
        super.nextState(message);
    }

    @Override
    public ViewState getState() {
        return ViewState.OTHER_TURN;
    }

}