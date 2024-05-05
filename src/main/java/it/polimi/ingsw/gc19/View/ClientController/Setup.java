package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedChooseGoalCardMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedColorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedPlaceInitialCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.ErrorType;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.GamePausedMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.StartPlayingGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.DisconnectFromGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;

import java.util.ArrayList;

/**
 * The client is connected in a game that is in the SETUP state.
 */
class Setup extends ClientState {

    public Setup(ClientController clientController, ClientInterface clientInterface) {
        super(clientController, clientInterface);
    }

    @Override
    public void nextState(GamePausedMessage message) {
        clientController.setNextState(new Pause(clientController, clientInterface));
    }

    @Override
    public void nextState(StartPlayingGameMessage message) {
        if (message.getNickFirstPlayer().equals(clientController.getNickname())) {
            clientController.setNextState(new Place(clientController, clientInterface));
        }
        else {
            clientController.setNextState(new OtherTurn(clientController, clientInterface));
        }
    }

    @Override
    public void nextState(DisconnectFromGameMessage message) {
        this.clientController.setLocalModel(null);
        this.clientInterface.getMessageHandler().setLocalModel(null);
        this.clientController.setNextState(new NotGame(clientController, clientInterface));
    }

    @Override
    public ViewState getState() {
        return ViewState.SETUP;
    }

}
