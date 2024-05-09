package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.ClientSettings;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.ConfigurationManager;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.ErrorType;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkError;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.Listeners.ListenersManager;
import it.polimi.ingsw.gc19.View.UI;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * The class is used to forward the actions given by the user to the client network interface.
 * The class can permit different action based on the state of the client, that vary when certain actions are
 * done or when certain messages are received.
 * It contains a method to parse a string and various states that can elaborate the action.
 */
public class ClientController {
    private String nickname;

    /**
     * The attribute represent the current state of the client.
     * Depending on its value only certain actions can be performed.
     */
    private ClientState viewState;

    /**
     * The attribute represent the previous state of the client.
     * Used to retrieve the old state after an action is refused,
     * the game is no more in PAUSE, ...
     */
    private ClientState prevState;

    private LocalModel localModel;

    private ClientInterface clientNetwork;

    private final ListenersManager listenersManager;

    private UI view;

    public ClientController() {
        this.listenersManager = new ListenersManager();
    }

    public ListenersManager getListenersManager(){
        return this.listenersManager;
    }

    public UI getView() {
        return view;
    }

    public void setView(UI view){
        this.view = view;
    }

    public void setClientInterface(ClientInterface clientInterface){
        this.clientNetwork = clientInterface;
        viewState = new NotPlayer(this, clientInterface, listenersManager);
        prevState = new NotPlayer(this, clientInterface, listenersManager);
    }

