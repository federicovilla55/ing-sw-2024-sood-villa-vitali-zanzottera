package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is used to confirm to client that his
 * player object on server has been successfully created
 */
public class CreatedPlayerMessage extends GameHandlingMessage{

    /**
     * Nickname of the created player
     */
    private final String nick;

    /**
     * Private token assigned by server to the client
     */
    private final String token;

    public CreatedPlayerMessage(String nick, String token) {
        this.nick = nick;
        this.token = token;
    }

    public CreatedPlayerMessage(String nick) {
        this(nick, null);
    }

    /**
     * Getter for nickname of created player
     * @return the nickname of the created player
     */
    public String getNick() {
        return this.nick;
    }

    /**
     * Getter for private token of player
      * @return the private token of the player
     */
    public String getToken(){
        return this.token;
    }

    /**
     * Implementation of the visitor pattern
     * @param visitor {@link MessageToClientVisitor} visitor of the message
     */
    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof GameHandlingMessageVisitor) ((GameHandlingMessageVisitor) visitor).visit(this);
    }

    /**
     * Overriding of {@link Object#equals(Object)} method. Two {@link CreatedPlayerMessage}
     * are considered to be equals if and only if theirs {@link #nick} are the same
     * @param o the {@link Object} to compare
     * @return <code>true</code> if and only if the two objects are equals.
     */
    @Override
    public boolean equals(Object o){
        if(o == null) return false;
        if(! (o instanceof CreatedPlayerMessage)) return false;
        return ((CreatedPlayerMessage) o).nick.equals(this.nick);
    }

}
