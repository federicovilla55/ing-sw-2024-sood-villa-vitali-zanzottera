package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

public class CreateNewGameMessage extends GameHandlingMessage {

    private final String gameName;
    private final int numPlayer;

    public CreateNewGameMessage(String nickname, String gameName, int numPlayer){
        super(nickname);
        this.gameName = gameName;
        this.numPlayer = numPlayer;
    }

    public CreateNewGameMessage(String gameName, int numPlayer, String nickname){
        super(nickname);
        this.gameName = gameName;
        this.numPlayer = numPlayer;
    }

    public String getGameName(){
        return this.gameName;
    }

    public int getNumPlayer(){
        return this.numPlayer;
    }

    @Override
    public void accept(MessageToServerVisitor visitor){
        if(visitor instanceof GameHandlingMessageVisitor) ((GameHandlingMessageVisitor) visitor).visit(this);
    }

}
