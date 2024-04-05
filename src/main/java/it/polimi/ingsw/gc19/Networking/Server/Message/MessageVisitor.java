package it.polimi.ingsw.gc19.Networking.Server.Message;


import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.GameEvents.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;

public interface MessageVisitor{
    void visit(AcceptedChooseGoalCard message);
    void visit(AcceptedColorMessage message);
    void visit(AcceptedPickCardFromDeckMessage message);
    void visit(AcceptedPickCardFromTable message);
    void visit(AcceptedPlaceCardMessage message);
    void visit(AcceptedPlaceInitialCard message);
    void visit(RefusedActionMessage message);
    void visit(NotifyChatMessage message);
    void visit(ConfigurationMessage message);
    void visit(GameConfigurationMessage message);
    void visit(OtherStationConfigurationMessage message);
    void visit(OwnStationConfigurationMessage message);
    void visit(TableConfigurationMessage message);
    void visit(CreatedGameMessage message);
    void visit(EndGameMessage message);
    void visit(GamePausedMessage message);
    void visit(GameResumedMessage message);
    void visit(NewPlayerConnectedToGameMessage message);
    void visit(StartPlayingGameMessage message);
    void visit(AvailableColorsMessage message);
    void visit(AvailableGamesMessage message);
    void visit(BeginFinalRoundMessage message);
    void visit(CreatedPlayerMessage message);
    void visit(DisconnectedPlayerMessage message);
    void visit(JoinedGameMessage message);
    void visit(PlayerReconnectedToGameMessage message);
    void visit(TurnStateMessage message);

}
