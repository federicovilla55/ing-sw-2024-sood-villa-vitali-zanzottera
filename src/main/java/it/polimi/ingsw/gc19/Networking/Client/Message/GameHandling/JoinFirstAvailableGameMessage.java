package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

public class JoinFirstAvailableGameMessage extends GameHandlingMessage{

    public JoinFirstAvailableGameMessage(String nickname) {
        super(nickname);
    }

    @Override
    public void accept(MessageToServerVisitor visitor) {
        if(visitor instanceof GameHandlingMessageVisitor) ((GameHandlingMessageVisitor) visitor).visit(this);
    }

}
