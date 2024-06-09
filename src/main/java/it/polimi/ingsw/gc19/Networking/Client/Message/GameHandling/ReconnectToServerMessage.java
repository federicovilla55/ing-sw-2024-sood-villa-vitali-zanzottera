package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;


/**
 * This method is used by user to tell server it wants to reconnect
 * to server.
 */
public class ReconnectToServerMessage extends GameHandlingMessage {

    /**
     * Nickname of the user who wants to reconnect
     */
    private final String nickname;

    /**
     * Secret token of th user who wants to reconnect
     */
    private final String token;

    public ReconnectToServerMessage(String nickname, String token){
        super(nickname);
        this.nickname = nickname;
        this.token = token;
    }

    /**
     * This message is used by {@link MessageToServerVisitor} to visit the message
     * @param visitor the {@link MessageToServerVisitor} for the message
     */
    @Override
    public void accept(MessageToServerVisitor visitor) {
        if(visitor instanceof GameHandlingMessageVisitor) ((GameHandlingMessageVisitor) visitor).visit(this);
    }

    /**
     * Getter for token associated to the client
     * @return the token contained in message
     */
    public String getToken(){
        return this.token;
    }

    /**
     * Overriding of {@link Object#equals(Object)}. Two {@link ReconnectToServerMessage}
     * are considered to be equals if and only if the {@link #nickname} are equals.
     * @param o the {@link Object} to compare
     * @return <code>true</code> if and only if the two messages are equals
     */
    @Override
    public boolean equals(Object o){
        if(o == null) return false;
        if(! (o instanceof ReconnectToServerMessage)) return false;
        return ((ReconnectToServerMessage) o ).nickname.equals(this.nickname);
    }

}