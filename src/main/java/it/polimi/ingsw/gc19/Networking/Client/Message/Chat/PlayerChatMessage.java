package it.polimi.ingsw.gc19.Networking.Client.Message.Chat;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

import java.util.ArrayList;

/**
 * This is an abstract class used to represent chat messages sent from client to server
 * and from server to client
 */
public class PlayerChatMessage extends MessageToServer{

    private final ArrayList<String> receivers;
    private final String message;

    public PlayerChatMessage(ArrayList<String> receivers, String sender, String message){
        super(sender);
        this.receivers = receivers;
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public ArrayList<String> getReceivers(){
        return this.receivers;
    }

    @Override
    public void accept(MessageToServerVisitor visitor) {
        if(visitor instanceof PlayerChatMessageVisitor) ((PlayerChatMessageVisitor) visitor).visit(this);
    }
}
