package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

import java.util.Map;

/**
 * This message is used by server to notify to clients
 * that a "place card" of some player has been accepted
 */
public class AcceptedPlacePlayableCardMessage extends AcceptedPlaceCardMessage{

    private final String anchorCode;
    private final PlayableCard cardToPlace;
    private final Direction direction;
    private final int numPoints;

    public AcceptedPlacePlayableCardMessage(String nick, String anchorCode, PlayableCard cardToPlace, Direction direction, Map<Symbol, Integer> visibleSymbols, int numPoints){
        super(nick, visibleSymbols);
        this.anchorCode = anchorCode;
        this.cardToPlace = cardToPlace;
        this.direction = direction;
        this.numPoints = numPoints;
    }

    /**
     * Getter for anchor card code
     * @return anchor card code
     */
    public String getAnchorCode() {
        return this.anchorCode;
    }

    /**
     * Getter for {@link PlayableCard} placed by player
     * @return the card placed by player
     */
    public PlayableCard getCardToPlace() {
        return this.cardToPlace;
    }

    /**
     * Getter for the direction of placing
     * @return the direction in which the card has been placed
     */
    public Direction getDirection() {
        return this.direction;
    }

    /**
     * Getter for new number of points after placing the card
     * @return the number of points after placing card
     */
    public int getNumPoints() {
        return this.numPoints;
    }

    /**
     * Implementation of the visitor pattern
     * @param visitor {@link MessageToClientVisitor} visitor of the message
     */
    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof AnswerToActionMessageVisitor) ((AnswerToActionMessageVisitor) visitor).visit(this);
    }

}
