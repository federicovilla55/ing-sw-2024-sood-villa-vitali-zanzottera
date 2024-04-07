package it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public class DisconnectedPlayerMessage extends NotifyEventOnGame {

    private final String removedNick;

    public DisconnectedPlayerMessage(String removedNick){
        this.removedNick = removedNick;
    }

    public String getRemovedNick(){
        return this.removedNick;
    }

    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof GameHandlingMessageVisitor) ((GameHandlingMessageVisitor) visitor).visit(this);
    }


}
