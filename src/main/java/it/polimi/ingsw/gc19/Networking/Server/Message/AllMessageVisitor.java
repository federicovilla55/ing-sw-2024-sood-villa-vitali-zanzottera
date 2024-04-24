package it.polimi.ingsw.gc19.Networking.Server.Message;

import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.ConfigurationMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.GameEventsMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

/**
 * This interface represents a generic {@link MessageToClientVisitor}
 * that can visit all messages sent by server to client
 */
public interface AllMessageVisitor extends AnswerToActionMessageVisitor,
                                           NotifyChatMessageVisitor,
                                           ConfigurationMessageVisitor,
                                           GameEventsMessageVisitor,
                                           GameHandlingMessageVisitor,
                                           TurnStateMessageVisitor,
                                           MessageToClientVisitor,
                                           NetworkHandlingErrorMessageVisitor {

}
