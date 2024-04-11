package it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents;

import it.polimi.ingsw.gc19.Enums.GameState;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameHandlingMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

/**
 * This abstract class is used to notify a generic event concerning a game
 */
public abstract class NotifyEventOnGame extends MessageToClient {

}
