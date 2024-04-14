package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

public abstract class GameHandlingMessage extends MessageToServer {
    protected GameHandlingMessage(String nickname) {
        super(nickname);
    }

}
