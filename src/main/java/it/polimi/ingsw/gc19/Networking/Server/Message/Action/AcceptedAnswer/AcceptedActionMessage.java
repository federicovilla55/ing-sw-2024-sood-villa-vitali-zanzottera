package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessage;

import java.util.ArrayList;

public abstract class AcceptedActionMessage extends AnswerToActionMessage {

    protected AcceptedActionMessage(){
        super();
    }

    protected AcceptedActionMessage(String header){
        super(header);
    }

    protected AcceptedActionMessage(ArrayList<String> header){
        super(header);
    }

}
