package it.polimi.ingsw.gc19.Networking.Client.Message.Heartbeat;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

public class HeartBeatMessage extends MessageToServer{

    protected HeartBeatMessage(String nickname) {
        super(nickname);
    }

    @Override
    public void accept(MessageToServerVisitor visitor) {
        if(visitor instanceof HeartBeatMessageVisitor) ((HeartBeatMessageVisitor) visitor).visit(this);
    }

}
