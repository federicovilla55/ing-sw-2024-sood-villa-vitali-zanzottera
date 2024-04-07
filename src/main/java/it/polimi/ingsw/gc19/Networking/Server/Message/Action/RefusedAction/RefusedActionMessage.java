package it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction;

import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessagePriorityLevel;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public class RefusedActionMessage extends AnswerToActionMessage {

    private final ErrorType errorType;
    private final String description;

    public RefusedActionMessage(ErrorType errorType, String description){
        super();
        this.errorType = errorType;
        this.description = description;
        this.setPriorityLevel(MessagePriorityLevel.HIGH);
    }

    public ErrorType getErrorType(){
        return this.errorType;
    }

    public String getDescription(){
        return this.description;
    }

    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof AnswerToActionMessageVisitor) ((AnswerToActionMessageVisitor) visitor).visit(this);
    }

}
