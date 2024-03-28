package it.polimi.ingsw.gc19.Networking.Client.Message.Chat;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

/**
 * This is an abstract class used to represent chat messages sent from client to server
 * and from server to client
 */
public abstract class SendChatMessage implements MessageToServer, MessageToClient{

    private final String sender;
    private final String message;

    SendChatMessage(String sender, String message){
        this.sender = sender;
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public String getSender(){
        return this.sender;
    }

}
