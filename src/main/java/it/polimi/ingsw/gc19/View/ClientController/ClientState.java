package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Enums.TurnState;
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
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.GameEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.PlayerCreationListener;
import it.polimi.ingsw.gc19.View.Listeners.ListenersManager;

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

    public void nextState(OwnAcceptedPickCardFromDeckMessage message) {}
    public void nextState(AcceptedPickCardFromTable message) {}

    public void nextState(CreatedPlayerMessage message) {
        this.listenersManager.notifyPlayerCreationListener(message.getNick());
    }

    public void nextState(CreatedGameMessage message) {
        this.listenersManager.notifyGameHandlingListener(GameHandlingEvents.CREATED_GAME, List.of(message.getGameName()));
    }

    public void nextState(JoinedGameMessage message){
        this.listenersManager.notifyGameHandlingListener(GameHandlingEvents.JOINED_GAME, List.of(message.getGameName()));
    }

    public void nextState(EndGameMessage message){
        for(String s: this.clientController.getLocalModel().getStations().keySet()){
            this.clientController.getLocalModel().getStations().get(s).setNumPoints(message.getUpdatedPoints().get(s));
        }

        this.listenersManager.notifyGameEventsListener(GameEvents.END_GAME, message.getWinnerNicks());
    }

    public void nextState(BeginFinalRoundMessage message){
        this.listenersManager.notifyGameEventsListener(GameEvents.BEGIN_FINAL_ROUND, List.of());
    };

    public void nextState(GamePausedMessage message) {
        clientController.setNextState(new Pause(clientController, clientInterface));
        this.listenersManager.notifyGameEventsListener(GameEvents.GAME_PAUSED, List.of());
    }

    public void nextState(GameResumedMessage message) {
        clientController.setNextState(clientController.getPrevState());
        this.listenersManager.notifyGameEventsListener(GameEvents.GAME_RESUMED, List.of());
    }

    public void nextState(StartPlayingGameMessage message) {
        this.listenersManager.notifyGameEventsListener(GameEvents.START_PLAYING_GAME, List.of());
        this.listenersManager.notifyTurnStateListener(message.getNickFirstPlayer(), TurnState.DRAW);
    }

    public void nextState(PlayerReconnectedToGameMessage message) {
        this.listenersManager.notifyGameEventsListener(GameEvents.RECONNECTED_PLAYER, List.of(message.getPlayerName()));
    }

    public void nextState(DisconnectFromGameMessage message) {
        this.listenersManager.notifyDisconnectionListener(message.getGameName());
    }

    public void nextState(DisconnectFromServerMessage message){
        this.listenersManager.notifyDisconnectionListener();
    };

    public void nextState(TurnStateMessage message) {
        this.listenersManager.notifyTurnStateListener(message.getNick(), message.getTurnState());
    }

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

    public void nextState(AvailableGamesMessage message){
        this.listenersManager.notifyGameHandlingListener(message.getAvailableGames().stream().toList());
    }

    abstract ViewState getState();

}