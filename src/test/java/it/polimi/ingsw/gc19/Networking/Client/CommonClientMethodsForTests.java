package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

public interface CommonClientMethodsForTests {

    public void waitForMessage(Class<? extends MessageToClient> messageToClientClass);
    public MessageToClient getMessage();
    public MessageToClient getMessage(Class<? extends MessageToClient> messageToClientClass);
    String getNickname();

}
