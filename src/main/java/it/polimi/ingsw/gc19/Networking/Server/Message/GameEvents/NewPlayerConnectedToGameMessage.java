package it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is used to tell to players connected to game
 * that a new player has connected
 */
public class NewPlayerConnectedToGameMessage extends NotifyEventOnGame {

    /**
     * Nickname of the new player that has connected to the game
     */
    private final String playerName;

    public NewPlayerConnectedToGameMessage(String playerName){
        this.playerName = playerName;
    }

    /**
     * Getter for new player nickname
     * @return the name of newly player connected to game
     */
    public String getPlayerName(){
        return this.playerName;
    }

    /**
     * Implementation of the visitor pattern
     * @param visitor {@link MessageToClientVisitor} visitor of the message
     */
    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof GameEventsMessageVisitor) ((GameEventsMessageVisitor) visitor).visit(this);
    }

}