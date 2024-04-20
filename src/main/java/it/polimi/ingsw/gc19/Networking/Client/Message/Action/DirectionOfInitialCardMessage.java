package it.polimi.ingsw.gc19.Networking.Client.Message.Action;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

public class DirectionOfInitialCardMessage extends ActionMessage{
    private final CardOrientation cardOrientation;

    public DirectionOfInitialCardMessage(String nickname, CardOrientation orientation) {
        super(nickname);
        this.cardOrientation = orientation;
    }

    public CardOrientation getDirectionOfInitialCard() {
        return this.cardOrientation;
    }

    public void accept(ActionMessageVisitor visitor){
        visitor.visit(this);
    }

    @Override
    public void accept(MessageToServerVisitor visitor){
        if(visitor instanceof ActionMessageVisitor) ((ActionMessageVisitor) visitor).visit(this);
    }

}
