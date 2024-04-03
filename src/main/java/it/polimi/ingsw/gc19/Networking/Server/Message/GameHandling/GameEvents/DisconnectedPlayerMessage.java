package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameEvents;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;

public class DisconnectedPlayerMessage extends GameHandlingMessage{

    private final String removedNick;

    public DisconnectedPlayerMessage(String removedNick){
        this.removedNick = removedNick;
    }

    public String getRemovedNick(){
        return this.removedNick;
    }

}
