package it.polimi.ingsw.gc19.View.Listeners.StateListeners;

import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.Listeners.Listener;

public interface StateListener extends Listener {
    void notify(ViewState viewState);

    void notify(String error);

}
