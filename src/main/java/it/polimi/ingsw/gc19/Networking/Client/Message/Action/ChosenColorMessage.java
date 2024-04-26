package it.polimi.ingsw.gc19.Networking.Client.Message.Action;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

/**
 * This message is used when client wants to notify server that
 * it has chosen its color
 */
public class ChosenColorMessage extends ActionMessage{

    private final Color chosenColor;

    public ChosenColorMessage(String nickname, Color chosenColor) {
        super(nickname);
        this.chosenColor = chosenColor;
    }

    /**
     * Getter for {@link Color} chosen by the user
     * @return the {@link Color} chosen by the player
     */
    public Color getChosenColor() {
        return this.chosenColor;
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
