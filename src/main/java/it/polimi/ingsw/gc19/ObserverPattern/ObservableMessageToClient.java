package it.polimi.ingsw.gc19.ObserverPattern;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;

public interface ObservableMessageToClient<T extends MessageToClient>{

    /**
     * This method is used by named {@link ObserverMessageToServer<T>} (e.g. {@link ClientHandler})
     * to register themselves to the broadcasting list of {@link ObservableMessageToServer<T>}.
     * @param name the name of the {@link ObserverMessageToServer<T>}
     * @param observerMessageToClient the {@link ObserverMessageToServer<T>} to be registered.
     */
    void attachObserver(String name, ObserverMessageToClient<T> observerMessageToClient);

    /**
     * This method is used by unnamed observers (e.g. unnamed logger) to register
     * themselves to the broadcasting list of {@link ObservableMessageToServer<T>}.
     * @param observerMessageToClient the {@link ObserverMessageToServer<T>} to register.
     */
    void attachObserver(ObserverMessageToClient<T> observerMessageToClient);

    /**
     * This method is used by {@link ObserverMessageToServer<T>} (both named and unnamed)
     * to unregister themselves from broadcasting list of {@link ObservableMessageToServer<T>}
     * @param observerMessageToClient the {@link ObserverMessageToServer<T>} to remove.
     */
    void removeObserver(ObserverMessageToClient<T> observerMessageToClient);

    /**
     * This method is used only by named {@link ObserverMessageToServer<T>} (e.g. {@link ClientHandler})
     * to unregister themselves from broadcasting list of {@link ObservableMessageToServer<T>}.
     * @param name the name of the {@link ObserverMessageToServer<T>} to unregister.
     */
    void removeObserver(String name);

    /**
     * This method is used by {@link ObservableMessageToServer<T>} to notify unnamed
     * observers (e.g. unnamed loggers) with a {@link MessageToClient}.
     * @param message the {@link MessageToClient} to be broadcast.
     */
    void notifyAnonymousObservers(MessageToClient message);

    /**
     * This method is used by {@link ObservableMessageToServer<T>} to notify named
     * observers (e.g. {@link ClientHandler} with a {@link MessageToClient}.
     * @param message the {@link MessageToClient} to be broadcast.
     */
    void notifyNamedObservers(MessageToClient message);

}
