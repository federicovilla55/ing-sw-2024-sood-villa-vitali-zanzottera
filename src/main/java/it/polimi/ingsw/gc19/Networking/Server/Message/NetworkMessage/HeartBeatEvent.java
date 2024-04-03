package it.polimi.ingsw.gc19.Networking.Server.Message.NetworkMessage;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

public class HeartBeatEvent extends MessageToClient{
    public String nickName;
    HeartBeatEvent(String nickName)
    {
         this.nickName = nickName;
    }
}
