package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;
import it.polimi.ingsw.gc19.Utils.Tuple;

/**
 * This message is sent by server to all games players, different from who
 * placed a card, to notify the updates of his station
 */
public class OtherAcceptedPickCardFromDeckMessage extends AcceptedPickCardMessage {

    /**
     * Infos about back of picked card. Others players may
     * only know the type and the symbol of the picked card
     */
    private final Tuple<Symbol, PlayableCardType> backPickedCard;

    public OtherAcceptedPickCardFromDeckMessage(String nick, Tuple<Symbol,PlayableCardType> backPickedCard, PlayableCardType deckType, Symbol symbolOnDeck) {
        super(nick, null, deckType, symbolOnDeck);
        this.backPickedCard = backPickedCard;
    }

    /**
     * Implementation of the visitor pattern
     * @param visitor {@link MessageToClientVisitor} visitor of the message
     */
    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof AnswerToActionMessageVisitor) ((AnswerToActionMessageVisitor) visitor).visit(this);
    }

    /**
     * Getter for {@link #backPickedCard}
     * @return {@link #backPickedCard}
     */
    public Tuple<Symbol, PlayableCardType> getBackPickedCard() {
        return backPickedCard;
    }

}