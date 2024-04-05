package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageVisitor;

public class BeginFinalRoundMessage extends GameHandlingMessage{

    @Override
    public void visit(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
