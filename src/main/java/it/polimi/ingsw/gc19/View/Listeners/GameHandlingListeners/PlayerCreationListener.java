package it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners;

import it.polimi.ingsw.gc19.View.Listeners.Listener;

/**
 * Classes that implement this interface are going to receive updates
 * about player creation events (e.g. errors or player effectivly created)
 */
public interface PlayerCreationListener extends Listener {

    /**
     * This method is used to notify listeners that player has
     * been correctly created
     * @param name is the name of the player
     */
    void notifyPlayerCreation(String name);

    /**
     * This method is used to notify to {@link PlayerCreationListener} that
     * an error has occurred.
     * @param error a {@link String} description of the error
     */
    void notifyPlayerCreationError(String error);

}