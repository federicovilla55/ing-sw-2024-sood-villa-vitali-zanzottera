package it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling;

import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;

public interface GameHandlingMessageVisitor{
    void visit(AvailableColorsMessage message);
    void visit(AvailableGamesMessage message);
    void visit(BeginFinalRoundMessage message);
    void visit(CreatedPlayerMessage message);
    void visit(DisconnectedPlayerMessage message);
    void visit(JoinedGameMessage message);
    void visit(PlayerReconnectedToGameMessage message);
    void visit(GameHandlingError message);

}
