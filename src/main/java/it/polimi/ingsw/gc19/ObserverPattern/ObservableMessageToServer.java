package it.polimi.ingsw.gc19.ObserverPattern;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;

public interface ObservableMessageToServer<T extends MessageToServer>{
    /**
     * This method is used by {@link ObserverMessageToServer<T>} to
     * register themselves to {@link ObservableMessageToServer<T>} broadcasting list.
     * @param observer the {@link ObserverMessageToServer<T>} to register.
     */
    void attachObserver(ObserverMessageToServer<T> observer);

    /**
     * This method is used by {@link ObserverMessageToServer<T>} to
     * unregister themselves from {@link ObservableMessageToServer<T>} broadcasting list.
     * @param observer the {@link ObserverMessageToServer<T>} to unregister.
     */
    void removeObserver(ObserverMessageToServer<T> observer);

}
