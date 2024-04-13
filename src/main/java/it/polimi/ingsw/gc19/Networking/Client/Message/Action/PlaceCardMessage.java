package it.polimi.ingsw.gc19.Networking.Client.Message.Action;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

public class PlaceCardMessage extends ActionMessage{

    private final String cardToPlaceCode;
    private final String anchorCode;
    private final Direction direction;
    private final CardOrientation cardOrientation;

    public PlaceCardMessage(String playerNickname, String cardToPlaceCode, String anchorCode, Direction direction, CardOrientation cardOrientation){
        super(playerNickname);
        this.cardToPlaceCode = cardToPlaceCode;
        this.anchorCode = anchorCode;
        this.direction = direction;
        this.cardOrientation = cardOrientation;
    }

    public String getCardToPlaceCode() {
        return this.cardToPlaceCode;
    }

    public String getAnchorCode() {
        return this.anchorCode;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public CardOrientation getCardOrientation(){
        return this.cardOrientation;
    }

    @Override
    public void accept(MessageToServerVisitor visitor){
        if(visitor instanceof ActionMessageVisitor) ((ActionMessageVisitor) visitor).visit(this);
    }

}
