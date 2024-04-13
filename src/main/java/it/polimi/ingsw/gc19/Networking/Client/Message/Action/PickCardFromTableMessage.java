package it.polimi.ingsw.gc19.Networking.Client.Message.Action;

import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

public class PickCardFromTableMessage extends ActionMessage{

    private final PlayableCardType type;
    private final int position;

    public PickCardFromTableMessage(String nickname, PlayableCardType type, int position) {
        super(nickname);
        this.type = type;
        this.position = position;
    }

    public PlayableCardType getType() {
        return this.type;
    }

    public int getPosition() {
        return this.position;
    }

    @Override
    public void accept(MessageToServerVisitor visitor) {
        if(visitor instanceof ActionMessageVisitor) ((ActionMessageVisitor) visitor).visit(this);
    }

}
