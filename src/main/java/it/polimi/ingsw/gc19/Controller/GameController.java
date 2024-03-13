package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Model.Game.Game;

public class GameController {

    Game gameAssociated;

    GameController(int num_player)
    {
        this.gameAssociated = new Game(num_player);
    }
    public void StartGame(){

    }

}
