package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

public interface VirtualClient {
    public void GetMessage(MessageToClient message);
}
