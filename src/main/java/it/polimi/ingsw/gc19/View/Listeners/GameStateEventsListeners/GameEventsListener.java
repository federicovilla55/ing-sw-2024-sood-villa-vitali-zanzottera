package it.polimi.ingsw.gc19.View.Listeners.GameStateEventsListeners;

public interface GameEventsListener{

    void notifyEvent(GameEvents type, String ... optArgs);

    void notifyEvent(GameEvents type);

}
