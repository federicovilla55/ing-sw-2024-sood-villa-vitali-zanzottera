package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Model.Game.Player;
import it.polimi.ingsw.gc19.Model.Tuple;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.ErrorType;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.AvailableGamesMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameEvents.CreatedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Controller{

    private static Controller controller = null;

    private final HashMap<String, GameController> playerInfo;
    private final HashMap<String, Tuple<Game, GameController>> gamesInfo;

    public static Controller getController(){
        if(controller == null){
            controller = new Controller();
            return controller;
        }
        return controller;
    }

    private Controller(){
        this.gamesInfo = new HashMap<>();
        this.playerInfo = new HashMap<>();
    }

    public boolean createClient(ClientHandler player){
        synchronized(this.playerInfo) {
            if(!this.playerInfo.containsKey(player.getName())){
                this.playerInfo.put(player.getName(), null);
                player.sendMessageToClient(new CreatedPlayerMessage(player.getName()));
                return true;
            }
            else{
                //send error message to player
                return false;
            }
        }
    }

    public void setPlayerInactive(String nickname){
        synchronized(this.playerInfo){
            this.playerInfo.get(nickname).removeClient(nickname);
        }
    }

    public void setPlayerInactive(ClientHandler client){
        synchronized(this.playerInfo){
            if(!this.playerInfo.containsKey(client.getName())){
                client.sendMessageToClient(new RefusedActionMessage(ErrorType.GENERIC,
                                                                    "You are not a player of some games!"));
            }
            this.playerInfo.get(client.getName()).removeClient(client.getName());
        }
    }

    public synchronized void createGame(String gameName, int numPlayer, ClientHandler player) throws IllegalArgumentException {
        Game gameToBuild = null;
        synchronized(this.gamesInfo) {
            if (!this.gamesInfo.containsKey(gameName)) {
                try {
                    gameToBuild = new Game(numPlayer);
                } catch (IOException exception) {
                    //@TODO: handle this exception
                }
                GameController gameController = new GameController(gameToBuild);
                this.gamesInfo.put(gameName, new Tuple<>(gameToBuild, gameController));
                player.sendMessageToClient(new CreatedGameMessage(gameName));
                this.registerToGame(player, gameName);
            }
            else{
                player.sendMessageToClient(new RefusedActionMessage(ErrorType.NAME_ALREADY_IN_USE,
                                                                    "Game name " + gameName + " is already in use!"));
                player.sendMessageToClient(new AvailableGamesMessage(new ArrayList<>(findNonActiveGames())));
            }
        }
    }

    public void registerToGame(ClientHandler player, String gameName) throws IllegalArgumentException{
        //Possible exceptions: player not found, game not found, game already full, player already registered to other games...?
        GameController gameControllerToJoin;
        synchronized(this.playerInfo) {
            if (!this.playerInfo.containsKey(player.getName())) {
                throw new IllegalArgumentException();
                //return
            }
            if (this.playerInfo.get(player.getName()) != null) {
                throw new IllegalArgumentException();
            }
        }
        synchronized(this.gamesInfo) {
            if (!this.gamesInfo.containsKey(gameName)) {
                throw new IllegalArgumentException();
            }
            if (this.gamesInfo.get(gameName).x().getNumPlayers() == this.gamesInfo.get(gameName).x().getNumJoinedPlayer()) {
                throw new IllegalArgumentException();
            }
        }
        gameControllerToJoin = this.gamesInfo.get(gameName).y();
        this.playerInfo.put(player.getName(), gameControllerToJoin);
        gameControllerToJoin.addClient(player.getName(), player); //Problems?
        //join game message?
    }

    private Set<String> findNonActiveGames(){
        synchronized(this.gamesInfo) {
            return this.gamesInfo.entrySet()
                                 .stream()
                                 .filter(e -> e.getValue().x().getNumJoinedPlayer() == e.getValue().x().getNumPlayers())
                                 .map(Map.Entry::getKey)
                                 .collect(Collectors.toSet());
        }
    }

    public void makeMove(ClientHandler player, String cardToInsert, String anchorCard, Direction directionToInsert) {
        if(!this.playerInfo.containsKey(player.getName())){
            player.sendMessageToClient(new RefusedActionMessage(ErrorType.GENERIC,
                                                                "Player " + player.getName() + " is not in the game!"));
            return;
        }
        GameController temp = this.playerInfo.get(player.getName());
        temp.placeCard(player.getName(), cardToInsert, anchorCard, directionToInsert, CardOrientation.UP);
    }

    public void setInitialCard(ClientHandler player, CardOrientation cardOrientation){
        if(!this.playerInfo.containsKey(player.getName())){
            player.sendMessageToClient(new RefusedActionMessage(ErrorType.GENERIC,
                                                                "Player " + player.getName() + " is not in the game!"));
            return;
        }
        GameController temp = this.playerInfo.get(player.getName());
        temp.placeInitialCard(player.getName(), cardOrientation);
    }

    public void pickCardFromTable(ClientHandler player, PlayableCardType cardType, int position){
        if(!this.playerInfo.containsKey(player.getName())){
            player.sendMessageToClient(new RefusedActionMessage(ErrorType.GENERIC,
                                                                "Player " + player.getName() + " is not in the game!"));
            return;
        }
        this.playerInfo.get(player.getName()).drawCardFromTable(player.getName(), cardType, position);
    }

    public void pickCardFromDeck(ClientHandler player, PlayableCardType cardType){
        if(!this.playerInfo.containsKey(player.getName())){
            player.sendMessageToClient(new RefusedActionMessage(ErrorType.GENERIC,
                                                                "Player " + player.getName() + " is not in the game!"));
            return;
        }
        this.playerInfo.get(player.getName()).drawCardFromDeck(player.getName(), cardType);
    }

    /*
     * Check if Player Exists, Check if there is game associated, if
     * Yes, send the state of the Game, if no, send list of nonActive games that it can join.
     * */
    public void reconnect(ClientHandler clientHandler, String gameName){
        if(!this.playerInfo.containsKey(clientHandler.getName())){
            //clientHandler.sendMessageToClient();
            return;
        }
        if(!this.gamesInfo.containsKey(gameName)){
            clientHandler.sendMessageToClient(new AvailableGamesMessage(new ArrayList<>(findNonActiveGames())));
            return;
        }
        if(this.gamesInfo.get(gameName).x().getPlayers().stream().map(Player::getName).noneMatch(n -> n.equals(gameName))){
            //clientHandler.sendMessageToClient()
            return;
        }
        this.gamesInfo.get(gameName).y().addClient(clientHandler.getName(), clientHandler);
    }

    public void sendChatMessage(ClientHandler player, ArrayList<String> usersToSend, String messageToSend){
        if(!this.playerInfo.containsKey(player.getName())){
            player.sendMessageToClient(new RefusedActionMessage(ErrorType.GENERIC,
                                                                "Player " + player.getName() + " is not in the game!"));
            return;
        }
        GameController temp = this.playerInfo.get(player.getName());
        temp.sendChatMessage(usersToSend, player.getName(), messageToSend);
    }

}
