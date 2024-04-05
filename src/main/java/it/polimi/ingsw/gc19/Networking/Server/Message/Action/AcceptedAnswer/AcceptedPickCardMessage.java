package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageVisitor;

import java.util.ArrayList;

public abstract class AcceptedPickCardMessage extends AcceptedActionMessage{

    private final String nick;
    private final PlayableCard pickedCard;
    private final Symbol symbol;

    protected AcceptedPickCardMessage(String nick, PlayableCard pickedCard, Symbol symbol) {
        this.nick = nick;
        this.pickedCard = pickedCard;
        this.symbol = symbol;
    }

    public String getNick() {
        return this.nick;
    }

    public PlayableCard getPickedCard() {
        return this.pickedCard;
    }

    public Symbol getSymbol() {
        return this.symbol;
    }

}
