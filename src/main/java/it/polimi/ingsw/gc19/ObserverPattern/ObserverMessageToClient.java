package it.polimi.ingsw.gc19.ObserverPattern;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

public interface ObserverMessageToClient<T extends MessageToClient>{

    /**
     * This method is used by {@link ObservableMessageToServer<T>} to
     * notify {@link ObserverMessageToServer<T>} with a new {@link T} message.
     * @param message the {@link T} message to be broadcast.
     */
    void update(T message);

}
