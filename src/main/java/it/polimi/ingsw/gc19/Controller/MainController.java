package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Model.Game.Player;
import it.polimi.ingsw.gc19.Model.Tuple;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Settings;

import java.io.IOException;
import java.util.*;

public class MainController {

    enum State{
        ACTIVE, INACTIVE;
    }

    private static MainController mainController = null;

    private static final HashMap<String, Tuple<State, String>> playerInfo = new HashMap<>();
    private static final HashMap<String, GameController> gamesInfo = new HashMap<>();

    public static MainController getMainController(){
        if(mainController == null){
            mainController = new MainController();
            return mainController;
        }
        return mainController;
    }

    /***
     *  Creates new Player if:
     * 1. There is no other player with the same name.
     * 2. The player is currently inactive.
     * @todo: The game the player was playing ended (figure out if we want to handle it here or in the reconnect).
     * */
    public boolean createClient(ClientHandler player) {
        synchronized (MainController.playerInfo) {
            if (!MainController.playerInfo.containsKey(player.getName())) {
                MainController.playerInfo.put(player.getName(), new Tuple<>(State.ACTIVE, null));
                return true;
            }
            else {
                if(MainController.playerInfo.containsKey(player.getName())){
                    player.update(new GameHandlingError(Error.PLAYER_NAME_ALREADY_IN_USE,
                                                        "Player " + player.getName() + " already in use!")
                                          .setHeader(player.getName()));
                    return false;
                }
                return false;
            }
        }
    }

    public static void fireGameAndPlayer(String gameName){
        new Thread(){
            @Override
            public void run() {
                GameController gameController;
                ArrayList<String> playersToRemove;
                try{
                    sleep(Settings.TIME_TO_WAIT_BEFORE_IN_GAME_CLIENT_DISCONNECTION * 1000);
                }
                catch(InterruptedException interruptedException){
                    //@TODO: handle this exception
                }
                synchronized(MainController.gamesInfo){
                    gameController = MainController.gamesInfo.remove(gameName);
                }
                gameController.getGameAssociated().getMessageFactory().sendMessageToAllGamePlayers(new DisconnectGameMessage(gameName));
                playersToRemove = gameController.getConnectedClients();
                synchronized(MainController.playerInfo){
                    for(String p : playersToRemove){
                        MainController.playerInfo.put(p, new Tuple<>(MainController.State.ACTIVE, null));
                        gameController.removeClient(p);
                    }
                }
            }
        }.start();

    }

    //inactive, null => in lobby and inactive
    //active, null => in lobby and active
    //inactive, not null => inactive and in game
    //active, not null => active and in game

    public void setPlayerInactive(String nickname){
        Tuple<State, String> playerInfo;
        String gameName;
        synchronized (MainController.playerInfo){
            playerInfo = MainController.playerInfo.get(nickname);
        }
        if(playerInfo == null) return;
        if(playerInfo.x() == State.ACTIVE && playerInfo.y() == null){
            synchronized (MainController.playerInfo){
                MainController.playerInfo.remove(nickname);
                return;
            }
        }
        if(playerInfo.x() == State.ACTIVE){
            synchronized (MainController.playerInfo){
                gameName = MainController.playerInfo.get(nickname).y();
                MainController.playerInfo.put(nickname, new Tuple<>(State.INACTIVE, gameName));
            }
            synchronized (MainController.gamesInfo){
                MainController.gamesInfo.get(gameName).removeClient(nickname);
            }
        }
    }

    public void disconnect(ClientHandler player) {
        GameController gameController;
        String gameName = null;
        synchronized (MainController.playerInfo){
            if(MainController.playerInfo.containsKey(player.getName())) {
                gameName = MainController.playerInfo.remove(player.getName()).y();
            }
            else{
                return;
            }
        }
        synchronized (MainController.gamesInfo){
            gameController = MainController.gamesInfo.get(gameName);
        }
        if(gameController != null) {
            gameController.removeClient(player.getName());
            player.update(new DisconnectGameMessage(gameName).setHeader(player.getName()));
        }
    }

    public boolean createGame(String gameName, int numPlayer, ClientHandler player, long randomSeed) throws IllegalArgumentException {
        Game gameToBuild = null;
        if(!checkPlayer(player)){
            return false;
        }
        synchronized (MainController.gamesInfo) {
            if (!MainController.gamesInfo.containsKey(gameName)) {
                try {
                    gameToBuild = new Game(numPlayer, gameName, randomSeed);
                } catch (IOException exception) {
                    player.update(new GameHandlingError(Error.CANNOT_BUILD_GAME,
                                                        "Cannot build game because there is an IO Exception. Try later...")
                                          .setHeader(player.getName()));
                    return false;
                }
                GameController gameController = new GameController(gameToBuild);
                MainController.gamesInfo.put(gameName, gameController);
                player.update(new CreatedGameMessage(gameName).setHeader(player.getName()));
                this.registerToGame(player, gameName);
                return true;
            }
            else {
                player.update(new GameHandlingError(Error.GAME_NAME_ALREADY_IN_USE,
                                                    "Game name " + gameName + " is already in use!")
                                      .setHeader(player.getName()));
                return false;
            }
        }
    }


