package it.polimi.ingsw.gc19.Networking.Server.Message.Action;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

import java.util.ArrayList;

/**
 * This abstract class represents the generic answer to a player's action.
 * Answer can be both positive or negative
 */
public abstract class AnswerToActionMessage extends MessageToClient{

    protected AnswerToActionMessage() {
        super();
    }

}
