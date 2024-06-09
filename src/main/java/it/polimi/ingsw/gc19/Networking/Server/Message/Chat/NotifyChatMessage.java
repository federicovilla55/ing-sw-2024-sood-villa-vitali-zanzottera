package it.polimi.ingsw.gc19.Networking.Server.Message.Chat;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is used by server to notify players that a new chat message
 * has arrived
 */
public class NotifyChatMessage extends MessageToClient{

    /**
     * Sender player
     */
    private final String sender;

    /**
     * Content of the message
     */
    private final String message;

    public NotifyChatMessage(String sender, String message){
        super();
        this.sender = sender;
        this.message = message;
    }

    /**
     * Getter for nickname of sender player
     * @return nickname of player who sent the message
     */
    public String getSender(){
        return this.sender;
    }

    /**
     * Getter for message's content
     * @return the message content
     */
    public String getMessage(){
        return this.message;
    }

    /**
     * Implementation of the visitor pattern
     * @param visitor {@link MessageToClientVisitor} visitor of the message
     */
    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof NotifyChatMessageVisitor) ((NotifyChatMessageVisitor) visitor).visit(this);
    }

}