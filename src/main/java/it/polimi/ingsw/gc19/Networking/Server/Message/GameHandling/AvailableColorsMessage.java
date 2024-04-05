package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageVisitor;

import java.util.ArrayList;
import java.util.List;

public class AvailableColorsMessage extends GameHandlingMessage{

    private final List<Color> availableColors;

    public AvailableColorsMessage(List<Color> availableColors) {
        this.availableColors = availableColors;
    }

    public List<Color> getAvailableColors() {
        return this.availableColors;
    }

    @Override
    public void visit(MessageVisitor visitor) {
        visitor.visit(this);
    }

}
