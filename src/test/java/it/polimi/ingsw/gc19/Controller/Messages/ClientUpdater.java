package it.polimi.ingsw.gc19.Controller.Messages;

import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.ReconnectToGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClientVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;

public class ClientUpdater implements MessageToClientVisitor {
    @Override
    public void visit(AcceptedChooseGoalCard message) {

    }

    @Override
    public void visit(AcceptedColorMessage message) {

    }

    @Override
    public void visit(AcceptedPickCardFromDeckMessage message) {

    }

    @Override
    public void visit(AcceptedPickCardFromTable message) {

    }

    @Override
    public void visit(AcceptedPlaceCardMessage message) {

    }

    @Override
    public void visit(AcceptedPlaceInitialCard message) {

    }

    @Override
    public void visit(RefusedActionMessage message) {

    }

    @Override
    public void visit(NotifyChatMessage message) {

    }

    @Override
    public void visit(ConfigurationMessage message) {

    }

    @Override
    public void visit(GameConfigurationMessage message) {

    }

    @Override
    public void visit(OtherStationConfigurationMessage message) {

    }

    @Override
    public void visit(OwnStationConfigurationMessage message) {

    }

    @Override
    public void visit(TableConfigurationMessage message) {

    }

    @Override
    public void visit(CreatedGameMessage message) {

    }

    @Override
    public void visit(EndGameMessage message) {

    }

    @Override
    public void visit(GamePausedMessage message) {

    }

    @Override
    public void visit(GameResumedMessage message) {

    }

    @Override
    public void visit(NewPlayerConnectedToGameMessage message) {

    }

    @Override
    public void visit(StartPlayingGameMessage message) {

    }

    @Override
    public void visit(AvailableColorsMessage message) {

    }

    @Override
    public void visit(AvailableGamesMessage message) {

    }

    @Override
    public void visit(BeginFinalRoundMessage message) {

    }

    @Override
    public void visit(CreatedPlayerMessage message) {

    }

    @Override
    public void visit(DisconnectedPlayerMessage message) {

    }

    @Override
    public void visit(JoinedGameMessage message) {

    }

    @Override
    public void visit(PlayerReconnectedToGameMessage message) {

    }

    @Override
    public void visit(TurnStateMessage message) {

    }

    @Override
    public void visit(ReconnectToGameMessage message) {

    }
}
