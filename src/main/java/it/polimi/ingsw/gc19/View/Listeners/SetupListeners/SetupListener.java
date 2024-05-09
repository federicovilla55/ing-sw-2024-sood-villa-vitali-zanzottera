package it.polimi.ingsw.gc19.View.Listeners.SetupListeners;

import it.polimi.ingsw.gc19.View.Listeners.Listener;

public interface SetupListener extends Listener {

    void notify(SetupEvent type);

    void notify(String error);

}