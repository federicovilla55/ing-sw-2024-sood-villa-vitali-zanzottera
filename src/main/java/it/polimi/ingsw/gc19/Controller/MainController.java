package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Model.Game.Player;
import it.polimi.ingsw.gc19.Model.Tuple;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.ErrorType;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.AvailableGamesMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.CreatedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MainController {

    private static MainController mainController = null;

    private final HashMap<String, GameController> playerInfo;
    private final HashMap<String, Tuple<Game, GameController>> gamesInfo;

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

    public boolean createClient(ClientHandler player, String playerNickname){
        synchronized(this.playerInfo) {
            if (!this.playerInfo.containsKey(playerNickname)) {
                this.playerInfo.put(playerNickname, null);
                player.update(new CreatedPlayerMessage(playerNickname));
                return true;
            }
        }
        return false;
    }

    public void setPlayerInactive(String nickname){
        synchronized(this.playerInfo){
            this.playerInfo.get(nickname).removeClient(nickname);
        }
    }

    public void setPlayerInactive(ClientHandler client){
        synchronized(this.playerInfo){
            this.playerInfo.get(client.getName()).removeClient(client.getName());
        }
    }

    public synchronized void createGame(String gameName, int numPlayer, ClientHandler player, long randomSeed) throws IllegalArgumentException {
        Game gameToBuild = null;
        synchronized(this.gamesInfo) {
            if (!this.gamesInfo.containsKey(gameName)) {
                try {
                    gameToBuild = new Game(numPlayer,randomSeed);
                } catch (IOException exception) {
                    //@TODO: handle this exception
                }
                GameController gameController = new GameController(gameToBuild);
                this.gamesInfo.put(gameName, new Tuple<>(gameToBuild, gameController));
                player.update(new CreatedGameMessage(gameName));
                this.registerToGame(player, gameName);
            }
            else{
                player.update(new RefusedActionMessage(ErrorType.NAME_ALREADY_IN_USE,
                                                       "Game name " + gameName + " is already in use!"));
                player.update(new AvailableGamesMessage(findAvailableGames()));
            }
        }
    }

    public synchronized void createGame(String gameName, int numPlayer, ClientHandler player) throws IllegalArgumentException {
        this.createGame(gameName, numPlayer, player, new Random().nextLong());
    }

    private void checkPlayer(ClientHandler player){
        synchronized(this.playerInfo) {
            if (!this.playerInfo.containsKey(player.getName())) {
                throw new IllegalArgumentException();
            }
            if (this.playerInfo.get(player.getName()) != null) {
                throw new IllegalArgumentException();
            }
        }
    }

    public void registerToFirstAvailableGame(ClientHandler player){
        checkPlayer(player);
        registerToGame(player, this.findAvailableGames().getFirst());
    }

    public void registerToGame(ClientHandler player, String gameName) throws IllegalArgumentException{
        //Possible exceptions: player not found, game not found, game already full, player already registered to other games...?
        checkPlayer(player);
        synchronized(this.gamesInfo) {
            if (!this.gamesInfo.containsKey(gameName)) {
                throw new IllegalArgumentException();
            }
            if (this.gamesInfo.get(gameName).x().getNumPlayers() == this.gamesInfo.get(gameName).x().getNumJoinedPlayer()) {
                throw new IllegalArgumentException();
            }
        }
        GameController gameControllerToJoin;
        gameControllerToJoin = this.gamesInfo.get(gameName).y();
        this.playerInfo.put(player.getName(), gameControllerToJoin);
        gameControllerToJoin.addClient(player.getName(), player); //Problems?
        //join game message?
    }

    private ArrayList<String> findAvailableGames(){
        synchronized(this.gamesInfo) {
            return this.gamesInfo.entrySet()
                                 .stream()
                                 .filter(e -> e.getValue().x().getGameState() == GameState.SETUP && e.getValue().x().getNumPlayers() != e.getValue().x().getNumJoinedPlayer())
                                 .map(Map.Entry::getKey)
                                 .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
    }

    public void makeMove(ClientHandler player, String cardToInsert, String anchorCard, Direction directionToInsert) {
        if(!this.playerInfo.containsKey(player.getName())){
            player.update(new RefusedActionMessage(ErrorType.GENERIC,
                                                   "Player " + player.getName() + " is not in the game!"));
        }
        GameController temp = this.playerInfo.get(player.getName());
        temp.placeCard(player.getName(), cardToInsert, anchorCard, directionToInsert, CardOrientation.UP);
    }

    public void setInitialCard(ClientHandler player, CardOrientation cardOrientation){
        if(!this.playerInfo.containsKey(player.getName())){
            player.update(new RefusedActionMessage(ErrorType.GENERIC,
                                                   "Player " + player.getName() + " is not in the game!"));
        }
        GameController temp = this.playerInfo.get(player.getName());
        temp.placeInitialCard(player.getName(), cardOrientation);
    }

    public void pickCardFromTable(ClientHandler player, PlayableCardType cardType, int position){
        if(!this.playerInfo.containsKey(player.getName())){
            player.update(new RefusedActionMessage(ErrorType.GENERIC,
                                                   "Player " + player.getName() + " is not in the game!"));
        }
        this.playerInfo.get(player.getName()).drawCardFromTable(player.getName(), cardType, position);
    }

    public void pickCardFromDeck(ClientHandler player, PlayableCardType cardType){
        if(!this.playerInfo.containsKey(player.getName())){
            player.update(new RefusedActionMessage(ErrorType.GENERIC,
                                                   "Player " + player.getName() + " is not in the game!"));
        }
        this.playerInfo.get(player.getName()).drawCardFromDeck(player.getName(), cardType);
    }

    public void choosePrivateGoalCard(ClientHandler player, int cardIdx){
        if(!this.playerInfo.containsKey(player.getName())){
            player.update(new RefusedActionMessage(ErrorType.GENERIC,
                                                   "Player " + player.getName() + " is not in the game!"));
        }
        this.playerInfo.get(player.getName()).choosePrivateGoal(player.getName(), cardIdx);
    }

    public void chooseColor(ClientHandler player, Color color){
        if(!this.playerInfo.containsKey(player.getName())){
            player.update(new RefusedActionMessage(ErrorType.GENERIC,
                                                   "Player " + player.getName() + " is not in the game!"));
        }
        this.playerInfo.get(player.getName()).chooseColor(player.getName(), color);
    }

    public void placeInitialCard(ClientHandler player, CardOrientation cardOrientation){
        if(!this.playerInfo.containsKey(player.getName())){
            player.update(new RefusedActionMessage(ErrorType.GENERIC,
                                                   "Player " + player.getName() + " is not in the game!"));
        }
        this.playerInfo.get(player.getName()).placeInitialCard(player.getName(), cardOrientation);
    }

    /*
     * Check if Player Exists, Check if there is game associated, if
     * Yes, send the state of the Game, if no, send list of nonActive games that it can join.
     * */
    public void reconnect(ClientHandler clientHandler, String gameName){
        if(!this.playerInfo.containsKey(clientHandler.getName())){
            //clientHandler.sendMessageToClient();
        }
        if(!this.gamesInfo.containsKey(gameName)){
            clientHandler.update(new AvailableGamesMessage(findAvailableGames()));
        }
        if(this.gamesInfo.get(gameName).x().getPlayers().stream().map(Player::getName).noneMatch(n -> n.equals(gameName))){
            //clientHandler.sendMessageToClient()
        }
        this.gamesInfo.get(gameName).y().addClient(clientHandler.getName(), clientHandler);
    }

    public void sendChatMessage(ClientHandler player, ArrayList<String> usersToSend, String messageToSend){
        if(!this.playerInfo.containsKey(player.getName())){
            player.update(new RefusedActionMessage(ErrorType.GENERIC,
                                                   "Player " + player.getName() + " is not in the game!"));
        }
        GameController temp = this.playerInfo.get(player.getName());
        temp.sendChatMessage(usersToSend, player.getName(), messageToSend);
    }

}