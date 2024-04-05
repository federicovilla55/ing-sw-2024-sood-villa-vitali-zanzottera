package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameEvents;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageVisitor;

public class GamePausedMessage extends NotifyEventOnGame{

    @Override
    public void visit(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
