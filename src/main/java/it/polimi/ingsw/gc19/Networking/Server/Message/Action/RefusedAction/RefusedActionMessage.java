package it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction;

import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageVisitor;

import java.util.ArrayList;

public class RefusedActionMessage extends AnswerToActionMessage {

    private final ErrorType errorType;
    private final String description;

    public RefusedActionMessage(ErrorType errorType, String description){
        super();
        this.errorType = errorType;
        this.description = description;
    }

    public ErrorType getErrorType(){
        return this.errorType;
    }

    public String getDescription(){
        return this.description;
    }

    @Override
    public void visit(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
