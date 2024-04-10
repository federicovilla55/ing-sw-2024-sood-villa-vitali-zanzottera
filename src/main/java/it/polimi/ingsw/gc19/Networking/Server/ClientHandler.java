package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Controller.GameController;
import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessagePriorityLevel;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.ClientHandlerRMI;
import it.polimi.ingsw.gc19.ObserverPattern.Observer;

import java.rmi.RemoteException;
import java.util.*;

/**
 * This class represents the "network interface" with which server can communicate with
 * one client both sending and receiving messages.
 * It implements <code>Observer<MessageToClient></code> so that <code>Observable<MessageToClient></code>
 * can push in its queue their messages. Also, it implements {@link VirtualGameServer} because
 * client remotely will be invoking these methods.
 */
public abstract class ClientHandler implements Observer<MessageToClient>, VirtualGameServer{

    private GameController gameController;
    protected final String username;

    // @TODO: maybe use a priority queue.
    // An example can be done with three ArrayDequeue (one for each priority level)
    // the methods that use the messageQueue will become more complex (to remove the top
    // element there's the need to watch all three queues).
    protected final ArrayDeque<MessageToClient> messageQueue;

    public ClientHandler(String username, GameController gameController){
        this.gameController = gameController;

        this.username = username;
        this.messageQueue = new ArrayDeque<>();

        new Thread(() -> {
            while(true){
                ClientHandler.this.sendMessage();
            }
        }).start();
    }

    /**
     * Getter method for player name
     * @return player name bound to this {@link ClientHandler}
     */
    public String getName() {
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
                messageQueue.notifyAll();
                try{
                    messageQueue.wait();
                }
                catch(InterruptedException ignored){ };
            }
            messageToSend = this.messageQueue.remove();
            //System.out.println(messageToSend.getClass() +  " " + messageToSend.getHeader());
            if(messageToSend.getHeader().contains(username)) {
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

    /**
     * Remote method with which player can a place card
     * @param cardToInsert card's to insert code
     * @param anchorCard anchor's code
     * @param directionToInsert direction in which place the card
     * @param orientation orientation of the placed card
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public void placeCard(String cardToInsert, String anchorCard, Direction directionToInsert, CardOrientation orientation) throws RemoteException {
        this.gameController.placeCard(username, cardToInsert, anchorCard, directionToInsert, orientation);
    }

    /**
     * Remote method with which player can send a chat message
     * @param usersToSend users to send message to
     * @param messageToSend message to be sent
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public void sendChatMessage(ArrayList<String> usersToSend, String messageToSend) throws RemoteException {
        this.gameController.sendChatMessage(usersToSend, username, messageToSend);
    }

    /**
     * Remote method used by player to place the initial card.
     * @param cardOrientation orientation of the initial card
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public void placeInitialCard(CardOrientation cardOrientation) throws RemoteException {
        this.gameController.placeInitialCard(username, cardOrientation);
    }

    /**
     * Remote method used by player to pick card from table
     * @param type type of card to be picked
     * @param position position on the table of the card
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public void pickCardFromTable(PlayableCardType type, int position) throws RemoteException {
        this.gameController.drawCardFromTable(username, type, position);
    }

    /**
     * Remote method used by player to pick card from table
     * @param type type of card to be picked
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public void pickCardFromDeck(PlayableCardType type) throws RemoteException {
        this.gameController.drawCardFromDeck(username, type);
    }

    /**
     * Remote method used by player to choose color
     * @param color color chosen
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public void chooseColor(Color color) throws RemoteException {
        this.gameController.chooseColor(username, color);
    }

    /**
     * Remote method with which player can choose his private goal card
     * @param cardIdx index of the card chosen
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public void choosePrivateGoalCard(int cardIdx) throws RemoteException {
        this.gameController.choosePrivateGoal(username, cardIdx);
    }

}