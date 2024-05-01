package it.polimi.ingsw.gc19.Networking.Client.Message.Heartbeat;

/**
 * This interface must be implemented by classes that
 * want to visit {@link ClientHeartBeatMessage}
 */
public interface HeartBeatMessageVisitor{

    /**
     * This method is used by {@link HeartBeatMessageVisitor} to visit
     * a message {@link ClientHeartBeatMessage}
     * @param message the {@link ClientHeartBeatMessage} to visit
     */
    void visit(ClientHeartBeatMessage message);

}
