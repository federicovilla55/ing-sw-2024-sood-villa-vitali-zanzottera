package it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners;

import it.polimi.ingsw.gc19.View.GameLocalView.LocalTable;
import it.polimi.ingsw.gc19.View.Listeners.Listener;

public interface TableListener extends Listener {

    void notify(LocalTable localTable);

}
