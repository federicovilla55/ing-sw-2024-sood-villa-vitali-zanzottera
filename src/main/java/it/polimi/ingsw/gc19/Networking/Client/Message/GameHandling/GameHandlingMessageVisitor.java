package it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling;

public interface GameHandlingMessageVisitor{
    void visit(CreateNewGameMessage message);
    void visit(NewUserMessage message);
    void visit(ReconnectToServerMessage message);
    void visit(DisconnectMessage message);
    void visit(JoinGameMessage message);
    void visit(JoinFirstAvailableGameMessage message);
    void visit(RequestAvailableGamesMessage message);
}
