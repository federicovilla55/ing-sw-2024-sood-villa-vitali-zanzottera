package it.polimi.ingsw.gc19.View.GameLocalView;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.ClientSettings;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.ErrorType;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.GameConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.AvailableGamesMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkError;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class is used to forward the actions given by the user to the client network interface.
 * The class can permit different action based on the state of the client, that vary when certain actions are
 * done or when certain messages are received.
 * It contains a method to parse a string and various states that can elaborate the action.
 */
public class ActionParser {
    private String nickname;

    /**
     * The attribute represent the current state of the client.
     * Depending on its value only certain actions can be perfomed.
     */
    public ClientState viewState;

    /**
     * The attribute represent the previous state of the client.
     * Used to retrieve the old state after an action is refused,
     * the game is no more in PAUSE, ...
     */
    private ClientState prevState;

    private LocalModel localModel;

    private ClientInterface clientNetwork;

    public ActionParser(){
        viewState = new NotPlayer();
        prevState = new NotPlayer();
    }

    public ActionParser(String nickname){
        this.nickname = nickname;
        viewState = new NotPlayer();
        prevState = new NotPlayer();
    }

    public ClientInterface getClientNetwork(){
        return this.clientNetwork;
    }

    /**
     * Public method used to forward a string containing an action to be performed
     * to the methods that effectively performs those actions.
     * @param action a string containing the actions to be done.
     */
    public void parseAction(String action) {
        viewState.parseAction(commandParser(action));
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setClient(ClientInterface client){
        this.clientNetwork = client;
        this.setNickname(client.getNickname());
    }

    public synchronized ViewState getState(){
        return viewState.getState();
    }

    public synchronized void setLocalModel(LocalModel localModel){
        this.localModel = localModel;
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
        this.viewState = new Disconnect(this);
        this.viewState.parseAction(null);
    }

    public synchronized boolean isDisconnected(){
        return (this.viewState.getState() == ViewState.DISCONNECT);
    }

    /**
     * To send a message in the chat.
     * send_message(message_content, receiver1 {, receiver2, ...})
     */
    public synchronized boolean sendMessage(ArrayList<String> command) {
        try{
            if (command.size() - 1 >= Command.SENDCHATMESSAGE.getNumArgs() &&
                    command.getFirst().equals(Command.SENDCHATMESSAGE.getCommandName())) {
                // first element is command name, second element is the message content
                // and the following elements are the receivers.
                ArrayList<String> usersToSend = new ArrayList<>();
                for (int i = 2; i < command.size(); i++) usersToSend.add(command.get(i));

                this.clientNetwork.sendChatMessage(usersToSend, command.get(1));
                return true;
            }
        }catch (IllegalStateException e) {}
        return false;
    }

    /**
     * To set the nickname of the client and request a connection.
     * create_player(nickname)
     */
    public synchronized boolean createPlayer(ArrayList<String> command){
        try {
            if (command.size() - 1 == Command.CREATEPLAYER.getNumArgs()
                    && command.getFirst().equals(Command.CREATEPLAYER.getCommandName())) {
                viewState = new Wait();
                prevState = new NotPlayer();

                clientNetwork.connect(command.get(1));

                return true;
            }
        }catch (IllegalStateException e) {}
        return false;
    }

    /**
     * To pick a card from a deck.
     * pick_card_deck(cardType)
     */
    public synchronized boolean pickCardFromDeck(ArrayList<String> command){
        try {
            if (command.size() - 1 == Command.PICKCARDDECK.getNumArgs()
                    && command.getFirst().equals(Command.PICKCARDDECK.getCommandName())) {
                viewState = new Wait();
                prevState = new Pick();

                clientNetwork.pickCardFromDeck(PlayableCardType.valueOf(command.get(1)));
                return true;
            }
        }catch (IllegalStateException e) {}
        return false;
    }

    /**
     * To pick a card from the table.
     * pick_card_table(cardType, tablePosition)
     */
    public synchronized boolean pickCardFromTable(ArrayList<String> command) {
        try {
            if (command.size() - 1 == Command.PICKCARDTABLE.getNumArgs() &&
                    command.getFirst().equals(Command.PICKCARDTABLE.getCommandName())) {

                clientNetwork.pickCardFromTable(PlayableCardType.valueOf(command.get(1)),
                        Integer.parseInt(command.get(2)));

                viewState = new Wait();
                prevState = new Pick();

                return true;
            }
        }catch (IllegalStateException e) {}
        return false;
    }

    /**
     * To place a card given its anchor, the direction and the orientation.
     * place_card(cardToInsert, anchorCard, directionToInsert, cardOrientation)
     */
    public synchronized boolean placeCard(ArrayList<String> command) {
        try {
            if (command.size() - 1 == Command.PLACECARD.getNumArgs() &&
                    command.getFirst().equals(Command.PLACECARD.getCommandName())) {
                PlayableCard cardToPlace = localModel.getPlayableCard(command.get(1));
                PlayableCard anchorCard = localModel.getPlayableCard(command.get(2));

                if(!getLocalModel().isCardPlaceablePersonalStation(
                        cardToPlace, anchorCard, Direction.valueOf(command.get(3)))){
                    // notify observer that the card can't be placed

                    return false;
                }

                clientNetwork.placeCard(command.get(1), command.get(2),
                        Direction.valueOf(command.get(3)),
                        CardOrientation.valueOf(command.get(4)));
                viewState = new Wait();
                prevState = new Place();

                return true;
            }
        }catch (IllegalStateException e) {}
        return false;
    }

    /**
     * To handle a RefusedActionMessage handle and modify the client
     * state consequently.
     * @param message RefusedActionMessage to analyze
     */
    public synchronized void handleError(RefusedActionMessage message){
        switch (message.getErrorType()){
            case ErrorType.INVALID_CARD_ERROR, ErrorType.INVALID_ANCHOR_ERROR -> {
                viewState = new Place();
            }
            case ErrorType.GENERIC, ErrorType.INVALID_TURN_STATE, ErrorType.INVALID_GAME_STATE -> {
                if(viewState.getState() == ViewState.WAIT){
                    viewState = prevState;
                }
            }
            case ErrorType.EMPTY_DECK, ErrorType.EMPTY_TABLE_SLOT -> {
                viewState = new Pick();
            }
            case ErrorType.INVALID_GOAL_CARD_ERROR, ErrorType.COLOR_ALREADY_CHOSEN -> {
                viewState = new Setup();
            }
        }
    }

    /**
     * To handle a NetworkHandlingErrorMessage handle and modify the client
     * state consequently.
     * @param message NetworkHandlingErrorMessage to analyze
     */
    public synchronized void handleError(NetworkHandlingErrorMessage message){
        if(message.getError() == NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER || message.getError() == NetworkError.COULD_NOT_RECONNECT){
            disconnect();
        }else if (message.getError() == NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER){
            viewState = new NotPlayer();
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
                viewState = new NotPlayer();
            }
            case Error.PLAYER_ALREADY_REGISTERED_TO_SOME_GAME -> {
                viewState = new Disconnect(this);
            }
            default -> {
                viewState = new NotGame();
            }

        }
    }

