package it.polimi.ingsw.gc19.Card;

import it.polimi.ingsw.gc19.Station.Station;

import java.util.HashMap;

public class SymbolEffect implements GoalEffect{

    private final HashMap<Symbol, Integer> requiredSymbol;
    private final int effectValue;

    protected SymbolEffect(HashMap<Symbol, Integer> requiredSymbol, int effectValue){
        this.requiredSymbol = requiredSymbol;
        this.effectValue = effectValue;
    }

    @Override
    public String getEffectDescription(){

        return "Type: goal card based on symbols" +
               "Points per pattern: " + String.valueOf(this.effectValue) +
               "Required pattern: " + this.requiredSymbol.toString();

    }

    @Override
    public int countPoints(Station station){

        //TODO: implement method

        return 0;

    }

}
