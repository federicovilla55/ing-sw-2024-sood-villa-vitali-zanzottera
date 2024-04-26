package it.polimi.ingsw.gc19.Networking.Client.Message.Heartbeat;

import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.CreateNewGameMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.GameHandlingMessageVisitor;

/**
 * This interface must be implemented by classes that
 * want to visit {@link HeartBeatMessage}
 */
public interface HeartBeatMessageVisitor{

    /**
     * This method is used by {@link HeartBeatMessageVisitor} to visit
     * a message {@link HeartBeatMessage}
     * @param message the {@link HeartBeatMessage} to visit
     */
    void visit(HeartBeatMessage message);

}
