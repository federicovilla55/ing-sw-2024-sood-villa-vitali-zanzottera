package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

public class ReconnectToServerMessage extends GameHandlingMessage {
    private final String nickname;
    private final String token;

    public ReconnectToServerMessage(String nickname, String token){
        super(nickname);
        this.nickname = nickname;
        this.token = token;
    }

    public String getNickname() {
        return this.nickname;
    }

    @Override
    public void accept(MessageToServerVisitor visitor) {
        if(visitor instanceof GameHandlingMessageVisitor) ((GameHandlingMessageVisitor) visitor).visit(this);
    }

    public String getToken(){
        return this.token;
    }

    @Override
    public boolean equals(Object o){
        if(o == null) return false;
        if(! (o instanceof ReconnectToServerMessage)) return false;
        return ((ReconnectToServerMessage) o ).nickname.equals(this.nickname);
    }

}
