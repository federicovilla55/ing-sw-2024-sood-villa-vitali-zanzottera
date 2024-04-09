package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public class ReconnectToGameMessage{

    private final String gameToReconnect;
    private final String nickname;

    private final String token;

    public ReconnectToGameMessage(String gameToReconnect, String nickname, String token){
        this.gameToReconnect = gameToReconnect;
        this.nickname = nickname;
        this.token = token;
    }

    public String getGameToReconnect() {
        return this.gameToReconnect;
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getToken(){
        return this.token;
    }

    @Override
    public boolean equals(Object o){
        if(o == null) return false;
        if(! (o instanceof ReconnectToGameMessage)) return false;
        return ((ReconnectToGameMessage) o).gameToReconnect.equals(this.gameToReconnect)
                && ((ReconnectToGameMessage) o ).nickname.equals(this.nickname);
    }
}
