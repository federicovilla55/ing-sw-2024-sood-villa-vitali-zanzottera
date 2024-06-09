package it.polimi.ingsw.gc19.Networking.Client.Message.Heartbeat;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

/**
 * This message is used by client to send heartbeat to server
 */
public class ClientHeartBeatMessage extends MessageToServer{

    public ClientHeartBeatMessage(String nickname) {
        super(nickname);
    }

    /**
     * This method is used by server to handle a {@link ClientHeartBeatMessage} from client.
     * @param visitor the {@link MessageToServerVisitor} for the message
     */
    @Override
    public void accept(MessageToServerVisitor visitor) {
        if(visitor instanceof HeartBeatMessageVisitor) ((HeartBeatMessageVisitor) visitor).visit(this);
    }

}