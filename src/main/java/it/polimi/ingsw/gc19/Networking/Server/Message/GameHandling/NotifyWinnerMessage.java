package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import java.util.List;

public class NotifyWinnerMessage extends GameHandlingMessage{

    private final List<String> winners;

    public NotifyWinnerMessage(List<String> winners) {
        this.winners = winners;
    }

    public List<String> getWinners() {
        return winners;
    }
    
}
