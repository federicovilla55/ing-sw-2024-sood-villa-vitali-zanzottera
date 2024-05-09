package it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners;

import it.polimi.ingsw.gc19.View.Listeners.Listener;

public interface PlayerCreationListener extends Listener {

    void notify(String name);

    void notifyPlayerCreationError(String error);

}
