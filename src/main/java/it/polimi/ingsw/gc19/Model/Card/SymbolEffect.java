package it.polimi.ingsw.gc19.Model.Card;

import it.polimi.ingsw.gc19.Model.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Station.Station;

import java.util.HashMap;

class SymbolEffect implements GoalEffect, PlayableEffect{

    private final HashMap<Symbol, Integer> requiredSymbol;
    private final int effectValue;

    protected SymbolEffect(HashMap<Symbol, Integer> requiredSymbol, int effectValue){
        this.requiredSymbol = requiredSymbol;
        this.effectValue = effectValue;
    }

    @Override
    public String getEffectDescription(){
        return "Points per pattern: " + this.effectValue +
               "Required pattern: " + this.requiredSymbol.toString();

    }

    @Override
    public int countPoints(Station station){
        return this.effectValue * station.getVisibleSymbolsInStation().entrySet().stream()
                                                                                 .mapToInt(s -> {
                                                                                       if (requiredSymbol.get(s.getKey()) != 0){
                                                                                           return s.getValue() / requiredSymbol.get(s.getKey());
                                                                                       }
                                                                                       else{
                                                                                           return 0;
                                                                                       }
                                                                                 })
                                                                                 .min()
                                                                                 .orElse(0);

    }

}
