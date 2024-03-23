package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Station.Station;

import java.util.HashMap;

@JsonTypeName("symbol")
class SymbolEffect implements GoalEffect, PlayableEffect{

    private final HashMap<Symbol, Integer> requiredSymbol;
    private final int cardValue;

    /**
     * This constructor creates a symbol effect
     * @param requiredSymbol the required symbols to have effect
     * @param cardValue the points associated to the pattern
     */
    @JsonCreator
    SymbolEffect(@JsonProperty("required") HashMap<Symbol, Integer> requiredSymbol,
                 @JsonProperty("value") int cardValue){
        this.requiredSymbol = requiredSymbol;
        this.cardValue = cardValue;
    }

    @Override
    public String getEffectDescription(){
        return "Points per pattern: " + this.cardValue + "\n" +
               "Required pattern: " + this.requiredSymbol.toString();

    }

    @Override
    public int countPoints(Station station){
        return this.cardValue * this.requiredSymbol
                                    .entrySet()
                                    .stream()
                                    .filter(e -> e.getValue() != 0)
                                    .mapToInt(s -> station.getVisibleSymbolsInStation().get(s.getKey()) / s.getValue())
                                    .min()
                                    .orElse(0);

    }

}
