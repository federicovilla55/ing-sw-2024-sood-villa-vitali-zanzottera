package it.polimi.ingsw.gc19.Controller;

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

    Controller()
    {
        Players = new ArrayList<String>();
        ActivePlayers = new ArrayList<String>();
        NonActivePlayers = new ArrayList<String>();
        PlayerToGameController = new HashMap<String, GameController>();
        activeGames = new ArrayList<String>();
        nonActiveGames = new ArrayList<String>();
    }

    private boolean checkAlreadyExist(String gameToCheck){
        for(String games : activeGames) {
            if(gameToCheck.equals(games)){
                return true;
            }
        }
        for(String games : nonActiveGames){
            if(gameToCheck.equals(games)) {
                return true;
            }
        }
        return false;
    }

    public void NewClient(String userName)
    {
        if(ActivePlayers.contains(userName))
        {
            //throw exception
        }
        else{
            ActivePlayers.contains(userName);
        }
    }

    public void SetToNonActive(String nickName)
    {
        ActivePlayers.remove(nickName);
        NonActivePlayers.add(nickName);
    }

    public void SetToActive(String nickName)
    {
        NonActivePlayers.remove(nickName);
        ActivePlayers.add(nickName);
    }

    public void createGame(String firstPlayerNickname, String gameName, int numPlayer, String creatorName) throws IOException { //gestire l'eccezzione
        if(checkAlreadyExist(gameName)){
            throw new IllegalArgumentException("Name already in use");
        }
        Game tempName = new Game(numPlayer);
        GameController temp = new GameController(tempName);
        nonActiveGames.add(gameName);
        //mapIdToController.put(gameName, temp);
        //mapIdtoController.get(gameName).gameAssociated.createNewPlayer(creatorName, new_player);
    }

    public void joinGame(ClientPlayer player, String gameToJoin, String nickToJoin) {
        if(activeGames.contains(gameToJoin) || (!nonActiveGames.contains(gameToJoin) && !activeGames.contains(gameToJoin))){
            throw new IllegalStateException("Cannot join this game anymore");
        }
        try {
            //mapIdToController.get(gameToJoin).gameAssociated.createNewPlayer(nickToJoin);
            //player.setNickname(nickToJoin);
        }
        catch (NameAlreadyInUseException e)
        {
            throw new IllegalArgumentException("Name already in use");
        }
    }

    public void move() {

    }
}
