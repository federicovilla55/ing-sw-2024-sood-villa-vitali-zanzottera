package it.polimi.ingsw.gc19.Networking.Server.Message.HeartBeat;

public interface HeartBeatMessageVisitor{

    void visit(ServerHeartBeatMessage message);

}