    /**
     * To choose a color at the beginning of the game.
     * choose_color(colorType)
     */
    public synchronized boolean chooseColor(ArrayList<String> command) {
        try {
            if (command.size() - 1 == Command.CHOOSECOLOR.getNumArgs() &&
                    command.getFirst().equals(Command.CHOOSECOLOR.getCommandName())) {

                if(localModel == null || !localModel.getAvailableColors().contains(Color.valueOf(command.get(1)))){
                    // notify the observer that the color selected is not available
                    return false;
                }

                clientNetwork.chooseColor(Color.valueOf(command.get(1)));
                return true;
            }
        }catch (IllegalStateException e) {}
        return false;
    }

    /**
     * To choose a goal card at the beginning of the game.
     * There should two cards to select from so the user should give a number
     * representing the selected card.
     * choose_goal(goalCardIndex)
     */
    public synchronized boolean chooseGoal(ArrayList<String> command) {
        try {
            if (command.size() - 1 == Command.CHOOSEPRIVATEGOAL.getNumArgs() &&
                    command.getFirst().equals(Command.CHOOSEPRIVATEGOAL.getCommandName())) {
                clientNetwork.choosePrivateGoalCard(Integer.parseInt(command.get(1)));
                return true;
            }
        }catch (IllegalStateException e) {}
        return false;
    }

