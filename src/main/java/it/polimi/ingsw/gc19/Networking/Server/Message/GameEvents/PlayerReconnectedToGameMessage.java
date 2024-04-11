package it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is used to tell player that an inactive
 * player has reconnected to game
 */
public class PlayerReconnectedToGameMessage extends NotifyEventOnGame {
    private final String playerName;

    public PlayerReconnectedToGameMessage(String playerName){
        this.playerName = playerName;
    }

    /**
     * Getter for player nickname
     * @return the nickname of reconnected player
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
