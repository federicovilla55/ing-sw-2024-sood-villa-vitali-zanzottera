package it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public interface GameEventsMessageVisitor{

    void visit(CreatedGameMessage message);
    void visit(EndGameMessage message);
    void visit(GamePausedMessage message);
    void visit(GameResumedMessage message);
    void visit(NewPlayerConnectedToGameMessage message);
    void visit(StartPlayingGameMessage message);
    void visit(BeginFinalRoundMessage message);
    void visit(DisconnectedPlayerMessage message);
    void visit(PlayerReconnectedToGameMessage message);

}
