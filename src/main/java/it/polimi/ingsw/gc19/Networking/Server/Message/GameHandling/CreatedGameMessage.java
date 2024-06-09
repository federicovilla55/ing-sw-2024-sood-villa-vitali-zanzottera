package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.ConfigurationMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is used to tell that the game with
 * the requested name has been successfully created
 */
public class CreatedGameMessage extends GameHandlingMessage {

    /**
     * Name of the created game
     */
    private final String gameName;

    public CreatedGameMessage(String gameName){
        this.gameName = gameName;
    }

    /**
     * Getter for name of created game
     * @return the name of the created game
     */
    public String getGameName() {
        return this.gameName;
    }

    /**
     * Implementation of the visitor pattern
     * @param visitor {@link MessageToClientVisitor} visitor of the message
     */
    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof GameHandlingMessageVisitor) ((GameHandlingMessageVisitor) visitor).visit(this);
    }

}
