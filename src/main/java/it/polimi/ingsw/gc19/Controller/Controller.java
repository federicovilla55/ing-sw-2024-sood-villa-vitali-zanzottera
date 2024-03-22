package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Model.Game.NameAlreadyInUseException;

import java.io.IOException;
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

    public void CreateGame(ClientPlayer new_player, String GameName, int num_player, String creatorName) throws IOException { //gestire l'eccezzione
        if(CheckAlreadyExist(GameName)){
            throw new IllegalArgumentException("Name already in use");
        }
        GameController temp = new GameController(num_player);
        new_player.gamePlay = GameName;
        nonActiveGames.add(GameName);
        mapIdtoController.put(GameName, temp);
        //mapIdtoController.get(GameName).gameAssociated.createNewPlayer(creatorName, new_player);
    }

    public void JoinGame(ClientPlayer player, String gameToJoin, String nickToJoin) {
        if(activeGames.contains(gameToJoin) || (!nonActiveGames.contains(gameToJoin) && !activeGames.contains(gameToJoin))){
            throw new IllegalStateException("Cannot join this game anymore");
        }
        try {
            player.gamePlay = gameToJoin;
            mapIdtoController.get(gameToJoin).gameAssociated.createNewPlayer(nickToJoin, player);
            player.setNickname(nickToJoin);
            if(mapIdtoController.get(gameToJoin).gameAssociated.getNumPlayers() == mapIdtoController.get(gameToJoin).gameAssociated.getNumJoinedPlayer())
            {
                Start_Game(gameToJoin);
            }
        }
        catch (NameAlreadyInUseException e)
        {
            throw new IllegalArgumentException("Name already in use");
        }
    }
    public void Start_Game(String gameToStart)
    {
        nonActiveGames.remove(gameToStart);
        activeGames.add(gameToStart);
        mapIdtoController.get(gameToStart).StartGame();
    }

    public void Move() {

    }
}
