package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;


/**
 * This method is used by user to tell server it wants to reconnect
 * to server.
 */
public class ReconnectToServerMessage extends GameHandlingMessage {
    private final String nickname;
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

    @Override
    public boolean equals(Object o){
        if(o == null) return false;
        if(! (o instanceof ReconnectToServerMessage)) return false;
        return ((ReconnectToServerMessage) o ).nickname.equals(this.nickname);
    }

}
