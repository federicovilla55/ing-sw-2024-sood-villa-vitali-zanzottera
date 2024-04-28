package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedChooseGoalCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.ConfigurationMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.BeginFinalRoundMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.DisconnectedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.PlayerReconnectedToGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;

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
     * @param message the {@link JoinedGameMessage to visit
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
     * a message {@link GameHandlingError}
     * @param message the {@link GameHandlingError} to visit
     */
    void visit(GameHandlingError message);

    /**
     * This method is used by {@link GameHandlingMessageVisitor} to visit
     * a message {@link DisconnectGameMessage}
     * @param message the {@link DisconnectGameMessage} to visit
     */
    void visit(DisconnectGameMessage message);

    /**
     * This method is used by {@link GameHandlingMessageVisitor} to visit
     * a message {@link PlayerCorrectlyDisconnected}
     * @param message the {@link PlayerCorrectlyDisconnected} to visit
     */
    void visit(PlayerCorrectlyDisconnected message);

}
