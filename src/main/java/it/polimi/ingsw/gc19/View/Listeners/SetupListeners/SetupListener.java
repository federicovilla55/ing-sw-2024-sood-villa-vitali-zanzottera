package it.polimi.ingsw.gc19.View.Listeners.SetupListeners;

import it.polimi.ingsw.gc19.View.Listeners.Listener;

/**
 * Classes that implement this interface are going to receive updates
 * about setup phase
 */
public interface SetupListener extends Listener {

    /**
     * This method is used to notify {@link SetupListener} that a
     * setup event has occurred
     * @param type a {@link SetupEvent} describing the type of the event
     */
    void notify(SetupEvent type);

    /**
     * This method is used to notify {@link SetupListener} that an
     * error has occurred
     * @param type the type of the error
     * @param error a {@link String} description of the error
     */
    void notify(SetupEvent type, String error);

}