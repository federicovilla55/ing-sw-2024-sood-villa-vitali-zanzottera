package it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners;

import it.polimi.ingsw.gc19.View.Listeners.Listener;

import java.util.List;

public interface GameHandlingListener extends Listener{

    void notify(GameHandlingEvents type, List<String> varArgs);

    void notify(String errorDescription);

}
