package it.polimi.ingsw.gc19.Model.Card;

import it.polimi.ingsw.gc19.Model.Station.Station;

interface GoalEffect{

    int countPoints(Station station);
    String getEffectDescription();

}
