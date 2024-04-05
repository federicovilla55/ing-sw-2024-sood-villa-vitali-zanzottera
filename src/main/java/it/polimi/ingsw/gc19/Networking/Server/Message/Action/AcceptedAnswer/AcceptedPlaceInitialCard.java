package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.ErrorType;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageVisitor;

import java.util.HashMap;
import java.util.Map;

public class AcceptedPlaceInitialCard extends AcceptedActionMessage{

    private final String nick;
    private final Map<Symbol, Integer> visibleSymbol;
    private final PlayableCard initialCard;

    public AcceptedPlaceInitialCard(String nick, PlayableCard initialCard, Map<Symbol, Integer> visibleSymbol){
        this.nick = nick;
        this.initialCard = initialCard;
        this.visibleSymbol = visibleSymbol;
    }

    public Map<Symbol, Integer> getVisibleSymbol() {
        return this.visibleSymbol;
    }

    public PlayableCard getInitialCard() {
        return this.initialCard;
    }

    @Override
    public void visit(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
