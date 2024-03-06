package it.polimi.ingsw.gc19.Model.Card;

import it.polimi.ingsw.gc19.Model.Enums.Symbol;
import it.polimi.ingsw.gc19.Station.Station;

import java.util.HashMap;

public class GoldEffect implements PlayableEffect{

    private final int cardValue;
    private final HashMap<Symbol, Integer> requiredSymbolToEffect;

    protected GoldEffect(int cardValue, HashMap<Symbol, Integer> requiredSymbolToEffect){
        this.cardValue = cardValue;
        this.requiredSymbolToEffect = requiredSymbolToEffect;
    }

    @Override
    public int countPoints(Station station, PlayableCard card){

        return this.cardValue * station.getVisibleSymbolsInStation().entrySet().stream()
                                       .mapToInt(s -> {
                                                            if (requiredSymbolToEffect.get(s.getKey()) != 0){
                                                                return s.getValue() / requiredSymbolToEffect.get(s.getKey());
                                                            }
                                                            else{
                                                                return 0;
                                                            }
                                       })
                                       .min()
                                       .orElse(0);

    }

    @Override
    public String getEffectDescription() {
        return " " + this.cardValue + "for every tuple with " + this.requiredSymbolToEffect.toString();
    }
}
