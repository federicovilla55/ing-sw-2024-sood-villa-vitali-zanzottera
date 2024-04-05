package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageVisitor;

public class DisconnectedPlayerMessage extends GameHandlingMessage{

    private final String removedNick;

    public DisconnectedPlayerMessage(String removedNick){
        this.removedNick = removedNick;
    }

    public String getRemovedNick(){
        return this.removedNick;
    }

    @Override
    public void visit(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
