package it.polimi.ingsw.gc19.Controller.Messages;

import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

import java.util.*;

public class ClientStub extends ClientHandler{

    public ClientStub(String username) {
        super(username);
    }

    void clearQueue() {
        messageQueue.clear();
    }

    MessageToClient getMessage() {
        try {
            return messageQueue.remove();
        }
        catch (NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public void update(MessageToClient message) {
        //Client stub is local, so it doesn't have to send messages over the network.
        //sendMessageToClient() has to do anything
    }

    @Override
    protected void sendMessage(){
        //Client stub is local, so it doesn't have to send messages over the network.
        //sendMessage() has to do anything
    }

    public ArrayList<MessageToClient> getIncomingMessages(){
        return new ArrayList<>(this.messageQueue);
    }

}
