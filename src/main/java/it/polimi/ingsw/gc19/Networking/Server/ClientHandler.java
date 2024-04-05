package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.ObserverPattern.Observer;

import java.util.ArrayDeque;
import java.util.Queue;

public abstract class ClientHandler implements Observer<MessageToClient>{

    protected final String username;
    protected final Queue<MessageToClient> messageQueue;

    public ClientHandler(String username){
        this.username = username;
        this.messageQueue = new ArrayDeque<>();
        new Thread(() -> {
            while(true){
                ClientHandler.this.sendMessage();
            }
        }).start();
    }

    public String getName() {
        return this.username;
    }

    public void sendMessageToClient(MessageToClient message) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(MessageToClient message) {
        synchronized(messageQueue){
            messageQueue.add(message);
            messageQueue.notify();
        }
    }

    protected void sendMessage(){
        MessageToClient messageToSend;
        synchronized(messageQueue){
            if(messageQueue.isEmpty()){
                messageQueue.notifyAll();
                try{
                    messageQueue.wait();
                }
                catch(InterruptedException ignored){ };
            }
            else{
                messageToSend = this.messageQueue.remove();
                if(messageToSend.getHeader().contains(this.username)){
                    this.update(messageToSend);
                }
                this.messageQueue.notifyAll();
            }
        }
    }

}