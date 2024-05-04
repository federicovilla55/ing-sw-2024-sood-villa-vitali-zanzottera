package it.polimi.ingsw.gc19.View.ClientController;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.ErrorType;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkError;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.View.Command.CommandType;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalStationPlayer;

import java.util.ArrayList;
import java.util.List;

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
    public ClientState viewState;

    /**
     * The attribute represent the previous state of the client.
     * Used to retrieve the old state after an action is refused,
     * the game is no more in PAUSE, ...
     */
    private ClientState prevState;

    private final LocalModel localModel;

    private final ClientInterface clientNetwork;

    //Quando viene parsata una richiesta di creazione del giocatore i chiama un metodo apposito del client controller che prova a fare la connect

    public ClientController(ClientInterface clientInterface){
        this.clientNetwork = clientInterface;
        this.localModel = new LocalModel();
        viewState = new NotPlayer(this, clientInterface);
        prevState = new NotPlayer(this, clientInterface);
    }

    /*
     * Public method used to forward a string containing an action to be performed
     * to the methods that effectively performs those actions.
     * @param action a string containing the actions to be done.
    public void parseAction(String action) {
        viewState.parseAction(commandParser(action));
    }*/

    //Probabilmente, getNick e setNick verranno assorbite in qualche metodo...
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public synchronized ViewState getState(){
        return viewState.getState();
    }

    public synchronized LocalModel getLocalModel(){
        return this.localModel;
    }

    /**
     * The method should be called when the client detects a disconnection.
     * The client state is changed in Disconnect and a new thread tries to
     * reconnect the client sending requests at a fixed rate.
     */
    public synchronized void disconnect(){
        if(this.viewState.getState() == ViewState.DISCONNECT) return;
        this.prevState = viewState;
        this.viewState = new Disconnect(this, clientNetwork);
    }

    public synchronized boolean isClientNetworkSet(){
        return (this.clientNetwork==null);
    }

    public synchronized boolean isDisconnected(){
        return (this.viewState.getState() == ViewState.DISCONNECT);
    }

    public synchronized void setNextState(ClientState clientState){
        this.prevState = viewState;
        this.viewState = clientState;
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
        if(viewState.getState() != ViewState.NOT_GAME || viewState.getState() != ViewState.NOT_PLAYER ||
            viewState.getState() != ViewState.PAUSE || viewState.getState() != ViewState.DISCONNECT) {
            //@TODO: notify view
            return;
        }


        if(users.size() > this.localModel.getNumPlayers()){
            //@TODO: view
        }
        else{
            for(String u : users){
                if(!this.localModel.getOtherStations().containsKey(u)){
                    //@TODO: view
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
        if(viewState.getState() == ViewState.NOT_PLAYER){
            //@TODO: notify view
            return;
        }

        setNickname(nick);
        viewState = new Wait(this, clientNetwork);
        prevState = new NotPlayer(this, clientNetwork);
        clientNetwork.connect(nick);
    }

    /**
     * To pick a card from a deck.
     * pick_card_deck(cardType)
     */
    public synchronized void pickCardFromDeck(PlayableCardType cardType){
        if(viewState.getState() != ViewState.PICK){
            //@TODO: notify view
            return;
        }

        viewState = new Wait(this, clientNetwork);
        prevState = new Pick(this, clientNetwork);
        clientNetwork.pickCardFromDeck(cardType);
    }

    /**
     * To pick a card from the table.
     * pick_card_table(cardType, tablePosition)
     */
    public synchronized void pickCardFromTable(PlayableCardType cardType, int position) {
        if(viewState.getState() != ViewState.PICK){
            //@TODO: notify view
            return;
        }
        clientNetwork.pickCardFromTable(cardType, position);

        viewState = new Wait(this, clientNetwork);
        prevState = new Pick(this, clientNetwork);
    }

    /**
     * To place a card given its anchor, the direction and the orientation.
     * place_card(cardToInsert, anchorCard, directionToInsert, cardOrientation)
     */
    public synchronized void placeCard(String cardToInsert, String anchor, Direction direction, CardOrientation cardOrientation) {
        if(viewState.getState() != ViewState.PLACE){
            //@TODO: notify view
            return;
        }

        PlayableCard cardToPlace = localModel.getPlayableCard(cardToInsert);
        PlayableCard anchorCard = localModel.getPlayableCard(anchor);

        if(cardToPlace == null){
            //@TODO: notify view
            return;
        }
        if(anchorCard == null){
            //TODO: notify view
            return;
        }

        if(!localModel.isCardPlaceablePersonalStation(cardToPlace, anchorCard, direction)){
            //@TODO: notify view
            return;
        }

        clientNetwork.placeCard(cardToInsert, anchor, direction, cardOrientation);

        viewState = new Wait(this, clientNetwork);
        prevState = new Place(this, clientNetwork);
    }

    /**
     * To handle a RefusedActionMessage handle and modify the client
     * state consequently.
     * @param message RefusedActionMessage to analyze
     */
    public synchronized void handleError(RefusedActionMessage message){
        switch (message.getErrorType()){
            case ErrorType.INVALID_CARD_ERROR, ErrorType.INVALID_ANCHOR_ERROR -> {
                viewState = new Place(this, clientNetwork);
            }
            case ErrorType.GENERIC, ErrorType.INVALID_TURN_STATE, ErrorType.INVALID_GAME_STATE -> {
                if(viewState.getState() == ViewState.WAIT){
                    viewState = prevState;
                }
            }
            case ErrorType.EMPTY_DECK, ErrorType.EMPTY_TABLE_SLOT -> {
                viewState = new Pick(this, clientNetwork);
            }
            case ErrorType.INVALID_GOAL_CARD_ERROR, ErrorType.COLOR_ALREADY_CHOSEN -> {
                viewState = new Setup(this, clientNetwork);
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
            viewState = new NotPlayer(this, clientNetwork);
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
                viewState = new NotPlayer(this, clientNetwork);
            }
            case Error.PLAYER_ALREADY_REGISTERED_TO_SOME_GAME -> {
                viewState = new Disconnect(this, clientNetwork);
            }
            default -> {
                viewState = new NotGame(this, clientNetwork);
            }
        }
    }

    /**
     * To choose a color at the beginning of the game.
     * choose_color(colorType)
     */
    public synchronized void chooseColor(Color color) {
        if(viewState.getState() != ViewState.SETUP){
            //@TODO: notify view
            return;
        }
        if(!localModel.getAvailableColors().contains(color)){
            //@TODO: notify the observer that the color selected is not available
        }
        else {
            clientNetwork.chooseColor(color);
            ((Setup) viewState).setColorChosen();
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
            ((Setup) viewState).setGoalChosen();
        }
        else{
            //@TODO: notify view
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
        ((Setup) viewState).setInitialCardPlaced();
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
        prevState = new NotGame(this, clientNetwork);
        viewState = new Wait(this, clientNetwork);
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
        prevState = new NotGame(this, clientNetwork);
        viewState = new Wait(this, clientNetwork);
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
            prevState = new NotGame(this, clientNetwork);
            viewState = new Wait(this, clientNetwork);
        }
        else{
            //@TODO: notify view
        }
    }

    /**
     * To parse a string containing the command we want to execute.
     * @return an arraylist that contains at the first position the command
     * name and in the following positions there are the parameters.
     * Ex. create_game(game1, 2, 4) => {create_game, game1, 2, 4}
     * Ex. available_games() => {available_games}
     */

}