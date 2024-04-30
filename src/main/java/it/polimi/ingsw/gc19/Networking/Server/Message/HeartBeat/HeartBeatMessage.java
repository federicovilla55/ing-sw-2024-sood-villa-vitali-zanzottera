package it.polimi.ingsw.gc19.Networking.Server.Message.HeartBeat;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public class HeartBeatMessage extends MessageToClient {

    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof HeartBeatMessageVisitor) ((HeartBeatMessageVisitor) visitor).visit(this);
    }

}
