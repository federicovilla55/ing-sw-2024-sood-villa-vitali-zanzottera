package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;

public class CreateNewGameMessage implements GameHandlingMessage{

    private final String gameName;
    private final int numPlayer;

    public CreateNewGameMessage(String gameName, int numPlayer){
        this.gameName = gameName; //@TODO: add game name to Game class
        this.numPlayer = numPlayer;
    }

    public String getGameName(){
        return this.gameName;
    }

    public int getNumPlayer(){
        return this.numPlayer;
    }
}
