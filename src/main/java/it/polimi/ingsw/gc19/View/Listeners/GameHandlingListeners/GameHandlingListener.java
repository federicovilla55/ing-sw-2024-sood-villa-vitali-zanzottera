package it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners;

import it.polimi.ingsw.gc19.View.Listeners.Listener;

import java.util.List;

/**
 * Classes that implement this interface are going to receive
 * updates about game handling events
 */
public interface GameHandlingListener extends Listener{

    /**
     * This method is used to notify listeners that an event regarding
     * game handling has happened.
     * @param type the {@link GameHandlingEvents} type of the event
     * @param varArgs variable {@link String} arguments
     */
    void notify(GameHandlingEvents type, List<String> varArgs);

    /**
     * This method is used to notify errors to {@link GameHandlingListener}
     * @param errorDescription a {@link String} description of the error
     */
    void notify(String errorDescription);

}