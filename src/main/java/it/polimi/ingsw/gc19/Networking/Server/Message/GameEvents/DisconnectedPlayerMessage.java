package it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is used to tell to all connected players of the game
 * that a player has disconnected
 */
public class DisconnectedPlayerMessage extends NotifyEventOnGame {

    /**
     * Nickname of the player who have been disconnected
     */
    private final String removedNick;

    public DisconnectedPlayerMessage(String removedNick){
        this.removedNick = removedNick;
    }

    /**
     * Getter for nickname of the disconnected player
     * @return the name of the disconnected player
     */
    public String getRemovedNick(){
        return this.removedNick;
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
