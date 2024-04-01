package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameEvents;

import java.util.ArrayList;

public class EndGameMessage extends NotifyEventOnGame{

    private final ArrayList<String> winnerNicks;

    public EndGameMessage(ArrayList<String> winnerNicks) {
        this.winnerNicks = winnerNicks;
    }

    public ArrayList<String> getWinnerNicks() {
        return this.winnerNicks;
    }

}
