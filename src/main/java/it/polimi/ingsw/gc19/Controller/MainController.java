package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Model.Game.Player;
import it.polimi.ingsw.gc19.Model.Tuple;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.ErrorType;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.CreatedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.AvailableGamesMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;

import java.io.IOException;
import java.util.*;

public class MainController {

    enum State{
        ACTIVE, INACTIVE;
    }

    private static MainController mainController = null;

    private final HashMap<String, Tuple<State, GameController>> playerInfo;
    private final HashMap<String, Tuple<Game, GameController>> gamesInfo;

    public static MainController getMainServer() {
        if (mainController == null) {
            mainController = new MainController();
            return mainController;
        }
        return mainController;
    }

    private MainController() {
        this.gamesInfo = new HashMap<>();
        this.playerInfo = new HashMap<>();
    }

    public void createClient(ClientHandler player, String playerNickname) {
        synchronized (this.playerInfo) {
            if (!this.playerInfo.containsKey(playerNickname) || this.playerInfo.get(playerNickname).y() == null) {
                this.playerInfo.put(playerNickname, new Tuple<>(true, null));
                player.update(new CreatedPlayerMessage(playerNickname));
            }
            else {
                if (this.playerInfo.get(playerNickname).x().equals(false) && this.playerInfo.get(playerNickname).y() != null) {
                    this.reconnect(player, this.playerInfo.get(playerNickname).y());
                }
            }
        }
    }

    public static void fireGameAndPlayer(String gameName){
        new Thread(){
            @Override
            public void run() {
                try{
                    this.wait(30 * 1000); //@TODO: define constant to handle this delay
                }
                catch(InterruptedException interruptedException){
                    //@TODO: handle this exception
                }
                //@TODO: remove players and game
            }
        }.start();

    }

    public void setPlayerInactive(String nickname) {
        synchronized (this.playerInfo) {
            String gameName = this.playerInfo.get(nickname).y();
            this.playerInfo.put(nickname, new Tuple<>(false, gameName));
        }
    }

    public void createGame(String gameName, int numPlayer, ClientHandler player, long randomSeed) throws IllegalArgumentException {
        Game gameToBuild = null;
        synchronized (this.gamesInfo) {
            if (!this.gamesInfo.containsKey(gameName)) {
                try {
                    gameToBuild = new Game(numPlayer, randomSeed);
                } catch (IOException exception) {
                    //@TODO: handle this exception
                }
                GameController gameController = new GameController(gameToBuild);
                this.gamesInfo.put(gameName, new Tuple<>(gameToBuild, gameController));
                player.update(new CreatedGameMessage(gameName));
                this.registerToGame(player, gameName);
            }
            else {
                player.update(new GameHandlingError(Error.GAME_NAME_ALREADY_IN_USE,
                                                    "Game name " + gameName + " is already in use!"));
            }
        }
    }


    public void createGame(String gameName, int numPlayer, ClientHandler player) throws IllegalArgumentException {
        this.createGame(gameName, numPlayer, player, new Random().nextLong());
    }

    private void checkPlayer(ClientHandler player) {
        synchronized (this.playerInfo) {
            if (!this.playerInfo.containsKey(player.getName())) {
                throw new IllegalArgumentException(); //guardare meglio
            }
            if (this.playerInfo.get(player.getName()).y() != null) {
                throw new IllegalArgumentException();
            }
        }
    }

    public void registerToFirstAvailableGame(ClientHandler player) {
        checkPlayer(player);
        registerToGame(player, this.findAvailableGames().getFirst());
    }

    public void registerToGame(ClientHandler player, String gameName) throws IllegalArgumentException {
        //Possible exceptions: player not found, game not found, game already full, player already registered to other games...?
        checkPlayer(player);
        synchronized (this.gamesInfo) {
            if (!this.gamesInfo.containsKey(gameName)) {
                player.update(new GameHandlingError(Error.GAME_NOT_FOUND, "Game " + gameName + "not found!"));
            }
            if (this.gamesInfo.get(gameName).x().getNumPlayers() == this.gamesInfo.get(gameName).x().getNumJoinedPlayer()) {
                player.update(new GameHandlingError(Error.GAME_NOT_ACCESSIBLE, "Game " + gameName + " is not accessible!"));
            }
        }
        GameController gameControllerToJoin;
        gameControllerToJoin = this.gamesInfo.get(gameName).y();
        this.playerInfo.put(player.getName(), new Tuple<>(true, gameName));
        player.update(new JoinedGameMessage(gameName));
        gameControllerToJoin.addClient(player.getName(), player);
    }

    private ArrayList<String> findAvailableGames() {
        synchronized (this.gamesInfo) {
            return this.gamesInfo.entrySet()
                                 .stream()
                                 .filter(e -> e.getValue().x().getGameState() == GameState.SETUP && e.getValue().x().getNumPlayers() != e.getValue().x().getNumJoinedPlayer())
                                 .map(Map.Entry::getKey)
                                 .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
    }

    private GameController getGameController(ClientHandler player) {
        return this.gamesInfo.get(playerInfo.get(player.getName()).y()).y();
    }


    /*
     * Check if Player Exists, Check if there is game associated, if
     * Yes, send the state of the Game, if no, send list of nonActive games that it can join.
     * */
    public void reconnect(ClientHandler clientHandler, String gameName) {
        if (!this.playerInfo.containsKey(clientHandler.getName())) {
            //clientHandler.sendMessageToClient();
        }
        if (!this.gamesInfo.containsKey(gameName)) {
            clientHandler.update(new AvailableGamesMessage(findAvailableGames()));
        }
        if (this.gamesInfo.get(gameName).x().getPlayers().stream().map(Player::getName).noneMatch(n -> n.equals(gameName))) {
            //clientHandler.sendMessageToClient()
        }
        this.gamesInfo.get(gameName).y().addClient(clientHandler.getName(), clientHandler);
    }

    public static void destroyMainController() {
        MainController.mainController = null;
    }
}

