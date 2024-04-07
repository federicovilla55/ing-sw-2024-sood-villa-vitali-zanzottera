package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public class OtherAcceptedPickCardFromDeckMessage extends AcceptedPickCardMessage {
    public OtherAcceptedPickCardFromDeckMessage(String nick, PlayableCardType deckType, Symbol symbol) {
        super(nick, null, deckType, symbol);
    }

    @Override
    public void visit(MessageToClientVisitor visitor) {
        visitor.visit(this);
    }

}
