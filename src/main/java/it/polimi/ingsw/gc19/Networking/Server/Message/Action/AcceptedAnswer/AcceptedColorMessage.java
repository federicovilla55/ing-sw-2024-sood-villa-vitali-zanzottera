package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is used to tell to player that
 * server has accepted his color choice
 */
public class AcceptedColorMessage extends AcceptedActionMessage{

    private final String player;
    private final Color chosenColor;

    public AcceptedColorMessage(String player, Color chosenColor){
        this.chosenColor = chosenColor;
        this.player = player;
    }

    /**
     * Getter for color chosen by player
     * @return color for player accepted by server
     */
    public Color getChosenColor() {
        return this.chosenColor;
    }

    /**
     * Getter for player name associated to message
     * @return player's name associated to message
     */
    public String getPlayer() {
        return this.player;
    }

    /**
     * Implementation of the visitor pattern
     * @param visitor {@link MessageToClientVisitor} visitor of the message
     */
    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof AnswerToActionMessageVisitor) ((AnswerToActionMessageVisitor) visitor).visit(this);
    }

}
