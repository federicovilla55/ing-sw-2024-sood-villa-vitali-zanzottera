package it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents;

import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is used to tell players of the game that
 * game has resumed
 */
public class GameResumedMessage extends NotifyEventOnGame{

    private final TurnState turnState;
    private final String activePlayer;

    public GameResumedMessage(TurnState turnState, String activePlayer) {
        this.turnState = turnState;
        this.activePlayer = activePlayer;
    }

    public TurnState getTurnState() {
        return turnState;
    }

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
