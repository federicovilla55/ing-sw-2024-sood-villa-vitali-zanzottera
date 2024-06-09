package it.polimi.ingsw.gc19.Networking.Server.Message.HeartBeat;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is used by server to send heartbeat to client
 */
public class ServerHeartBeatMessage extends MessageToClient {

    /**
     * Implementation of the visitor pattern
     * @param visitor {@link MessageToClientVisitor} visitor of the message
     */
    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof HeartBeatMessageVisitor) ((HeartBeatMessageVisitor) visitor).visit(this);
    }

}