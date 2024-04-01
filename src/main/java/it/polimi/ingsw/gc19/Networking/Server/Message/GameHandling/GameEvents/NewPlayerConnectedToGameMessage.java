package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameEvents;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;

public class NewPlayerConnectedToGameMessage extends GameHandlingMessage {

    private final String playerName;

    public NewPlayerConnectedToGameMessage(String playerName){
        this.playerName = playerName;
    }

    public String getPlayerName(){
        return this.playerName;
    }

}
