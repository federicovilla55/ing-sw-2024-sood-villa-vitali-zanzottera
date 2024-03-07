package it.polimi.ingsw.gc19.Model.Card;

import it.polimi.ingsw.gc19.Model.Station.Station;

public interface PlayableEffect{
    String getEffectDescription();
    int countPoints(Station station, PlayableCard card);
}
