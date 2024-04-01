package it.polimi.ingsw.gc19.Model;

import it.polimi.ingsw.gc19.Controller.MessageFactory;

public abstract class Publisher{
    private MessageFactory messageFactory;

    public void setMessageFactory(MessageFactory messageFactory){
        this.messageFactory = messageFactory;
    }

    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

}
