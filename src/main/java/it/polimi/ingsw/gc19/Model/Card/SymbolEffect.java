package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.polimi.ingsw.gc19.Model.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Station.Station;

import java.util.HashMap;
import java.util.Optional;

class SymbolEffect implements GoalEffect, PlayableEffect{

    private final HashMap<Symbol, Integer> requiredSymbol;
    private final int effectValue;

    @JsonCreator
    SymbolEffect(@JsonProperty("required") HashMap<Symbol, Integer> requiredSymbol,
                 @JsonProperty("value") int effectValue){
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
        return this.effectValue * this.requiredSymbol
                                      .entrySet()
                                      .stream()
                                      .filter(e -> e.getValue() != 0)
                                      .mapToInt(s -> station.getVisibleSymbolsInStation().get(s.getKey()) / s.getValue())
                                      .min()
                                      .orElse(0);

    }

}
