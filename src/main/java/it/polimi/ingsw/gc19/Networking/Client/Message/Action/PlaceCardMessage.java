package it.polimi.ingsw.gc19.Networking.Client.Message.Action;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

/**
 * This message is used by client to notify server that it
 * wants to place a card in a certain direction from an anchor,
 * and with a specified orientation
 */
public class PlaceCardMessage extends ActionMessage{

    private final String cardToPlaceCode;
    private final String anchorCode;
    private final Direction direction;
    private final CardOrientation cardOrientation;

    public PlaceCardMessage(String playerNickname, String cardToPlaceCode, String anchorCode, Direction direction, CardOrientation cardOrientation){
        super(playerNickname);
        this.cardToPlaceCode = cardToPlaceCode;
        this.anchorCode = anchorCode;
        this.direction = direction;
        this.cardOrientation = cardOrientation;
    }

    /**
     * Getter for card to place code
     * @return the card to place code
     */
    public String getCardToPlaceCode() {
        return this.cardToPlaceCode;
    }

    /**
     * Getter for anchor card code
     * @return the anchor card code
     */
    public String getAnchorCode() {
        return this.anchorCode;
    }

    /**
     * Getter for direction of card
     * @return the direction in which card has to be placed
     */
    public Direction getDirection() {
        return this.direction;
    }

    /**
     * Getter for the orientation in which card has to be placed
     * @return the direction in which card has to be placed
     */
    public CardOrientation getCardOrientation(){
        return this.cardOrientation;
    }

    /**
     * This method is used by {@link MessageToServerVisitor} to visit the message.
     * @param visitor the {@link MessageToServerVisitor} for the message
     */
    @Override
    public void accept(MessageToServerVisitor visitor){
        if(visitor instanceof ActionMessageVisitor) ((ActionMessageVisitor) visitor).visit(this);
    }

}
