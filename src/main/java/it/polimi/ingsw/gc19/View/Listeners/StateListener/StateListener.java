package it.polimi.ingsw.gc19.View.Listeners.StateListener;

import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.Listeners.Listener;

/**
 * Classes that implement this interface are going to receive updates
 * about {@link ViewState} events
 */
public interface StateListener extends Listener{

    /**
     * This method is used to notify {@link StateListener} that an event on
     * {@link ViewState} has happened
     * @param viewState the new {@link ViewState}
     */
    void notify(ViewState viewState);

}