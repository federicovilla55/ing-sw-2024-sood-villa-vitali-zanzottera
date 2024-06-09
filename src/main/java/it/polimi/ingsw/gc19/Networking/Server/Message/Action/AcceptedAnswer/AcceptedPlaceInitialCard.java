package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

import java.util.Map;

/**
 * This message is used by server to notify to all active
 * game players that an initial card has been placed
 */
public class AcceptedPlaceInitialCard extends AcceptedPlaceCardMessage{

    /**
     * Initial card whose place has been accepted
     */
    private final PlayableCard initialCard;

    public AcceptedPlaceInitialCard(String nick, PlayableCard initialCard, Map<Symbol, Integer> visibleSymbol){
        super(nick, visibleSymbol);
        this.initialCard = initialCard;
    }

    /**
     * Getter for the initial placed
     * @return the initial card placed
     */
    public PlayableCard getInitialCard() {
        return this.initialCard;
    }


    /**
     * Getter for card to place orientation
     * @return the {@link CardOrientation} of the card to place
     */
    public CardOrientation getOrient(){
        return this.initialCard.getCardOrientation();
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
