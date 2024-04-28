package it.polimi.ingsw.gc19.Networking.Server.ServerRMI;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * This class is used server-side to represents a client that
 * is connected to server using RMI. It acts as a {@link VirtualGameServer}
 * for that client: each player has his onw {@link VirtualGameServer} when he connects
 * to a game.
 */
public class ClientHandlerRMI extends ClientHandler implements VirtualGameServer {

    private final VirtualClient virtualClientAssociated;

    public ClientHandlerRMI(VirtualClient virtualClientAssociated, String username) {
        super(username, null);
        this.virtualClientAssociated = virtualClientAssociated;
    }

    public ClientHandlerRMI(VirtualClient virtualClientAssociated, ClientHandler clientHandler) {
        super(clientHandler.getUsername(), clientHandler.getGameController());
        this.messageQueue.addAll(clientHandler.getQueueOfMessages());
        this.virtualClientAssociated = virtualClientAssociated;
    }

    /**
     * This method sends a message to the client using RMI. It overrides
     * method {@link ClientHandler#sendMessageToClient(MessageToClient)} for <code>ClientHandler</code>
     * @param message the message to be sent to client
     */
    @Override
    public void sendMessageToClient(MessageToClient message) {
        try {
            virtualClientAssociated.pushUpdate(message);
        }
        catch (RemoteException remoteException) {
            System.err.println("[EXCEPTION] Remote Exception occurred while trying to send message to client RMI " + virtualClientAssociated + " with RMI. Skipping...");
        }
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
        if(gameController != null) {
            this.gameController.placeCard(username, cardToInsert, anchorCard, directionToInsert, orientation);
        }
        else{
            sendMessageToClient(new GameHandlingError(Error.GAME_NOT_FOUND,
                                                      "You aren't connected to any game! It can be finished or you have lost connection!")
                                        .setHeader(this.username));
        }
    }

    /**
     * Remote method with which player can send a chat message
     * @param usersToSend users to send message to
     * @param messageToSend message to be sent
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public void sendChatMessage(ArrayList<String> usersToSend, String messageToSend) throws RemoteException {
        if(gameController != null) {
            this.gameController.sendChatMessage(usersToSend, username, messageToSend);
        }
        else{
            sendMessageToClient(new GameHandlingError(Error.GAME_NOT_FOUND,
                                                      "You aren't connected to any game! Maybe it's finished or you have to reconnect!")
                                        .setHeader(this.username));
        }
    }

    /**
     * Remote method used by player to place the initial card.
     * @param cardOrientation orientation of the initial card
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public void placeInitialCard(CardOrientation cardOrientation) throws RemoteException {
        if(gameController != null) {
            this.gameController.placeInitialCard(username, cardOrientation);
        }
        else{
            sendMessageToClient(new GameHandlingError(Error.GAME_NOT_FOUND,
                                                      "You aren't connected to any game! It can be finished or you have lost connection!")
                                        .setHeader(this.username));
        }
    }

    /**
     * Remote method used by player to pick card from table
     * @param type type of card to be picked
     * @param position position on the table of the card
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public void pickCardFromTable(PlayableCardType type, int position) throws RemoteException {
        if(gameController != null) {
            this.gameController.drawCardFromTable(username, type, position);
        }
        else{
            sendMessageToClient(new GameHandlingError(Error.GAME_NOT_FOUND,
                                                      "You aren't connected to any game! It can be finished or you have lost connection!")
                                        .setHeader(this.username));
        }
    }

    /**
     * Remote method used by player to pick card from table
     * @param type type of card to be picked
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public void pickCardFromDeck(PlayableCardType type) throws RemoteException {
        if(gameController != null) {
            this.gameController.drawCardFromDeck(username, type);
        }
        else{
            sendMessageToClient(new GameHandlingError(Error.GAME_NOT_FOUND,
                                                      "You aren't connected to any game! It can be finished or you have lost connection!")
                                        .setHeader(this.username));
        }
    }

    /**
     * Remote method used by player to choose color
     * @param color color chosen
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public void chooseColor(Color color) throws RemoteException {
        if(gameController != null) {
            this.gameController.chooseColor(username, color);
        }
        else{
            sendMessageToClient(new GameHandlingError(Error.GAME_NOT_FOUND,
                                                      "You aren't connected to any game! It can be finished or you have lost connection!")
                                        .setHeader(this.username));
        }
    }

    /**
     * Remote method with which player can choose his private goal card
     * @param cardIdx index of the card chosen
     * @throws RemoteException exception thrown if something goes wrong
     */
    @Override
    public void choosePrivateGoalCard(int cardIdx) throws RemoteException {
        if(gameController != null) {
            this.gameController.choosePrivateGoal(username, cardIdx);
        }
        else{
            sendMessageToClient(new GameHandlingError(Error.GAME_NOT_FOUND,
                                                      "You aren't connected to any game! It can be finished or you have lost connection!")
                                        .setHeader(this.username));
        }
    }

}

