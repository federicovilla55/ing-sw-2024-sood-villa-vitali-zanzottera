package it.polimi.ingsw.gc19.Controller.Messages;

import it.polimi.ingsw.gc19.Controller.GameController;
import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.ClientHandlerRMI;

import java.rmi.RemoteException;
import java.util.*;

public class ClientStub extends ClientHandler {

    public ClientStub(String username, GameController gameController) {
        super(username, gameController);
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

    public void placeCard(String cardToInsert, String anchorCard, Direction directionToInsert, CardOrientation orientation) throws RemoteException {
        this.gameController.placeCard(username, cardToInsert, anchorCard, directionToInsert, orientation);
    }

    public void sendChatMessage(ArrayList<String> usersToSend, String messageToSend){
        this.gameController.sendChatMessage(usersToSend, username, messageToSend);
    }

    public void placeInitialCard(CardOrientation cardOrientation){
        this.gameController.placeInitialCard(username, cardOrientation);
    }

    public void pickCardFromTable(PlayableCardType type, int position){
        this.gameController.drawCardFromTable(username, type, position);
    }

    public void pickCardFromDeck(PlayableCardType type){
        this.gameController.drawCardFromDeck(username, type);
    }

    public void chooseColor(Color color){
        this.gameController.chooseColor(username, color);
    }

    public void choosePrivateGoalCard(int cardIdx){
        this.gameController.choosePrivateGoal(username, cardIdx);
    }

}
