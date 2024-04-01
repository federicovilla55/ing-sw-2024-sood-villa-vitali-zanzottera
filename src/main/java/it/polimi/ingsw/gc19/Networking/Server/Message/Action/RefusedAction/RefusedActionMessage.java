package it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction;

import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessage;

import java.util.ArrayList;

public class RefusedActionMessage extends AnswerToActionMessage {

    private final ErrorType errorType;
    private final String description;

    public RefusedActionMessage(ErrorType errorType, String description){
        super();
        this.errorType = errorType;
        this.description = description;
    }

    public RefusedActionMessage(String header, ErrorType errorType, String description){
        super(header);
        this.errorType = errorType;
        this.description = description;
    }

    public RefusedActionMessage(ArrayList<String> header, ErrorType errorType, String description){
        super(header);
        this.errorType = errorType;
        this.description = description;
    }

    public ErrorType getErrorType(){
        return this.errorType;
    }

    public String getDescription(){
        return this.description;
    }

}
