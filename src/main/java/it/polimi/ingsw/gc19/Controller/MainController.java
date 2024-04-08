package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Model.Game.Player;
import it.polimi.ingsw.gc19.Model.Tuple;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;

import java.io.IOException;
import java.util.*;

public class MainController {

    enum State{
        ACTIVE, INACTIVE;
    }

    private static MainController mainController = null;

    private final HashMap<String, Tuple<State, String>> playerInfo;
    private final HashMap<String, GameController> gamesInfo;

    public static MainController getMainServer(){
        if(mainController == null){
            mainController = new MainController();
            return mainController;
        }
        return mainController;
    }

    private MainController(){
        this.gamesInfo = new HashMap<>();
        this.playerInfo = new HashMap<>();
    }

    public boolean createClient(ClientHandler player) {
        synchronized (this.playerInfo) {
            if (!this.playerInfo.containsKey(player.getName()) || this.playerInfo.get(player.getName()).y() == null) {
                this.playerInfo.put(player.getName(), new Tuple<>(State.ACTIVE, null));
                player.update(new CreatedPlayerMessage(player.getName()));
                return true;
            }
            else {
                if (this.playerInfo.get(player.getName()).x() == State.INACTIVE && this.playerInfo.get(player.getName()).y() != null) {
                    this.reconnect(player, this.playerInfo.get(player.getName()).y());
                    return true;
                }
                return false;
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

    public void setPlayerInactive(String nickname){
        String gameName;
        GameController gameController;
        synchronized(this.playerInfo){
            gameName = this.playerInfo.get(nickname).y();
            this.playerInfo.put(nickname, new Tuple<>(State.INACTIVE, gameName));
        }
        synchronized(this.gamesInfo){
            gameController = this.gamesInfo.get(gameName);
        }
        if(gameController != null){
            gameController.removeClient(nickname);
        }
    }

    public void disconnect(String nickname, ClientHandler player) {
        this.setPlayerInactive(nickname);
        synchronized(this.playerInfo) {
            String gameName = this.playerInfo.get(nickname).y();
            if(gameName != null) {
                player.update(new DisconnectGameMessage(gameName));
            }
        }
    }

    public boolean createGame(String gameName, int numPlayer, ClientHandler player, long randomSeed) throws IllegalArgumentException {
        Game gameToBuild = null;
        synchronized (this.gamesInfo) {
            if (!this.gamesInfo.containsKey(gameName)) {
                try {
                    gameToBuild = new Game(numPlayer, randomSeed);
                } catch (IOException exception) {
                    //@TODO: handle this exception
                }
                GameController gameController = new GameController(gameToBuild);
                this.gamesInfo.put(gameName, gameController);
                player.update(new CreatedGameMessage(gameName));
                this.registerToGame(player, gameName);
                return true;
            }
            else {
                player.update(new GameHandlingError(Error.GAME_NAME_ALREADY_IN_USE,
                                                    "Game name " + gameName + " is already in use!"));
                return false;
            }
        }
    }


    public boolean createGame(String gameName, int numPlayer, ClientHandler player) throws IllegalArgumentException {
        return this.createGame(gameName, numPlayer, player, new Random().nextLong());
    }

    private boolean checkPlayer(ClientHandler player){
        synchronized (this.playerInfo) {
            if (!this.playerInfo.containsKey(player.getName())) {
                //Un player può arrivare qui senza essere in playerInfo?
                return false;
            }
            if (this.playerInfo.get(player.getName()).y() != null) {
                player.update(new GameHandlingError(Error.PLAYER_ALREADY_REGISTERED_TO_SOME_GAME,
                                                    "You can't join a new game because you are already registered to game " + this.playerInfo.get(player.getName()).y()));
                return false;
            }
        }
        return true;
    }

    public boolean registerToFirstAvailableGame(ClientHandler player) {
        checkPlayer(player);
        return registerToGame(player, this.findAvailableGames().getFirst());
    }

    public boolean registerToGame(ClientHandler player, String gameName){
        //Possible exceptions: player not found, game not found, game already full, player already registered to other games...?
        GameController gameControllerToJoin;
        if(!checkPlayer(player)){
            return false;
        }
        synchronized (this.gamesInfo) {
            if (!this.gamesInfo.containsKey(gameName)) {
                player.update(new GameHandlingError(Error.GAME_NOT_FOUND, "Game " + gameName + "not found!"));
                return false;
            }
            if (this.gamesInfo.get(gameName).getGameAssociated().getNumPlayers() == this.gamesInfo.get(gameName).getGameAssociated().getNumJoinedPlayer()) {
                player.update(new GameHandlingError(Error.GAME_NOT_ACCESSIBLE, "Game " + gameName + " is not accessible!"));
                return false;
            }
            gameControllerToJoin = this.gamesInfo.get(gameName);
        }
        synchronized (this.playerInfo){
            this.playerInfo.put(player.getName(), new Tuple<>(State.ACTIVE, gameName));
        }
        player.update(new JoinedGameMessage(gameName));
        gameControllerToJoin.addClient(player.getName(), player);
        return true;
    }

    private ArrayList<String> findAvailableGames() {
        synchronized (this.gamesInfo) {
            return this.gamesInfo.entrySet()
                                 .stream()
                                 .filter(e -> e.getValue().getGameAssociated().getGameState() == GameState.SETUP &&
                                         e.getValue().getGameAssociated().getNumPlayers() != e.getValue().getGameAssociated().getNumJoinedPlayer())
                                 .map(Map.Entry::getKey)
                                 .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
    }

    /*
     * Check if Player Exists, Check if there is game associated, if
     * Yes, send the state of the Game, if no, send list of nonActive games that it can join.
     * */
    public void reconnect(ClientHandler clientHandler, String gameName){
        if(!this.playerInfo.containsKey(clientHandler.getName())){
            clientHandler.update(new GameHandlingError(Error.CLIENT_NOT_REGISTERED_TO_SERVER, "Player with name " + clientHandler.getName() + " is not registered to server!"));
            return;
        }
        if(!this.gamesInfo.containsKey(gameName)){
            clientHandler.update(new GameHandlingError(Error.GAME_NOT_FOUND, "Game " + gameName + "not found!"));
            return;
        }
        if(this.gamesInfo.get(gameName).getGameAssociated().getPlayers().stream().map(Player::getName).noneMatch(n -> n.equals(clientHandler.getName()))){
            clientHandler.update(new GameHandlingError(Error.PLAYER_NOT_IN_GAME, "This player in not in the specified game!"));
            return;
        }
        clientHandler.update(new JoinedGameMessage(gameName));
        this.gamesInfo.get(gameName).addClient(clientHandler.getName(), clientHandler);
    }

    public static void destroyMainController() {
        MainController.mainController = null;
    }

}

