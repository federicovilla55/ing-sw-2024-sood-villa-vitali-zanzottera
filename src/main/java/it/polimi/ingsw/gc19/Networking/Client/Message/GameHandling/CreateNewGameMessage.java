package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

public class CreateNewGameMessage extends GameHandlingMessage {

    private final String gameName;
    private final int numPlayer;
    private final long randomSeed;

    public CreateNewGameMessage(String nickname, String gameName, int numPlayer, long randoMseed){
        super(nickname);
        this.gameName = gameName;
        this.numPlayer = numPlayer;
        this.randomSeed = randoMseed;
    }

    public String getGameName(){
        return this.gameName;
    }

    public int getNumPlayer(){
        return this.numPlayer;
    }

    public long getRandomSeed(){
        return this.randomSeed;
    }

    @Override
    public void accept(MessageToServerVisitor visitor){
        if(visitor instanceof GameHandlingMessageVisitor) ((GameHandlingMessageVisitor) visitor).visit(this);
    }

}
