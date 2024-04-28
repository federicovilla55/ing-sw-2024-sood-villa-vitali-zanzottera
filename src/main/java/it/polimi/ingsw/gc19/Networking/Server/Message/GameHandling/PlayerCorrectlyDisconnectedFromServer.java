package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is used by server to notify client
 * that it has been correctly disconnected by the server (e.g.
 * its nickname has been deleted in server)
 */
public class PlayerCorrectlyDisconnectedFromServer extends GameHandlingMessage{
    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof GameHandlingMessageVisitor) ((GameHandlingMessageVisitor) visitor).visit(this);
    }

}
