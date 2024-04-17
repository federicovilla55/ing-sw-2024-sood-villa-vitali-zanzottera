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
import it.polimi.ingsw.gc19.ObserverPattern.ObserverMessageToClient;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainController {

    enum State{
        ACTIVE, INACTIVE;
    }

    private static MainController mainController = null;

    private final HashMap<String, Tuple<State, String>> playerInfo = new HashMap<>();
    private final HashMap<String, GameController> gamesInfo = new HashMap<>();

    /**
     * This method is used for implementing Singleton pattern in {@link MainController}
     * @return {@link MainController} static instance
     */
    public static MainController getMainController(){
        if(mainController == null){
            mainController = new MainController();
            return mainController;
        }
        return mainController;
    }

    /**
     * This method creates a client. First it checks if player's name is already in use.
     * If so, it returns false; otherwise it creates a new client and puts it in the lobby.
     * A player is in lobby if its {@link State} is <code>State.ACTIVE</code> and it has no associated game.
     * @param player is the {@link ClientHandler} corresponding to player
     * @return <code>true</code> if and only if the player has been correctly created
     */
    public boolean createClient(ClientHandler player) {
        synchronized (this.playerInfo) {
            if (!this.playerInfo.containsKey(player.getName())) {
                this.playerInfo.put(player.getName(), new Tuple<>(State.ACTIVE, null));
                return true;
            }
            else {
                player.update(new GameHandlingError(Error.PLAYER_NAME_ALREADY_IN_USE,
                                                    "Player " + player.getName() + " already in use!").setHeader(player.getName()));
                return false;
            }
        }
    }

    /**
     * This method is used to delete a game from <code>gamesInfo</code> when it finishes. Consequently,
     * it also kicks the game players inside the lobby, sending them a {@link DisconnectGameMessage}.
     * Responsible thread sleeps for <code>Settings.TIME_TO_WAIT_BEFORE_IN_GAME_CLIENT_DISCONNECTION</code>
     * before doing all above operations.
     * @param gameName is the name of the game to delete
     */
    public void fireGameAndPlayer(String gameName){
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            GameController gameController;
            ArrayList<String> playersToRemove;
            synchronized(gamesInfo){
                gameController = gamesInfo.remove(gameName);
            }
            if(gameController != null) {
                gameController.getGameAssociated().getMessageFactory().sendMessageToAllGamePlayers(new DisconnectGameMessage(gameName));
                playersToRemove = gameController.getConnectedClients();
                synchronized (playerInfo) {
                    for (String p : playersToRemove) {
                        playerInfo.put(p, new Tuple<>(MainController.State.ACTIVE, null));
                        gameController.removeClient(p);
                    }
                }
            }
        }, Settings.TIME_TO_WAIT_BEFORE_IN_GAME_CLIENT_DISCONNECTION, TimeUnit.SECONDS);
    }

    /**
     * This method handles player {@link State}. We can have four different situation:
     * <ul>
     *     <li>
     *         <code>(State.INACTIVE, null)</code>: in lobby and inactive
     *     </li>
     *     <li>
     *          <code>(State.ACTIVE, null)</code>: in lobby and active
     *     </li>
     *     <li>
     *          <code>(State.INACTIVE, not null)</code>: in game and inactive
     *     </li>
     *     <li>
     *          <code>(State.ACTIVE, not null)</code>: in game and active
     *     </li>
     * </ul>
     * When a lobby player is signaled to be inactive his state is set to <code>State.INACTIVE</code> and
     * it's deleted from <code>playersInfo</code>.
     * When a game player is signaled to be inactive his state becomes <code>State.INACTIVE</code> and
     * this method calls {@link GameController#removeClient(String)} to remove player's observer.
     * @param nickname nickname of the player became inactive
     */
    public void setPlayerInactive(String nickname){
        Tuple<State, String> playerInfo;
        String gameName;
        synchronized (this.playerInfo){
            playerInfo = this.playerInfo.get(nickname);
        }
        if(playerInfo == null) return;
        if(playerInfo.x() == State.ACTIVE && playerInfo.y() == null){
            synchronized (this.playerInfo){
                this.playerInfo.remove(nickname);
                return;
            }
        }
        if(playerInfo.x() == State.ACTIVE){
            synchronized (this.playerInfo){
                gameName = this.playerInfo.get(nickname).y();
                this.playerInfo.put(nickname, new Tuple<>(State.INACTIVE, gameName));
            }
            synchronized (this.gamesInfo){
                this.gamesInfo.get(gameName).removeClient(nickname);
            }
        }
    }

    /**
     * This method is used when a player ask explicitly to be disconnected both
     * from game or lobby. It removes the player from <code>playerInfo</code> and
     * if player has an associated game signals to its game controller that it has
     * to be removed.
     * @param player player's o be disconnected {@link ClientHandler}
     */
    public void disconnect(ClientHandler player) {
        GameController gameController;
        String gameName = null;
        synchronized (this.playerInfo){
            if(this.playerInfo.containsKey(player.getName())) {
                gameName = this.playerInfo.remove(player.getName()).y();
            }
            else{
                return;
            }
        }
        synchronized (this.gamesInfo){
            gameController = this.gamesInfo.get(gameName);
        }
        if(gameController != null) {
            gameController.removeClient(player.getName());
            player.update(new DisconnectGameMessage(gameName).setHeader(player.getName()));
        }
    }

    /**
     * This method creates a new game. First, it calls {@link MainController#checkPlayer(ClientHandler)} to test if player
     * can create a new game (not registered to other games).
     * Then, it checks if {@param gameName} is already in use: if yes, then send to player {@link GameHandlingError}
     * with <code>ErrorType.CANNOT_BUILD_GAME</code>.
     * If all is ok, it builds a new game along with its game controller, sends a {@link CreatedGameMessage} to player
     * and registers him to the game.
     * @param gameName game name chosen by the client
     * @param numPlayer number of player chosen by the client
     * @param player {@link ClientHandler} of the player building the game
     * @param randomSeed random seed of the game
     * @return true if game has been correctly created
     */
    public boolean createGame(String gameName, int numPlayer, ClientHandler player, long randomSeed){
        Game gameToBuild = null;
        if(!checkPlayer(player)){
            return false;
        }
        synchronized (this.gamesInfo) {
            if (!this.gamesInfo.containsKey(gameName)) {
                try {
                    gameToBuild = new Game(numPlayer, gameName, randomSeed);
                } catch (IOException exception) {
                    player.update(new GameHandlingError(Error.CANNOT_BUILD_GAME,
                                                        "Cannot build game because there is an IO Exception. Try later...")
                                          .setHeader(player.getName()));
                    return false;
                }
                GameController gameController = new GameController(gameToBuild);
                this.gamesInfo.put(gameName, gameController);
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

    /**
     * This method acts the same as {@link MainController#createGame} but random seed is not specified by the player.
     * @param gameName game name chosen by the player
     * @param numPlayer number of players chosen by the game builder player
     * @param player {@link ClientHandler} of the game builder player
     * @return true if the game has been correctly created
     */

    public boolean createGame(String gameName, int numPlayer, ClientHandler player){
        return this.createGame(gameName, numPlayer, player, new Random().nextLong());
    }

    /**
     * This method checks if a player can be registered to some games.
     * Player must not have the same name of other registered players and must not be in
     * other active games.
     * @param player {@link ClientHandler} of the player to check
     * @return true if a player can be registered to a game
     */
    private boolean checkPlayer(ClientHandler player){
        synchronized (this.playerInfo) {
            if (!this.playerInfo.containsKey(player.getName())) {
                return false;
            }
            if (this.playerInfo.get(player.getName()).y() != null) {
                player.update(new GameHandlingError(Error.PLAYER_ALREADY_REGISTERED_TO_SOME_GAME,
                                                    "You can't join a new game because you are already registered to game " + this.playerInfo.get(player.getName()).y())
                                      .setHeader(player.getName()));
                return false;
            }
        }
        return true;
    }

    /**
     * This method registers a player to an available game: the first
     * of the <code>ArrayList<String></code> returned by {@link MainController#findAvailableGames()}
     * It send to player {@link GameHandlingError} with error type <code>NO_GAMES_FREE_TO_JOIN</code> if no game is available.
     * @param player is the {@link ClientHandler} of the player to be registered
     * @return name of joined game if it exists, otherwise null.
     */
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

    /**
     * This method register a player to the game named <code>gameName</code>. It checks if
     * the player has enough right to be registered to a game with {@link MainController#checkPlayer(ClientHandler)}.
     * Then it checks if the specified game exists, and it is accessible (in <code>SETUP</code> and with
     * {@link Game#getNumJoinedPlayer()} not equal to {@link Game#getNumPlayers()}).
     * If yes, it sets entry <code>(State.ACTIVE, gameName)</code> in <code>playerInfo</code> and calls
     * {@link GameController#addClient(String, ObserverMessageToClient)} to register the new player.
     * @param player {@link ClientHandler} of the player to be added
     * @param gameName name of the game to be registered to
     * @return true if {@param player} has been correctly registered to specified game
     * */
    public boolean registerToGame(ClientHandler player, String gameName){
        GameController gameControllerToJoin;
        if(!checkPlayer(player)){
            return false;
        }
        synchronized (this.gamesInfo) {
            if (!this.gamesInfo.containsKey(gameName)) {
                player.update(new GameHandlingError(Error.GAME_NOT_FOUND,
                                                    "Game " + gameName + "not found!")
                                      .setHeader(player.getName()));
                return false;
            }
            if (this.gamesInfo.get(gameName).getGameAssociated().getNumPlayers() == this.gamesInfo.get(gameName).getGameAssociated().getNumJoinedPlayer()) {
                player.update(new GameHandlingError(Error.GAME_NOT_ACCESSIBLE,
                                                    "Game " + gameName + " is not accessible!")
                                      .setHeader(player.getName()));
                return false;
            }
            gameControllerToJoin = this.gamesInfo.get(gameName);
        }
        synchronized (this.playerInfo){
            this.playerInfo.put(player.getName(), new Tuple<>(State.ACTIVE, gameName));
        }
        player.setGameController(gameControllerToJoin);
        player.update(new JoinedGameMessage(gameName).setHeader(player.getName()));
        gameControllerToJoin.addClient(player.getName(), player);
        return true;
    }

    /**
     * This method finds all available games. Nothing can be guaranteed about the other
     * of available games name in the array list returned. For example, if game <code>A</code> has been
     * available for more than <code>B</code>, first element of the array list can be <code>B</code>.
     * @return an <code>ArrayList<String></code> containing all available games names.
     */
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

    /**
     * This method is responsible for advanced functionality resilience to disconnection.
     * It is used when a player wants to reconnect to a game. Player cannot reconnect while they are in lobby.
     * First, it checks if player is registered in <code>playerInfo</code> and then if it is part
     * of some game. If the answer is no, then it sends {@link AvailableGamesMessage}, otherwise signals
     * to game controller associated to the game that a player has reconnected and updates <code>playerInfo</code>
     * putting <code>(State.ACTIVE, gameName)</code>
     * @param clientHandler player's to be reconnected {@link ClientHandler}
     * @return true if player can be reconnected
     */
    public boolean reconnect(ClientHandler clientHandler){
        String gameName = null;
        synchronized (this.playerInfo){
            if(this.playerInfo.containsKey(clientHandler.getName())) {
                gameName = this.playerInfo.get(clientHandler.getName()).y();
            }
            else{
                clientHandler.update(new AvailableGamesMessage(findAvailableGames()).setHeader(clientHandler.getName()));
                return false;
            }
        }
        synchronized (this.gamesInfo) {
            if (!this.gamesInfo.containsKey(gameName)) {
                clientHandler.update(new AvailableGamesMessage(findAvailableGames()).setHeader(clientHandler.getName()));
                return false;
            }
            if (this.gamesInfo.get(gameName).getGameAssociated().getPlayers().stream().map(Player::getName).noneMatch(n -> n.equals(clientHandler.getName()))) {
                clientHandler.update(new GameHandlingError(Error.PLAYER_NOT_IN_GAME,
                                                           "You are not a player of the specified game!")
                                             .setHeader(clientHandler.getName()));
                return false;
            }
            clientHandler.update(new JoinedGameMessage(gameName).setHeader(clientHandler.getName()));
            this.gamesInfo.get(gameName).removeClient(clientHandler.getName());
            this.gamesInfo.get(gameName).addClient(clientHandler.getName(), clientHandler);
            clientHandler.setGameController(this.gamesInfo.get(gameName));

            synchronized (this.playerInfo) {
                playerInfo.put(clientHandler.getName(), new Tuple<>(State.ACTIVE, gameName));
            }

            return true;
        }
    }

    public boolean isPlayerActive(String nick) {
        Tuple<State, String> playerInfo =  this.playerInfo.get(nick);
        if(playerInfo == null) return false;
        return playerInfo.x() == State.ACTIVE;
    }

    /**
     * This static method destroy an instance of {@link MainController}
     */
    public void resetMainController() {
        synchronized (this.playerInfo) {
            this.playerInfo.clear();
        }
        synchronized (this.gamesInfo){
            this.gamesInfo.clear();
        }
    }
}

