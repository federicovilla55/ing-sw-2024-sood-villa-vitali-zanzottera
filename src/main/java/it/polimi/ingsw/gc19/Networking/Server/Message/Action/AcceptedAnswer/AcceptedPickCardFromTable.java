package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public class AcceptedPickCardFromTable extends AcceptedPickCardMessage{

    private final int position;
    private final PlayableCard cardToPutInSlot;

    public AcceptedPickCardFromTable(String nick, PlayableCard pickedCard, Symbol symbol, int position, PlayableCardType deckType, PlayableCard cardToPutInSlot) {
        super(nick, pickedCard, deckType, symbol);
        this.cardToPutInSlot = cardToPutInSlot;
        this.position = position;
    }

    /**
     * Getter for position of card to put on table
     * @return coords of card to put on table
     */
    public int getCoords(){
        return this.position;
    }

    /**
     * Getter for card to put in empty slot on table
     * @return card to place on table
     */
    public PlayableCard getCardToPutInSlot() {
        return this.cardToPutInSlot;
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
