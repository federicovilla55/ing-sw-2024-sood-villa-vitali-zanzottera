package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

/**
 * This method is used by client to notify server
 * that it wants to join the game with the specified name
 */
public class JoinGameMessage extends GameHandlingMessage{

    private final String gameName;

    public JoinGameMessage(String gameName, String nickname){
        super(nickname);
        this.gameName = gameName;
    }

    /**
     * Getter for game to join name
     * @return the name of the game to join
     */
    public String getGameName(){
        return this.gameName;
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
