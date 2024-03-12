package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Model.Game.Game;

import java.util.*;

public class Controller {
    List<Game> activeGames;
    List<Game> nonActiveGames;
    Controller()
    {

    }

    private boolean CheckAlreadyExist(Game gameToCheck){
        for(Game games : activeGames) {
            if(gameToCheck.getName().equals(games.getName())){
                return true;
            }
        }
        for(Game games : nonActiveGames){
            if(gameToCheck.getName().equals(games.getName())) {
                return true;
            }
        }
        return false;
    }

    public void CreateGame(ClientPlayer new_player, Game gameToCreate)
    {
        if(CheckAlreadyExist(gameToCreate)){
            return; //if with same name already exist launch exception
        }
        new_player.gamePlay = gameToCreate;
        nonActiveGames.add(gameToCreate);
    }

    public void JoinGame(ClientPlayer player, Game gameToJoin)
    {
        player.gamePlay = gameToJoin;
    }

    public void ExitNonActiveGame(ClientPlayer player, Game gameToExit)
    {
        player.gamePlay = null;
        //If game does not have players, game will be eliminated
    }

    public void ExitActiveGame(ClientPlayer player, Game gameToExit)
    {

    }

    public void Start_Game(Game gameToStart)
    {
        nonActiveGames.remove(gameToStart);
        activeGames.add(gameToStart);
    }

}
