package it.polimi.ingsw.gc19.Card;

import it.polimi.ingsw.gc19.Other.Tuple;
import it.polimi.ingsw.gc19.Station.Station;

import java.util.List;

public class PatternEffect implements GoalEffect{

    private final int cardValue;
    private final List<Tuple<Integer, Integer>> direction;
    private final List<Symbol> requiredSymbol;

    public PatternEffect(int cardValue, List<Tuple<Integer, Integer>> direction, List<Symbol> requiredSymbol){

        this.cardValue = cardValue;
        this.direction = direction;
        this.requiredSymbol = requiredSymbol;

    }

    @Override
    public int countPoints(Station station){
        return 0; //TODO: implement method
    }

    @Override
    public String getEffectDescription(){

        return "Type: goal card based on card pattern" +
               "Points per pattern: " + String.valueOf(this.cardValue) +
               "Pattern required: " + "... qualcosa ...";

    }

}
