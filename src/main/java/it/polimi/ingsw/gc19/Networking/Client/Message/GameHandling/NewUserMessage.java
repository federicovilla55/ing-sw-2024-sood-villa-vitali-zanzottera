package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is used by client to tells server
 * they would like to create a new player with the specified name.
 */
public class NewUserMessage extends GameHandlingMessage {

    public NewUserMessage(String nickname){
        super(nickname);
    }

    /**
     * This method is used by {@link MessageToServerVisitor} to visit the message.
     * @param visitor the {@link MessageToServerVisitor} for the message
     */
    @Override
    public void accept(MessageToServerVisitor visitor) {
        if(visitor instanceof GameHandlingMessageVisitor) ((GameHandlingMessageVisitor) visitor).visit(this);
    }

}
