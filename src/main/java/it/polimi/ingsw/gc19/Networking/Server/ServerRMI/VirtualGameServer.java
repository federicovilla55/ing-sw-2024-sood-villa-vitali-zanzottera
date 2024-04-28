package it.polimi.ingsw.gc19.Networking.Server.ServerRMI;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * This is the remote interface seen by clients of the game server.
 * From it, they can call all methods regarding game (e.g. placing a card or send a chat message).
 */
public interface VirtualGameServer extends Remote {

    /**
     * This remote method is used by clients to place a card.
     * @param cardToInsert is the card to insert code
     * @param anchorCard is the code of the anchor card
     * @param directionToInsert is the {@link Direction} in which place the card from the anchor
     * @param orientation is the {@link CardOrientation} in which place card
     * @throws RemoteException if something goes wrong while performing the requested action
     */
    void placeCard(String cardToInsert, String anchorCard, Direction directionToInsert, CardOrientation orientation) throws RemoteException;

    /**
     * This remote method is used to send a chat message.
     * @param usersToSend is the <code>List<String></code> of receiver clients name
     * @param messageToSend is the message to send in chat
     * @throws RemoteException if something goes wrong while performing the requested action.
     */
    void sendChatMessage(ArrayList<String> usersToSend, String messageToSend) throws RemoteException;

    /**
     * This remote method is used by clients who need to place their initial card
     * @param cardOrientation is the {@link CardOrientation} in which place the initial card
     * @throws RemoteException if something goes wrong while performing the requested action
     */
    void placeInitialCard(CardOrientation cardOrientation) throws RemoteException;

    /**
     * This remote method is used by clients who need to pick card from table
     * @param type id the {@link PlayableCardType} of the card to pick
     * @param position is the position on table of the card to pick
     * @throws RemoteException if something goes wrong while performing the requested action
     */
    void pickCardFromTable(PlayableCardType type, int position) throws RemoteException;

    /**
     * This remote method is used by clients who need to pick card from deck.
     * @param type is the {@link PlayableCardType} of the card to pick
     * @throws RemoteException if something goes wrong while performing the requested action
     */
    void pickCardFromDeck(PlayableCardType type) throws RemoteException;

    /**
     * This remote method is used by clients who need to choose their {@link Color}
     * @param color is the {@link Color} chosen by player
     * @throws RemoteException if something goes wrong while performing the given action.
     */
    void chooseColor(Color color) throws RemoteException;

    /**
     * This remote method is used by player to choose their private goal card.
     * @param cardIdx is the index of the chosen private goal card
     * @throws RemoteException if something goes wrong while performing the requested action.
     */
    void choosePrivateGoalCard(int cardIdx) throws RemoteException;

}
