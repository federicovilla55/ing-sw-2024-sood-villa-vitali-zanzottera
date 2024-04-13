package it.polimi.ingsw.gc19.Networking.Client.Message.Action;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

public class ChosenGoalCardMessage extends ActionMessage{

    private final int cardIdx;

    public ChosenGoalCardMessage(String nickname, int cardIdx){
        super(nickname);
        this.cardIdx = cardIdx;
    }

    public int getCardIdx() {
        return this.cardIdx;
    }

    @Override
    public void accept(MessageToServerVisitor visitor){
        if(visitor instanceof ActionMessageVisitor) ((ActionMessageVisitor) visitor).visit(this);
    }

}
