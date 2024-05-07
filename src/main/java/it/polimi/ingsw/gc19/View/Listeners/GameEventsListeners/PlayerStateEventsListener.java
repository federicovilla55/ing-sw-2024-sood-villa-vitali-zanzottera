package it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners;

public interface PlayerStateEventsListener{

    void notifyConnection(String name);

    void notifyDisconnection(String name);

    void notifyReconnection(String name);

}
