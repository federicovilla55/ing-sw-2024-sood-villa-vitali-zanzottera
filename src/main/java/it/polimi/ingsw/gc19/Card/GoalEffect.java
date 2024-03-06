package it.polimi.ingsw.gc19.Card;

import it.polimi.ingsw.gc19.Station.Station;

public interface GoalEffect{

    public int countPoints(Station station, GoalCard card);
    public String getEffectDescription();

}
