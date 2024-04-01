package it.polimi.ingsw.gc19.Networking.Server;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.ObserverPattern.Observer;

public class ClientHandle implements Observer<MessageToClient>{
    private String name;
    private long GetLastTimeStep;

    void SendMessageToClient() {

    }

    @Override
    public void update(MessageToClient message) {

    }
}
