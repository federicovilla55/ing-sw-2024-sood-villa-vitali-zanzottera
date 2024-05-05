package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedChooseGoalCardMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedColorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedPlaceInitialCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.ErrorType;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.GamePausedMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.StartPlayingGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;

import java.util.ArrayList;

/**
 * The client is connected in a game that is in the SETUP state.
 */
class Setup extends ClientState {

    //Attenzione che quando costruisco un nuovo oggetto Setup, l'info sui booleani si perde

    private boolean colorChosen;
    private boolean goalChosen;
    private boolean initialCardPlaced;

    public Setup(ClientController clientController, ClientInterface clientInterface) {
        super(clientController, clientInterface);
        this.colorChosen = false;
        this.goalChosen = false;
        this.initialCardPlaced = false;
    }

    @Override
    public void nextState(GamePausedMessage message) {
        clientController.setNextState(new Pause(clientController, clientInterface));
    }

    @Override
    public void nextState(AcceptedColorMessage message) {
        this.colorChosen = true;
        if(this.goalChosen && this.initialCardPlaced){
            clientController.setNextState(new Wait(clientController, clientInterface));
        }
    }

    @Override
    public void nextState(AcceptedPlaceInitialCard message) {
        this.initialCardPlaced = true;
        if(this.goalChosen && this.colorChosen){
            clientController.setNextState(new Wait(clientController, clientInterface));
        }
    }

    @Override
    public void nextState(AcceptedChooseGoalCardMessage message) {
        this.goalChosen = true;
        if(this.colorChosen && this.initialCardPlaced) {
            clientController.setNextState(new Wait(clientController, clientInterface));
        }
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
        switch (message.getErrorType()){
            case ErrorType.COLOR_ALREADY_CHOSEN -> colorChosen = false;
            case INVALID_GOAL_CARD_ERROR -> goalChosen = false;
        }
    }

    @Override
    public ViewState getState() {
        return ViewState.SETUP;
    }

    // Is there a way we can block action soon after the command is sent?
    // Maybe this should be done in the ClientApp class?
    // Anyway here's a few methods for that:
    public boolean isGoalChosen() {
        return goalChosen;
    }
    public boolean isColorChosen() {
        return colorChosen;
    }
    public boolean isInitialCardPlaced() {
        return initialCardPlaced;
    }
    // Basically after the clientApp understand which of the three actions
    // the user requested, se must ask the Setup state if the command
    // was already asked or not, therefore accepting the action.
}
