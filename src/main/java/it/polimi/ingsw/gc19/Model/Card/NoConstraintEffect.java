package it.polimi.ingsw.gc19.Model.Card;

import it.polimi.ingsw.gc19.Station.Station;

public class NoConstraintEffect implements PlayableEffect{
    private final int cardValue;

    protected NoConstraintEffect(int cardValue){
        this.cardValue = cardValue;
    }

    @Override
    public int countPoints(Station station, PlayableCard card){
        return cardValue;
    }

    @Override
    public String getEffectDescription() {
        return cardValue + " points";
    }

}
