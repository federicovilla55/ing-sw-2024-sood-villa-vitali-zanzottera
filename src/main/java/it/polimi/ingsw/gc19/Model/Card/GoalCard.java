package it.polimi.ingsw.gc19.Model.Card;

import it.polimi.ingsw.gc19.Model.Station.Station;

public class GoalCard extends Card{

    private final GoalEffect goalEffect;

    protected GoalCard(String cardCode, GoalEffect goalEffect){
        super(cardCode);
        this.goalEffect = goalEffect;
    }

    @Override
    public String getCardDescription(){
        return this.goalEffect.getEffectDescription();
    }

    @Override
    public int countPoints(Station station){
        return this.goalEffect.countPoints(station, this);
    }

}
