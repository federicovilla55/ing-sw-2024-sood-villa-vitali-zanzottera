package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Model.Game.Game;

import java.io.IOException;
import java.util.*;

public class Controller {
    List<String> Players;
    Map<String, GameController> GameNameToController;
    CurrentGameStructure gameStructure;

    private final Object PlayerLock;

    public ArrayList<String> getActivePlayer(){
        return gameStructure.getActivePlayers();
    }

    public Controller()
    {
        gameStructure = new CurrentGameStructure();
        this.PlayerLock = new Object();
    }

    private boolean checkAlreadyExist(String gameToCheck){
        return gameStructure.checkGameAlreadyExist(gameToCheck);
    }

    public boolean NewClient(String userName){
        synchronized (PlayerLock) {
            if (Players.contains(userName)) {
                return false;
            } else {
                Players.add(userName);
                return true;
            }
        }
    }

    public void createGame(String PlayerNickname, String gameName, int numPlayer) throws IOException { //chiedere per gameName
        if(checkAlreadyExist(gameName)){
            throw new IllegalArgumentException("Name already in use");
        }
        Game tempGame = new Game(numPlayer);
        GameController temp = new GameController(tempGame);
        gameStructure.insertGame(gameName, tempGame);
        gameStructure.insertGameControllerForPlayer(PlayerNickname, temp);
        GameNameToController.put(gameName, temp);
    }

    public void joinGame(String player, String gameName) {
        Game gameToJoin = gameStructure.getGameFromName(gameName);
        GameController gameController = gameStructure.getGameControllerFromPlayer(gameToJoin.getPlayers().getFirst().getName());
        gameController.addClient(player);
    }

    public void makeMove(String nickName, String cardToInsert, String anchorCard, Direction directionToInsert) {
        GameController temp = gameStructure.getGameControllerFromPlayer(nickName);
        temp.placeCard(nickName, cardToInsert, anchorCard, directionToInsert, CardOrientation.UP);
    }

    public void setInitialCard(String nickName, CardOrientation cardOrientation){
        GameController temp = gameStructure.getGameControllerFromPlayer(nickName);
        temp.placeInitialCard(nickName, cardOrientation);
    }

    /*
    * Check if Player Exists, Check if there is game associated, if
    * Yes, send the state of the Game, if no, send list of nonActive games that it can join.
    * */
    public void Recconect() {

    }


}
