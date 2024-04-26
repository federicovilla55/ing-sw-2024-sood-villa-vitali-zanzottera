package it.polimi.ingsw.gc19.Networking.Client.Message.Action;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

/**
 * This message is used by client to notify server
 * that it has chosen his private goal card.
 */
public class ChosenGoalCardMessage extends ActionMessage{

    private final int cardIdx;

    public ChosenGoalCardMessage(String nickname, int cardIdx){
        super(nickname);
        this.cardIdx = cardIdx;
    }

    /**
     * Getter for private goal card index
     * @return the index of the private goal card chosen
     */
    public int getCardIdx() {
        return this.cardIdx;
    }

    /**
     * This method is used by {@link MessageToServerVisitor} to visit the message
     * @param visitor the {@link MessageToServerVisitor} for the message
     */
    @Override
    public void accept(MessageToServerVisitor visitor){
        if(visitor instanceof ActionMessageVisitor) ((ActionMessageVisitor) visitor).visit(this);
    }

}
