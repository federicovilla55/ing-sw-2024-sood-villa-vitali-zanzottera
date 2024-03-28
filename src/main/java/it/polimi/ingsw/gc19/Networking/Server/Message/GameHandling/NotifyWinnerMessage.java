package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import java.util.ArrayList;

public class NotifyWinnerMessage implements GameHandlingMessage{
    private final ArrayList<String> winners;

    public NotifyWinnerMessage(ArrayList<String> winners){
        this.winners = winners;
    }

    public ArrayList<String> getWinners() {
        return this.winners;
    }

}
