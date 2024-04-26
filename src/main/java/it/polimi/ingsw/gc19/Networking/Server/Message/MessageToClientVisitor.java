package it.polimi.ingsw.gc19.Networking.Server.Message;

import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.ConfigurationMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.GameEventsMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessageVisitor;

/**
 * This is an empty interface used to mark a class as a visitor for {@link MessageToClient}.
 */
public interface MessageToClientVisitor{

}
