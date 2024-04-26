package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
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
import it.polimi.ingsw.gc19.View.GameLocalView.*;

import java.util.List;
import java.util.Map;

/**
 * Handles incoming messages from the server to the client by implementing the AllMessageVisitor interface (design patter visitor).
 */
public class MessageHandler implements AllMessageVisitor {
    private ClientInterface client;

    private LocalModel localModel;

    public MessageHandler(ClientInterface client /*, ClientState */){
        this.client = client;
        this.localModel = new LocalModel(); // just after a game is created/joined
    }

    @Override
    public void visit(AcceptedChooseGoalCard message) {
        this.localModel.setPrivateGoal(message.getGoalCard());
    }

    @Override
    public void visit(AcceptedColorMessage message) {
        this.localModel.setColor(message.getChosenColor());
    }

    @Override
    public void visit(OwnAcceptedPickCardFromDeckMessage message) {
        this.localModel.updateCardsInHand(message.getPickedCard());
    }

    @Override
    public void visit(OtherAcceptedPickCardFromDeckMessage message) {
    }

    @Override
    public void visit(AcceptedPickCardFromTable message) {
        if(message.getNick().equals(localModel.getNickname())){
            this.localModel.updateCardsInHand(message.getPickedCard());
        }

        this.localModel.updateCardsInTable(message.getPickedCard(), message.getDeckType(), message.getCoords());
    }

    @Override
    public void visit(AcceptedPlaceCardMessage message) {
        if(this.localModel.getNickname().equals(message.getNick())){
            this.localModel.placeCardPersonalStation(message.getAnchorCode(), message.getCardToPlace(),
                                                    message.getDirection(), message.getCardToPlace().getCardOrientation());
        }
    }

    @Override
    public void visit(AcceptedPlaceInitialCard message) {
        if(message.getNick().equals(this.localModel.getNickname())){
            System.out.println("Personal nickname: " + this.localModel.getNickname());
            this.localModel.placeInitialCardPersonalStation(message.getInitialCard());
        }else {
            System.out.println("Personal nickname: " + this.localModel.getNickname());
            this.localModel.placeInitialCardOtherStation(message.getNick(), message.getInitialCard());
        }
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
        this.localModel.setNumPlayers(message.getNumPlayers());
        this.localModel.setFirstPlayer(message.getFirstPlayer());
        // @todo: handle final round and game state
    }

    @Override
    public void visit(OtherStationConfigurationMessage message) {
        this.localModel.setOtherStations(message.getNick(),
                new OtherStation(message.getNick(), message.getColor(), message.getVisibleSymbols(),
                        message.getNumPoints(), message.getPlacedCardSequence()));
        // @todo: add "setVisibleSimbols", "setNumPoints", "PlacedCardSequence" in Station
   }

    @Override
    public void visit(OwnStationConfigurationMessage message) {
        this.localModel.setPersonalStation(new PersonalStation(message.getNick(), message.getColor(), message.getVisibleSymbols(),
                message.getNumPoints(), message.getPlacedCardSequence(), message.getPrivateGoalCard(),
                message.getGoalCard1(), message.getGoalCard2()));
    }

    @Override
    public void visit(TableConfigurationMessage message) {
        this.localModel.setTable(new LocalTable(message.getSxResource(), message.getDxResource(),
                        message.getSxGold(), message.getDxGold(), message.getSxPublicGoal(),
                        message.getDxPublicGoal(), message.getNextSeedOfResourceDeck(),
                        message.getNextSeedOfGoldDeck()));
    }

    @Override
    public void visit(AvailableColorsMessage message) {
        this.localModel.setAvailableColors(message.getAvailableColors());
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
        this.localModel.setPlayerActive(message.getPlayerName());
    }

    @Override
    public void visit(StartPlayingGameMessage message) {
        this.localModel.setFirstPlayer(message.getNickFirstPlayer());
    }

    @Override
    public void visit(CreatedGameMessage message) {
        this.client.setGameName(message.getGameName());
    }

    @Override
    public void visit(AvailableGamesMessage message) {
        this.localModel.setAvailableGames(message.getAvailableGames());
    }

    @Override
    public void visit(BeginFinalRoundMessage message) {

    }

    @Override
    public void visit(CreatedPlayerMessage message) {
        this.client.setNickname(message.getNick());
        this.client.setToken(message.getToken());
        this.localModel.setNickname(message.getNick());
    }

    @Override
    public void visit(DisconnectedPlayerMessage message) {
        // @ todo: your own disconnection?
        this.localModel.setPlayerInactive(message.getRemovedNick());
    }

    @Override
    public void visit(JoinedGameMessage message) {
        this.client.setGameName(message.getGameName());
    }

    @Override
    public void visit(PlayerReconnectedToGameMessage message) {
        this.localModel.setPlayerActive(message.getPlayerName());
    }

    @Override
    public void visit(GameHandlingError message) {
    }

    @Override
    public void visit(DisconnectGameMessage disconnectGameMessage) {
        // @todo: change gamestate
    }

    @Override
    public void visit(TurnStateMessage message) {
        // @todo: how to handle the new turn, should we
        // just change the interface and permit more operation to the
        // client or should we just block them with a method in the client?
    }
}