package it.polimi.ingsw.gc19.Networking.Client.Message.Action;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

public class ChosenColorMessage extends ActionMessage{

    private final Color chosenColor;

    public ChosenColorMessage(String nickname, Color chosenColor) {
        super(nickname);
        this.chosenColor = chosenColor;
    }

    public Color getChosenColor() {
        return this.chosenColor;
    }

    @Override
    public void accept(MessageToServerVisitor visitor) {
        if(visitor instanceof ActionMessageVisitor) ((ActionMessageVisitor) visitor).visit(this);
    }

}
