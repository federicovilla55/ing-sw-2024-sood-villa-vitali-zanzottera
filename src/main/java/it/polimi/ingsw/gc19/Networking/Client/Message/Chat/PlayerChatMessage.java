package it.polimi.ingsw.gc19.Networking.Client.Message.Chat;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

import java.util.ArrayList;

/**
 * This message is used when player wants to send a message
 * in the chat of its game
 */
public class PlayerChatMessage extends MessageToServer{

    private final ArrayList<String> receivers;
    private final String message;

    public PlayerChatMessage(ArrayList<String> receivers, String sender, String message){
        super(sender);
        this.receivers = receivers;
        this.message = message;
    }

    /**
     * Getter for message player wants to send
     * @return the message player wants to send
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Getter for receivers of the message
     * @return a List of Strings of the receivers of the message
     */
    public ArrayList<String> getReceivers(){
        return this.receivers;
    }

    /**
     * This method is used by {@link MessageToServerVisitor} to visit the message
     * @param visitor the {@link MessageToServerVisitor} for the message
     */
    @Override
    public void accept(MessageToServerVisitor visitor) {
        if(visitor instanceof PlayerChatMessageVisitor) ((PlayerChatMessageVisitor) visitor).visit(this);
    }

}
