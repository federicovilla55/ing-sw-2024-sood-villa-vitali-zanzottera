package it.polimi.ingsw.gc19.ObserverPattern;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;

import java.net.Socket;

public interface ObserverMessageToServer<T extends MessageToServer>{
    void update(Socket senderSocket, MessageToServer message);
    boolean accept(T message);

}
