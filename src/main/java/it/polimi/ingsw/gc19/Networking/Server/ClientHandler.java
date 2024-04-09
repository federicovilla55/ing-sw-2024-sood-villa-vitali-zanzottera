package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Controller.GameController;
import it.polimi.ingsw.gc19.Controller.MainController;
import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessagePriorityComparator;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessagePriorityLevel;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.ClientHandlerRMI;
import it.polimi.ingsw.gc19.ObserverPattern.Observer;

import java.rmi.RemoteException;
import java.util.*;

public abstract class ClientHandler implements Observer<MessageToClient>, VirtualGameServer{

    private GameController gameController;
    protected final String username;

    // @todo: maybe use a priority queue.
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

    public String getName() {
        return this.username;
    }

    public GameController getGameController(){
        return this.gameController;
    }

    public Deque<MessageToClient> getQueueOfMessages(){
        return this.messageQueue;
    }

    public abstract void sendMessageToClient(MessageToClient message);

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

    public void setGameController(GameController gameController){
        this.gameController = gameController;
    }

    @Override
    public void placeCard(String cardToInsert, String anchorCard, Direction directionToInsert, CardOrientation orientation) throws RemoteException {
        this.gameController.placeCard(username, cardToInsert, anchorCard, directionToInsert, orientation);
    }

    @Override
    public void sendChatMessage(ArrayList<String> usersToSend, String messageToSend) throws RemoteException {
        this.gameController.sendChatMessage(usersToSend, username, messageToSend);
    }

    @Override
    public void placeInitialCard(CardOrientation cardOrientation) throws RemoteException {
        this.gameController.placeInitialCard(username, cardOrientation);
    }

    @Override
    public void pickCardFromTable(PlayableCardType type, int position) throws RemoteException {
        this.gameController.drawCardFromTable(username, type, position);
    }

    @Override
    public void pickCardFromDeck(PlayableCardType type) throws RemoteException {
        this.gameController.drawCardFromDeck(username, type);
    }

    @Override
    public void chooseColor(Color color) throws RemoteException {
        this.gameController.chooseColor(username, color);
    }

    @Override
    public void choosePrivateGoalCard(int cardIdx) throws RemoteException {
        this.gameController.choosePrivateGoal(username, cardIdx);
    }

}