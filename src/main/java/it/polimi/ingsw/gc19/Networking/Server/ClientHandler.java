package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Controller.GameController;
import it.polimi.ingsw.gc19.Controller.MainController;
import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.GameState;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessagePriorityComparator;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessagePriorityLevel;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.ClientHandlerRMI;
import it.polimi.ingsw.gc19.ObserverPattern.Observer;

import java.rmi.RemoteException;
import java.util.*;

public abstract class ClientHandler implements Observer<MessageToClient>, VirtualGameServer{

    protected MainController mainController;
    private GameController gameController;
    protected final String username;
    protected final Queue<MessageToClient> messageQueue;
    protected Long lastSignalFromClient;

    public ClientHandler(String username, GameController gameController, MainController mainController){
        this.mainController = mainController;
        this.gameController = gameController;

        this.username = username;
        this.messageQueue = new ArrayDeque<>();

        this.messageQueue = new PriorityQueue<>(new MessagePriorityComparator());

        this.lastSignalFromClient = null;

        new Thread(() -> {
            while(true){
                ClientHandler.this.sendMessage();
            }
        }).start();
    }

    public String getName() {
        return this.username;
    }

    public abstract void sendMessageToClient(MessageToClient message);

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
            while(messageQueue.isEmpty()){
                messageQueue.notifyAll();
                try{
                    messageQueue.wait();
                }
                catch(InterruptedException ignored){ };
            }

            messageToSend = this.messageQueue.remove();
            this.sendMessageToClient(messageToSend);
            this.messageQueue.notifyAll();

        }
    }

    public void setMainController(MainController mainController){
        this.mainController = mainController;
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

}