    /**
     * To place the initial card at the beginning of the game vien its orientation.
     * place_initial_card(orientation)
     */
    public synchronized boolean placeInitialCard(ArrayList<String> command) {
        try {
            if (command.size() - 1 == Command.PLACEINITIALCARD.getNumArgs() &&
                    command.getFirst().equals(Command.PLACEINITIALCARD.getCommandName())) {
                clientNetwork.placeInitialCard(CardOrientation.valueOf(command.get(1)));
                return true;
            }
        }catch (IllegalStateException e) {}
        return false;
    }

    /**
     * To ask the server which games are free to join.
     * available_games()
     */
    public synchronized boolean availableGames(ArrayList<String> command) {
        try {
            if (command.size() - 1 == Command.AVAILABLEGAMES.getNumArgs() &&
                    command.getFirst().equals(Command.AVAILABLEGAMES.getCommandName())) {
                clientNetwork.availableGames();
                return true;
            }
        }catch (IllegalStateException e) {}
        return false;
    }

    /**
     * To notify the server that the player want to join the first available fame.
     * join_first_game()
     */
    public synchronized boolean joinFirstGame(ArrayList<String> command) {
        try {
            if (command.size() - 1 == Command.JOINFIRSTGAME.getNumArgs() &&
                    command.getFirst().equals(Command.JOINFIRSTGAME.getCommandName())) {
                clientNetwork.joinFirstAvailableGame();
                prevState = new NotGame();
                viewState = new Wait();

                return true;
            }
        }catch (IllegalStateException e) {}
        return false;
    }

    /**
     * To notify the server that the player want to join
     * a particular game.
     * join_game(gameName)
     */
    public synchronized boolean joinGame(ArrayList<String> command) {
        try {
            if (command.size() - 1 == Command.JOINGAME.getNumArgs() &&
                    command.getFirst().equals(Command.JOINGAME.getCommandName())) {
                clientNetwork.joinGame(command.get(1));
                prevState = new NotGame();
                viewState = new Wait();
                return true;
            }
        }catch (IllegalStateException e) {}
        return false;
    }

    /**
     * To create a game specifying the game name and the number of players.
     * create_game(gameName, numPlayers)
     */
    public synchronized boolean createGame(ArrayList<String> command) {
        try {
            if (command.size() - 1 == Command.CREATEGAME.getNumArgs() &&
                    command.getFirst().equals(Command.CREATEGAME.getCommandName())) {
                clientNetwork.createGame(command.get(1), Integer.parseInt(command.get(2)));
                prevState = new NotGame();
                viewState = new Wait();
                return true;
            }
        }catch (IllegalStateException e) {}
        return false;
    }

