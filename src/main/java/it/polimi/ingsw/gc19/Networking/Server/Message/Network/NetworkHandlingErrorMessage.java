package it.polimi.ingsw.gc19.Networking.Server.Message.Network;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessagePriorityLevel;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * With this message the server notifies to clients that a {@link NetworkError}
 * has occurred while trying to perform an action.
 */
public class NetworkHandlingErrorMessage extends MessageToClient {

    private final NetworkError networkError;
    private final String description;

    public NetworkHandlingErrorMessage(NetworkError networkError, String description){
        this.networkError = networkError;
        this.description = description;
        this.setPriorityLevel(MessagePriorityLevel.HIGH);
    }

    /**
     * Getter for {@link NetworkError} of the message
     * @return the {@link NetworkError} associated to the message.
     */
    public NetworkError getError() {
        return this.networkError;
    }

    /**
     * Getter for description of the {@link NetworkError}
     * @return a {@link String} description of the error
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * This method is used to implement Visitor design pattern for server to client messages.
     * @param visitor {@link MessageToClientVisitor} visitor of the message
     */
    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof NetworkHandlingErrorMessageVisitor) ((NetworkHandlingErrorMessageVisitor) visitor).visit(this);
    }

    /**
     * This method compares an {@link Object} with a {@link NetworkHandlingErrorMessage}:
     * it acts the same as {@link Object#equals(Object)} but it checks only
     * if <code>((NetworkHandlingErrorMessage) o).networkError == networkError</code>
     * @param o the {@link Object} to compare
     * @return true if and only if the two objects are equals according to what
     * described above.
     */
    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if(o instanceof NetworkHandlingErrorMessage){
            return ((NetworkHandlingErrorMessage) o).networkError == this.networkError;
        }
        return false;
    }

}