    public boolean createGame(String gameName, int numPlayer, ClientHandler player) throws IllegalArgumentException {
        return this.createGame(gameName, numPlayer, player, new Random().nextLong());
    }

    private boolean checkPlayer(ClientHandler player){
        synchronized (MainController.playerInfo) {
            if (!MainController.playerInfo.containsKey(player.getName())) {
                return false; //See if this condition is reachable
            }
            if (MainController.playerInfo.get(player.getName()).y() != null) {
                player.update(new GameHandlingError(Error.PLAYER_ALREADY_REGISTERED_TO_SOME_GAME,
                                                    "You can't join a new game because you are already registered to game " + MainController.playerInfo.get(player.getName()).y())
                                      .setHeader(player.getName()));
                return false;
            }
        }
        return true;
    }

    public String registerToFirstAvailableGame(ClientHandler player) {
        ArrayList<String> availableGames = findAvailableGames();
        if(!findAvailableGames().isEmpty()) {
            String gameName = findAvailableGames().getFirst();
            if(registerToGame(player, this.findAvailableGames().getFirst())){
                return gameName;
            }
            else{
                return null;
            }
        }
        else{
            player.update(new GameHandlingError(Error.NO_GAMES_FREE_TO_JOIN, "Attention, there aren't games to join! Try later...").setHeader(player.getName()));
            return null;
        }
    }

    public boolean registerToGame(ClientHandler player, String gameName){
        GameController gameControllerToJoin;
        if(!checkPlayer(player)){
            return false;
        }
        synchronized (MainController.gamesInfo) {
            if (!MainController.gamesInfo.containsKey(gameName)) {
                player.update(new GameHandlingError(Error.GAME_NOT_FOUND,
                                                    "Game " + gameName + "not found!")
                                      .setHeader(player.getName()));
                return false;
            }
            if (MainController.gamesInfo.get(gameName).getGameAssociated().getNumPlayers() == MainController.gamesInfo.get(gameName).getGameAssociated().getNumJoinedPlayer()) {
                player.update(new GameHandlingError(Error.GAME_NOT_ACCESSIBLE,
                                                    "Game " + gameName + " is not accessible!")
                                      .setHeader(player.getName()));
                return false;
            }
            gameControllerToJoin = MainController.gamesInfo.get(gameName);
        }
        synchronized (MainController.playerInfo){
            MainController.playerInfo.put(player.getName(), new Tuple<>(State.ACTIVE, gameName));
        }
        player.setGameController(gameControllerToJoin);
        player.update(new JoinedGameMessage(gameName).setHeader(player.getName()));
        gameControllerToJoin.addClient(player.getName(), player);
        return true;
    }

    private ArrayList<String> findAvailableGames() {
        synchronized (MainController.gamesInfo) {
            return MainController.gamesInfo.entrySet()
                                 .stream()
                                 .filter(e -> e.getValue().getGameAssociated().getGameState() == GameState.SETUP &&
                                         e.getValue().getGameAssociated().getNumPlayers() != e.getValue().getGameAssociated().getNumJoinedPlayer())
                                 .map(Map.Entry::getKey)
                                 .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
    }

    public boolean reconnect(ClientHandler clientHandler){
        String gameName = null;
        synchronized (MainController.playerInfo){
            if(MainController.playerInfo.containsKey(clientHandler.getName())) {
                gameName = MainController.playerInfo.get(clientHandler.getName()).y();
            }
            else{
                clientHandler.update(new AvailableGamesMessage(findAvailableGames()).setHeader(clientHandler.getName()));
                return false;
            }
        }
        synchronized (MainController.gamesInfo) {
            if (!MainController.gamesInfo.containsKey(gameName)) {
                clientHandler.update(new AvailableGamesMessage(findAvailableGames()).setHeader(clientHandler.getName()));
                return false;
            }
            if (MainController.gamesInfo.get(gameName).getGameAssociated().getPlayers().stream().map(Player::getName).noneMatch(n -> n.equals(clientHandler.getName()))) {
                clientHandler.update(new GameHandlingError(Error.PLAYER_NOT_IN_GAME,
                                                           "You are not a player of the specified game!")
                                             .setHeader(clientHandler.getName()));
                return false;
            }
            clientHandler.update(new JoinedGameMessage(gameName).setHeader(clientHandler.getName()));
            MainController.gamesInfo.get(gameName).addClient(clientHandler.getName(), clientHandler);
            clientHandler.setGameController(MainController.gamesInfo.get(gameName));

            synchronized (MainController.playerInfo) {
                playerInfo.put(clientHandler.getName(), new Tuple<>(State.ACTIVE, gameName));
            }

            return true;
        }
    }

    public static void destroyMainController() {
        MainController.mainController = null;
        synchronized (MainController.playerInfo) {
            MainController.playerInfo.clear();
        }
        synchronized (MainController.gamesInfo){
            MainController.gamesInfo.clear();
        }
    }
}

