package it.polimi.ingsw.gc19.View.Listeners.TurnStateListeners;

import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.View.Listeners.Listener;

public interface TurnStateListener extends Listener {

    void notify(String nick, TurnState turnState);

    void notify(String error);

}
