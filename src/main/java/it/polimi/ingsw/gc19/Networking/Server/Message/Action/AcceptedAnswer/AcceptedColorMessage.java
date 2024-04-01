package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.Color;

public class AcceptedColorMessage extends AcceptedActionMessage{

    private final Color chosenColor;

    public AcceptedColorMessage(Color chosenColor){
        this.chosenColor = chosenColor;
    }

    public Color getChosenColor() {
        return this.chosenColor;
    }

}
