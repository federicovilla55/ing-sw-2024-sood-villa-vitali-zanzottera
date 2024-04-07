package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.BeginFinalRoundMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.DisconnectedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.PlayerReconnectedToGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;

public interface GameHandlingMessageVisitor{

    void visit(CreatedGameMessage message);
    void visit(AvailableGamesMessage message);
    void visit(BeginFinalRoundMessage message);
    void visit(CreatedPlayerMessage message);
    void visit(DisconnectedPlayerMessage message);
    void visit(JoinedGameMessage message);
    void visit(PlayerReconnectedToGameMessage message);
    void visit(GameHandlingError message);
    void visit(DisconnectGameMessage disconnectGameMessage);
}
