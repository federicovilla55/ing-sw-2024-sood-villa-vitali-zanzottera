package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Controller.GameController;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessagePriorityLevel;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.ClientHandlerRMI;
import it.polimi.ingsw.gc19.ObserverPattern.ObserverMessageToClient;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * This class represents the "network interface" with which server can communicate with
 * one client both sending and receiving messages.
 * It implements <code>Observer<MessageToClient></code> so that <code>Observable<MessageToClient></code>
 * can push in its queue their messages.
 */
public abstract class ClientHandler extends Thread implements ObserverMessageToClient<MessageToClient> {

    protected GameController gameController;
    protected String username;
    protected final ArrayDeque<MessageToClient> messageQueue;

    public ClientHandler(String username, GameController gameController){
        this.gameController = gameController;

        this.username = username;
        this.messageQueue = new ArrayDeque<>();
    }

    public void run(){
        while(!Thread.interrupted()){
            ClientHandler.this.sendMessage();
        }
    }

    public ClientHandler(String username){
        this(username, null);
    }

    public ClientHandler(){
        this(null, null);
    }

    /**
     * Setter for username
     * @param username the username of the client
     */
    public void setUsername(String username){
        this.username = username;
    }

    /**
     * Getter method for player name
     * @return player name bound to this {@link ClientHandler}
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Getter method for game controller
     * @return game controller associated with this {@link ClientHandler}
     */
    public GameController getGameController(){
        return this.gameController;
    }

    /**
     * Getter for the queue of incoming messages
     * @return queue of the incoming messages
     */
    public Deque<MessageToClient> getQueueOfMessages(){
        return this.messageQueue;
    }

    /**
     * This method is abstract. It must be implemented by all class extending {@link ClientHandler}
     * in order to send message to client through the network
     * @param message message to be sent
     */
    public abstract void sendMessageToClient(MessageToClient message);

    /**
     * This method is used by Observable to push a {@link MessageToClient} message inside the queue
     * of messages to be sent to client through the network.
     * @param message is the message to be pushed inside the queue
     */
    @Override
    public void update(MessageToClient message) {
        synchronized(messageQueue){
            if(message.getMessagePriorityLevel() == MessagePriorityLevel.HIGH) {
                messageQueue.addFirst(message);
            }else{
                messageQueue.add(message);
            }
            messageQueue.notify();
        }
    }

    /**
     * This method picks a message from the queue and send calls the appropriate
     * method for sending it through the network (RMI or TCP for this project)
     * with {@link ClientHandlerRMI#sendMessageToClient(MessageToClient)}
     */
    protected void sendMessage(){
        MessageToClient messageToSend;
        synchronized(messageQueue){
            while(messageQueue.isEmpty()){
                try{
                    messageQueue.wait();
                }
                catch(InterruptedException ignored){ }; //@TODO: interrupted exception to handle
            }
            messageToSend = this.messageQueue.remove();
            if(messageToSend.getHeader() == null || messageToSend.getHeader().contains(username)) {
                this.sendMessageToClient(messageToSend);
            }
            this.messageQueue.notifyAll();
        }
    }

    /**
     * Setter for game controller
     * @param gameController game controller to be set inside {@link ClientHandler}
     */
    public void setGameController(GameController gameController){
        this.gameController = gameController;
    }

    public void interruptClientHandler(){
        this.interrupt();
    }

}