package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Model.Game.Game;

public class ClientPlayer {
    private final String name;
    private final String SecretKey;
    public Game gamePlay;

    ClientPlayer(String name, String password){
        this.name = name;
        this.SecretKey = "ciao";
        gamePlay = null;
    }
    String getName(){
        return this.name;
    }
}
