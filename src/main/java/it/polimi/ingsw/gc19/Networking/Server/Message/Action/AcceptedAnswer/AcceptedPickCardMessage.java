package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;

/**
 * This class is used by server to notify connected clients
 * that someone has picked a card and this action has been accepted
 */
public abstract class AcceptedPickCardMessage extends AcceptedActionMessage{

    /**
     * Nick of player for which pick card action has been accepted
     */
    private final String nick;

    /**
     * The cad that has been picked
     */
    private final PlayableCard pickedCard;

    /**
     * The type of the deck from which card has been picked
     */
    private final PlayableCardType deckType;

    /**
     * Next symbol on top of deck
     */
    private final Symbol symbol;

    protected AcceptedPickCardMessage(String nick, PlayableCard pickedCard, PlayableCardType deckType, Symbol symbol) {
        this.nick = nick;
        this.pickedCard = pickedCard;
        this.deckType = deckType;
        this.symbol = symbol;
    }

    /**
     * Getter for nickname of player who requested to pick a card
     * @return name of player who requested to pik a card
     */
    public String getNick() {
        return this.nick;
    }

    /**
     * Getter for picked card
     * @return the picked card
     */
    public PlayableCard getPickedCard() {
        return this.pickedCard;
    }

    /**
     * Getter for type of deck from which card has been picked
     * @return type of deck where card has been picked
     */
    public PlayableCardType getDeckType() {
        return this.deckType;
    }

    /**
     * Getter for symbol of picked card
     * @return picked card's seed
     */
    public Symbol getSymbol() {
        return this.symbol;
    }

}
