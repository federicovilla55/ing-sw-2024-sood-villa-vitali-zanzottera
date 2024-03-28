package it.polimi.ingsw.gc19.Networking.Client.Message.Chat;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;

import java.util.ArrayList;

public class SendOneToMoreMessage extends SendChatMessage implements MessageToServer{

    private final ArrayList<String> receivers;

    public SendOneToMoreMessage(String sender, ArrayList<String> receivers, String message){ //is gameName necessary?
        super(sender, message);
        this.receivers = receivers;
    }

    public SendOneToMoreMessage addReceiver(String newReceiver){
        this.receivers.add(newReceiver);
        return this;
    }

    public ArrayList<String> getReceivers() {
        return this.receivers;
    }

}
