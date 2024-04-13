package it.polimi.ingsw.gc19.Networking.Client.Message.Heartbeat;

public interface HeartBeatMessageVisitor{
    void visit(HeartBeatMessage message);

}
