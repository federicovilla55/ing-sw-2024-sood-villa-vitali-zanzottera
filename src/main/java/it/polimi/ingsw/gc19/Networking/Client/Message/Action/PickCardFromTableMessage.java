package it.polimi.ingsw.gc19.Networking.Client.Message.Action;

import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

/**
 * This message is used by client to notify server that
 * it wants to pick a card from table
 */
public class PickCardFromTableMessage extends ActionMessage{

    private final PlayableCardType type;
    private final int position;

    public PickCardFromTableMessage(String nickname, PlayableCardType type, int position) {
        super(nickname);
        this.type = type;
        this.position = position;
    }

    /**
     * Getter for {@link PlayableCardType} of the card to pick
     * @return the {@link PlayableCardType} of the card to pick
     */
    public PlayableCardType getType() {
        return this.type;
    }

    /**
     * Getter for the position on table of the card to pick
     * @return the position on table of the card to pick
     */
    public int getPosition() {
        return this.position;
    }

    /**
     * This method is used by {@link MessageToServerVisitor} to visit the message
     * @param visitor the {@link MessageToServerVisitor} for the message
     */
    @Override
    public void accept(MessageToServerVisitor visitor) {
        if(visitor instanceof ActionMessageVisitor) ((ActionMessageVisitor) visitor).visit(this);
    }

}
