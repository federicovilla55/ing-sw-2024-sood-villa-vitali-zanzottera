package it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.GameEventsMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

import java.util.List;

/**
 * This message contains a list of available colors
 */
public class AvailableColorsMessage extends NotifyEventOnGame{

    /**
     * Available colors
     */
    private final List<Color> availableColors;

    public AvailableColorsMessage(List<Color> availableColors) {
        this.availableColors = availableColors;
    }

    /**
     * Getter for list of available colors
     * @return the available colors for the game
     */
    public List<Color> getAvailableColors() {
        return this.availableColors;
    }

    /**
     * Implementation of the visitor pattern
     * @param visitor {@link MessageToClientVisitor} visitor of the message
     */
    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof GameEventsMessageVisitor) ((GameEventsMessageVisitor) visitor).visit(this);
    }

}
