package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Enums.Color;

public class AcceptedColorMessage extends AcceptedActionMessage{

    private final String player;
    private final Color chosenColor;

    public AcceptedColorMessage(String player, Color chosenColor){
        this.chosenColor = chosenColor;
        this.player = player;
    }

    public Color getChosenColor() {
        return this.chosenColor;
    }

    public String getPlayer() {
        return this.player;
    }
}
