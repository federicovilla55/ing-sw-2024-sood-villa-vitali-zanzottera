package it.polimi.ingsw.gc19.Networking.Client.Message.Chat;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

import java.util.ArrayList;

/**
 * This is an abstract class used to represent chat messages sent from client to server
 * and from server to client
 */
public class PlayerChatMessage implements MessageToServer{

    private final ArrayList<String> receivers;
    private final String sender;
    private final String message;

    PlayerChatMessage(ArrayList<String> receivers, String sender, String message){
        this.receivers = receivers;
        this.sender = sender;
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public String getSender(){
        return this.sender;
    }

    public ArrayList<String> getReceivers(){
        return this.receivers;
    }

}
