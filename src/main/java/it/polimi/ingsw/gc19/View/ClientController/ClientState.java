package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.GameConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OwnStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.ListenersManager;

import java.util.List;

public abstract class ClientState {

    protected ClientController clientController;

    protected ClientInterface clientInterface;
    protected ListenersManager listenersManager;

    protected ClientState(ClientController clientController){
        this.clientController = clientController;
        this.clientInterface = clientController.getClientInterface();
        this.listenersManager = clientController.getListenersManager();
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
        this.listenersManager.notifyGameHandlingListener(GameHandlingEvents.JOINED_GAMES, List.of(message.getGameName()));
    }

    public void nextState(EndGameMessage message){
        for(String s: this.clientController.getLocalModel().getOtherStations().keySet()){
            if(message.getUpdatedPoints().containsKey(s)) {
                this.clientController.getLocalModel().getOtherStations().get(s).setNumPoints(message.getUpdatedPoints().get(s));
            }
        }

        //this.listenersManager.notifyStateListener(ViewState.END);
    }

    public void nextState(BeginFinalRoundMessage message){
        this.clientController.getView().notify("Final round begins!");
    };

    public void nextState(GamePausedMessage message) {
        clientController.setNextState(new Pause(clientController));
        //this.listenersManager.notifyStateListener(ViewState.PAUSE);
    }

    public void nextState(GameResumedMessage message) {
        clientController.setNextState(clientController.getPrevState());
    }

    public void nextState(StartPlayingGameMessage message) {
        this.clientController.getView().notify("Game is starting!");
    }

    public void nextState(PlayerReconnectedToGameMessage message) { }

    public void nextState(DisconnectFromGameMessage message) {
        this.clientController.getView().notify("You leave the game!");
    }

    public void nextState(DisconnectFromServerMessage message){
        this.clientController.getView().notify("You leave the server!");
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
        this.listenersManager.notifyGameHandlingListener(GameHandlingEvents.AVAILABLE_GAMES, message.getAvailableGames().stream().toList());
    }

    public void nextState(OwnStationConfigurationMessage message){ };

    abstract ViewState getState();

}