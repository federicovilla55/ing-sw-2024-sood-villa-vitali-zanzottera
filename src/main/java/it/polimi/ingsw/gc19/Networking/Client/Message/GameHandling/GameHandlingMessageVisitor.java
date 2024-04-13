package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;

public interface GameHandlingMessageVisitor{
    void visit(CreateNewGameMessage message);
    void visit(JoinedGameMessage message);
    void visit(NewUserMessage message);
    void visit(ReconnectToGameMessage message);

}
