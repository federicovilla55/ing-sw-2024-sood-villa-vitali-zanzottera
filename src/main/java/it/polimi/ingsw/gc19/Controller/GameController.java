package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Model.Game;

import java.io.IOException;

public class GameController {

    Game gameAssociated;


    GameController(int num_player) throws IOException {
        this.gameAssociated = new Game(num_player);
    }
    public void StartGame(){
        gameAssociated.startGame();
        //NotifyClientToPlay(gameAssociated.getFirstPlayer().getClient());
    }

    public void insertCard() {

    }

    public void drawACard()
    {

    }

    public void setNextPlayer()
    {

    }

    public void calculateFinalResult()
    {

    }

    public void finalTurn()
    {

    }

    public void NotifyClientToPlay(ClientPlayer ClientToNotify)
    {

    }
}
