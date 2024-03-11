package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Model.Game.Game;

public class ClientPlayer {
    private final String name;
    private final String password;

    public Game gamePlay;

    ClientPlayer(String name, String password){
        this.name = name;
        this.password = password;
        gamePlay = null;
    }
    String getName(){
        return this.name;
    }
    String getEncrypedPass()
    {
        return this.password;
    }
}
