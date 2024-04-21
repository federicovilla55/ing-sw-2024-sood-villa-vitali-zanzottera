package it.polimi.ingsw.gc19.ObserverPattern;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

public interface ObservableMessageToClient<T extends MessageToClient>{

    void attachObserver(String nick, ObserverMessageToClient<T> observerMessageToClient);
    void attachObserver(ObserverMessageToClient<MessageToClient> observerMessageToClient);
    void removeObserver(ObserverMessageToClient<T> observerMessageToClient);
    void removeObserver(String nick);
    void notifyAnonymousObservers(MessageToClient message);
    void notifyNamedObservers(MessageToClient message);

}
