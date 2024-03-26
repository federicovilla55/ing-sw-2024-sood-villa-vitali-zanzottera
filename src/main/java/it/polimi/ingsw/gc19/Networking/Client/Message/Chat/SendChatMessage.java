package it.polimi.ingsw.gc19.Networking.Client.Message.Chat;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;

abstract class SendChatMessage implements MessageToServer{

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
