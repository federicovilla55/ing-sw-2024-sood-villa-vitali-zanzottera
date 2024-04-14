package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

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

    @Override
    public void accept(MessageToServerVisitor visitor){
        if(visitor instanceof GameHandlingMessageVisitor) ((GameHandlingMessageVisitor) visitor).visit(this);
    }

}
