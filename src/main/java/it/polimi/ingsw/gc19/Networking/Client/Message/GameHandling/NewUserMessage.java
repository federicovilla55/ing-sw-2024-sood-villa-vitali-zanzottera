package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;

public class NewUserMessage extends GameHandlingMessage{
    private final String nickname;

    public NewUserMessage(String nickname){
        this.nickname = nickname;
    }

    public String getNickname() {
        return this.nickname;
    }

}
