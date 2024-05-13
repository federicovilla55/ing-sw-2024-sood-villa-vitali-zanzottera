package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Chat.Message;
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
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupEvent;
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
        this.prevState = new NotPlayer(this);
        this.viewState = new NotPlayer(this);

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

    public ClientInterface getClientInterface() {
        return clientNetwork;
    }

    public void setClientInterface(ClientInterface clientInterface){
        this.clientNetwork = clientInterface;
        viewState = new NotPlayer(this);
        prevState = new NotPlayer(this);
        this.listenersManager.notifyStateListener(viewState.getState());
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
        this.setNextState(new Disconnect(this), true);
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

    public synchronized void setNextState(ClientState clientState, boolean notify){
        System.out.println(prevState.getState() + "  " + clientState.getState());
        if(notify && !viewState.getState().equals(clientState.getState())){
            this.listenersManager.notifyStateListener(clientState.getState());
        }
        this.prevState = viewState;
        this.viewState = clientState;
    }

    public synchronized ClientState getCurrentState(){
        return this.viewState;
    }

    public synchronized ClientState getPrevState(){
        return this.prevState;
    }

    /**
     * To send a message in the chat.
     * send_message(message_content, receiver1 {, receiver2, ...})
     */
    public synchronized void sendChatMessage(String message, List<String> users) {
        if(viewState.getState() == ViewState.NOT_GAME || viewState.getState() == ViewState.NOT_PLAYER || viewState.getState() == ViewState.DISCONNECT) {
            this.view.notifyGenericError("You cannot send a chat message when you are disconnected or not registered to a game!");
            return;
        }

        if(users.size() > this.localModel.getNumPlayers()){
            this.view.notifyGenericError("Incorrect players name for users to send message to!");
        }
        else{
            for(String u : users){
                if(!this.localModel.getStations().containsKey(u)){
                    this.view.notifyGenericError("You are trying to send a message to user " + u + " that does not exists in game!");
                    return;
                }
            }
            if(!users.contains(this.nickname)) {
                localModel.getMessages().add(new Message(message, this.nickname, new ArrayList<>(users)));
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
            this.view.notifyGenericError("You are already connected (or waiting to) a player!");
            return;
        }

        this.nickname = nick;
        setNextState(new Wait(this), true);
        prevState = new NotPlayer(this);
        clientNetwork.connect(nick);
    }

    /**
     * To pick a card from a deck.
     * pick_card_deck(cardType)
     */
    public synchronized void pickCardFromDeck(PlayableCardType cardType){
        if(viewState.getState() != ViewState.PICK){
            this.view.notifyGenericError("Cannot pick card from deck at this moment!");
            return;
        }

        setNextState(new Wait(this), true);
        prevState = new Pick(this);
        clientNetwork.pickCardFromDeck(cardType);
    }

    /**
     * To pick a card from the table.
     * pick_card_table(cardType, tablePosition)
     */
    public synchronized void pickCardFromTable(PlayableCardType cardType, int position) {
        if(viewState.getState() != ViewState.PICK){
            this.view.notifyGenericError("Cannot pick card from table at this moment!");
            return;
        }

        if(position < 0 || position > 2){
            this.view.notifyGenericError("Position of card on table is incorrect!");
            return;
        }

        clientNetwork.pickCardFromTable(cardType, position);

        setNextState(new Wait(this), true);
        prevState = new Pick(this);
    }

    /**
     * To place a card given its anchor, the direction and the orientation.
     * place_card(cardToInsert, anchorCard, directionToInsert, cardOrientation)
     */
    public synchronized void placeCard(String cardToInsert, String anchor, Direction direction, CardOrientation cardOrientation) {
        if(viewState.getState() != ViewState.PLACE){
            this.view.notifyGenericError("Cannot place card!");
            return;
        }

        PlayableCard cardToPlace = localModel.getPlayableCard(cardToInsert);
        PlayableCard anchorCard = localModel.getPlayableCard(anchor);

        if(cardToPlace != null && anchorCard != null) {
            cardToPlace.setCardState(cardOrientation);
        }
        else{
            this.listenersManager.notifyErrorStationListener(cardToInsert, anchor, direction.toString().toLowerCase());
            return;
        }

        //apply card orientation to see correctly if a card is placeable
        if(!localModel.isCardPlaceablePersonalStation(cardToPlace, anchorCard, direction)){
            this.listenersManager.notifyErrorStationListener(cardToInsert, anchor, direction.toString().toLowerCase());
            return;
        }

        cardToPlace.setCardState(CardOrientation.UP);

        clientNetwork.placeCard(cardToInsert, anchor, direction, cardOrientation);

        setNextState(new Wait(this), true);
        prevState = new Place(this);
    }

    /**
     * To handle a RefusedActionMessage handle and modify the client
     * state consequently.
     * @param message RefusedActionMessage to analyze
     */
    public synchronized void handleError(RefusedActionMessage message){
        this.view.notifyGenericError(message.getDescription());
        switch (message.getErrorType()){
            case ErrorType.INVALID_CARD_ERROR, ErrorType.INVALID_ANCHOR_ERROR -> {
                setNextState(new Place(this), true);
            }
            case ErrorType.GENERIC, ErrorType.INVALID_TURN_STATE, ErrorType.INVALID_GAME_STATE, ErrorType.NOT_YOUR_TURN -> {
                if(viewState.getState() == ViewState.WAIT){
                    setNextState(prevState, true);
                }
            }
            case ErrorType.EMPTY_DECK, ErrorType.EMPTY_TABLE_SLOT -> {
                setNextState(new Pick(this), true);
            }
            case ErrorType.INVALID_GOAL_CARD_ERROR, ErrorType.COLOR_ALREADY_CHOSEN -> {
                setNextState(new Setup(this), true);
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
            this.view.notifyGenericError(message.getDescription());
        }else if (message.getError() == NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER){
            setNextState(new NotPlayer(this), true);
            this.view.notifyGenericError(message.getDescription());
        }
        else if (message.getError() == NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER){
                setNextState(prevState, true);
            }
    }

    /**
     * To handle a GameHandlingError handle and modify the client
     * state consequently.
     * @param message GameHandlingError to analyze
     */
    public synchronized void handleError(GameHandlingErrorMessage message){
        this.getListenersManager().notifyErrorGameHandlingListener(message.getDescription());
        switch (message.getErrorType()){
            case Error.PLAYER_NAME_ALREADY_IN_USE -> {
                setNextState(new NotPlayer(this), false);
            }
            case Error.PLAYER_ALREADY_REGISTERED_TO_SOME_GAME -> {
                setNextState(new Disconnect(this), false);
            }
            default -> {
                setNextState(new NotGame(this), false);
            }
        }
    }

    /**
     * To choose a color at the beginning of the game.
     * choose_color(colorType)
     */
    public synchronized void chooseColor(Color color) {
        if(viewState.getState() != ViewState.SETUP){
            this.view.notifyGenericError("You cannot choose a color!");
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
            this.view.notifyGenericError("You cannot choose a goal!");
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
        if(viewState.getState() != ViewState.SETUP) {
            this.view.notifyGenericError("You cannot place initial card!");
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
            this.view.notifyGenericError("You can not choose a game to play!");
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
            this.view.notifyGenericError("You can not join a game to play!");
            return;
        }
        clientNetwork.joinFirstAvailableGame();
        prevState = new NotGame(this);
        setNextState(new Wait(this), true);
    }

    /**
     * To notify the server that the player want to join
     * a particular game.
     * join_game(gameName)
     */
    public synchronized void joinGame(String gameName) {
        if(viewState.getState() != ViewState.NOT_GAME){
            this.view.notifyGenericError("You can not join a game to play!");
            return;
        }
        clientNetwork.joinGame(gameName);
        prevState = new NotGame(this);
        setNextState(new Wait(this), true);
    }

    /**
     * To create a game specifying the game name and the number of players.
     * create_game(gameName, numPlayers)
     */
    //Maybe returning something?
    public synchronized void createGame(String gameName, int numPlayers) {
        if(viewState.getState() != ViewState.NOT_GAME){
            this.view.notifyGenericError("You can not create a game!");
            return;
        }
        if(numPlayers > 1 && numPlayers < 5){
            clientNetwork.createGame(gameName, numPlayers);
            prevState = new NotGame(this);
            setNextState(new Wait(this), true);
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

                this.setNextState(new Wait(this), true);
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
        ConfigurationManager.deleteConfiguration(this.nickname);

        this.setNextState(new Wait(this), true);

        this.localModel = null;

        int numOfTry = 0;

        while (!Thread.currentThread().isInterrupted() && numOfTry < ClientSettings.MAX_DISCONNECTION_TRY_IN_CASE_OF_ERROR_BEFORE_ABORTING){
            try{
                this.clientNetwork.disconnect();

                ScheduledExecutorService clientKiller = new ScheduledThreadPoolExecutor(1);
                clientKiller.schedule(() -> {
                    this.clientNetwork.stopClient();
                    this.clientNetwork.getMessageHandler().interruptMessageHandler();

                    try{
                        TimeUnit.SECONDS.sleep(5);
                        System.exit(-1);
                    }
                    catch (InterruptedException interruptedException){
                        Thread.currentThread().interrupt();
                    }

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

        //add sys exit
    }

    public void availableColors() {
        if(viewState.getState() != ViewState.SETUP){
            this.view.notifyGenericError("You can not choose a color when not in setup!");
            return;
        }
        this.getListenersManager().notifySetupListener(SetupEvent.AVAILABLE_COLOR);
    }
}