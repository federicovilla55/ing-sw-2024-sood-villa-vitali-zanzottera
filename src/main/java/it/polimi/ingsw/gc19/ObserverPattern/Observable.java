package it.polimi.ingsw.gc19.ObserverPattern;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

public interface Observable<T extends MessageToClient>{

    void attachObserver(String nick, Observer<T> observer);

    void removeObserver(Observer<T> observer);

    void notifyAnonymousObservers(MessageToClient message);

    void notifyNamedObservers(MessageToClient message);

    void notifyObservers(MessageToClient message);

}
