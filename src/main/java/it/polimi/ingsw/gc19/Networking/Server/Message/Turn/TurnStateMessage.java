package it.polimi.ingsw.gc19.Networking.Server.Message.Turn;

import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This message is used to update players about turn state
 */
public class TurnStateMessage extends MessageToClient{

    /**
     * Nickname of the player that have to play
     */
    private final String nick;

    /**
     * {@link TurnState} for the active player
     */
    private final TurnState turnState;

    public TurnStateMessage(String nick, TurnState turnState){
        this.nick = nick;
        this.turnState = turnState;
    }

    /**
     * Getter for nickname of in turn player
     * @return the nickname of in turn player
     */
    public String getNick(){
        return this.nick;
    }

    /**
     * Getter for turn of player
     * @return the turn state
     */
    public TurnState getTurnState(){
        return this.turnState;
    }

    /**
     * Implementation of the visitor pattern
     * @param visitor {@link MessageToClientVisitor} visitor of the message
     */
    @Override
    public void accept(MessageToClientVisitor visitor) {
        if(visitor instanceof TurnStateMessageVisitor) ((TurnStateMessageVisitor) visitor).visit(this);
    }

}