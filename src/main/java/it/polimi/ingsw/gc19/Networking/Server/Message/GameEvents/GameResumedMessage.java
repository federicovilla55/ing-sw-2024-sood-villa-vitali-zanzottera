package it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents;

import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is used to tell players of the game that
 * game has resumed
 */
public class GameResumedMessage extends NotifyEventOnGame{

    /**
     * Current {@link TurnState}
     */
    private final TurnState turnState;

    /**
     * Nickname of the player that has to play
     */
    private final String activePlayer;

    public GameResumedMessage(TurnState turnState, String activePlayer) {
        this.turnState = turnState;
        this.activePlayer = activePlayer;
    }

    /**
     * Getter for {@link #turnState}
     * @return {@link #turnState}
     */
    public TurnState getTurnState() {
        return turnState;
    }

    /**
     * Getter for {@link #activePlayer}
     * @return {@link #activePlayer}
     */
    public String getActivePlayer() {
        return activePlayer;
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