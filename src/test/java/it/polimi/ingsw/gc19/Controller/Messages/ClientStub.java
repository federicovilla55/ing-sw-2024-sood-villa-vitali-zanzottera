package it.polimi.ingsw.gc19.Controller.Messages;

import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

import java.util.*;

public class ClientStub extends ClientHandler{

    public ClientStub(String username) {
        super(username);
    }

    @Override
    public void sendMessageToClient(MessageToClient message) {

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
        // this client stub is local, does not send messages but saves them in the queue
        this.messageQueue.add(message);
    }

    @Override
    protected void sendMessage(){
        //Client stub is local, so it doesn't have to send messages over the network.
        //sendMessage() has to do nothing
    }

    public ArrayList<MessageToClient> getIncomingMessages(){
        return new ArrayList<>(this.messageQueue);
    }

}