    /**
     * To parse a string containing the command we want to execute.
     * @return an arraylist that contains at the first position the command
     * name and in the following positions there are the parameters.
     * Ex. create_game(game1, 2, 4) => {create_game, game1, 2, 4}
     * Ex. available_games() => {available_games}
     */
    public static ArrayList<String> commandParser(String command){
        if(command == null) return new ArrayList<>();

        ArrayList<String> commandParsed = new ArrayList<>();
        command = command.replaceAll("\\s", "");
        Pattern pattern = Pattern.compile("(\\w+)\\((.*?)\\)");
        Matcher matcher = pattern.matcher(command);

        if (matcher.matches()) {
            commandParsed.add(matcher.group(1));
            String[] arguments = matcher.group(2).split(",\\s*");
            for (String arg : arguments) {
                if(!arg.isEmpty()) {
                    commandParsed.add(arg);
                }
            }
        } else {
            System.out.println("Invalid command format...");
        }

        return commandParsed;
    }

    /**
     * This state represent the moment a client opens the game and is not connected
     * to the server. The client needs to select a nickname and ask for a connection request.
     */
    class NotPlayer extends ClientState{
        @Override
        public ViewState getState() {
            return ViewState.NOTPLAYER;
        }

        @Override
        public void parseAction(ArrayList<String> command) {
            if(!createPlayer(command)){
                // notify the given command was not found or suitable for the round
            }
        }
    }

    /**
     * The client is connected to the server, but it is in no game.
     * Methods to determine the available games and to join/create games are permitted.
     */
    class NotGame extends ClientState{

        @Override
        public ViewState getState() {
            return ViewState.NOTGAME;
        }

        @Override
        public void parseAction(ArrayList<String> command) {
            if(!(createGame(command) || joinGame(command)
                || joinFirstGame(command) || availableGames(command))){
                // notify the given command was not found or suitable for the round
            }
        }
    }

    /**
     * The client is connected in a game that is in the SETUP state.
     */
    class Setup extends ClientState{
        private boolean colorChosen = false;
        private boolean goalChosen = false;
        private boolean initialCardPlaced = false;

        @Override
        public void nextState(GamePausedMessage message){
            prevState = viewState;
            viewState = new Pause();
        }

        @Override
        public void nextState(StartPlayingGameMessage message) {
            if(message.getNickFirstPlayer().equals(getNickname())){
                viewState = new Place();
            }else{
                viewState = new OtherTurn();
            }
        }

        @Override
        public void nextState(GameHandlingErrorMessage message){
            handleError(message);
        }
        @Override
        public void nextState(NetworkHandlingErrorMessage message){
            handleError(message);
        }

        @Override
        public void nextState(RefusedActionMessage message){
            handleError(message);
            if(message.getErrorType() == ErrorType.COLOR_ALREADY_CHOSEN) colorChosen = false;
            else if (message.getErrorType() == ErrorType.INVALID_GOAL_CARD_ERROR) goalChosen = false;
        }


        @Override
        public ViewState getState() {
            return ViewState.SETUP;
        }

        @Override
        public void parseAction(ArrayList<String> command) {
            if(!colorChosen) colorChosen = chooseColor(command);
            if(!goalChosen) goalChosen = chooseGoal(command);
            if(!initialCardPlaced) initialCardPlaced = placeInitialCard(command);

            sendMessage(command);
        }
    }

    /**
     * The client is waiting for a message from the server to continue playing.
     * This can be because of a place/draw he just made or because it's someone
     * else's turn.
     */
    class Wait extends ClientState{
        @Override
        public void nextState(CreatedPlayerMessage message) {
            viewState = new NotGame();
        }

        @Override
        public void nextState(TurnStateMessage message) {
            if(message.getNick().equals(getNickname()) && message.getTurnState() == TurnState.DRAW) {
                viewState = new Pick();
            }else if(!message.getNick().equals(getNickname())){
                viewState = new OtherTurn();
            }
        }

        @Override
        public void nextState(EndGameMessage message) {
            prevState = new End();
            viewState = new End();
        }

        @Override
        public void nextState(OwnAcceptedPickCardFromDeckMessage message){
            viewState = new OtherTurn();
        }

        @Override
        public void nextState(JoinedGameMessage message) {
            viewState = new Setup();
        }

