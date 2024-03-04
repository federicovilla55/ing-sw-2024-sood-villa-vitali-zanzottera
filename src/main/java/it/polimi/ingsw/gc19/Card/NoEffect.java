package it.polimi.ingsw.gc19.Card;

import it.polimi.ingsw.gc19.Station.Station;

public class NoEffect implements PlayableEffect{
    private final int cardValue;

    protected NoEffect(int cardValue){
        this.cardValue = cardValue;
    }

    @Override
    public int countPoints(Station station){
        return cardValue;
    }

    @Override
    public String getEffectDescription() {
        return cardValue + " points";
    }

}
