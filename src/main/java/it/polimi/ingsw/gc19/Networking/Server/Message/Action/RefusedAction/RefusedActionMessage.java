package it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction;

import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessagePriorityLevel;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is used when server needs to notify a player of an
 * error occurred while performing a certain action (e.g. placing or picking card)
 */
public class RefusedActionMessage extends AnswerToActionMessage {

    /**
     * The type of the error
     */
    private final ErrorType errorType;

    /**
     * A brief string description of the error
     */
    private final String description;

    public RefusedActionMessage(ErrorType errorType, String description){
        super();
        this.errorType = errorType;
        this.description = description;
        this.setPriorityLevel(MessagePriorityLevel.HIGH);
    }

    /**
     * Getter for error type bound to the message
     * @return the type of error contained in message
     */
    public ErrorType getErrorType(){
        return this.errorType;
    }

    /**
     * Getter for the description of the error
     * @return a text description of the error
     */
    public String getDescription(){
        return this.description;
    }

    /**
     * Implementation of the visitor pattern
     * @param visitor {@link MessageToClientVisitor} visitor of the message
     */
    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof AnswerToActionMessageVisitor) ((AnswerToActionMessageVisitor) visitor).visit(this);
    }

}