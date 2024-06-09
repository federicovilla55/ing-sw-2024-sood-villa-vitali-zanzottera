package it.polimi.ingsw.gc19.Networking.Client.Message.Action;

import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

/**
 * This message is used by client to notify server
 * that it wants to pick a card from deck
 */
public class PickCardFromDeckMessage extends ActionMessage{

    /**
     * The {@link PlayableCardType} of the deck from which usr would like to pick
     */
    private final PlayableCardType type;

    public PickCardFromDeckMessage(String nickname, PlayableCardType type) {
        super(nickname);
        this.type = type;
    }

    /**
     * Getter for deck type from which user would like to pick
     * @return the {@link PlayableCardType} of the deck from which usr would like to pick
     */
    public PlayableCardType getType(){
        return this.type;
    }

    /**
     * This message is used by {@link MessageToServerVisitor} to visit the message.
     * @param visitor the {@link MessageToServerVisitor} for the message
     */
    @Override
    public void accept(MessageToServerVisitor visitor) {
        if(visitor instanceof ActionMessageVisitor) ((ActionMessageVisitor) visitor).visit(this);
    }

}