package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageVisitor;

public class AcceptedPickCardFromDeckMessage extends AcceptedPickCardMessage{
    public AcceptedPickCardFromDeckMessage(String nick, PlayableCard pickedCard, Symbol symbol) {
        super(nick, pickedCard, symbol);
    }

    @Override
    public void visit(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
