package it.polimi.ingsw.gc19.ObserverPattern;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

public interface ObserverMessageToClient<T extends MessageToClient>{

    void update(T message);

}
