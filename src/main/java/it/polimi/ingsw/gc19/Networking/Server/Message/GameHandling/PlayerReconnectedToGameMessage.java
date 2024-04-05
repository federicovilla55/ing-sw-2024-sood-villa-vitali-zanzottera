package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameEvents.NotifyEventOnGame;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageVisitor;

public class PlayerReconnectedToGameMessage extends GameHandlingMessage {
    private final String playerName;

    public PlayerReconnectedToGameMessage(String playerName){
        this.playerName = playerName;
    }

    public String getPlayerName(){
        return this.playerName;
    }

    @Override
    public void visit(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
