package it.polimi.ingsw.gc19.Networking.Server.Message.Action;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

import java.util.ArrayList;

public abstract class AnswerToActionMessage extends MessageToClient{

    protected AnswerToActionMessage() {
        super();
    }

    protected AnswerToActionMessage(String header){
        super(header);
    }

    protected AnswerToActionMessage(ArrayList<String> header){
        super(header);
    }

}
