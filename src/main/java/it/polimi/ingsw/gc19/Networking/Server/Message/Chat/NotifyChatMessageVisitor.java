package it.polimi.ingsw.gc19.Networking.Server.Message.Chat;

import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public interface NotifyChatMessageVisitor{
    void visit(NotifyChatMessage message);

}
