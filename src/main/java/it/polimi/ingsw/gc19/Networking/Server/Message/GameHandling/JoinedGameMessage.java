package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is used to notify player that he has correctly joined a game
 */
public class JoinedGameMessage extends GameHandlingMessage {

    /**
     * Name of the game that player has joined
     */
    private final String gameName;

    public JoinedGameMessage(String gameName){
        this.gameName = gameName;
    }

    /**
     * Getter for game name to which player has correctly
     * been registered
     * @return the name of the game to which player has registered
     */
    public String getGameName() {
        return gameName;
    }

    /**
     * Implementation of the visitor pattern
     * @param visitor {@link MessageToClientVisitor} visitor of the message
     */
    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof GameHandlingMessageVisitor) ((GameHandlingMessageVisitor) visitor).visit(this);
    }

}