package it.polimi.ingsw.gc19.Networking.Server.Message.Configuration;

import it.polimi.ingsw.gc19.Enums.GameState;
import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is sent when server wants to update player
 * about game configuration
 */
public class GameConfigurationMessage extends ConfigurationMessage {

    /**
     * The current game state
     */
    private final GameState gameState;

    /**
     * The current turn state
     */
    private final TurnState turnState;

    /**
     * Nickname of the first player, if already established, otherwise <code>null</code>
     */
    private final String firstPlayer;

    /**
     * The nickname of the active player, if already established, otherwise <code>null</code>
     */
    private final String activePlayer;

    /**
     * This attribute is <code>true</code> if and only if game is in its final round
     */
    private final boolean finalRound;

    /**
     * Number of players connected to the game
     */
    private final int numPlayers;

    public GameConfigurationMessage(GameState gameState, TurnState turnState, String firstPlayer, String activePlayer, boolean finalRound, int numPlayers) {
        this.gameState = gameState;
        this.turnState = turnState;
        this.firstPlayer = firstPlayer;
        this.activePlayer = activePlayer;
        this.finalRound = finalRound;
        this.numPlayers = numPlayers;
    }

    /**
     * Getter for game state
     * @return current game state
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Getter for turn state
     * @return current turn state
     */
    public TurnState getTurnState() {
        return turnState;
    }

    /**
     * Getter for nickname of first player
     * @return the nickname of the first player
     */
    public String getFirstPlayer() {
        return firstPlayer;
    }

    /**
     * Getter for nickname of active player
     * @return active player's nickname
     */
    public String getActivePlayer() {
        return activePlayer;
    }

    /**
     * Getter for final round condition
     * @return true if game is in final round
     */
    public boolean getFinalRound() {
        return finalRound;
    }

    /**
     * Getter for number of player in game
     * @return the number of player in game
     */
    public int getNumPlayers() {
        return numPlayers;
    }

    /**
     * Implementation of the visitor pattern
     * @param visitor {@link MessageToClientVisitor} visitor of the message
     */
    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof ConfigurationMessageVisitor) ((ConfigurationMessageVisitor) visitor).visit(this);
    }

}