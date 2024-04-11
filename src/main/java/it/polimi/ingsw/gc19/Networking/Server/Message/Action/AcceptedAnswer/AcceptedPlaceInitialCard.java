package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

import java.util.Map;

/**
 * This message is used by server to notify to all active
 * game players that an initial card has been placed
 */
public class AcceptedPlaceInitialCard extends AcceptedActionMessage{

    private final String nick;
    private final Map<Symbol, Integer> visibleSymbol;
    private final PlayableCard initialCard;

    public AcceptedPlaceInitialCard(String nick, PlayableCard initialCard, Map<Symbol, Integer> visibleSymbol){
        this.nick = nick;
        this.initialCard = initialCard;
        this.visibleSymbol = visibleSymbol;
    }

    /**
     * Getter for updated hashmap of visible symbols after placing initial card
     * @return hew hashmap of visible symbols after placing initial card
     */
    public Map<Symbol, Integer> getVisibleSymbol() {
        return this.visibleSymbol;
    }

    /**
     * Getter for the initial placed
     * @return the initial card placed
     */
    public PlayableCard getInitialCard() {
        return this.initialCard;
    }

    /**
     * Getter for nickname of player who placed the initial card
     * @return nickname of player who placed initial card
     */
    public String getNick() {
        return this.nick;
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
