package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

import java.util.Map;

public class AcceptedPlaceCardMessage extends AcceptedActionMessage{

    private final String nick;
    private final String anchorCode;
    private final PlayableCard cardToPlace;
    private final Direction direction;
    private final int numPoints;
    private final Map<Symbol, Integer> visibleSymbols;

    public AcceptedPlaceCardMessage(String nick, String anchorCode, PlayableCard cardToPlace, Direction direction, Map<Symbol, Integer> visibleSymbols, int numPoints){
        super();
        this.anchorCode = anchorCode;
        this.cardToPlace = cardToPlace;
        this.direction = direction;
        this.visibleSymbols = visibleSymbols;
        this.numPoints = numPoints;
        this.nick = nick;
    }

    public String getAnchorCode() {
        return this.anchorCode;
    }

    public PlayableCard getCardToPlace() {
        return this.cardToPlace;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public int getNumPoints() {
        return this.numPoints;
    }

    public Map<Symbol, Integer> getVisibleSymbols(){
        return this.visibleSymbols;
    }

    public String getNick() {
        return this.nick;
    }

    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof AnswerToActionMessageVisitor) ((AnswerToActionMessageVisitor) visitor).visit(this);
    }

}
