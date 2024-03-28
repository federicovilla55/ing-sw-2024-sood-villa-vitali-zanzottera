package it.polimi.ingsw.gc19.Networking.Server.Message.NetworkMessage;

public class HeartBeatEvent extends Event {
    public String nickName;
    HeartBeatEvent(String nickName)
    {
         this.nickName = nickName;
    }
}
