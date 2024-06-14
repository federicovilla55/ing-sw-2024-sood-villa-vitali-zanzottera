package it.polimi.ingsw.gc19.ObserverPattern;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Server.ServerSocket.MessageToServerDispatcher;

import java.net.Socket;

public interface ObserverMessageToServer<T extends MessageToServer>{
    /**
     * This method is used by {@link ObservableMessageToServer<T>} (e.g. {@link MessageToServerDispatcher})
     * to notify the object that a new message has arrived
     * @param senderSocket {@link Socket} from which message has arrived
     * @param message {@link MessageToServer} arrived
     */
    void update(Socket senderSocket, MessageToServer message);

    /**
     * This method is used by {@link ObserverMessageToServer<T>} to tell
     * {@link  ObservableMessageToServer<T>} that it can accept the specified message
     * @param message {@link T} that could be accepted
     * @return true if and only if message can be accepted
     */
    boolean accept(T message);

}
