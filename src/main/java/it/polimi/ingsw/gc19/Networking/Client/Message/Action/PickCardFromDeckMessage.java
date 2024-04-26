package it.polimi.ingsw.gc19.Networking.Client.Message.Action;

import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

/**
 * This message is used by client to notify server
 * that it wants to pick a card from deck
 */
public class PickCardFromDeckMessage extends ActionMessage{

    private final PlayableCardType type;
    public PickCardFromDeckMessage(String nickname, PlayableCardType type) {
        super(nickname);
        this.type = type;
    }

    public PlayableCardType getType(){
        return this.type;
    }

    @Override
    public void accept(MessageToServerVisitor visitor) {
        if(visitor instanceof ActionMessageVisitor) ((ActionMessageVisitor) visitor).visit(this);
    }

}
