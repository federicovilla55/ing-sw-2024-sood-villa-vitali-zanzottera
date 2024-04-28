package it.polimi.ingsw.gc19.View.GameLocalView;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.ClientRMI;
import it.polimi.ingsw.gc19.Networking.Client.ClientTCP.ClientTCP;
import it.polimi.ingsw.gc19.Networking.Client.Message.Heartbeat.HeartBeatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkError;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;

import java.io.StringReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
        this.viewState = new Disconnect();
        System.out.println("Disconnect..." + this.getState() + " " + prevState);
        this.viewState.parseAction(null);
    }

    public synchronized boolean isDisconnected(){
        return (this.viewState.getState() == ViewState.DISCONNECT);
    }

    public void sendMessage(ArrayList<String> command){
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

    public void createPlayer(ArrayList<String> command){
        if(command.size()-1 == Command.CREATEPLAYER.getNumArgs()
                && command.getFirst().equals(Command.CREATEPLAYER.getCommandName())){
            viewState = new Wait();
            prevState = new NotPlayer();

            clientNetwork.setNickname(command.get(1));

            clientNetwork.connect();
        }
    }

    public void pickCardFromDeck(ArrayList<String> command){
        if(command.size()-1 == Command.PICKCARDDECK.getNumArgs()
                && command.getFirst().equals(Command.PICKCARDDECK.getCommandName())){
            viewState = new Wait();
            prevState = new Pick();

            // how is the player created...
            clientNetwork.pickCardFromDeck(PlayableCardType.valueOf(command.get(1)));
        }
    }

    public void pickCardFromTable(ArrayList<String> command){
        if(command.size()-1 == Command.PICKCARDTABLE.getNumArgs() &&
                command.getFirst().equals(Command.PICKCARDTABLE.getCommandName())){

            clientNetwork.pickCardFromTable(PlayableCardType.valueOf(command.get(1)),
                    Integer.parseInt(command.get(2)));

            viewState = new Wait();
            prevState = new Pick();
        }
    }

    public void placeCard(ArrayList<String> command){
        if(command.size()-1 == Command.PLACECARD.getNumArgs() &&
                command.getFirst().equals(Command.PLACECARD.getCommandName())){
            clientNetwork.placeCard(command.get(1), command.get(2),
                    Direction.valueOf(command.get(3)),
                    CardOrientation.valueOf(command.get(4)));
            viewState = new Wait();
            prevState = new Place();
        }
    }

    public void chooseColor(ArrayList<String> command) {
        if(command.size()-1 == Command.CHOOSECOLOR.getNumArgs() &&
                command.getFirst().equals(Command.CHOOSECOLOR.getCommandName())){
            clientNetwork.chooseColor(Color.valueOf(command.get(1)));
        }

    }

    public void chooseGoal(ArrayList<String> command) {
        if(command.size()-1 == Command.CHOOSEPRIVATEGOAL.getNumArgs() &&
                command.getFirst().equals(Command.CHOOSEPRIVATEGOAL.getCommandName())){
            clientNetwork.choosePrivateGoalCard(Integer.parseInt(command.get(1)));
        }
    }

    public void placeInitialCard(ArrayList<String> command) {
        if(command.size()-1 == Command.PLACEINITIALCARD.getNumArgs() &&
                command.getFirst().equals(Command.PLACEINITIALCARD.getCommandName())){
            clientNetwork.placeInitialCard(CardOrientation.valueOf(command.get(1)));
        }
    }

    public void availableGames(ArrayList<String> command) {
        if(command.size()-1 == Command.AVAILABLEGAMES.getNumArgs() &&
                command.getFirst().equals(Command.AVAILABLEGAMES.getCommandName())){
            clientNetwork.availableGames();
        }
    }

    public void joinFirstGame(ArrayList<String> command) {
        if(command.size()-1 == Command.JOINFIRSTGAME.getNumArgs() &&
                command.getFirst().equals(Command.JOINFIRSTGAME.getCommandName())){
            clientNetwork.joinFirstAvailableGame();
            prevState = new NotGame();
            viewState = new Wait();
        }
    }

    public void joinGame(ArrayList<String> command) {
        if(command.size()-1 == Command.JOINGAME.getNumArgs() &&
                command.getFirst().equals(Command.JOINGAME.getCommandName())){
            clientNetwork.joinGame(command.get(1));
            prevState = new NotGame();
            viewState = new Wait();
        }
    }

    public void createGameSeed(ArrayList<String> command) {
        if(command.size()-1 == Command.CREATEGAMESEED.getNumArgs() &&
                command.getFirst().equals(Command.CREATEGAMESEED.getCommandName())){
            clientNetwork.createGame(command.get(1), Integer.parseInt(command.get(2)), Integer.parseInt(command.get(3)));
            prevState = new NotGame();
            viewState = new Wait();
        }
    }

    public void createGame(ArrayList<String> command) {
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

        // error

        @Override
        public void nextState(StartPlayingGameMessage message) {
            if(message.getNickFirstPlayer().equals(getNickname())){
                viewState = new Place();
            }else{
                viewState = new OtherTurn();
            }
        }

        @Override
        public ViewState getState() {
            return ViewState.SETUP;
        }

        @Override
        public void parseAction(ArrayList<String> command) {
            // send_mesage(...)
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
            if(message.getNick().equals(getNickname()) && message.getTurnState() == TurnState.DRAW) {
                viewState = new Pick();
            }else if(!message.getNick().equals(getNickname())){
                viewState = new OtherTurn();
            }
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
        public void nextState(RefusedActionMessage message){
            viewState = prevState;
        }

        @Override
        public void nextState(GameHandlingError message){
            viewState = prevState;
        }

        @Override
        public void nextState(NetworkHandlingErrorMessage message){
            /*@todo: if(message.getError() == NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER){
                viewState = new NotPlayer();
            }*/
            viewState = new Disconnect();
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
        public void parseAction(ArrayList<String> command) {
            // place_card(cardToInsert, anchorCard, directionToInsert, cardOrientation)
            placeCard(command);

            // send_message(...)
            placeCard(command);
        }
    }

    class Pick extends ClientState{

        @Override
        public ViewState getState() {
            return ViewState.PICK;
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
        // @todo: maybe otherTurn can be included into a WAIT?

        public void nextState(TurnStateMessage message) {
            if(message.getNick().equals(nickname) &&
                message.getTurnState() == TurnState.PLACE){
                viewState = new Place();
            }
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
        ScheduledExecutorService scheduler = null;
        @Override
        public void nextState(JoinedGameMessage message) {
            System.out.println("Riconnesso");
            viewState = prevState;

            // or shutdown
            scheduler.shutdownNow();
        }


        @Override
        public ViewState getState() {
            return ViewState.DISCONNECT;
        }

        @Override
        void parseAction(ArrayList<String> command) {
            if(scheduler == null) {
                scheduler = Executors.newScheduledThreadPool(1);
                scheduler.scheduleAtFixedRate(() -> {
                    System.out.println("Provo a riconnettermi..." + this.getState() + " " + getNickname() + " ");
                    clientNetwork.reconnect();
                }, 0, 10, TimeUnit.SECONDS);
            }
        }

    }

    class Pause extends ClientState{

        @Override
        public void nextState(GameResumedMessage message) {

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


