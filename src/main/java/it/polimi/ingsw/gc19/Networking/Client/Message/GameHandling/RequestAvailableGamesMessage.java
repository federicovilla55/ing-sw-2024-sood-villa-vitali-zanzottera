package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;

/**
 * This message is used by client to ask server about available games.
 */
public class RequestAvailableGamesMessage extends GameHandlingMessage{
    public RequestAvailableGamesMessage(String nickname) {
        super(nickname);
    }

    /**
     * This method is used by {@link MessageToServerVisitor} to visit the message
     * @param visitor the {@link MessageToServerVisitor} for the message
     */
    @Override
    public void accept(MessageToServerVisitor visitor) {
        if(visitor instanceof GameHandlingMessageVisitor) ((GameHandlingMessageVisitor) visitor).visit(this);
    }

}
