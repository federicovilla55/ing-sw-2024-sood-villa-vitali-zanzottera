package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Enums.GameState;
import it.polimi.ingsw.gc19.Enums.TurnState;
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

/**
 * This is an abstract class to represent the states of the Client in each moment of the game.
 * From the creation of the player, to the creation of the game, to the various states of the game
 * and the pause or disconnect phases.
 */
public abstract class ClientState {

    /**
     * Instance of the class used to forward the actions given by the user
     * through commands to the client network interface.
     */
    protected ClientController clientController;

    /**
     * Interface of a generic client that can be extended to handle
     * connections (TCP or RMI), connection from configuration files and send commands.
     */
    protected ClientInterface clientInterface;

    /**
     * Attribute used to attach the listeners that listen and notify
     * of changes, events or errors generated during the game.
     */
    protected ListenersManager listenersManager;

    protected ClientState(ClientController clientController){
        this.clientController = clientController;
        this.clientInterface = clientController.getClientInterface();
        this.listenersManager = clientController.getListenersManager();
    }

    /**
     * Generic message that doesn't change the state of the client.
     * If everything works currently the client should not receive
     * those kinds of messages.
     * Even if those messages are received they do not change the state.
     * @param message a generic {@link MessageToClient}.
     */
    public void nextState(MessageToClient message) {}

    /**
     * Change to the state after a {@link OwnAcceptedPickCardFromDeckMessage} arrived.
     * Used to notify the client that a message containing a description of a pick
     * from the deck arrived.
     * @param message a {@link OwnAcceptedPickCardFromDeckMessage} message with the
     *                content of the latest pick action.
     */
    public void nextState(OwnAcceptedPickCardFromDeckMessage message) {}

    /**
     * Change to the state of the client after the confirmation of a card picked
     * from the table arrived.
     * @param message a {@link AcceptedPickCardFromTable}.
     */
    public void nextState(AcceptedPickCardFromTable message) {}

    /**
     * Change to the state of the client after a confirmation of a player creation is arrived.
     * @param message a {@link CreatedPlayerMessage}.
     */
    public void nextState(CreatedPlayerMessage message) { }

    /**
     * Change to the state of the client after a game creation confirmation has arrived.
     * @param message a {@link CreatedGameMessage}.
     */
    public void nextState(CreatedGameMessage message) {
        this.listenersManager.notifyGameHandlingListener(GameHandlingEvents.CREATED_GAME, List.of(message.getGameName()));
        this.clientController.setNextState(new Setup(clientController), false);
    }

    /**
     * Change to the state of the client after a join game confirmation has arrived.
     * @param message a {@link JoinedGameMessage}.
     */
    public void nextState(JoinedGameMessage message){
        this.listenersManager.notifyGameHandlingListener(GameHandlingEvents.JOINED_GAMES, List.of(message.getGameName()));
    }

    /**
     * Change to the state of the client after the server informs that the game the client is currently
     * playing is ended.
     * @param message a {@link CreatedGameMessage}.
     */
    public void nextState(EndGameMessage message){
        for(String s: this.clientController.getLocalModel().getStations().keySet()){
            if(message.getUpdatedPoints().containsKey(s)) {
                this.clientController.getLocalModel().setNumPoints(s, message.getUpdatedPoints().get(s));
            }
        }
        this.clientController.setNextState(new End(clientController), true);
    }

    /**
     * Change to the state of the client after the server informs that the game the client is currently
     * playing is ended.
     * @param message a {@link CreatedGameMessage}.
     */
    public void nextState(BeginFinalRoundMessage message){
        this.clientController.getView().notify("Final round begins!");
    };

    /**
     * To change the state of the client in PAUSE after the server informs the client that the
     * game is in the PAUSE state.
     * @param message a {@link GamePausedMessage} from the server.
     */
    public void nextState(GamePausedMessage message) {
        clientController.setNextState(new Pause(clientController), true);
    }

    /**
     * To change the state of the client after the server informs the client that the game is
     * no more in the PAUSE state.
     * @param message a {@link GameResumedMessage} from the server.
     */
    public void nextState(GameResumedMessage message) {
        handleTurnStateAndFirstPlayer(message.getActivePlayer(), clientController.getNickname(), message.getTurnState());
    }

    /**
     * To change the state of the client after the server informs the client that the game is started.
     * @param message a {@link StartPlayingGameMessage} from the server.
     */
    public void nextState(StartPlayingGameMessage message) {
        if (this.clientController.getView() != null)
            this.clientController.getView().notify("Game is starting!");
    }

