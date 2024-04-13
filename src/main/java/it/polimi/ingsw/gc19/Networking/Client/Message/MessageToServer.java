package it.polimi.ingsw.gc19.Networking.Client.Message;

import java.io.Serializable;
import java.rmi.Remote;

public abstract class MessageToServer implements Serializable{

    private final String nickname;

    protected MessageToServer(String nickname){
        this.nickname = nickname;
    }

    public String getNickname() {
        return this.nickname;
    }

    public abstract void accept(MessageToServerVisitor visitor);

}
