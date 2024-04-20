package it.polimi.ingsw.gc19.ObserverPattern;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;

public interface ObservableMessageToServer<T extends MessageToServer>{
    void attachObserver(ObserverMessageToServer<T> observer);
    void removeObserver(ObserverMessageToServer<T> observer);
}
