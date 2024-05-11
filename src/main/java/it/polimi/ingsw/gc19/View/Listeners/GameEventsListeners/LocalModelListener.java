package it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners;

import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.Listeners.Listener;

public interface LocalModelListener extends Listener {

    void notify(LocalModelEvents type, LocalModel localModel, String ... varArgs);

    void notify(String error);

}
