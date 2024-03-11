package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Model.Game.Game;

import java.util.*;

public class Controller {
    List<Game> activeGames;
    List<Game> nonActiveGames;
    Controller()
    {

    }
    void CreateGame(ClientPlayer new_player, Game gameToCreate)
    {
        new_player.gamePlay = gameToCreate;
        nonActiveGames.add(gameToCreate);
    }

    void JoinGame(ClientPlayer player, Game gameToJoin)
    {
        player.gamePlay = gameToJoin;
    }

    void ExitNonActiveGame(ClientPlayer player, Game gameToExit)
    {
        player.gamePlay = null;
        //If game does not have players, game will be eliminated
    }

    void ExitActiveGame(ClientPlayer player, Game gameToExit)
    {

    }

    void Start_Game(Game gameToStart)
    {
        nonActiveGames.remove(gameToStart);
        activeGames.add(gameToStart);
    }

}
