package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is sent by server to notify the player who placed a
 * card about the updates in his station
 */
public class OwnAcceptedPickCardFromDeckMessage extends AcceptedPickCardMessage{
    public OwnAcceptedPickCardFromDeckMessage(String nick, PlayableCard pickedCard, PlayableCardType deckType, Symbol symbol) {
        super(nick, pickedCard, deckType, symbol);
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