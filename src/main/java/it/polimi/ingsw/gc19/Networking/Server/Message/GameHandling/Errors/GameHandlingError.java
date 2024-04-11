package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessagePriorityLevel;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is used to tell player that an error concerning game
 * (game name not valid, player name already in used...) has occurred
 */
public class GameHandlingError extends MessageToClient{

    private final Error errorType;
    private final String description;

    public GameHandlingError(Error errorType, String description) {
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

    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if(o instanceof GameHandlingError){
            return ((GameHandlingError) o).errorType == this.errorType;
        }
        return false;
    }

}
