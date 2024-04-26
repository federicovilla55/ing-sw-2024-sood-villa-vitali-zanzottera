package it.polimi.ingsw.gc19.Networking.Client.Message;

import java.io.Serializable;
import java.rmi.Remote;

/**
 * This abstract class represents a generic message sent by client
 * to server.It implements {@link Serializable} so that it can be transmitted
 * with TCP
 */
public abstract class MessageToServer implements Serializable{

    private final String nickname;

    protected MessageToServer(String nickname){
        this.nickname = nickname;
    }

    /**
     * Getter for nickname of sender client
     * @return the nickname of the sender client
     */
    public String getNickname() {
        return this.nickname;
    }

    /**
     * This method is used to implement Visitor pattern.
     * @param visitor the {@link MessageToServerVisitor} for the message
     */
    public abstract void accept(MessageToServerVisitor visitor);

}
