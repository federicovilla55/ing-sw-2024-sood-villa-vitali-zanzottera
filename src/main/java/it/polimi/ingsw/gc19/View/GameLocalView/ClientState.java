package it.polimi.ingsw.gc19.View.GameLocalView;

import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.GameConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;

import java.util.ArrayList;

public abstract class ClientState {
    public void nextState(MessageToClient message) {}
    public void nextState(AcceptedChooseGoalCardMessage message) {}
    public void nextState(AcceptedColorMessage message) {}
    public void nextState(OwnAcceptedPickCardFromDeckMessage message) {}
    public void nextState(OtherAcceptedPickCardFromDeckMessage message) {}
    public void nextState(AcceptedPickCardFromTable message) {}
    public void nextState(AcceptedPlaceCardMessage message) {}
    public void nextState(AcceptedPlaceInitialCard message) {}
    public void nextState(CreatedPlayerMessage message) {}
    public void nextState(GamePausedMessage message) {}
    public void nextState(GameResumedMessage message) {}
    public void nextState(StartPlayingGameMessage message) {}
    public void nextState(CreatedGameMessage message) {}
    public void nextState(JoinedGameMessage message) {}
    public void nextState(EndGameMessage message) {}
    public void nextState(PlayerReconnectedToGameMessage message) {}
    public void nextState(DisconnectFromGameMessage message) {}
    public void nextState(TurnStateMessage message) {}
    public void nextState(RefusedActionMessage message) {}
    public void nextState(GameHandlingError message){}
    public void nextState(NetworkHandlingErrorMessage message){}

    public void nextState(GameConfigurationMessage message){}
    public void nextState(AvailableGamesMessage message){}

    abstract ViewState getState();

    // use boolean/string to return information on action
    abstract void parseAction(ArrayList<String> command); // choose_color(RED)
    // ...(
}