    public LocalModel getLocalModel() {
        return localModel;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public synchronized ViewState getState(){
        return viewState.getState();
    }

    /**
     * The method should be called when the client detects a disconnection.
     * The client state is changed in Disconnect and a new thread tries to
     * reconnect the client sending requests at a fixed rate.
     */
    public synchronized void signalPossibleNetworkProblem(){
        if(this.viewState.getState() == ViewState.DISCONNECT) return;
        this.prevState = viewState;
        this.viewState = new Disconnect(this, clientNetwork, listenersManager);
    }

    public synchronized void setLocalModel(LocalModel localModel){
        this.localModel = localModel;
    }

    public synchronized boolean isDisconnected(){
        if(this.viewState != null) {
            return this.viewState.getState() == ViewState.DISCONNECT;
        }
        return false;
    }

    public synchronized void setNextState(ClientState clientState){
        this.prevState = viewState;
        this.viewState = clientState;
    }

    public synchronized ClientState getCurrentState(){
        return this.viewState;
    }

    public synchronized ClientState getPrevState(){
        //@TODO: add here the logic for updating prvState according to nextSate
        return this.prevState;
    }

    /**
     * To send a message in the chat.
     * send_message(message_content, receiver1 {, receiver2, ...})
     */
    public synchronized void sendChatMessage(String message, List<String> users) {
        if(viewState.getState() == ViewState.NOT_GAME || viewState.getState() == ViewState.NOT_PLAYER || viewState.getState() == ViewState.DISCONNECT) {
            this.listenersManager.notifyErrorGameHandlingListener("You cannot send a chat message when you are disconnected or not registered to a game!");
            return;
        }

        if(users.size() > this.localModel.getNumPlayers()){
            this.listenersManager.notifyErrorChatListener("Number of players is incorrect!");
        }
        else{
            for(String u : users){
                if(!this.localModel.getStations().containsKey(u)){
                    this.listenersManager.notifyErrorChatListener("You are trying to send a message to user " + u + " that does not exists in game!");
                    return;
                }
            }

            clientNetwork.sendChatMessage(new ArrayList<>(users), message);
        }
    }

    /**
     * To set the nickname of the client and request a connection.
     * create_player(nickname)
     */
    public synchronized void createPlayer(String nick){
        if(viewState.getState() != ViewState.NOT_PLAYER){
            //@TODO: notify view
            return;
        }

        this.nickname = nick;
        viewState = new Wait(this, clientNetwork, listenersManager);
        prevState = new NotPlayer(this, clientNetwork, listenersManager);
        clientNetwork.connect(nick);
    }

    /**
     * To pick a card from a deck.
     * pick_card_deck(cardType)
     */
    public synchronized void pickCardFromDeck(PlayableCardType cardType){
        if(viewState.getState() != ViewState.PICK){
            this.listenersManager.notifyErrorTableListener("You cannot pick card from deck of type " + cardType + "!");
            return;
        }

        viewState = new Wait(this, clientNetwork, listenersManager);
        prevState = new Pick(this, clientNetwork, listenersManager);
        clientNetwork.pickCardFromDeck(cardType);
    }

    /**
     * To pick a card from the table.
     * pick_card_table(cardType, tablePosition)
     */
    public synchronized void pickCardFromTable(PlayableCardType cardType, int position) {
        if(viewState.getState() != ViewState.PICK){
            this.listenersManager.notifyErrorTableListener("You cannot pick card from table of type " + cardType + " and position " + position + "!");
            return;
        }

        clientNetwork.pickCardFromTable(cardType, position);

        viewState = new Wait(this, clientNetwork, listenersManager);
        prevState = new Pick(this, clientNetwork, listenersManager);
    }

    /**
     * To place a card given its anchor, the direction and the orientation.
     * place_card(cardToInsert, anchorCard, directionToInsert, cardOrientation)
     */
    public synchronized void placeCard(String cardToInsert, String anchor, Direction direction, CardOrientation cardOrientation) {
        if(viewState.getState() != ViewState.PLACE){
            this.listenersManager.notifyErrorTurnStateListener("Cannot place card!");
            return;
        }

        PlayableCard cardToPlace = localModel.getPlayableCard(cardToInsert);
        PlayableCard anchorCard = localModel.getPlayableCard(anchor);

        if(!localModel.isCardPlaceablePersonalStation(cardToPlace, anchorCard, direction)){
            this.listenersManager.notifyErrorStationListener(cardToPlace.getCardCode(), anchorCard.getCardCode());
            return;
        }

        clientNetwork.placeCard(cardToInsert, anchor, direction, cardOrientation);

        viewState = new Wait(this, clientNetwork, listenersManager);
        prevState = new Place(this, clientNetwork, listenersManager);
    }

    /**
     * To handle a RefusedActionMessage handle and modify the client
     * state consequently.
     * @param message RefusedActionMessage to analyze
     */
    public synchronized void handleError(RefusedActionMessage message){
        //@TODO: decide what type of messages broadast to view
        switch (message.getErrorType()){
            case ErrorType.INVALID_CARD_ERROR, ErrorType.INVALID_ANCHOR_ERROR -> {
                viewState = new Place(this, clientNetwork, listenersManager);
            }
            case ErrorType.GENERIC, ErrorType.INVALID_TURN_STATE, ErrorType.INVALID_GAME_STATE -> {
                if(viewState.getState() == ViewState.WAIT){
                    viewState = prevState;
                }
            }
            case ErrorType.EMPTY_DECK, ErrorType.EMPTY_TABLE_SLOT -> {
                viewState = new Pick(this, clientNetwork, listenersManager);
            }
            case ErrorType.INVALID_GOAL_CARD_ERROR, ErrorType.COLOR_ALREADY_CHOSEN -> {
                viewState = new Setup(this, clientNetwork, listenersManager);
            }
        }
    }

    /**
     * To handle a NetworkHandlingErrorMessage handle and modify the client
     * state consequently.
     * @param message NetworkHandlingErrorMessage to analyze
     */
    public synchronized void handleError(NetworkHandlingErrorMessage message){
        if(message.getError() == NetworkError.COULD_NOT_RECONNECT){
            disconnect();
        }else if (message.getError() == NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER){
            viewState = new NotPlayer(this, clientNetwork, listenersManager);
        }
    }

    /**
     * To handle a GameHandlingError handle and modify the client
     * state consequently.
     * @param message GameHandlingError to analyze
     */
    public synchronized void handleError(GameHandlingErrorMessage message){
        switch (message.getErrorType()){
            case Error.PLAYER_NAME_ALREADY_IN_USE -> {
                viewState = new NotPlayer(this, clientNetwork, listenersManager);
            }
            case Error.PLAYER_ALREADY_REGISTERED_TO_SOME_GAME -> {
                viewState = new Disconnect(this, clientNetwork, listenersManager);
            }
            default -> {
                viewState = new NotGame(this, clientNetwork, listenersManager);
            }
        }
    }

    /**
     * To choose a color at the beginning of the game.
     * choose_color(colorType)
     */
    public synchronized void chooseColor(Color color) {
        if(viewState.getState() != ViewState.SETUP){
            //@TODO: how to notify view?
            return;
        }
        if(!localModel.getAvailableColors().contains(color)){
            this.listenersManager.notifyErrorSetupListener("The requested color is not available!");
        }
        else {
            clientNetwork.chooseColor(color);
        }
    }

    /**
     * To choose a goal card at the beginning of the game.
     * There should two cards to select from so the user should give a number
     * representing the selected card.
     * choose_goal(goalCardIndex)
     */
    public synchronized void chooseGoal(int cardIdx) {
        if(viewState.getState() != ViewState.SETUP){
            //@TODO: notify view
            return;
        }
        if((cardIdx >= 0) && (cardIdx < 2)) {
            clientNetwork.choosePrivateGoalCard(cardIdx);
        }
        else{
            this.listenersManager.notifyErrorSetupListener("The requested position for goal card is not correct!");
        }
    }

    /**
     * To place the initial card at the beginning of the game vien its orientation.
     * place_initial_card(orientation)
     */
    public synchronized void placeInitialCard(CardOrientation cardOrientation) {
        if(viewState.getState() != ViewState.SETUP){
            //@TODO: notify view
            return;
        }
        clientNetwork.placeInitialCard(cardOrientation);
    }

    /**
     * To ask the server which games are free to join.
     * available_games()
     */
    public synchronized void availableGames() {
        if(viewState.getState() != ViewState.NOT_GAME){
            //@TODO: notify view
            return;
        }
       clientNetwork.availableGames();
    }

    /**
     * To notify the server that the player want to join the first available fame.
     * join_first_game()
     */
    public synchronized void joinFirstAvailableGame() {
        if(viewState.getState() != ViewState.NOT_GAME){
            //@TODO: notify view
            return;
        }
        clientNetwork.joinFirstAvailableGame();
        prevState = new NotGame(this, clientNetwork, listenersManager);
        viewState = new Wait(this, clientNetwork, listenersManager);
    }

    /**
     * To notify the server that the player want to join
     * a particular game.
     * join_game(gameName)
     */
    public synchronized void joinGame(String gameName) {
        if(viewState.getState() != ViewState.NOT_GAME){
            //@TODO: notify view
            return;
        }
        clientNetwork.joinGame(gameName);
        prevState = new NotGame(this, clientNetwork, listenersManager);
        viewState = new Wait(this, clientNetwork, listenersManager);
    }

    /**
     * To create a game specifying the game name and the number of players.
     * create_game(gameName, numPlayers)
     */
    //Maybe returning something?
    public synchronized void createGame(String gameName, int numPlayers) {
        if(viewState.getState() != ViewState.NOT_GAME){
            //@TODO: notify view
            return;
        }
        if(numPlayers > 1 && numPlayers < 5){
            clientNetwork.createGame(gameName, numPlayers);
            prevState = new NotGame(this, clientNetwork, listenersManager);
            viewState = new Wait(this, clientNetwork, listenersManager);
        }
        else{
            this.listenersManager.notifyErrorGameHandlingListener("Games cannot have " + numPlayers + " players! Retry...");
        }
    }

    public synchronized void logoutFromGame(){
        int numOfTry = 0;

        while(!Thread.currentThread().isInterrupted() && numOfTry < ClientSettings.MAX_LOGOUT_TRY_IN_CASE_OF_ERROR_BEFORE_ABORTING){
            try {
                this.clientNetwork.logoutFromGame();

                this.viewState = new Wait(this, clientNetwork, listenersManager);
                this.localModel = null;

                return;
            }
            catch (RuntimeException runtimeException){
                numOfTry++;

                try{
                    TimeUnit.MILLISECONDS.sleep(ClientSettings.DELTA_TIME_BETWEEN_LOGOUT_TRY_IN_CASE_OF_ERROR);
                }
                catch (InterruptedException interruptedException){
                    Thread.currentThread().interrupt();
                }

            }
        }
    }

    public synchronized void disconnect(){
        int numOfTry = 0;

        while (!Thread.currentThread().isInterrupted() && numOfTry < ClientSettings.MAX_DISCONNECTION_TRY_IN_CASE_OF_ERROR_BEFORE_ABORTING){
            try{
                ConfigurationManager.deleteConfiguration(this.nickname);

                this.clientNetwork.disconnect();

                this.viewState = new Wait(this, this.clientNetwork, listenersManager);

                this.localModel = null;

                ScheduledExecutorService clientKiller = new ScheduledThreadPoolExecutor(1);
                clientKiller.schedule(() -> {
                    this.clientNetwork.stopClient();
                    this.clientNetwork.getMessageHandler().interruptMessageHandler();
                }, 2500, TimeUnit.MILLISECONDS);

                return;
            }
            catch (RuntimeException runtimeException){

                numOfTry++;

                try{
                    TimeUnit.MILLISECONDS.sleep(ClientSettings.DELTA_TIME_BETWEEN_DISCONNECTION_TRY_IN_CASE_OF_ERROR);
                }
                catch (InterruptedException interruptedException){
                    Thread.currentThread().interrupt();
                }

            }
        }
    }

}