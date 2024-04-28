package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is used to confirm to client that he has been
 * disconnected from the game
 */
public class DisconnectFromGameMessage extends GameHandlingMessage {
    private final String gameName;

    public DisconnectFromGameMessage(String gameName){
        this.gameName = gameName;
    }

    /**
     * Getter for game name from which player has been disconnected
     * @return the game name from which player has been disconnected
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
