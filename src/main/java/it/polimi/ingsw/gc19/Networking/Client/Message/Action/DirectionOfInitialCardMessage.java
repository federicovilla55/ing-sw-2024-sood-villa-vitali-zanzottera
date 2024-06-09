package it.polimi.ingsw.gc19.Networking.Client.Message.Action;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

/**
 * This message is used by client to notify server that it wants
 * to place the initial card in the specified direction
 */
public class DirectionOfInitialCardMessage extends ActionMessage{

    /**
     * {@link CardOrientation} of the initial card
     */
    private final CardOrientation cardOrientation;

    public DirectionOfInitialCardMessage(String nickname, CardOrientation orientation) {
        super(nickname);
        this.cardOrientation = orientation;
    }

    /**
     * Getter for {@link CardOrientation} of the initial card
     * @return the {@link CardOrientation} of the initial card
     */
    public CardOrientation getDirectionOfInitialCard() {
        return this.cardOrientation;
    }

    /**
     * This message is used by {@link MessageToServerVisitor} to visit the message.
     * @param visitor the {@link MessageToServerVisitor} for the message
     */
    @Override
    public void accept(MessageToServerVisitor visitor){
        if(visitor instanceof ActionMessageVisitor) ((ActionMessageVisitor) visitor).visit(this);
    }

}