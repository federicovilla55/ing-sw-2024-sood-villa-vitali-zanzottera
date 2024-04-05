package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageVisitor;

public class JoinGameMessage /*extends GameHandlingMessage*/{

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
