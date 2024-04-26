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
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
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

    public MessageHandler(ClientInterface client){
        this.messagesToHandle = new ArrayDeque<>();
        this.client = client;
        this.localModel = new LocalModel();
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
        //this.localModel.setPrivateGoal(message.getGoalCard());
    }

    @Override
    public void visit(AcceptedColorMessage message) {
        //this.localModel.setColor(message.getChosenColor());
    }

    @Override
    public void visit(OwnAcceptedPickCardFromDeckMessage message) {
        //this.localModel.updateCardsInHand(message.getPickedCard());
    }

    @Override
    public void visit(OtherAcceptedPickCardFromDeckMessage message) {
    }

    @Override
    public void visit(AcceptedPickCardFromTable message) {
        /*if(message.getNick().equals(localModel.getNickname())){
            this.localModel.updateCardsInHand(message.getPickedCard());
        }

        this.localModel.updateCardsInTable(message.getPickedCard(), message.getDeckType(), message.getCoords());*/
    }

    @Override
    public void visit(AcceptedPlaceCardMessage message) {
        /*if(this.localModel.getNickname().equals(message.getNick())){
            this.localModel.placeCardPersonalStation(null, message.getCardToPlace(),
                                                    message.getDirection(), message.getCardToPlace().getCardOrientation());
        }*/
    }

    @Override
    public void visit(AcceptedPlaceInitialCard message) {
        //this.localModel.placeInitialCardOtherStation(message.getNick(), message.getInitialCard().getCardOrientation());
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
        /*this.localModel.setNumPlayers(message.getNumPlayers());
        this.localModel.setFirstPlayer(message.getFirstPlayer());*/
        // @todo: handle final round and game state
    }

    @Override
    public void visit(OtherStationConfigurationMessage message) {
        // @todo: add "setVisibleSimbols", "setNumPoints", "PlacedCardSequence" in Station
   }

    @Override
    public void visit(OwnStationConfigurationMessage message) {
        /*this.clientRMI.personalStation = new ViewStation(message.getNick(), message.getColor(), message.getCardsInHand(), message.getVisibleSymbols(),
                                                     message.getPrivateGoalCard(), message.getNumPoints(), message.getInitialCard(),
                                                     message.getGoalCard1(), message.getGoalCard2(), message.getPlacedCardSequence());*/
    }

    @Override
    public void visit(TableConfigurationMessage message) {
        /*this.localModel.setTable(new LocalTable(message.getSxResource(), message.getDxResource(),
                        message.getSxGold(), message.getDxGold(), message.getSxPublicGoal(),
                        message.getDxPublicGoal(), message.getNextSeedOfResourceDeck(),
                        message.getNextSeedOfGoldDeck()));*/
    }

    @Override
    public void visit(AvailableColorsMessage message) {
        // @todo: where to set colors?
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
        //this.localModel.setAvailableGames(message.getAvailableGames());
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
        // @ todo: your own disconnection?
        //this.localModel.setPlayerInactive(message.getRemovedNick());
    }

    @Override
    public void visit(JoinedGameMessage message) {
        this.client.setGameName(message.getGameName());
    }

    @Override
    public void visit(PlayerReconnectedToGameMessage message) {
        //this.localModel.setPlayerActive(message.getPlayerName());
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