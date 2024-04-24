package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

/**
 * This message is used by player to ask server to create a new game
 * with the specified name, number of and players and (maybe) a random seed
 */
public class CreateNewGameMessage extends GameHandlingMessage {

    private final String gameName;
    private final int numPlayer;
    private final Long randomSeed;

    public CreateNewGameMessage(String nickname, String gameName, int numPlayer, Long randomSeed){
        super(nickname);
        this.gameName = gameName;
        this.numPlayer = numPlayer;
        this.randomSeed = randomSeed;
    }

    /**
     * Getter for the name of the game to build
     * @return the name of the game to build
     */
    public String getGameName(){
        return this.gameName;
    }

    /**
     * Getter for number of player in the game
     * @return the number of player in game
     */
    public int getNumPlayer(){
        return this.numPlayer;
    }

    /**
     * Getter for specified random seed. If <code>null</code> no
     * random has been specified.
     * @return the random seed for the game
     */
    public Long getRandomSeed(){
        return this.randomSeed;
    }

    /**
     * This method is used by {@link MessageToServerVisitor} to visit the message
     * @param visitor the {@link MessageToServerVisitor} for the message
     */
    @Override
    public void accept(MessageToServerVisitor visitor){
        if(visitor instanceof GameHandlingMessageVisitor) ((GameHandlingMessageVisitor) visitor).visit(this);
    }

}