        @Override
        public void nextState(CreatedGameMessage message) {
            viewState = new Setup();
        }

        @Override
        public void nextState(GameHandlingErrorMessage message){
            handleError(message);
        }
        @Override
        public void nextState(NetworkHandlingErrorMessage message){
            handleError(message);
        }
        @Override
        public void nextState(RefusedActionMessage message){
            handleError(message);
        }

        @Override
        public void nextState(GamePausedMessage message){
            viewState = new Pause();
        }

        @Override
        public void nextState(GameConfigurationMessage message){
            if(message.getGameState() == GameState.SETUP){
                viewState = new Setup();
            }else{
                viewState = (message.getActivePlayer().equals(getNickname())) ?
                        new Place() : new OtherTurn();
            }
        }

        @Override
        public ViewState getState() {
            return ViewState.WAIT;
        }

        @Override
        public void parseAction(ArrayList<String> command) {
            if(!sendMessage(command)){
                // notify the given command was not found or suitable for the round
            }
        }
    }

    /**
     * The clients can place a card in its station.
     */
    class Place extends ClientState{
        @Override
        public ViewState getState() {
            return ViewState.PLACE;
        }

        @Override
        public void nextState(GamePausedMessage message){
            prevState = viewState;
            viewState = new Pause();
        }

        @Override
        public void nextState(GameHandlingErrorMessage message){
            handleError(message);
        }
        @Override
        public void nextState(NetworkHandlingErrorMessage message){
            handleError(message);
        }
        @Override
        public void nextState(RefusedActionMessage message){
            handleError(message);
        }

        @Override
        public void parseAction(ArrayList<String> command) {
            if(!(placeCard(command) || sendMessage(command))){
                // notify the given command was not found or suitable for the round
            }
        }
    }

    /**
     * The client can pick a card from one of the two decks or from
     * one of the four cards in the table.
     */
    class Pick extends ClientState{
        @Override
        public ViewState getState() {
            return ViewState.PICK;
        }

        @Override
        public void nextState(GameHandlingErrorMessage message){
            handleError(message);
        }
        @Override
        public void nextState(NetworkHandlingErrorMessage message){
            handleError(message);
        }
        @Override
        public void nextState(RefusedActionMessage message){
            handleError(message);
        }

        @Override
        public void nextState(GamePausedMessage message){
            prevState = viewState;
            viewState = new Pause();
        }

        @Override
        public void parseAction(ArrayList<String> command) {
            if(!(pickCardFromTable(command) || pickCardFromDeck(command)
                    || sendMessage(command))){
                // notify the given command was not found or suitable for the round
            }
        }
    }

    class OtherTurn extends ClientState {
        @Override
        public void nextState(EndGameMessage message) {
            prevState = new End();
            viewState = new End();
        }

        @Override
        public void nextState(GameHandlingErrorMessage message){
            handleError(message);
        }
        @Override
        public void nextState(NetworkHandlingErrorMessage message){
            handleError(message);
        }
        @Override
        public void nextState(RefusedActionMessage message){
            handleError(message);
        }

        public void nextState(TurnStateMessage message) {
            if(message.getNick().equals(nickname) &&
                message.getTurnState() == TurnState.PLACE){
                viewState = new Place();
            }
        }

        @Override
        public void nextState(GamePausedMessage message){
            prevState = viewState;
            viewState = new Pause();
        }

        @Override
        public ViewState getState() {
            return ViewState.OTHERTURN;
        }

        @Override
        public void parseAction(ArrayList<String> command) {
            if(!sendMessage(command)){
                // notify the given command was not found or suitable for the round
            }
        }
    }

    /**
     * Because all the other players in the game left, the game is in pause
     * and therefore the client can't make any action in the game.
     */
    class Pause extends ClientState{
        @Override
        public void nextState(GameHandlingErrorMessage message){
            handleError(message);
        }
        @Override
        public void nextState(NetworkHandlingErrorMessage message){
            handleError(message);
        }
        @Override
        public void nextState(RefusedActionMessage message){
            handleError(message);
        }

