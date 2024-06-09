package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessagePriorityLevel;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is used to tell player that an error concerning game
 * (game name not valid, player name already in used...) has occurred
 */
public class GameHandlingErrorMessage extends GameHandlingMessage {

    /**
     * The type of the error
     */
    private final Error errorType;

    /**
     * A brief string description of the error
     */
    private final String description;

    public GameHandlingErrorMessage(Error errorType, String description) {
        this.errorType = errorType;
        this.description = description;
        this.setPriorityLevel(MessagePriorityLevel.HIGH);
    }

    /**
     * Getter for error type associated to game
     * @return error associated to the message
     */
    public Error getErrorType() {
        return errorType;
    }

    /**
     * Getter for string description of error
     * @return a string description of error
     */
    public String getDescription() {
        return description;
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
     * Overriding of {@link Object#equals(Object)} method. Two {@link GameHandlingErrorMessage}
     * are considered to be equals if and only if theirs {@link #errorType} are the same
     * @param o the {@link Object} to compare
     * @return <code>true</code> if and only if the two objects are equals.
     */
    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if(o instanceof GameHandlingErrorMessage){
            return ((GameHandlingErrorMessage) o).errorType == this.errorType;
        }
        return false;
    }

}