    /**
     * To inform the state of the client that a player has been disconnected from the game.
     * @param message a {@link DisconnectFromGameMessage} from the server.
     */
    public void nextState(DisconnectFromGameMessage message) {
        if(this.clientController.getView() != null) this.clientController.getView().notify("You leave the game!");
        this.clientController.setLocalModel(null);
        this.clientInterface.getMessageHandler().setLocalModel(null);
        this.clientController.setNextState(new NotGame(clientController), true);
    }

    /**
     * To inform the client that a player has been disconnected from the game.
     * @param message a {@link DisconnectFromGameMessage} from the server.
     */
    public void nextState(DisconnectFromServerMessage message){
        if(this.clientController.getView() != null) this.clientController.getView().notify("You leave the server!");
        this.clientController.setLocalModel(null);
        this.clientInterface.getMessageHandler().setLocalModel(null);
        this.clientInterface.getMessageHandler().interruptMessageHandler();
        this.clientInterface.stopClient();
        System.exit(-1);
    };

    /**
     * To change the state of the client after the server sends a message that declares a change
     * of the current turn.
     * @param message a {@link TurnStateMessage} from the server.
     */
    public void nextState(TurnStateMessage message) {
        this.listenersManager.notifyTurnStateListener(message.getNick(), message.getTurnState());
    }

    /**
     * To update the client after a {@link GameHandlingErrorMessage} is arrived.
     * The client should return from the WAIT to its previously state.
     * @param message a {@link GameHandlingErrorMessage} from the server.
     */
    public void nextState(GameHandlingErrorMessage message) {
        clientController.handleError(message);
    }

    /**
     * To update the client state after a network error happened.
     * @param message a {@link NetworkHandlingErrorMessage}
     */
    public void nextState(NetworkHandlingErrorMessage message) {
        clientController.handleError(message);
    }

    /**
     * To update the client state after an action previously sent was refused.
     * Notify the view too.
     * @param message a {@link RefusedActionMessage}
     */
    public void nextState(RefusedActionMessage message) {
        clientController.handleError(message);
    }

    /**
     * To update the client state after the server sent the configuration of the game.
     * Notify the view too.
     * @param message a {@link GameConfigurationMessage}
     */
    public void nextState(GameConfigurationMessage message) {
        if (message.getGameState() == GameState.SETUP) {
            clientController.setNextState(new Setup(clientController), true);
        }
        else {
            if(message.getGameState() == GameState.PAUSE){
                clientController.setNextState(new Pause(clientController), true);
                return;
            }

            //FOR ERRORS, CHECK HERE
            /*if(message.getGameState() == GameState.END){
                clientController.setNextState(new End(clientController), true);
                return;
            }*/

            if(message.getFinalRound()){
                clientController.getView().notify("Final round has begun!");
            }

            handleTurnStateAndFirstPlayer(message.getActivePlayer(), clientInterface.getNickname(), message.getTurnState());
            this.listenersManager.notifyTurnStateListener(message.getActivePlayer(), message.getTurnState());
        }
    }

    /**
     * Handles a {@link TurnStateMessage} and first player. For example, if
     * <code>activePlayer</code> is the user, then it sets current state in
     * {@link ClientController} to {@link ViewState#PICK} ot {@link ViewState#PLACE}
     * @param activePlayer the nickname of the player in turn
     * @param nickname the nickname of the user
     * @param turnState the {@link TurnState}
     */
    private void handleTurnStateAndFirstPlayer(String activePlayer, String nickname, TurnState turnState) {
        if (activePlayer.equals(nickname)) {
            if (turnState.equals(TurnState.PLACE))
                clientController.setNextState(new Place(clientController), true);
            else if (turnState.equals(TurnState.DRAW))
                clientController.setNextState(new Pick(clientController), true);
        }
        else{
            clientController.setNextState(new OtherTurn(clientController), true);
        }
    }

    /**
     * To update the view after a message containing the available games in the lobby was received from the server.
     * @param message an {@link AvailableGamesMessage} send by the server.
     */
    public void nextState(AvailableGamesMessage message){
        this.listenersManager.notifyGameHandlingListener(GameHandlingEvents.AVAILABLE_GAMES, message.getAvailableGames().stream().toList());
    }

    /**
     * To update the view and the {@link ViewState} of {@link ClientController} after a message with the configuration of the client's station
     * has arrived after the server sent it.
     * @param message an {@link OwnStationConfigurationMessage}
     */
    public void nextState(OwnStationConfigurationMessage message){ };

    /**
     * To return the state of the client, an element of class {@link ViewState}
     * @return the current ViewState of the client.
     */
    abstract ViewState getState();

}