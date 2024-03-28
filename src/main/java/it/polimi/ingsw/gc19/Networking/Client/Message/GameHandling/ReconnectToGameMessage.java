package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;

public class ReconnectToGameMessage implements GameHandlingMessage{

    private final String gameToReconnect;
    private final String nickname;

    public ReconnectToGameMessage(String gameToReconnect, String nickname){
        this.gameToReconnect = gameToReconnect;
        this.nickname = nickname;
    }

    public String getGameToReconnect() {
        return this.gameToReconnect;
    }

    public String getNickname() {
        return this.nickname;
    }

}
