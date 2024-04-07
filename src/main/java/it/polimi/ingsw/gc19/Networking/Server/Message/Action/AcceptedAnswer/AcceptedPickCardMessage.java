package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;

public abstract class AcceptedPickCardMessage extends AcceptedActionMessage{

    private final String nick;
    private final PlayableCard pickedCard;

    private final PlayableCardType deckType;
    private final Symbol symbol;

    protected AcceptedPickCardMessage(String nick, PlayableCard pickedCard, PlayableCardType deckType, Symbol symbol) {
        this.nick = nick;
        this.pickedCard = pickedCard;
        this.deckType = deckType;
        this.symbol = symbol;
    }

    public String getNick() {
        return this.nick;
    }

    public PlayableCard getPickedCard() {
        return this.pickedCard;
    }

    public PlayableCardType getDeckType() {
        return this.deckType;
    }

    public Symbol getSymbol() {
        return this.symbol;
    }

}
