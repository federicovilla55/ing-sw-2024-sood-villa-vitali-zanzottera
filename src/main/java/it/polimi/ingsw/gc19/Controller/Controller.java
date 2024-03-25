package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Model.Game.NameAlreadyInUseException;

import java.io.IOException;
import java.util.*;

public class Controller {
    List<String> activeGames;
    List<String> nonActiveGames;
    List<String> Players;
    List<String> ActivePlayers;
    List<String> NonActivePlayers;
    Map<String, GameController> PlayerToGameController;
    Map<String, GameController> GameNameToController;

    private final Object PlayerLock;

    public Controller()
    {
        Players = new ArrayList<String>();
        ActivePlayers = new ArrayList<String>();
        NonActivePlayers = new ArrayList<String>();
        PlayerToGameController = new HashMap<String, GameController>();
        activeGames = new ArrayList<String>();
        nonActiveGames = new ArrayList<String>();
        this.PlayerLock = new Object();
    }

    private boolean checkAlreadyExist(String gameToCheck){
        for(String games : activeGames) {
            if(gameToCheck.equals(games)){return true;}
        }
        for(String games : nonActiveGames){
            if(gameToCheck.equals(games)) {return true; }
        }
        return false;
    }

    public boolean NewClient(String userName)
    {
        synchronized (PlayerLock) {
            if (Players.contains(userName)) {
                return false;
            } else {
                Players.add(userName);
                return true;
            }
        }
    }
    public void SetToNonActive(String nickName)
    {
        //ActivePlayers.remove(nickName);
        //NonActivePlayers.add(nickName);
        if(PlayerToGameController.containsKey(nickName)) {
            PlayerToGameController.get(nickName).removeClient(nickName);
        }
    }

    public void SetToActive(String nickName)
    {
        //NonActivePlayers.remove(nickName);
        //ActivePlayers.add(nickName);
        if(PlayerToGameController.containsKey(nickName)) {
            PlayerToGameController.get(nickName).addClient(nickName);
        }
    }

    public void createGame(String PlayerNickname, String gameName, int numPlayer) throws IOException {
        if(checkAlreadyExist(gameName)){
            throw new IllegalArgumentException("Name already in use");
        }
        Game tempName = new Game(numPlayer);
        GameController temp = new GameController(tempName);
        nonActiveGames.add(gameName);
        PlayerToGameController.put(PlayerNickname, temp);
        GameNameToController.put(gameName, temp);
    }

    public void joinGame(String player, String gameToJoin, String nickToJoin) {
        if(activeGames.contains(gameToJoin) || (!nonActiveGames.contains(gameToJoin) && !activeGames.contains(gameToJoin))){
            throw new IllegalStateException("Cannot join this game anymore");
        }
    }

    public void makeMove(String nickName, String cardToInsert, String anchorCard, Direction directionToInsert) {
        GameController temp = PlayerToGameController.get(nickName);
        temp.placeCard(nickName, cardToInsert, anchorCard, directionToInsert);
    }
    public void setInitialCard(String nickName, CardOrientation cardOrientation){
        GameController temp = PlayerToGameController.get(nickName);
        temp.placeInitialCard(nickName, cardOrientation);
    }

    /*
    * Check if Player Exists, Check if there is game associated, if
    * Yes, send the state of the Game, if no, send list of nonActive games that it can join.
    * */
    public void Recconect() {

    }
}
