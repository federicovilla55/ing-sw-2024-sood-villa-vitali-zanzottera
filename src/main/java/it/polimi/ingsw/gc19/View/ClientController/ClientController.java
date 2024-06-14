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
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.RequestGameExitMessage;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;

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

    /**
     * Nickname of the player
     */
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

    /**
     * {@link LocalModel} connected to the controller
     */
    private LocalModel localModel;

    /**
     * {@link ClientInterface} connected to the controller
     */
    private ClientInterface clientNetwork;

    /**
     * {@link ListenersManager} connected to the controller
     */
    private final ListenersManager listenersManager;

    /**
     * {@link UI} connected to the controller
     */
    private UI view;

    public ClientController() {
        this.prevState = new NotPlayer(this);
        this.viewState = new NotPlayer(this);

        this.listenersManager = new ListenersManager();
    }

    /**
     * Getter for connected {@link ListenersManager}
     * @return the current connected {@link ListenersManager}
     */
    public ListenersManager getListenersManager(){
        return this.listenersManager;
    }

    /**
     * Getter for {@link #view}
     * @return the current connected {@link UI}
     */
    public UI getView() {
        return view;
    }

    /**
     * Setter for {@link #view}
     * @param view the {@link UI} to be set
     */
    public void setView(UI view){
        this.view = view;
    }

    /**
     * Getter for connected {@link ClientInterface}
     * @return the connected {@link ClientInterface}
     */
    public ClientInterface getClientInterface() {
        return clientNetwork;
    }

    /**
     * Setter for {@link #clientNetwork}
     * @param clientInterface the {@link ClientInterface} to be set
     */
    public void setClientInterface(ClientInterface clientInterface){
        this.clientNetwork = clientInterface;
        viewState = new NotPlayer(this);
        prevState = new NotPlayer(this);
        this.listenersManager.notifyStateListener(viewState.getState());
    }

    /**
     * Getter for connected {@link LocalModel}
     * @return the connected {@link LocalModel}
     */
    public LocalModel getLocalModel() {
        return localModel;
    }

    /**
     * Getter for nickname
     * @return the nickname stored
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Setter for {@link #nickname}
     * @param nickname the nickname to be set
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Getter for current {@link ViewState}
     * @return the current {@link ViewState}
     */
    public synchronized ViewState getState(){
        return viewState.getState();
    }

    /**
     * The method should be called when the client detects a disconnection.
     * The client state is changed in {@link Disconnect} and a new thread tries to
     * reconnect the client sending requests at a fixed rate.
     */
    public synchronized void signalPossibleNetworkProblem(){
        if(this.viewState.getState().equals(ViewState.DISCONNECT)) return;
        this.setNextState(new Disconnect(this), true);
    }

    /**
     * Setter for {@link #localModel}
     * @param localModel the {@link LocalModel} to be set
     */
    public synchronized void setLocalModel(LocalModel localModel){
        this.localModel = localModel;
    }

    /**
     * Checks if client is disconnected (e.g. its {@link #viewState}
     * is {@link ViewState#DISCONNECT}).
     * @return <code>true</code> if client is disconnected
     */
    public synchronized boolean isDisconnected(){
        if(this.viewState != null) {
            return this.viewState.getState().equals(ViewState.DISCONNECT);
        }
        return false;
    }

    /**
     * Setter for {@link #viewState}.
     * @param clientState the next {@link ViewState} to be set
     * @param notify <code>true</code> if and only if {@link #view} need to be
     *               notified about changes
     */
    public synchronized void setNextState(ClientState clientState, boolean notify){
        this.prevState = viewState;
        this.viewState = clientState;
        this.listenersManager.notifyStateListener(clientState.getState());
    }

    /**
     * Getter for current state
     * @return the current {@link #viewState}
     */
    public synchronized ClientState getCurrentState(){
        return this.viewState;
    }

    /**
     * Getter for {@link #prevState}
     * @return the {@link #prevState}
     */
    public synchronized ClientState getPrevState(){
        return this.prevState;
    }

    /**
     * To send a message in the chat.
     * send_message(message_content, receiver1 {, receiver2, ...})
     * @param message the message to send
     * @param users the list of players to which send the message
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
            if(users.contains(this.nickname)) {
                this.view.notifyGenericError("You cannot send a message to yourself!");
                return;
            }

            for(String u : users){
                if(!this.localModel.getStations().containsKey(u)){
                    this.view.notifyGenericError("You are trying to send a message to user " + u + " that does not exists in game!");
                    return;
                }
            }

            ArrayList<String> receivers = new ArrayList<>(users);
            receivers.add(this.nickname);
            clientNetwork.sendChatMessage(receivers, message);
        }
    }

    /**
     * To set the nickname of the client and request a connection.
     * create_player(nickname)
     * @param nick the nickname of the player to create
     */
    public synchronized void createPlayer(String nick){
        if(viewState.getState() != ViewState.NOT_PLAYER){
            this.view.notifyGenericError("You are already connected (or waiting to) a player!");
            return;
        }

        this.nickname = nick;
        this.clientNetwork.configure(nick, null);
        setNextState(new Wait(this), true);
        prevState = new NotPlayer(this);
        clientNetwork.connect(nick);
    }

    /**
     * To pick a card from a deck.
     * pick_card_deck(cardType)
     * @param cardType the {@link PlayableCardType} of the picked card
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
     * @param cardType the {@link PlayableCardType} of the picked card
     * @param position position on table of the card
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
     * @return <code>true</code> if and only if card can be placed (locally).
     * @param cardOrientation the {@link CardOrientation} of the placed card
     * @param cardToInsert the code of the card to insert
     * @param direction the {@link Direction} in which card has to be placed
     * @param anchor the code of the {@link PlayableCard} used as anchor
     */
    public synchronized boolean placeCard(String cardToInsert, String anchor, Direction direction, CardOrientation cardOrientation) {
        if(viewState.getState() != ViewState.PLACE){
            this.view.notifyGenericError("Cannot place card at this moment!");
            return false;
        }

        PlayableCard cardToPlace = localModel.getPlayableCard(cardToInsert);
        PlayableCard anchorCard = localModel.getPlayableCard(anchor);

        if(cardToPlace != null && anchorCard != null) {
            cardToPlace.setCardState(cardOrientation);
        }
        else{
            this.listenersManager.notifyErrorStationListener(this.nickname, cardToInsert, anchor, direction.toString().toLowerCase());
            return false;
        }

        //apply card orientation to see correctly if a card is placeable
        if(!localModel.isCardPlaceablePersonalStation(cardToPlace, anchorCard, direction)){
            this.listenersManager.notifyErrorStationListener(this.nickname, cardToInsert, anchor, direction.toString().toLowerCase());
            return false;
        }

        cardToPlace.setCardState(CardOrientation.UP);

        clientNetwork.placeCard(cardToInsert, anchor, direction, cardOrientation);

        setNextState(new Wait(this), true);
        prevState = new Place(this);

        return true;
    }

    /**
     * To handle a {@link RefusedActionMessage} handle and modify the client
     * state consequently.
     * @param message {@link RefusedActionMessage} to analyze
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
     * To handle a {@link NetworkHandlingErrorMessage} handle and modify the client
     * state consequently.
     * @param message {@link NetworkHandlingErrorMessage} to analyze
     */
    public synchronized void handleError(NetworkHandlingErrorMessage message){
        if(message.getError() == NetworkError.COULD_NOT_RECONNECT){
            ConfigurationManager.deleteConfiguration(this.nickname);
            setNextState(new NotPlayer(this), true);
            this.view.notifyGenericError(message.getDescription());
        }
        else if (message.getError() == NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER){
            setNextState(new NotPlayer(this), true);
            this.view.notifyGenericError(message.getDescription());
        }
        else if (message.getError() == NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER){
            this.view.notifyGenericError(message.getDescription());
            setNextState(prevState, true);
        }
    }

    /**
     * To handle a {@link GameHandlingErrorMessage} handle and modify the client
     * state consequently.
     * @param message {@link GameHandlingErrorMessage} to analyze
     */
    public synchronized void handleError(GameHandlingErrorMessage message){
        switch (message.getErrorType()){
            case Error.PLAYER_NAME_ALREADY_IN_USE -> {
                this.listenersManager.notifyErrorPlayerCreationListener(message.getDescription());
                setNextState(new NotPlayer(this), false);
                return;
            }
            case Error.PLAYER_ALREADY_REGISTERED_TO_SOME_GAME -> {
                setNextState(new Disconnect(this), false);
            }
            default -> {
                setNextState(new NotGame(this), false);
            }
        }
        this.getListenersManager().notifyErrorGameHandlingListener(message.getDescription());
    }

    /**
     * To choose a color at the beginning of the game.
     * choose_color(colorType)
     * @param color the {@link Color} would like to have
     */
    public synchronized void chooseColor(Color color) {
        if(viewState.getState() != ViewState.SETUP){
            this.view.notifyGenericError("You cannot choose a color at this moment!");
            return;
        }
        if(!localModel.getAvailableColors().contains(color)){
            this.listenersManager.notifyErrorSetupListener(SetupEvent.COLOR_NOT_AVAILABLE,"The requested color is not available!");
        }
        else {
            clientNetwork.chooseColor(color);
        }
    }

    /**
     * To choose a {@link GoalCard} at the beginning of the game.
     * There should two cards to select from so the user should give a number
     * representing the selected card.
     * choose_goal(goalCardIndex)
     * @param cardIdx the index (0 or 1) of the chosen {@link GoalCard}
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
            this.listenersManager.notifyErrorSetupListener(SetupEvent.GOAL_NOT_ACCEPTED,"The requested position for goal card is not correct!");
        }
    }

    /**
     * To place the initial card at the beginning of the game given its orientation.
     * place_initial_card(orientation)
     * @param cardOrientation the {@link CardOrientation} in which initial card has to be placed
     */
    public synchronized void placeInitialCard(CardOrientation cardOrientation) {
        if(viewState.getState() != ViewState.SETUP) {
            this.view.notifyGenericError("You cannot place initial card at this moment!");
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
            this.view.notifyGenericError("You can not choose a game to play at this moment!");
            return;
        }
        else{
            ((NotGame) this.viewState).startGameSearch();
        }
        //clientNetwork.availableGames();
    }

    /**
     * To notify the server that the player want to join the first available game.
     * join_first_game()
     */
    public synchronized void joinFirstAvailableGame() {
        if(viewState.getState() != ViewState.NOT_GAME){
            this.view.notifyGenericError("You can not join a game to play at this moment!");
            return;
        }
        ((NotGame) this.viewState).stopGameSearch();
        clientNetwork.joinFirstAvailableGame();
        prevState = new NotGame(this);
        setNextState(new Wait(this), true);
    }

    /**
     * To notify the server that the player want to join
     * a particular game.
     * join_game(gameName)
     * @param gameName the name of the game to join
     */
    public synchronized void joinGame(String gameName) {
        if(viewState.getState() != ViewState.NOT_GAME){
            this.view.notifyGenericError("You can not join a game to play at this moment!");
            return;
        }
        ((NotGame) this.viewState).stopGameSearch();
        clientNetwork.joinGame(gameName);
        prevState = new NotGame(this);
        setNextState(new Wait(this), true);
    }

    /**
     * To create a game specifying the game name and the number of players.
     * create_game(gameName, numPlayers)
     * @param gameName the name of the game to build
     * @param numPlayers the number of players for the game
     */
    //Maybe returning something?
    public synchronized void createGame(String gameName, int numPlayers) {
        if(viewState.getState() != ViewState.NOT_GAME){
            if (this.view !=null)
                this.view.notifyGenericError("You can not create a game at this moment!");
            return;
        }
        if(numPlayers > 1 && numPlayers < 5){
            ((NotGame) this.viewState).stopGameSearch();
            clientNetwork.createGame(gameName, numPlayers);
            prevState = new NotGame(this);
            setNextState(new Wait(this), true);
        }
        else{
            this.listenersManager.notifyErrorGameHandlingListener("Games cannot have " + numPlayers + " players! Retry...");
        }
    }

    /**
     * Used to log out from game. It sets {@link #viewState} to
     * {@link ViewState#NOT_GAME} and tries to send, throughout {@link #clientNetwork},
     * a {@link RequestGameExitMessage}. Sets {@link #localModel} to <code>null</code>
     * also for connected UIs.
     */
    public synchronized void logoutFromGame(){
        int numOfTry = 0;

        while(!Thread.currentThread().isInterrupted() && numOfTry < ClientSettings.MAX_LOGOUT_TRY_IN_CASE_OF_ERROR_BEFORE_ABORTING){
            try {
                this.clientNetwork.logoutFromGame();

                this.setNextState(new NotGame(this), false);
                this.clientNetwork.getMessageHandler().setLocalModel(null);
                this.getView().setLocalModel(null);
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

    /**
     * Used to disconnect from server.
     */
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
                        System.exit(1);
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

        System.exit(1);
    }

    /**
     * This method handles available colors requests from user.
     */
    public void availableColors() {
        if(viewState.getState() != ViewState.SETUP){
            this.view.notifyGenericError("You can not choose a color at this moment!");
            return;
        }
        this.getListenersManager().notifySetupListener(SetupEvent.AVAILABLE_COLOR);
    }
}