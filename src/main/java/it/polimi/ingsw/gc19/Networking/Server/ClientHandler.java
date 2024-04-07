package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessagePriorityComparator;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessagePriorityLevel;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.ObserverPattern.Observer;

import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.PriorityQueue;
import java.util.Queue;

public abstract class ClientHandler implements Observer<MessageToClient>{

    protected final String username;
    protected final PriorityQueue<MessageToClient> messageQueue;

    public ClientHandler(String username){
        this.username = username;
        this.messageQueue = new PriorityQueue<>(new MessagePriorityComparator());
        new Thread(() -> {
            while(true){
                ClientHandler.this.sendMessage();
            }
        }).start();
    }

    public String getName() {
        return this.username;
    }

    @Override
    public void update(MessageToClient message){
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
                try {
                    this.pushUpdate(messageToSend);
                }
                catch(RemoteException remoteException){
                    //@TODO: handle this exception
                }
                this.messageQueue.notifyAll();
            }
        }
    }

}