package it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners;

import it.polimi.ingsw.gc19.View.GameLocalView.LocalStationPlayer;
import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;

public interface StationListener {

    void notify(PersonalStation localStationPlayer);

    void notify(OtherStation otherStation);

    void notify(String ... varArgs);

}
