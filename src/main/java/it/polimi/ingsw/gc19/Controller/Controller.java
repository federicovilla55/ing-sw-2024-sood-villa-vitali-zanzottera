package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Model.Player.NameAlreadyInUseException;

import java.util.*;

public class Controller {
    List<String> activeGames;
    List<String> nonActiveGames;

    Map<String, GameController> mapIdtoController;
    Controller()
    {
        mapIdtoController = new HashMap<String, GameController>();
    }

    private boolean CheckAlreadyExist(String gameToCheck){
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

    public void CreateGame(ClientPlayer new_player, String GameName, int num_player)
    {
        if(CheckAlreadyExist(GameName)){
            throw new IllegalArgumentException("Name already in use");
        }
        GameController temp = new GameController(num_player);
        new_player.gamePlay = GameName;
        nonActiveGames.add(GameName);
        mapIdtoController.put(GameName, temp);
    }

    public void JoinGame(ClientPlayer player, String gameToJoin, String nickToJoin) {
        if(activeGames.contains(gameToJoin) || (!nonActiveGames.contains(gameToJoin) && !activeGames.contains(gameToJoin))){
            throw new IllegalStateException("Cannot join this game anymore");
        }
        try {
            player.gamePlay = gameToJoin;
            gameToJoin.createNewPlayer(nickToJoin);
            player.setNickname(nickToJoin);
            if(gameToJoin.getNumPlayers() == gameToJoin.getNumJoinedPlayer())
            {
                Start_Game(gameToJoin);
            }
        }
        catch (NameAlreadyInUseException e)
        {
            throw new IllegalArgumentException("Name already in use");
        }
    }

    public void Start_Game(Game gameToStart)
    {
        nonActiveGames.remove(gameToStart);
        activeGames.add(gameToStart);

    }

}
