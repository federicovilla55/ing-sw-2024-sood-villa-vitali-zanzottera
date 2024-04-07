package it.polimi.ingsw.gc19.Networking.Server.Message.Turn;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public interface TurnStateMessageVisitor{
    void visit(TurnStateMessage message);

}
