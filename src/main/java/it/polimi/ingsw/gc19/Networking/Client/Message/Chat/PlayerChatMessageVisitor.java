package it.polimi.ingsw.gc19.Networking.Client.Message.Chat;

/**
 * This interface is implemented by classes that want
 * to visit messages of type {@link PlayerChatMessage}
 */
public interface PlayerChatMessageVisitor{

    /**
     * This method is used by {@link PlayerChatMessageVisitor} to visit
     * a message {@link PlayerChatMessage}
     * @param message the {@link PlayerChatMessage} to visit
     */
    void visit(PlayerChatMessage message);

}
