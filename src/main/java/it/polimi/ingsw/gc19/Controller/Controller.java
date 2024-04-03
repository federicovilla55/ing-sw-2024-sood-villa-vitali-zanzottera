package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.State;
import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Model.Tuple;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;

import java.io.IOException;
import java.util.*;

public class Controller{

    private static Controller controller = null;

    private final HashMap<String, Tuple<State, GameController>> playerInfo;
    private final HashMap<String, Tuple<Game, GameController>> gamesInfo;

    private final Object PlayerLock;

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
        this.PlayerLock = new Object();
    }


    private boolean checkAlreadyExist(String gameToCheck){
        return this.gamesInfo.containsKey(gameToCheck);
    }

    public boolean clientNameAlreadyTaken(String userName){
        return this.playerInfo.containsKey(userName);
    }

    public synchronized boolean createClient(String playerNickname, ClientHandler client){
        if(!this.playerInfo.containsKey(playerNickname)){
            this.playerInfo.put(playerNickname, new Tuple<>(State.ACTIVE,null));
            return true;
        }
        return false;
    }

    public synchronized void createGame(String gameName, int numPlayer) throws IllegalArgumentException {
        Game gameToBuild = null;
        if (!this.gamesInfo.containsKey(gameName)) {
            try {
                gameToBuild = new Game(numPlayer);
            } catch (IOException exception) {
                //@TODO: handle this exception
            }
            GameController gameController = new GameController(gameToBuild);
            this.gamesInfo.put(gameName, new Tuple<>(gameToBuild, gameController));

        }
    }

    public void registerToGame(ClientHandler player, String gameName){
        //Possible exceptions: player not found, game not found, game already full, player already registered to other games...?
        if(!this.playerInfo.containsKey(player.getName())){
            throw new IllegalArgumentException();
        }
        if(!this.gamesInfo.containsKey(gameName)){
            throw new IllegalArgumentException();
        }
        if(this.gamesInfo.get(gameName).x().getNumPlayers() == this.gamesInfo.get(gameName).x().getNumJoinedPlayer()){
            throw new IllegalArgumentException();
        }
        if(this.playerInfo.get(player.getName()).y() != null){
            throw new IllegalArgumentException();
        }

        GameController gameControllerToJoin = this.gamesInfo.get(gameName).y();
        this.playerInfo.put(player.getName(), new Tuple<>(State.ACTIVE, gameControllerToJoin));
        gameControllerToJoin.addClient(player.getName(), player);
    }

    public void makeMove(String nickName, String cardToInsert, String anchorCard, Direction directionToInsert) {
        GameController temp = this.playerInfo.get(nickName).y();
        temp.placeCard(nickName, cardToInsert, anchorCard, directionToInsert, CardOrientation.UP);
    }

    public void setInitialCard(String nickName, CardOrientation cardOrientation){
        GameController temp = this.playerInfo.get(nickName).y();
        temp.placeInitialCard(nickName, cardOrientation);
    }

    /*
     * Check if Player Exists, Check if there is game associated, if
     * Yes, send the state of the Game, if no, send list of nonActive games that it can join.
     * */
    public void Recconect() {

    }

    public void sendChatMessage(String nickName, ArrayList<String> usersToSend, String messageToSend){
        GameController temp = this.playerInfo.get(nickName).y();
        temp.sendChatMessage(usersToSend, nickName, messageToSend);
    }

}
