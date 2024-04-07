package it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents;

import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

import java.util.List;
import java.util.Map;

public class EndGameMessage extends NotifyEventOnGame{

    private final List<String> winnerNicks;
    private final Map<String, Integer> updatedPoints;

    public EndGameMessage(List<String> winnerNicks, Map<String, Integer> updatedPoints) {
        this.winnerNicks = winnerNicks;
        this.updatedPoints = updatedPoints;
    }

    public List<String> getWinnerNicks() {
        return this.winnerNicks;
    }

    public Map<String, Integer> getUpdatedPoints() {
        return this.updatedPoints;
    }

    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof GameEventsMessageVisitor) ((GameEventsMessageVisitor) visitor).visit(this);
    }

}
