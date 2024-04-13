package it.polimi.ingsw.gc19.Networking.Server.Message;

import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AnswerToActionMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.ConfigurationMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.GameEventsMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessageVisitor;

public interface AllMessageVisitor extends AnswerToActionMessageVisitor,
                                           NotifyChatMessageVisitor,
                                           ConfigurationMessageVisitor,
                                           GameEventsMessageVisitor,
                                           GameHandlingMessageVisitor,
                                           TurnStateMessageVisitor,
                                           MessageToClientVisitor{

}
