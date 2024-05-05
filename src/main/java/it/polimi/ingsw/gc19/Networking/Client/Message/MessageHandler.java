package it.polimi.ingsw.gc19.Networking.Client.Message;

import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.AllMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
import it.polimi.ingsw.gc19.View.GameLocalView.*;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalTable;

import java.util.ArrayDeque;

/**
 * Handles incoming messages from the server to the client by implementing the AllMessageVisitor interface (design patter visitor).
 */
public class MessageHandler extends Thread implements AllMessageVisitor{

    private final ArrayDeque<MessageToClient> messagesToHandle;

    private ClientInterface client;

    private LocalModel localModel;
    private final ClientController clientController;

    public MessageHandler(ClientController clientController, LocalModel localModel){
        this.messagesToHandle = new ArrayDeque<>();
        this.localModel = localModel;
        this.clientController = clientController;
    }

    public void setClient(ClientInterface client){
        this.client = client;
    }

    public void update(MessageToClient message) {
        synchronized (this.messagesToHandle){
            this.messagesToHandle.add(message);
            this.messagesToHandle.notifyAll();
        }
    }

    public synchronized void setLocalModel(LocalModel model){
        this.localModel = model;
        this.notifyAll();
    }

    private synchronized void waitForLocalModel(){
        while(this.localModel == null){
            try{
                this.wait();
            }
            catch (InterruptedException interruptedException){
                Thread.currentThread().interrupt();
            }
        }
    }

    public ArrayDeque<MessageToClient> getMessagesToHandle(){
        return this.messagesToHandle;
    }

