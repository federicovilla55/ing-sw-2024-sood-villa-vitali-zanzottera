package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public class BeginFinalRoundMessage extends GameHandlingMessage{

    @Override
    public void visit(MessageToClientVisitor visitor) {
        visitor.visit(this);
    }

}
