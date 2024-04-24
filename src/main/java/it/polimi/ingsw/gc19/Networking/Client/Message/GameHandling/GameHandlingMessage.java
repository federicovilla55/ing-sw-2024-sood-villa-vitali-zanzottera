package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

/**
 * This abstract class represent a generic type of message
 * regarding handling game, such as create a new game or join it.
 */
public abstract class GameHandlingMessage extends MessageToServer {
    protected GameHandlingMessage(String nickname) {
        super(nickname);
    }

}
