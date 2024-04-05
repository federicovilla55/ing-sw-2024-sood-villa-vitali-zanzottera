package it.polimi.ingsw.gc19.Networking.Server.Message.Configuration;

import it.polimi.ingsw.gc19.Enums.GameState;
import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public class GameConfigurationMessage extends ConfigurationMessage {
    public GameConfigurationMessage(GameState gameState, TurnState turnState, String firstPlayer, String activePlayer, boolean finalRound, int numPlayers) {
        this.gameState = gameState;
        this.turnState = turnState;
        this.firstPlayer = firstPlayer;
        this.activePlayer = activePlayer;
        this.finalRound = finalRound;
        this.numPlayers = numPlayers;
    }

    private final GameState gameState;
    private final TurnState turnState;
    private final String firstPlayer;
    private final String activePlayer;
    private final boolean finalRound;
    private final int numPlayers;

    public GameState getGameState() {
        return gameState;
    }

    public TurnState getTurnState() {
        return turnState;
    }

    public String getFirstPlayer() {
        return firstPlayer;
    }

    public String getActivePlayer() {
        return activePlayer;
    }

    public boolean getFinalRound() {
        return finalRound;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    @Override
    public void visit(MessageToClientVisitor visitor) {
        visitor.visit(this);
    }

}