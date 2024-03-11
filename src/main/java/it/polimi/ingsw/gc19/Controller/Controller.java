package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Model.Game.Game;

import java.util.*;

public class Controller {
    List<Game> activeGames;
    List<Game> nonActiveGames;
    Map<ClientPlayer, Game> clientToGame;
    Controller()
    {
       clientToGame = new HashMap<ClientPlayer, Game>();
    }
    void CreateGame(ClientPlayer new_player, Game gameToCreate)
    {
        clientToGame.put(new_player,gameToCreate);
        nonActiveGames.add(gameToCreate);
    }

    void JoinGame(ClientPlayer player, Game gameToJoin)
    {
        clientToGame.put(player,gameToJoin);
    }

    void ExitNonActiveGame(ClientPlayer player, Game gameToExit)
    {

    }

    void ExitActiveGame(ClientPlayer player, Game gameToExit)
    {

    }

    
}
