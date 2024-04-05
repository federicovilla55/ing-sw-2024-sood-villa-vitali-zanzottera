package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Tuple;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageVisitor;

public class AcceptedPickCardFromTable extends AcceptedPickCardMessage{

    private final int position;
    private final PlayableCard cardToPutInSlot;

    public AcceptedPickCardFromTable(String nick, PlayableCard pickedCard, Symbol symbol, int position, PlayableCard cardToPutInSlot) {
        super(nick, pickedCard, symbol);
        this.cardToPutInSlot = cardToPutInSlot;
        this.position = position;
    }

    public int getCoords(){
        return this.position;
    }

    public PlayableCard getCardToPutInSlot() {
        return this.cardToPutInSlot;
    }

    @Override
    public void visit(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
