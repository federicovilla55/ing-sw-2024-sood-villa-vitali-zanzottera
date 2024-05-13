package it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners;

import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.View.Listeners.Listener;

/**
 * Classes that implements this interface that turns are changed
 */
public interface TurnStateListener extends Listener {

    /**
     * This method is used to notify listeners that an event regarding
     * turn state has happened
     * @param nick the nickname of the player currently playing
     * @param turnState the {@link TurnState} of that player
     */
    void notify(String nick, TurnState turnState);

}