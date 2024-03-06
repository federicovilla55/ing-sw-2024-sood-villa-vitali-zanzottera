package it.polimi.ingsw.gc19.Model.Card;

import it.polimi.ingsw.gc19.Model.Enums.Direction;
import it.polimi.ingsw.gc19.Model.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Station.Station;

import java.util.List;

public class PatternEffect implements GoalEffect{

    private final int cardValue;
    private final List<Direction> direction;
    private final List<Symbol> requiredSymbol;

    protected PatternEffect(int cardValue, List<Direction> direction, List<Symbol> requiredSymbol){
        this.cardValue = cardValue;
        this.direction = direction;
        this.requiredSymbol = requiredSymbol;
    }

    @Override
    public int countPoints(Station station, GoalCard card){
        return 0; //TODO: implement method
    }

    @Override
    public String getEffectDescription(){
        return "Type: goal card based on card pattern" +
               "Points per pattern: " + String.valueOf(this.cardValue) +
               "Pattern required: " + "... qualcosa ...";
    }

}
