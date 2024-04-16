package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Tuple;
import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.ClientRMI;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.AllMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;

import java.util.List;
import java.util.Map;

/**
 * Handles incoming messages from the server to the client by implementing the AllMessageVisitor interface (design patter visitor).
 */
public class MessageHandler implements AllMessageVisitor {
    private ClientInterface client;

    public MessageHandler(ClientInterface client){
        this.client = client;
    }

    @Override
    public void visit(AcceptedChooseGoalCard message) {

    }

    @Override
    public void visit(AcceptedColorMessage message) {

    }

    @Override
    public void visit(OwnAcceptedPickCardFromDeckMessage message) {

    }

    @Override
    public void visit(OtherAcceptedPickCardFromDeckMessage message) {

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
    public void visit(RefusedActionMessage visitor) {

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
        /*this.clientRMI.othersStation.add(new ViewStation(message.getNick(), message.getColor(), null, message.getVisibleSymbols(),
                null, message.getNumPoints(), null, null, null, message.getPlacedCardSequence()));*/
   }

    @Override
    public void visit(OwnStationConfigurationMessage message) {
        /*this.clientRMI.personalStation = new ViewStation(message.getNick(), message.getColor(), message.getCardsInHand(), message.getVisibleSymbols(),
                                                     message.getPrivateGoalCard(), message.getNumPoints(), message.getInitialCard(),
                                                     message.getGoalCard1(), message.getGoalCard2(), message.getPlacedCardSequence());*/
    }

    @Override
    public void visit(TableConfigurationMessage message) {
        /*this.clientRMI.table = new ViewTable(message.getSxResource(), message.getSxResource(), message.getSxGold(),
                                         message.getDxGold(), message.getSxPublicGoal(), message.getDxPublicGoal(),
                                         message.getNextSeedOfResourceDeck(), message.getNextSeedOfGoldDeck());*/
    }

    @Override
    public void visit(AvailableColorsMessage message) {
        // this.clientRMI.availableColors = message.getAvailableColors();
    }

    @Override
    public void visit(EndGameMessage message) {
        // If the game is ended there should be no more interaction
        // with the game server.
    }

    @Override
    public void visit(GamePausedMessage message) {

    }

    @Override
    public void visit(GameResumedMessage message) {

    }

    @Override
    public void visit(NewPlayerConnectedToGameMessage message) {
        // this.clientRMI.players.add(message.getPlayerName());
    }

    @Override
    public void visit(StartPlayingGameMessage message) {
        // this.clientRMI.nicknameFirstPlayer = message.getNickFirstPlayer();
    }

    @Override
    public void visit(CreatedGameMessage message) {
        this.client.setGameName(message.getGameName());
    }

    @Override
    public void visit(AvailableGamesMessage message) {
        // this.clientRMI.availableGames = message.getAvailableGames();
    }

    @Override
    public void visit(BeginFinalRoundMessage message) {

    }

    @Override
    public void visit(CreatedPlayerMessage message) {
        this.client.setNickname(message.getNick());
        this.client.setToken(message.getToken());
    }

    @Override
    public void visit(DisconnectedPlayerMessage message) {
        // If a player is disconnected it has no game server associated.
    }

    @Override
    public void visit(JoinedGameMessage message) {
        this.client.setGameName(message.getGameName());
    }

    @Override
    public void visit(PlayerReconnectedToGameMessage message) {
    }

    @Override
    public void visit(GameHandlingError message) {
    }

    @Override
    public void visit(DisconnectGameMessage disconnectGameMessage) {
    }

    @Override
    public void visit(TurnStateMessage message) {
        // @todo: how to handle the new turn, should we
        // just change the interface and permit more operation to the
        // client or should we just block them with a method in the client?
    }
}