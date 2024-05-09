package it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners;

import it.polimi.ingsw.gc19.View.Listeners.Listener;
import it.polimi.ingsw.gc19.View.Listeners.ListenersManager;

import java.util.List;

public interface GameStateListener extends Listener {

    void notify(GameEvents gameEvents);
    void notify(GameEvents type, List<String> varArgs);

}
