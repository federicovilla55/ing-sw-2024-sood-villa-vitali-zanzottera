package it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners;

import it.polimi.ingsw.gc19.View.GameLocalView.LocalTable;
import it.polimi.ingsw.gc19.View.Listeners.Listener;

/**
 * Classes that implement this interface are going
 * to receive updates about events on {@link LocalTable}
 */
public interface TableListener extends Listener {

    /**
     * This method is used to notify listeners that {@link LocalTable}
     * has changed
     * @param localTable the {@link LocalTable} that has changed
     */
    void notify(LocalTable localTable);

}