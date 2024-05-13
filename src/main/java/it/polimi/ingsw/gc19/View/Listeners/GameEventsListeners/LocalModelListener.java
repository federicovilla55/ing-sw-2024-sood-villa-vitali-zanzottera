package it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners;

import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.Listeners.Listener;

/**
 * Classes that implements this interface are going to
 * receive updates about {@link LocalModel} events.
 */
public interface LocalModelListener extends Listener {

    /**
     * This method is used to notify a generic event regarding {@link LocalModel}
     * @param type a {@link LocalModelEvents} representing the event type
     * @param localModel the {@link LocalModel} on which the event happened
     * @param varArgs eventual arguments
     */
    void notify(LocalModelEvents type, LocalModel localModel, String ... varArgs);

    /**
     * This method is used to notify listeners about errors
     * @param error a {@link String} description of the error
     */
    void notify(String error);

}