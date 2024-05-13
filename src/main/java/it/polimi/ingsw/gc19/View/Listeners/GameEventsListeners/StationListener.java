package it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners;

import it.polimi.ingsw.gc19.View.GameLocalView.OtherStation;
import it.polimi.ingsw.gc19.View.GameLocalView.PersonalStation;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalStationPlayer;

/**
 * Classes that implements this interface are going to receive
 * updates about events regarding {@link LocalStationPlayer}
 */
public interface StationListener {

    /**
     * This method is used to notify listeners about {@link PersonalStation} event
     * @param localStationPlayer is the {@link PersonalStation} that has changed
     */
    void notify(PersonalStation localStationPlayer);

    /**
     * This method is used to notify listeners about {@link OtherStation} event
     * @param otherStation is the {@link OtherStation} that has changed
     */
    void notify(OtherStation otherStation);

    /**
     * This method is used to notify error to {@link StationListener}
     * @param varArgs variable arguments describing the error
     */
    void notifyErrorStation(String ... varArgs);

}