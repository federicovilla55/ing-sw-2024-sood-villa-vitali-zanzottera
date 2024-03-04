package it.polimi.ingsw.gc19.Card;

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
    public int countPoints(Station station){
        //TODO: implement this method
        return 0;
    }

    @Override
    public String getEffectDescription() {
        return " " + this.cardValue + "for every tuple with " + this.requiredSymbolToEffect.toString();
    }
}
