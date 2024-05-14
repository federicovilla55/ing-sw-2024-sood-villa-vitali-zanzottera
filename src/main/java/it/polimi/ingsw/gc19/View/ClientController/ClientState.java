package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Enums.GameState;
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

    public void nextState(CreatedPlayerMessage message) { }

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
    }

    public void nextState(BeginFinalRoundMessage message){
        this.clientController.getView().notify("Final round begins!");
    };

    public void nextState(GamePausedMessage message) {
        clientController.setNextState(new Pause(clientController), true);
        //this.listenersManager.notifyStateListener(ViewState.PAUSE);
    }

    public void nextState(GameResumedMessage message) {
        clientController.setNextState(clientController.getPrevState(), true);
    }

    public void nextState(StartPlayingGameMessage message) {
        this.clientController.getView().notify("Game is starting!");
    }

    public void nextState(PlayerReconnectedToGameMessage message) {
        this.clientController.getView().notify("Player '" + message.getPlayerName() + "' ha reconnected to the game...");
    }

    public void nextState(DisconnectFromGameMessage message) {
        this.clientController.getView().notify("You leave the game!");
        this.clientController.setLocalModel(null);
        this.clientInterface.getMessageHandler().setLocalModel(null);
        this.clientController.setNextState(new NotGame(clientController), true);
    }

    public void nextState(DisconnectFromServerMessage message){
        this.clientController.getView().notify("You leave the server!");
        this.clientController.setLocalModel(null);
        this.clientInterface.getMessageHandler().setLocalModel(null);
        this.clientInterface.getMessageHandler().interruptMessageHandler();
        this.clientInterface.stopClient();
        System.exit(-1);
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

    public void nextState(GameConfigurationMessage message) {
        if (message.getGameState() == GameState.SETUP) {
            clientController.setNextState(new Setup(clientController), true);
        }
        else {
            if(message.getGameState() == GameState.PAUSE){
                clientController.setNextState(new Pause(clientController), true);
                return;
            }

            if(message.getGameState() == GameState.END){
                clientController.setNextState(new End(clientController), true);
                return;
            }

            if(message.getFinalRound()){
                clientController.getView().notify("Final round has begun!");
            }

            if(message.getActivePlayer().equals(clientInterface.getNickname())){
                clientController.setNextState(new Place(clientController), true);
            }
            else{
                clientController.setNextState(new OtherTurn(clientController), true);
            }
            this.listenersManager.notifyTurnStateListener(message.getActivePlayer(), message.getTurnState());
        }
    }

    public void nextState(AvailableGamesMessage message){
        this.listenersManager.notifyGameHandlingListener(GameHandlingEvents.AVAILABLE_GAMES, message.getAvailableGames().stream().toList());
    }

    public void nextState(OwnStationConfigurationMessage message){ };

    abstract ViewState getState();

}