        @Override
        public void nextState(GameResumedMessage message) {
            viewState = prevState;
        }

        @Override
        public ViewState getState() {
            return ViewState.PAUSE;
        }

        @Override
        public void parseAction(ArrayList<String> command) {
            if(!sendMessage(command)){
                // notify the given command was not found or suitable for the round
            }
        }
    }

    /**
     * The game ended. The client can still write in chat or try to connect
     * to new games.
     */
    class End extends ClientState{
        @Override
        public void nextState(GameHandlingErrorMessage message){
            handleError(message);
        }
        @Override
        public void nextState(NetworkHandlingErrorMessage message){
            handleError(message);
        }
        @Override
        public void nextState(RefusedActionMessage message){
            handleError(message);
        }
        @Override
        public void nextState(CreatedPlayerMessage message) {
            viewState = new NotGame();
        }

        @Override
        public ViewState getState() {
            return ViewState.END;
        }

        @Override
        public void parseAction(ArrayList<String> command) {
            if(!(createPlayer(command) || sendMessage(command))){
                // notify the given command was not found or suitable for the round
            }
        }
    }

    /**
     * The client is currently disconnected from the game. A new thread is created
     * to try to reconnect to the game or the main lobby. As soon as a connection is
     * established, the client changes its state.
     */
    class Disconnect extends ClientState {
        private final ActionParser actionParser;
        Thread reconnectScheduler;

        public Disconnect(ActionParser actionParser) {
            this.actionParser = actionParser;
        }

        @Override
        public void nextState(JoinedGameMessage message) {
            reconnectScheduler.interrupt();
            actionParser.clientNetwork.startSendingHeartbeat();
            actionParser.viewState = new Wait();
        }

        //When we receive correct message from server, we notify client to start sending heartbeat
        @Override
        public void nextState(AvailableGamesMessage message) {
            reconnectScheduler.interrupt();
            actionParser.clientNetwork.startSendingHeartbeat();
            actionParser.viewState = new NotGame();
        }

        @Override
        public void nextState(GameHandlingErrorMessage message) {
            switch (message.getErrorType()) {
                case Error.PLAYER_NAME_ALREADY_IN_USE -> {
                    actionParser.viewState = new NotPlayer();
                }
                case Error.PLAYER_NOT_IN_GAME -> {
                    actionParser.viewState = new Disconnect(this.actionParser);
                }
                default -> {
                    actionParser.viewState = new NotGame();
                }

            }
        }

        @Override
        public void nextState(NetworkHandlingErrorMessage message) {
            //@TODO: handle better the error?
            actionParser.handleError(message);
        }


        @Override
        public ViewState getState() {
            return ViewState.DISCONNECT;
        }

        @Override
        void parseAction(ArrayList<String> command) {
            if (actionParser.clientNetwork != null) {
                reconnectScheduler = new Thread(this::reconnect);
                reconnectScheduler.start();
            }
        }

        public void reconnect() {
            int numReconnect = 0;

            while (numReconnect < ClientSettings.MAX_RECONNECTION_TRY_BEFORE_ABORTING && !Thread.currentThread().isInterrupted()) {
                try {
                    actionParser.clientNetwork.reconnect();
                } catch (IllegalStateException e) {
                    // Token file not found...
                    actionParser.viewState = new NotPlayer();
                    reconnectScheduler.interrupt();
                    return;
                } catch (RuntimeException e) {
                    numReconnect++;
                }

                try {
                    TimeUnit.MILLISECONDS.sleep(ClientSettings.MAX_TRY_TIME_BEFORE_SIGNAL_DISCONNECTION * 1000);
                } catch (InterruptedException e) {
                    reconnectScheduler.interrupt();
                    return;
                }
            }

        }
    }
}