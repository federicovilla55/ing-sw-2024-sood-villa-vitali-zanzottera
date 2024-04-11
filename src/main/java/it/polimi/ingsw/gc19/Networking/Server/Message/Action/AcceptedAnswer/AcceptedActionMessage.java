package it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer;

import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessage;

/**
 * This abstract class represents a generic accepted action message.
 * Its subclasses are used whenever a player action have gone right.
 */
public abstract class AcceptedActionMessage extends AnswerToActionMessage {

    protected AcceptedActionMessage(){
        super();
    }

}
