package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameEvents;

import it.polimi.ingsw.gc19.Enums.Symbol;

import java.util.ArrayList;
import java.util.HashMap;

public class EndGameMessage extends NotifyEventOnGame{

    private final ArrayList<String> winnerNicks;
    private final HashMap<Symbol, Integer> updatedPoints;

    public EndGameMessage(ArrayList<String> winnerNicks, HashMap<Symbol, Integer> updatedPoints) {
        this.winnerNicks = winnerNicks;
        this.updatedPoints = updatedPoints;
    }

    public ArrayList<String> getWinnerNicks() {
        return this.winnerNicks;
    }

    public HashMap<Symbol, Integer> getUpdatedPoints() {
        return this.updatedPoints;
    }

}
