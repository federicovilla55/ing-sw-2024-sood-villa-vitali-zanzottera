package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.AllMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
import it.polimi.ingsw.gc19.View.GameLocalView.*;
import it.polimi.ingsw.gc19.ObserverPattern.ObserverMessageToClient;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalTable;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;

/**
 * Handles incoming messages from the server to the client by implementing the AllMessageVisitor interface (design patter visitor).
 */
public class MessageHandler extends Thread implements AllMessageVisitor{
    private final ArrayDeque<MessageToClient> messagesToHandle;
    private ClientInterface client;

    private LocalModel localModel;

    private ActionParser actionParser;

    public MessageHandler(ClientInterface client, ActionParser actionParser){
        this.messagesToHandle = new ArrayDeque<>();
        this.client = client;
        this.localModel = new LocalModel();
        this.actionParser = actionParser;
    }

    public void update(MessageToClient message) {
        synchronized (this.messagesToHandle){
            this.messagesToHandle.add(message);
            this.messagesToHandle.notifyAll();
        }
    }

    @Override
    public void run() {
        MessageToClient message;
        while (!Thread.interrupted()){
            synchronized (this.messagesToHandle){
                while(this.messagesToHandle.isEmpty()){
                    try{
                        this.messagesToHandle.wait();
                    }
                    catch (InterruptedException interruptedException){
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                message = this.messagesToHandle.remove();
                this.messagesToHandle.notifyAll();
            }

            message.accept(this);
        }
    }

    public void interruptMessageHandler(){
        this.interrupt();
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
        actionParser.viewState.nextState(message);
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
        actionParser.viewState.nextState(message);
    }

    @Override
    public void visit(AcceptedPlacePlayableCardMessage message) {
        if(this.localModel.getNickname().equals(message.getNick())){
            this.localModel.placeCardPersonalStation(message.getAnchorCode(), message.getCardToPlace(),
                    message.getDirection(), message.getCardToPlace().getCardOrientation());
            actionParser.viewState.nextState(message);
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
        this.localModel.updateMessages(message.getMessage(), message.getSender(), message.getHeader());
        actionParser.viewState.nextState(message);
    }

    @Override
    public void visit(ConfigurationMessage message) {

    }

    @Override
    public void visit(GameConfigurationMessage message) {
        this.localModel.setNumPlayers(message.getNumPlayers());
        this.localModel.setFirstPlayer(message.getFirstPlayer());
        actionParser.viewState.nextState(message);

        // @todo: handle final round and game state
    }

    @Override
    public void visit(OtherStationConfigurationMessage message) {
        this.localModel.setOtherStations(message.getNick(),
                new OtherStation(message.getNick(), message.getColor(), message.getVisibleSymbols(),
                        message.getNumPoints(), message.getPlacedCardSequence()));
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
        actionParser.viewState.nextState(message);

    }

    @Override
    public void visit(GamePausedMessage message) {
        actionParser.viewState.nextState(message);
    }

    @Override
    public void visit(GameResumedMessage message) {
        actionParser.viewState.nextState(message);
    }

    @Override
    public void visit(NewPlayerConnectedToGameMessage message) {
        this.localModel.setPlayerActive(message.getPlayerName());
    }

    @Override
    public void visit(StartPlayingGameMessage message) {
        this.localModel.setFirstPlayer(message.getNickFirstPlayer());
        actionParser.viewState.nextState(message);
    }

    @Override
    public void visit(CreatedGameMessage message) {
        this.localModel.setGameName(message.getGameName());
        actionParser.viewState.nextState(message);
    }

    @Override
    public void visit(AvailableGamesMessage message) {
        this.localModel.setAvailableGames(message.getAvailableGames());
    }

    @Override
    public void visit(BeginFinalRoundMessage message) {
        actionParser.viewState.nextState(message);
    }

    @Override
    public void visit(CreatedPlayerMessage message) {
        this.client.setNickname(message.getNick());
        this.client.setToken(message.getToken());
        this.localModel.setNickname(message.getNick());
        actionParser.viewState.nextState(message);
    }

    @Override
    public void visit(DisconnectedPlayerMessage message) {
        // @ todo: your own disconnection?
        this.localModel.setPlayerInactive(message.getRemovedNick());
    }

    @Override
    public void visit(JoinedGameMessage message) {
        this.localModel.setGameName(message.getGameName());
        actionParser.viewState.nextState(message);
    }

    @Override
    public void visit(PlayerReconnectedToGameMessage message) {
        this.localModel.setPlayerActive(message.getPlayerName());
        actionParser.viewState.nextState(message);
    }

    @Override
    public void visit(GameHandlingError message) {
    }

    @Override
    public void visit(DisconnectGameMessage disconnectGameMessage) {

    }

    @Override
    public void visit(TurnStateMessage message) {
        actionParser.viewState.nextState(message);
    }

    @Override
    public void visit(NetworkHandlingErrorMessage message) {

    }

}