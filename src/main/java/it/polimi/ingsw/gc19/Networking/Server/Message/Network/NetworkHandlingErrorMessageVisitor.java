package it.polimi.ingsw.gc19.Networking.Server.Message.Network;

/**
 * Classes that need to visit {@link NetworkHandlingErrorMessage}
 * need to implement this interface
 */
public interface NetworkHandlingErrorMessageVisitor{

    /**
     * This method is used by {@link NetworkHandlingErrorMessageVisitor} to visit
     * a message {@link NetworkHandlingErrorMessage}
     * @param message the {@link NetworkHandlingErrorMessage} to visit
     */
    void visit(NetworkHandlingErrorMessage message);

}
