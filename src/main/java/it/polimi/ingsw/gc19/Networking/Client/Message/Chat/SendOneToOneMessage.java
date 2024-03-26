package it.polimi.ingsw.gc19.Networking.Client.Message.Chat;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;

public class SendOneToOneMessage extends SendChatMessage implements MessageToServer{

    private final String receiver;

    public SendOneToOneMessage(String sender, String receiver, String message){
        super(sender, message);
        this.receiver = receiver;
    }

    public String getReceiver() {
        return this.receiver;
    }

}
