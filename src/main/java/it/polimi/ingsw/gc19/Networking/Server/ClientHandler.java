package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.ObserverPattern.Observer;

import java.util.ArrayDeque;
import java.util.Queue;

public class ClientHandler implements Observer<MessageToClient>{
    protected String username;
    protected long getLastTimeStep;
    protected final Queue<MessageToClient> messageQueue;
    private final Object getLastTimeStepLock;

    public ClientHandler() {
        this.getLastTimeStepLock = new Object();
        this.messageQueue = new ArrayDeque<>();
        new Thread(){
            @Override
            public void run() {
                while(true){
                    ClientHandler.this.sendMessage();
                }
            }
        }.start();
    }

    public void UpdateHeartBeat() {
        this.getLastTimeStep = System.currentTimeMillis();
    }
    public String getName() {
        return this.username;
    }

    public long getGetLastTimeStep(){
        synchronized (getLastTimeStepLock){
            return this.getLastTimeStep;
        }
    }
    public void sendMessageToClient(MessageToClient message){

    }

    @Override
    public void update(MessageToClient message) {
        synchronized (messageQueue) {
            messageQueue.add(message);
            messageQueue.notify();
        }
    }

    private void sendMessage(){
        MessageToClient messageToSend;
        synchronized(messageQueue){
            if(messageQueue.isEmpty()){
                messageQueue.notify();
                try{
                    messageQueue.wait();
                }
                catch(InterruptedException ignored){ };
            }
            else{
                messageToSend = this.messageQueue.remove();
                if(messageToSend.getHeader().contains(this.username)){
                    this.sendMessageToClient(messageToSend);
                }
                this.messageQueue.notify();
            }
        }
    }

}