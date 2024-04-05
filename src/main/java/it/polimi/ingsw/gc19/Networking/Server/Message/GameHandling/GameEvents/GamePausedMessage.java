package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameEvents;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public class GamePausedMessage extends NotifyEventOnGame{

    @Override
    public void visit(MessageToClientVisitor visitor) {
        visitor.visit(this);
    }

}
