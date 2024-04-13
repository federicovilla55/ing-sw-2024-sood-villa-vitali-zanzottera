package it.polimi.ingsw.gc19.Networking.Server.ServerRMI;

import com.fasterxml.jackson.databind.introspect.AnnotationCollector;
import it.polimi.ingsw.gc19.Controller.GameController;
import it.polimi.ingsw.gc19.Controller.MainController;
import it.polimi.ingsw.gc19.Costants.ImportantConstants;
import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Settings;
import it.polimi.ingsw.gc19.Networking.Server.VirtualGameServer;

import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientHandlerRMI extends ClientHandler implements VirtualGameServer {

    private final VirtualClient virtualClientAssociated;

    public ClientHandlerRMI(VirtualClient virtualClientAssociated, String username) {
        super(username, null);
        this.virtualClientAssociated = virtualClientAssociated;

    }

    public ClientHandlerRMI(VirtualClient virtualClientAssociated, ClientHandler clientHandler) {
        super(clientHandler.getName(), clientHandler.getGameController());
        this.messageQueue.addAll(clientHandler.getQueueOfMessages());
        this.virtualClientAssociated = virtualClientAssociated;
    }

    /**
     * This method sends a message to the client using RMI. It overrides
     * method {@link ClientHandler#sendMessageToClient(MessageToClient)} for <code>ClientHandler</code>
     *
     * @param message the message to be sent to client
     */
    @Override
    public void sendMessageToClient(MessageToClient message) {
        try {
            virtualClientAssociated.pushUpdate(message);
        } catch (RemoteException remoteException) {
            //System.out.println("Remote Exception: " + remoteException.getMessage() + "  -> " + message.getClass());
            //@TODO: handle this exception
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