    @Override
    public void run() {
        MessageToClient message;
        while (!Thread.currentThread().isInterrupted()){
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
    public void visit(AcceptedChooseGoalCardMessage message) {
        waitForLocalModel();
        this.localModel.setPrivateGoal(message.getGoalCard());
    }

    @Override
    public void visit(AcceptedColorMessage message) {
        waitForLocalModel();
        this.localModel.setColor(message.getChosenColor());
    }

    @Override
    public void visit(OwnAcceptedPickCardFromDeckMessage message) {
        waitForLocalModel();
        this.localModel.updateCardsInHand(message.getPickedCard());
        clientController.getCurrentState().nextState(message);
    }

    @Override
    public void visit(OtherAcceptedPickCardFromDeckMessage message) {
        //????????????????
    }

    @Override
    public void visit(AcceptedPickCardFromTable message) {
        waitForLocalModel();
        this.localModel.updateCardsInHand(message.getPickedCard());
        this.localModel.updateCardsInTable(message.getCardToPutInSlot(), message.getDeckType(), message.getCoords());
        clientController.getCurrentState().nextState(message);
    }

    @Override
    public void visit(AcceptedPlacePlayableCardMessage message) {
        waitForLocalModel();
        if(this.localModel.getNickname().equals(message.getNick())){
            this.localModel.placeCardPersonalStation(message.getAnchorCode(), message.getCardToPlace(),
                    message.getDirection(), message.getCardToPlace().getCardOrientation());
            clientController.getCurrentState().nextState(message);
        }
    }

    @Override
    public void visit(AcceptedPlaceInitialCard message) {
        waitForLocalModel();
        if(message.getNick().equals(this.localModel.getNickname())){
            this.localModel.placeInitialCardPersonalStation(message.getInitialCard());
        }
    }

    @Override
    public void visit(RefusedActionMessage message) {
        this.clientController.handleError(message);
    }

    @Override
    public void visit(NotifyChatMessage message) {
        waitForLocalModel();
        this.localModel.updateMessages(message.getMessage(), message.getSender(), message.getHeader());
        clientController.getCurrentState().nextState(message);
    }

    @Override
    public void visit(ConfigurationMessage message) {

    }

    @Override
    public void visit(GameConfigurationMessage message) {
        waitForLocalModel();
        this.localModel.setNumPlayers(message.getNumPlayers());
        this.localModel.setFirstPlayer(message.getFirstPlayer());
        clientController.getCurrentState().nextState(message);
    }

    @Override
    public void visit(OtherStationConfigurationMessage message) {
        waitForLocalModel();
        this.localModel.setOtherStations(message.getNick(),
                new OtherStation(message.getNick(), message.getColor(), message.getVisibleSymbols(),
                        message.getNumPoints(), message.getPlacedCardSequence()));
   }

    @Override
    public void visit(OwnStationConfigurationMessage message) {
        waitForLocalModel();
        this.localModel.setPersonalStation(new PersonalStation(message.getNick(), message.getColor(), message.getVisibleSymbols(),
                                                               message.getNumPoints(), message.getPlacedCardSequence(), message.getPrivateGoalCard(),
                                                               message.getGoalCard1(), message.getGoalCard2()));
    }

    @Override

    public void visit(TableConfigurationMessage message) {
        waitForLocalModel();
        this.localModel.setTable(new LocalTable(message.getSxResource(), message.getDxResource(),
                                                message.getSxGold(), message.getDxGold(), message.getSxPublicGoal(),
                                                message.getDxPublicGoal(), message.getNextSeedOfResourceDeck(),
                                                message.getNextSeedOfGoldDeck()));
    }

    @Override
    public void visit(AvailableColorsMessage message) {
        waitForLocalModel();
        this.localModel.setAvailableColors(message.getAvailableColors());
    }

    @Override
    public void visit(EndGameMessage message) {
        clientController.getCurrentState().nextState(message);
    }

    @Override
    public void visit(GamePausedMessage message) {
        clientController.getCurrentState().nextState(message);
    }

    @Override
    public void visit(GameResumedMessage message) {
        clientController.getCurrentState().nextState(message);
    }

    @Override
    public void visit(NewPlayerConnectedToGameMessage message) {
        waitForLocalModel();
        this.localModel.setPlayerActive(message.getPlayerName());
    }

    @Override
    public void visit(StartPlayingGameMessage message) {
        waitForLocalModel();
        this.localModel.setFirstPlayer(message.getNickFirstPlayer());
        clientController.getCurrentState().nextState(message);
    }

    @Override
    public void visit(CreatedGameMessage message) {
        waitForLocalModel();
        this.localModel.setGameName(message.getGameName());
        clientController.getCurrentState().nextState(message);
    }

    @Override
    public void visit(AvailableGamesMessage message) {
        //@TODO: handle available games: simply notify view about available games
    }

    @Override
    public void visit(BeginFinalRoundMessage message) {
        clientController.getCurrentState().nextState(message);
    }

    @Override
    public void visit(CreatedPlayerMessage message) {
        waitForLocalModel();
        this.client.configure(message.getNick(), message.getToken());
        this.localModel.setNickname(message.getNick());
        clientController.getCurrentState().nextState(message);
    }

    @Override
    public void visit(DisconnectedPlayerMessage message) {
        waitForLocalModel();
        this.localModel.setPlayerInactive(message.getRemovedNick());
        clientController.getCurrentState().nextState(message);
    }

    @Override
    public void visit(JoinedGameMessage message) {
        waitForLocalModel();
        this.localModel.setGameName(message.getGameName());
        clientController.getCurrentState().nextState(message);
    }

    @Override
    public  void visit(PlayerReconnectedToGameMessage message) {
        waitForLocalModel();
        this.localModel.setPlayerActive(message.getPlayerName());
        clientController.getCurrentState().nextState(message);
    }

    @Override
    public void visit(GameHandlingErrorMessage message) {
        clientController.getCurrentState().nextState(message);
    }

    @Override
    public void visit(DisconnectFromGameMessage message) {
        clientController.getCurrentState().nextState(message);
    }

    @Override
    public void visit(DisconnectFromServerMessage message) {
        clientController.getCurrentState().nextState(message);
    }

    @Override
    public void visit(TurnStateMessage message) {
        clientController.getCurrentState().nextState(message);
    }

    @Override
    public void visit(NetworkHandlingErrorMessage message) {
        clientController.getCurrentState().nextState(message);
    }

}