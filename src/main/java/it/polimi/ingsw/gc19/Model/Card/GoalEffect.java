package it.polimi.ingsw.gc19.Model.Card;

import it.polimi.ingsw.gc19.Station.Station;

public interface GoalEffect{

    public int countPoints(Station station);
    public String getEffectDescription();

}
