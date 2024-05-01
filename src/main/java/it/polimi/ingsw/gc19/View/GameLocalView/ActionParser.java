package it.polimi.ingsw.gc19.View.GameLocalView;

import it.polimi.ingsw.gc19.Enums.*;
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
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkError;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionParser {
    private String nickname;

    public ClientState viewState;

    private ClientState prevState;

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

    public synchronized void disconnect(){
        this.prevState = viewState;
        this.viewState = new Disconnect(); //@TODO: maybe check if we are alredy in diconnect?
        this.viewState.parseAction(null);
    }

    public synchronized boolean isDisconnected(){
        return (this.viewState.getState() == ViewState.DISCONNECT);
    }

    public synchronized void goToWait(){
        this.prevState = viewState;
        this.viewState = new Wait();
    }

    public synchronized void sendMessage(ArrayList<String> command){
        if(command.size()-1 == Command.SENDCHATMESSAGE.getNumArgs() &&
            command.getFirst().equals(Command.SENDCHATMESSAGE.getCommandName())) {
            // first element is command name, second element is the message content
            // and the following elements are the receivers.
            ArrayList<String> usersToSend = new ArrayList<>();
            for(int i = 2; i < command.size(); i++) usersToSend.add(command.get(i));

            this.clientNetwork.sendChatMessage(usersToSend, command.get(1));
        }
        // The action doesn't modify the state of the ViewClient.
    }

    public synchronized void createPlayer(ArrayList<String> command){
        if(command.size()-1 == Command.CREATEPLAYER.getNumArgs()
                && command.getFirst().equals(Command.CREATEPLAYER.getCommandName())){
            viewState = new Wait();
            prevState = new NotPlayer();

            clientNetwork.connect(command.get(1));
        }
    }

    public synchronized void pickCardFromDeck(ArrayList<String> command){
        if(command.size()-1 == Command.PICKCARDDECK.getNumArgs()
                && command.getFirst().equals(Command.PICKCARDDECK.getCommandName())){
            viewState = new Wait();
            prevState = new Pick();

            // how is the player created...
            clientNetwork.pickCardFromDeck(PlayableCardType.valueOf(command.get(1)));
        }
    }

    public synchronized void pickCardFromTable(ArrayList<String> command){
        if(command.size()-1 == Command.PICKCARDTABLE.getNumArgs() &&
                command.getFirst().equals(Command.PICKCARDTABLE.getCommandName())){

            clientNetwork.pickCardFromTable(PlayableCardType.valueOf(command.get(1)),
                    Integer.parseInt(command.get(2)));

            viewState = new Wait();
            prevState = new Pick();
        }
    }

    public synchronized void placeCard(ArrayList<String> command){
        if(command.size()-1 == Command.PLACECARD.getNumArgs() &&
                command.getFirst().equals(Command.PLACECARD.getCommandName())){
            // @todo: is placeable???
            clientNetwork.placeCard(command.get(1), command.get(2),
                    Direction.valueOf(command.get(3)),
                    CardOrientation.valueOf(command.get(4)));
            viewState = new Wait();
            prevState = new Place();
        }
    }

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

    public synchronized void handleError(NetworkHandlingErrorMessage message){
        if(message.getError() == NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER || message.getError() == NetworkError.COULD_NOT_RECONNECT){
            disconnect();
        }else if (message.getError() == NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER){
            viewState = new NotPlayer();
        }
    }

    public synchronized void handleError(GameHandlingError message){
        switch (message.getErrorType()){
            case Error.PLAYER_NAME_ALREADY_IN_USE -> {
                viewState = new NotPlayer();
            }
            case Error.PLAYER_ALREADY_REGISTERED_TO_SOME_GAME -> {
                viewState = new Disconnect();
            }
            default -> {
                viewState = new NotGame();
            }

        }
    }

    public synchronized void chooseColor(ArrayList<String> command) {
        if(command.size()-1 == Command.CHOOSECOLOR.getNumArgs() &&
                command.getFirst().equals(Command.CHOOSECOLOR.getCommandName())){
            clientNetwork.chooseColor(Color.valueOf(command.get(1)));
        }

    }

    public synchronized void chooseGoal(ArrayList<String> command) {
        if(command.size()-1 == Command.CHOOSEPRIVATEGOAL.getNumArgs() &&
                command.getFirst().equals(Command.CHOOSEPRIVATEGOAL.getCommandName())){
            clientNetwork.choosePrivateGoalCard(Integer.parseInt(command.get(1)));
        }
    }

    public synchronized void placeInitialCard(ArrayList<String> command) {
        if(command.size()-1 == Command.PLACEINITIALCARD.getNumArgs() &&
                command.getFirst().equals(Command.PLACEINITIALCARD.getCommandName())){
            clientNetwork.placeInitialCard(CardOrientation.valueOf(command.get(1)));
        }
    }

    public synchronized void availableGames(ArrayList<String> command) {
        if(command.size()-1 == Command.AVAILABLEGAMES.getNumArgs() &&
                command.getFirst().equals(Command.AVAILABLEGAMES.getCommandName())){
            clientNetwork.availableGames();
        }
    }

    public synchronized void joinFirstGame(ArrayList<String> command) {
        if(command.size()-1 == Command.JOINFIRSTGAME.getNumArgs() &&
                command.getFirst().equals(Command.JOINFIRSTGAME.getCommandName())){
            clientNetwork.joinFirstAvailableGame();
            prevState = new NotGame();
            viewState = new Wait();
        }
    }

    public synchronized void joinGame(ArrayList<String> command) {
        if(command.size()-1 == Command.JOINGAME.getNumArgs() &&
                command.getFirst().equals(Command.JOINGAME.getCommandName())){
            clientNetwork.joinGame(command.get(1));
            prevState = new NotGame();
            viewState = new Wait();
        }
    }

    public synchronized void createGameSeed(ArrayList<String> command) {
        if(command.size()-1 == Command.CREATEGAMESEED.getNumArgs() &&
                command.getFirst().equals(Command.CREATEGAMESEED.getCommandName())){
            clientNetwork.createGame(command.get(1), Integer.parseInt(command.get(2)), Integer.parseInt(command.get(3)));
            prevState = new NotGame();
            viewState = new Wait();
        }
    }

    public synchronized void createGame(ArrayList<String> command) {
        if(command.size()-1 == Command.CREATEGAME.getNumArgs() &&
                command.getFirst().equals(Command.CREATEGAME.getCommandName())){
            clientNetwork.createGame(command.get(1), Integer.parseInt(command.get(2)));
            prevState = new NotGame();
            viewState = new Wait();
        }
    }

    public static ArrayList<String> commandParser(String command){
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


    class NotPlayer extends ClientState{

        @Override
        public ViewState getState() {
            return ViewState.NOTPLAYER;
        }

        @Override
        public void parseAction(ArrayList<String> command) {
            // create_player(nickname_player)
            createPlayer(command);
        }
    }

    class NotGame extends ClientState{

        @Override
        public ViewState getState() {
            return ViewState.NOTGAME;
        }

        @Override
        public void parseAction(ArrayList<String> command) {
            // create_game(game_name, num_players)
            createGame(command);

            // create_game_seed(game_name, num_players, game_seed)
            createGameSeed(command);

            // join_game(game_name)
            joinGame(command);

            // join_first_game()
            joinFirstGame(command);

            // available_games()
            availableGames(command);
        }
    }

    class Setup extends ClientState{

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
        public void nextState(GameHandlingError message){
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
        public ViewState getState() {
            return ViewState.SETUP;
        }

        @Override
        public void parseAction(ArrayList<String> command) {
            // send_message(...)
            sendMessage(command);

            // choose_color(color)
            chooseColor(command);

            // choose_goal(card_index)
            chooseGoal(command);

            // place_initial_card(orientation)
            placeInitialCard(command);
        }
    }

    class Wait extends ClientState{
        @Override
        public void nextState(CreatedPlayerMessage message) {
            viewState = new NotGame();
        }

        @Override
        public void nextState(TurnStateMessage message) {
            //if(prevState.getState() == ViewState.NOTPLAYER || prevState.getState() == ViewState.PAUSE) return;

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
        public void nextState(GameHandlingError message){
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
            }
        }

        @Override
        public ViewState getState() {
            return ViewState.WAIT;
        }

        @Override
        public void parseAction(ArrayList<String> command) {
            // send_message(...)
            sendMessage(command);
        }
    }

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
        public void nextState(GameHandlingError message){
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
            // place_card(cardToInsert, anchorCard, directionToInsert, cardOrientation)
            placeCard(command);

            // send_message(...)
            sendMessage(command);
        }
    }

    class Pick extends ClientState{
        @Override
        public ViewState getState() {
            return ViewState.PICK;
        }

        @Override
        public void nextState(GameHandlingError message){
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
            // pick_card_table(cardType, position)
            pickCardFromTable(command);

            // pick_card_deck(cardType)
            pickCardFromDeck(command);

            // send_message(...)
            sendMessage(command);
        }
    }

    class OtherTurn extends ClientState {
        @Override
        public void nextState(EndGameMessage message) {
            prevState = new End();
            viewState = new End();
        }

        @Override
        public void nextState(GameHandlingError message){
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
            // send_message(...)
            sendMessage(command);
        }
    }

    class Disconnect extends ClientState{
        //ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        Thread reconnectScheduler;

        int numReconnect = 0;

        @Override
        public void nextState(JoinedGameMessage message) {
            //scheduler.shutdown();
            System.out.println("Joined the game " + message.getGameName());
            //reconnectScheduler.interrupt();
            System.out.println("Thread interrotto...");
            viewState = new Wait();
        }

        @Override
        public void nextState(AvailableGamesMessage message) {
            //scheduler.shutdown();
            //reconnectScheduler.interrupt();
            viewState = new NotGame();
        }

        @Override
        public void nextState(GameHandlingError message){
            System.out.println("GameHandlingError " + message.getErrorType());
            handleError(message);
        }
        @Override
        public void nextState(NetworkHandlingErrorMessage message){
            System.out.println("NetworkError " + message.getError());
            handleError(message);
        }


        @Override
        public ViewState getState() {
            return ViewState.DISCONNECT;
        }

        @Override
        void parseAction(ArrayList<String> command) {
            System.out.println("Thread costruito: ");
            reconnectScheduler = new Thread(this::reconnect);
            reconnectScheduler.start();
            System.out.println("Thread start " + reconnectScheduler.getName());
        }

        public void reconnect(){
            System.out.println("ok reconnect");
            while (numReconnect < ClientSettings.MAX_RECONNECTION_TRY_BEFORE_ABORTING && !Thread.currentThread().isInterrupted()){
                System.out.println("Tentativo reconnect: " + numReconnect);
                try {
                    clientNetwork.reconnect();
                    System.out.println("Richiesta reconnect andata...");
                } catch (IllegalStateException e) {
                    // Token file not found...
                    System.out.println("Token file not found...");
                    viewState = new NotPlayer();
                    Thread.currentThread().interrupt();
                    return;
                } catch (RuntimeException e) {
                    System.out.println("RunTimeException..." + e.getMessage());
                    e.printStackTrace();
                    numReconnect++;
                }

                try {
                    Thread.currentThread().sleep(2500/*ClientSettings.MAX_TRY_TIME_BEFORE_SIGNAL_DISCONNECTION*1000*/);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

        }
    }

    class Pause extends ClientState{
        @Override
        public void nextState(GameHandlingError message){
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
            // send_message(...)
            sendMessage(command);
        }
    }

    class End extends ClientState{
        @Override
        public void nextState(GameHandlingError message){
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
            // create_player(nickname_player)
            createPlayer(command);

            // send_message(...)
            sendMessage(command);
        }
    }



}