package it.polimi.ingsw.gc19.Networking.Client.Message.Chat;

import it.polimi.ingsw.gc19.Networking.Client.Message.Action.PlaceCardMessage;

public interface PlayerChatMessageVisitor{
    void visit(PlayerChatMessage message);

}
