package it.polimi.ingsw.gc19.Networking.Server.Message.InitialConfiguration;

import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;

import java.util.HashMap;

public class InitialStationConfigurationMessage extends InitialConfigurationMessage{

    private final String nick;
    private final HashMap<Symbol, Integer> visibleSymbols;
    private final int numPoints;
    private final PlayableCard initialCard;
    private final GoalCard goalCard1;
    private final GoalCard goalCard2;

    public InitialStationConfigurationMessage(String nick, HashMap<Symbol, Integer> visibleSymbols, int numPoints, PlayableCard initialCard, GoalCard goalCard1, GoalCard goalCard2){
        this.nick = nick;
        this.visibleSymbols = visibleSymbols;
        this.numPoints = numPoints;
        this.initialCard = initialCard;
        this.goalCard1 = goalCard1;
        this.goalCard2 = goalCard2;
    }

    public String getNick() {
        return this.nick;
    }

    public HashMap<Symbol, Integer> getVisibleSymbols() {
        return this.visibleSymbols;
    }

    public int getNumPoints() {
        return this.numPoints;
    }

    public PlayableCard getInitialCard() {
        return this.initialCard;
    }

    public GoalCard getGoalCard1() {
        return this.goalCard1;
    }

    public GoalCard getGoalCard2() {
        return this.goalCard2;
    }

}
