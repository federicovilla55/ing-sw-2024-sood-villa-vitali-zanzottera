package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.GameConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.ListenersManager;

import java.util.ArrayList;
import java.util.List;

public abstract class ClientState {

    protected ClientController clientController;

    protected ClientInterface clientInterface;

    protected ListenersManager listenersManager;

    protected ClientState(ClientController clientController, ClientInterface clientInterface){
        this.clientController = clientController;
        this.clientInterface = clientInterface;
    }

    public void setListenersManager(ListenersManager listenersManager){
        this.listenersManager = listenersManager;
    }

    public void nextState(MessageToClient message) {}
    public void nextState(AcceptedChooseGoalCardMessage message) {}
    public void nextState(AcceptedColorMessage message) {}
    public void nextState(OwnAcceptedPickCardFromDeckMessage message) {}
    public void nextState(OtherAcceptedPickCardFromDeckMessage message) {}
    public void nextState(AcceptedPickCardFromTable message) {}
    public void nextState(AcceptedPlaceCardMessage message) {}
    public void nextState(AcceptedPlaceInitialCard message) {}

    public void nextState(CreatedPlayerMessage message) {
        this.listenersManager.notify(GameHandlingEvents.CREATED_PLAYER, List.of(message.getNick()));
    }

    public void nextState(CreatedGameMessage message) {
        this.listenersManager.notify(GameHandlingEvents.CREATED_GAME, List.of(message.getGameName()));
    }

    public void nextState(JoinedGameMessage message){
        this.listenersManager.notify(GameHandlingEvents.JOINED_GAME, List.of(message.getGameName()));
    }

    public void nextState(EndGameMessage message){
        this.listenersManager.notify(GameHandlingEvents.END_GAME, List.of());
    }

    public void nextState(BeginFinalRoundMessage message){ };

    public void nextState(GamePausedMessage message) {}
    public void nextState(GameResumedMessage message) {}
    public void nextState(StartPlayingGameMessage message) {}
    public void nextState(PlayerReconnectedToGameMessage message) {}

    public void nextState(DisconnectFromGameMessage message) {
        this.listenersManager.notify(GameHandlingEvents.DISCONNECTED_FROM_GAME, List.of(message.getGameName()));
    }

    public void nextState(DisconnectFromServerMessage message){
        this.listenersManager.notify(GameHandlingEvents.DISCONNECTED_FROM_SERVER, List.of());
    };

    public void nextState(TurnStateMessage message) {}

    public void nextState(GameHandlingErrorMessage message) {
        clientController.handleError(message);
    }

    public void nextState(NetworkHandlingErrorMessage message) {
        clientController.handleError(message);
    }

    public void nextState(RefusedActionMessage message) {
        clientController.handleError(message);
    }

    public void nextState(GameConfigurationMessage message){}
    public void nextState(AvailableGamesMessage message){}
    abstract ViewState getState();
}