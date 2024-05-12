package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.EndGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;


class OtherTurn extends ClientState {

    public OtherTurn(ClientController clientController) {
        super(clientController);
    }

    @Override
    public void nextState(EndGameMessage message) {
        super.nextState(message);
        clientController.setNextState(new End(clientController), true);
    }

    @Override
    public void nextState(TurnStateMessage message) {
        if (message.getNick().equals(clientController.getNickname()) && message.getTurnState() == TurnState.PLACE) {
            clientController.setNextState(new Place(clientController), true);
        }
        super.nextState(message);
    }

    @Override
    public ViewState getState() {
        return ViewState.OTHER_TURN;
    }

}