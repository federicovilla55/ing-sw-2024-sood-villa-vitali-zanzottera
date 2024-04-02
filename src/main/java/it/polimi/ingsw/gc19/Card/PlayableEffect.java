package it.polimi.ingsw.gc19.Card;

import it.polimi.ingsw.gc19.Station.Station;

public interface PlayableEffect{
    String getEffectDescription();
    int countPoints(Station station, Card card);
}
