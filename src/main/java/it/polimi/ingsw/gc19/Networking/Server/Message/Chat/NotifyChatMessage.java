package it.polimi.ingsw.gc19.Networking.Server.Message.Chat;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageVisitor;

import java.util.ArrayList;

public class NotifyChatMessage extends MessageToClient{

    private final String sender;
    private final String message;

    public NotifyChatMessage(String sender, String message){
        super();
        this.sender = sender;
        this.message = message;
    }

    public String getSender(){
        return this.sender;
    }

    public String getMessage(){
        return this.message;
    }

    @Override
    public void visit(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
