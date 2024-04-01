package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameEvents;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;

public class CreatedGameMessage extends GameHandlingMessage{

    private final String gameName;

    public CreatedGameMessage(String gameName){
        this.gameName = gameName;
    }

    public String getGameName() {
        return this.gameName;
    }

}
