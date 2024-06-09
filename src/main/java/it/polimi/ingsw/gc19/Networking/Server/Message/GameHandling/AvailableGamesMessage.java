package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * This message is used to tell player about available games
 * that he can join
 */
public class AvailableGamesMessage extends GameHandlingMessage{

    /**
     * List of all available games in server
     */
    private final List<String> availableGames;

    public AvailableGamesMessage(List<String> availableGames){
        this.availableGames = availableGames;
    }

    /**
     * Getter for available games
     * @return a list of all available games
     */
    public List<String> getAvailableGames() {
        return this.availableGames;
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
