package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;

public class JoinGameMessage implements GameHandlingMessage{

    private final String gameName;
    private final String nickname;

    public JoinGameMessage(String gameName, String nickname){
        this.gameName = gameName;
        this.nickname = nickname;
    }

    public String getGameName(){
        return this.gameName;
    }

    public String getNickname(){
        return this.nickname;
    }

}
