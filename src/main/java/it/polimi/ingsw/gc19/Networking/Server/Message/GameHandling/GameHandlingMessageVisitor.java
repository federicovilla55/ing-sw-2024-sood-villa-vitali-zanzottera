package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.BeginFinalRoundMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.DisconnectedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.PlayerReconnectedToGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;

/**
 * Classes that need to visit {@link GameHandlingMessage} must
 * implement this interface
 */
public interface GameHandlingMessageVisitor{

    /**
     * This method is used by {@link GameHandlingMessageVisitor} to visit
     * a message {@link CreatedGameMessage}
     * @param message the {@link CreatedGameMessage} to visit
     */
    void visit(CreatedGameMessage message);

    /**
     * This method is used by {@link GameHandlingMessageVisitor} to visit
     * a message {@link AvailableGamesMessage}
     * @param message the {@link AvailableGamesMessage} to visit
     */
    void visit(AvailableGamesMessage message);

    /**
     * This method is used by {@link GameHandlingMessageVisitor} to visit
     * a message {@link BeginFinalRoundMessage}
     * @param message the {@link BeginFinalRoundMessage} to visit
     */
    void visit(BeginFinalRoundMessage message);

    /**
     * This method is used by {@link GameHandlingMessageVisitor} to visit
     * a message {@link CreatedPlayerMessage}
     * @param message the {@link CreatedPlayerMessage} to visit
     */
    void visit(CreatedPlayerMessage message);

    /**
     * This method is used by {@link GameHandlingMessageVisitor} to visit
     * a message {@link DisconnectedPlayerMessage}
     * @param message the {@link DisconnectedPlayerMessage} to visit
     */
    void visit(DisconnectedPlayerMessage message);

    /**
     * This method is used by {@link GameHandlingMessageVisitor} to visit
     * a message {@link JoinedGameMessage}
     * @param message the {@link JoinedGameMessage} to visit
     */
    void visit(JoinedGameMessage message);

    /**
     * This method is used by {@link GameHandlingMessageVisitor} to visit
     * a message {@link PlayerReconnectedToGameMessage}
     * @param message the {@link PlayerReconnectedToGameMessage} to visit
     */
    void visit(PlayerReconnectedToGameMessage message);

    /**
     * This method is used by {@link GameHandlingMessageVisitor} to visit
     * a message {@link GameHandlingErrorMessage}
     * @param message the {@link GameHandlingErrorMessage} to visit
     */
    void visit(GameHandlingErrorMessage message);

    /**
     * This method is used by {@link GameHandlingMessageVisitor} to visit
     * a message {@link DisconnectFromGameMessage}
     * @param message the {@link DisconnectFromGameMessage} to visit
     */
    void visit(DisconnectFromGameMessage message);

    /**
     * This method is used by {@link GameHandlingMessageVisitor} to visit
     * a message {@link DisconnectFromServerMessage}
     * @param message the {@link DisconnectFromServerMessage} to visit
     */
    void visit(DisconnectFromServerMessage message);

}
