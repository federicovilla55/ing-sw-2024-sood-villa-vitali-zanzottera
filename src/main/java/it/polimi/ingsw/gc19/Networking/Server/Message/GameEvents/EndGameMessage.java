package it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents;

import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

import java.util.List;
import java.util.Map;

/**
 * This message is used to tell to all active players that
 * game has ended. It also contains a (list of) winners
 */
public class EndGameMessage extends NotifyEventOnGame{

    private final List<String> winnerNicks;
    private final Map<String, Integer> updatedPoints;

    public EndGameMessage(List<String> winnerNicks, Map<String, Integer> updatedPoints) {
        this.winnerNicks = winnerNicks;
        this.updatedPoints = updatedPoints;
    }

    /**
     * Getter for winner nicks
     * @return the list of winner nicks
     */
    public List<String> getWinnerNicks() {
        return this.winnerNicks;
    }

    /**
     * Getter for updated points after revealing private goal card
     * @return updated points after revealing private goal card
     */
    public Map<String, Integer> getUpdatedPoints() {
        return this.updatedPoints;
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
