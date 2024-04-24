package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

/**
 * This interface has to be implemented by all
 * classes that want to visit messages of type {@link GameHandlingMessage}
 */
public interface GameHandlingMessageVisitor{

    /**
     * This method is used by {@link GameHandlingMessageVisitor} to visit
     * a message {@link CreateNewGameMessage}
     * @param message the {@link CreateNewGameMessage} to visit
     */
    void visit(CreateNewGameMessage message);

    /**
     * This method is used by {@link GameHandlingMessageVisitor} to visit
     * a message {@link NewUserMessage}
     * @param message the {@link NewUserMessage} to visit
     */
    void visit(NewUserMessage message);

    /**
     * This method is used by {@link GameHandlingMessageVisitor} to visit
     * a message {@link ReconnectToServerMessage}
     * @param message the {@link ReconnectToServerMessage} to visit
     */
    void visit(ReconnectToServerMessage message);

    /**
     * This method is used by {@link GameHandlingMessageVisitor} to visit
     * a message {@link DisconnectMessage}
     * @param message the {@link DisconnectMessage} to visit
     */
    void visit(DisconnectMessage message);

    /**
     * This method is used by {@link GameHandlingMessageVisitor} to visit
     * a message {@link JoinGameMessage}
     * @param message the {@link JoinGameMessage} to visit
     */
    void visit(JoinGameMessage message);

    /**
     * This method is used by {@link GameHandlingMessageVisitor} to visit
     * a message {@link JoinFirstAvailableGameMessage}
     * @param message the {@link JoinFirstAvailableGameMessage} to visit
     */
    void visit(JoinFirstAvailableGameMessage message);

    /**
     * This method is used by {@link GameHandlingMessageVisitor} to visit
     * a message {@link RequestAvailableGamesMessage}
     * @param message the {@link RequestAvailableGamesMessage} to visit
     */
    void visit(RequestAvailableGamesMessage message);
}
