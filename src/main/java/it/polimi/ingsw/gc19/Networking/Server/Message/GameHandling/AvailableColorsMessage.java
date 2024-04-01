package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Enums.Color;

import java.util.ArrayList;

public class AvailableColorsMessage extends GameHandlingMessage{

    private final ArrayList<Color> availableColors;

    public AvailableColorsMessage(ArrayList<Color> availableColors) {
        this.availableColors = availableColors;
    }

    public ArrayList<Color> getAvailableColors() {
        return this.availableColors;
    }

}
