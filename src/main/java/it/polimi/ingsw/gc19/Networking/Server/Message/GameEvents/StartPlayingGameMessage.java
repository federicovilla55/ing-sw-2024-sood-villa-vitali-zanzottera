package it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is used to tell to player that they can start to
 * play their game
 */
public class StartPlayingGameMessage extends NotifyEventOnGame {

    private final String nickFirstPlayer;

    public StartPlayingGameMessage(String nickFirstPlayer){
        this.nickFirstPlayer = nickFirstPlayer;
    }

    /**
     * Getter for nickname of first player
     * @return the nickname of first player to play
     */
    public String getNickFirstPlayer() {
        return this.nickFirstPlayer;
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
