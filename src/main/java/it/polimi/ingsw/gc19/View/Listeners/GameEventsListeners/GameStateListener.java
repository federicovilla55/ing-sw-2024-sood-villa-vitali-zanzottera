package it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners;

import it.polimi.ingsw.gc19.View.Listeners.Listener;
import it.polimi.ingsw.gc19.View.Listeners.ListenersManager;

public interface GameStateListener extends Listener {

    void notify(GameEvents gameEvents);

}
