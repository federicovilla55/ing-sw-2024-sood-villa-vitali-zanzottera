package it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents;

import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedChooseGoalCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.ConfigurationMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * Classes that need to visit {@link NotifyEventOnGame} have
 * to implement this interface
 */
public interface GameEventsMessageVisitor{

    /**
     * This method is used by {@link GameEventsMessageVisitor} to visit
     * a message {@link AcceptedChooseGoalCard}
     * @param message the {@link AcceptedChooseGoalCard} to visit
     */
    void visit(AvailableColorsMessage message);

    /**
     * This method is used by {@link ConfigurationMessageVisitor} to visit
     * a message {@link EndGameMessage}
     * @param message the {@link EndGameMessage} to visit
     */
    void visit(EndGameMessage message);

    /**
     * This method is used by {@link ConfigurationMessageVisitor} to visit
     * a message {@link GamePausedMessage}
     * @param message the {@link GamePausedMessage} to visit
     */
    void visit(GamePausedMessage message);

    /**
     * This method is used by {@link ConfigurationMessageVisitor} to visit
     * a message {@link GameResumedMessage}
     * @param message the {@link GameResumedMessage} to visit
     */
    void visit(GameResumedMessage message);

    /**
     * This method is used by {@link ConfigurationMessageVisitor} to visit
     * a message {@link NewPlayerConnectedToGameMessage}
     * @param message the {@link NewPlayerConnectedToGameMessage} to visit
     */
    void visit(NewPlayerConnectedToGameMessage message);

    /**
     * This method is used by {@link ConfigurationMessageVisitor} to visit
     * a message {@link StartPlayingGameMessage}
     * @param message the {@link StartPlayingGameMessage} to visit
     */
    void visit(StartPlayingGameMessage message);

    /**
     * This method is used by {@link ConfigurationMessageVisitor} to visit
     * a message {@link BeginFinalRoundMessage}
     * @param message the {@link BeginFinalRoundMessage} to visit
     */
    void visit(BeginFinalRoundMessage message);

    /**
     * This method is used by {@link ConfigurationMessageVisitor} to visit
     * a message {@link DisconnectedPlayerMessage}
     * @param message the {@link DisconnectedPlayerMessage} to visit
     */
    void visit(DisconnectedPlayerMessage message);

    /**
     * This method is used by {@link ConfigurationMessageVisitor} to visit
     * a message {@link PlayerReconnectedToGameMessage}
     * @param message the {@link PlayerReconnectedToGameMessage} to visit
     */
    void visit(PlayerReconnectedToGameMessage message);

}
