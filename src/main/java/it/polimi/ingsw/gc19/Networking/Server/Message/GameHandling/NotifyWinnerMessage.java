package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;

import java.util.ArrayList;

public class NotifyWinnerMessage extends GameHandlingMessage{

    private final ArrayList<String> winners;

    public NotifyWinnerMessage(ArrayList<String> winners) {
        this.winners = winners;
    }

    public ArrayList<String> getWinners() {
        return winners;
    }
    
}
