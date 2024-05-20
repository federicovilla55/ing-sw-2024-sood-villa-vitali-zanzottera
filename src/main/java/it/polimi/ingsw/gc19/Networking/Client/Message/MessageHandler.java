package it.polimi.ingsw.gc19.Networking.Client.Message;

import it.polimi.ingsw.gc19.Model.Chat.Message;
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
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.View.GameLocalView.*;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalTable;

import java.util.ArrayDeque;

/**
 * This class is responsible for visiting all messages (excluding heartbeats) received
 * from the network. If necessary, it updates {@link LocalModel} and notifies {@link ClientController}.
 * Its implementation is strongly based on visitor design pattern.
 */
public class MessageHandler extends Thread implements AllMessageVisitor{

    private final ArrayDeque<MessageToClient> messagesToHandle;

    private LocalModel localModel;
    private final ClientController clientController;

    public MessageHandler(ClientController clientController){
        this.messagesToHandle = new ArrayDeque<>();
        this.clientController = clientController;
    }

    /**
     * Getter for {@link ClientController} associated.
     * @return the {@link ClientController} associated to the object.
     */
    public ClientController getClientController() {
        return clientController;
    }

    /**
     * This method is used to push a new {@link MessageToClient} inside the dequeue of updates
     * of {@link MessageHandler}
     * @param message the {@link MessageToClient} to push inside the dequeue
     */
    public void update(MessageToClient message) {
        synchronized (this.messagesToHandle){
            this.messagesToHandle.add(message);
            this.messagesToHandle.notifyAll();
        }
    }

    /**
     * Setter for {@link LocalModel}
     * @param model the {@link LocalModel} to set
     */
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

    /**
     * Getter for {@link MessageToClient} dequeue
     * @return {@link ArrayDeque<MessageToClient>} containing all the {@link MessageToClient} that
     * {@link MessageHandler} haven't processed yet
     */
    public ArrayDeque<MessageToClient> getMessagesToHandle(){
        synchronized (this.messagesToHandle) {
            return this.messagesToHandle;
        }
    }

    /**
     * This method is inherited from {@link Thread}. It is executed by the thread
     * bound to {@link MessageHandler}. It retrieves one by one messages from dequeue
     * and processes them.
     */
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

    /**
     * This method is used to interrupt thread of {@link MessageHandler}
     */
    public void interruptMessageHandler(){
        this.interrupt();
    }

    /**
     * This method visits a {@link AcceptedChooseGoalCardMessage}: it updates {@link LocalModel}.
     * @param message the {@link AcceptedChooseGoalCardMessage} to visit
     */
    @Override
    public void visit(AcceptedChooseGoalCardMessage message) {
        this.localModel.setPrivateGoal(message.getGoalCard());
    }

    /**
     * This method visits a {@link AcceptedColorMessage}: it updates {@link LocalModel}.
     * @param message the {@link AcceptedColorMessage} to visit
     */
    @Override
    public void visit(AcceptedColorMessage message) {
        waitForLocalModel();
        if(message.getPlayer().equals(this.localModel.getNickname())) {
            this.localModel.setColor(message.getChosenColor());
        }
        else {
            this.localModel.getOtherStations().get(message.getPlayer()).setChosenColor(message.getChosenColor());
        }
    }

    /**
     * This method visits a {@link OwnAcceptedPickCardFromDeckMessage}: it updates {@link LocalModel}
     * and notifies {@link ClientController} to change its state.
     * @param message the {@link OwnAcceptedPickCardFromDeckMessage} to visit
     */
    @Override
    public void visit(OwnAcceptedPickCardFromDeckMessage message) {
        waitForLocalModel();
        this.localModel.updateCardsInHand(message.getPickedCard());
        this.localModel.setNextSeedOfDeck(message.getDeckType(), message.getSymbol());
        clientController.getCurrentState().nextState(message);
    }

    /**
     * This method visits a {@link OtherAcceptedPickCardFromDeckMessage}: it updates {@link LocalModel}
     * and notifies {@link ClientController} to change its state.
     * @param message the {@link OtherAcceptedPickCardFromDeckMessage} to visit
     */
    @Override
    public void visit(OtherAcceptedPickCardFromDeckMessage message) {
        waitForLocalModel();
        this.localModel.getOtherStations().get(message.getNick()).addBackCard(new Tuple<>(message.getBackPickedCard().x(), message.getBackPickedCard().y()));
        this.localModel.setNextSeedOfDeck(message.getDeckType(), message.getSymbol());
        clientController.getCurrentState().nextState(message);
    }

    /**
     * This method visits a {@link AcceptedPickCardFromTable}: it updates {@link LocalModel}
     * and notifies {@link ClientController} to change its state.
     * @param message the {@link AcceptedPickCardFromTable} to visit
     */
    @Override
    public void visit(AcceptedPickCardFromTable message) {
        waitForLocalModel();
        if(message.getNick().equals(this.localModel.getNickname())) {
            this.localModel.updateCardsInHand(message.getPickedCard());
        }
        else {
            this.localModel.getOtherStations().get(message.getNick()).addBackCard(new Tuple<>(message.getPickedCard().getSeed(), message.getPickedCard().getCardType()));
        }
        this.localModel.updateCardsInTable(message.getCardToPutInSlot(), message.getDeckType(), message.getCoords());
        this.localModel.setNextSeedOfDeck(message.getDeckType(), message.getSymbol());
        clientController.getCurrentState().nextState(message);

    }

    /**
     * This method visits a {@link AcceptedPlacePlayableCardMessage}: it updates {@link LocalModel}
     * and notifies {@link ClientController} to change its state.
     * @param message the {@link AcceptedPlacePlayableCardMessage} to visit
     */
    @Override
    public void visit(AcceptedPlacePlayableCardMessage message) {
        waitForLocalModel();

        this.localModel.placeCard(message.getNick(), message.getAnchorCode(),
                message.getCardToPlace(), message.getDirection());

        this.localModel.setNumPoints(message.getNick(), message.getNumPoints());
        this.localModel.setVisibleSymbols(message.getNick(), message.getVisibleSymbols());
    }

    /**
     * This method visits a {@link AcceptedPlaceInitialCard}: it updates {@link LocalModel}.
     * @param message the {@link AcceptedPlaceInitialCard} to visit
     */
    @Override
    public void visit(AcceptedPlaceInitialCard message) {
        waitForLocalModel();
        this.localModel.placeInitialCard(message.getNick(), message.getInitialCard());
        this.localModel.setVisibleSymbols(message.getNick(), message.getVisibleSymbols());
    }

    /**
     * This method visits a {@link RefusedActionMessage}: notifies {@link ClientController} to change its state.
     * @param message the {@link RefusedActionMessage} to visit
     */
    @Override
    public void visit(RefusedActionMessage message) {
        this.clientController.getCurrentState().nextState(message);
    }

    /**
     * This method visits a {@link NotifyChatMessage}: it updates {@link LocalModel}.
     * @param message the {@link NotifyChatMessage} to visit
     */
    @Override
    public void visit(NotifyChatMessage message) {
        waitForLocalModel();
        this.localModel.updateMessages(new Message(message.getMessage(), message.getSender(), String.valueOf(message.getHeader())));
    }

    /**
     * This method visits a {@link GameConfigurationMessage}: it updates {@link LocalModel}
     * and notifies {@link ClientController} to change its state.
     * @param message the {@link GameConfigurationMessage} to visit
     */
    @Override
    public void visit(GameConfigurationMessage message) {
        waitForLocalModel();
        this.localModel.setNumPlayers(message.getNumPlayers());
        if(message.getFirstPlayer() != null) {
            this.localModel.setFirstPlayer(message.getFirstPlayer());
        }
        clientController.getCurrentState().nextState(message);
    }

    /**
     * This method visits a {@link OtherStationConfigurationMessage}: it updates {@link LocalModel}.
     * @param message the {@link OtherStationConfigurationMessage} to visit
     */
    @Override
    public void visit(OtherStationConfigurationMessage message) {
        waitForLocalModel();
        this.localModel.setOtherStations(message.getNick(),
                new OtherStation(message.getNick(), message.getColor(), message.getVisibleSymbols(),
                        message.getNumPoints(), message.getPlacedCardSequence(), message.getCardsInHand()));
    }

    /**
     * This method visits a {@link OwnStationConfigurationMessage}: it updates {@link LocalModel}.
     * @param message the {@link OwnStationConfigurationMessage} to visit
     */
    @Override
    public void visit(OwnStationConfigurationMessage message) {
        this.clientController.getLocalModel().setPersonalStation(new PersonalStation(message.getNick(), message.getColor(), message.getVisibleSymbols(),
                                                                                     message.getNumPoints(), message.getPlacedCardSequence(), message.getPrivateGoalCard(),
                                                                                     message.getGoalCard1(), message.getGoalCard2(), message.getCardsInHand(), message.getInitialCard()));
    }

    /**
     * This method visits a {@link TableConfigurationMessage}: it updates {@link LocalModel}.
     * @param message the {@link TableConfigurationMessage} to visit
     */
    @Override
    public void visit(TableConfigurationMessage message) {
        waitForLocalModel();
        this.localModel.setTable(new LocalTable(message.getSxResource(), message.getDxResource(),
                                                message.getSxGold(), message.getDxGold(), message.getSxPublicGoal(),
                                                message.getDxPublicGoal(), message.getNextSeedOfResourceDeck(),
                                                message.getNextSeedOfGoldDeck()));
    }

    /**
     * This method visits an {@link AvailableColorsMessage}: it updates {@link LocalModel}.
     * @param message the {@link AvailableColorsMessage} to visit
     */
    @Override
    public void visit(AvailableColorsMessage message) {
        this.localModel.setAvailableColors(message.getAvailableColors());
    }

    /**
     * This method visits a {@link EndGameMessage}: it updates {@link LocalModel}
     * and notifies {@link ClientController} to change its state.
     * @param message the {@link EndGameMessage} to visit
     */
    @Override
    public void visit(EndGameMessage message) {
        localModel.setWinners(message.getWinnerNicks());
        clientController.getCurrentState().nextState(message);
    }

    /**
     * This method visits a {@link GamePausedMessage}: it notifies {@link ClientController} to change its state.
     * @param message the {@link GamePausedMessage} to visit
     */
    @Override
    public void visit(GamePausedMessage message) {
        clientController.getCurrentState().nextState(message);
    }

    /**
     * This method visits a {@link GameResumedMessage}: it notifies {@link ClientController} to change its state.
     * @param message the {@link GameResumedMessage} to visit
     */
    @Override
    public void visit(GameResumedMessage message) {
        clientController.getCurrentState().nextState(message);
    }

    /**
     * This method visits a {@link NewPlayerConnectedToGameMessage}: it updates {@link LocalModel}
     * and notifies {@link ClientController} to change its state.
     * @param message the {@link NewPlayerConnectedToGameMessage} to visit
     */
    @Override
    public void visit(NewPlayerConnectedToGameMessage message) {
        waitForLocalModel();
        this.localModel.setPlayerActive(message.getPlayerName());
    }

    /**
     * This method visits a {@link StartPlayingGameMessage}: it updates {@link LocalModel}
     * and notifies {@link ClientController} to change its state.
     * @param message the {@link StartPlayingGameMessage} to visit
     */
    @Override
    public void visit(StartPlayingGameMessage message) {
        waitForLocalModel();
        this.localModel.setFirstPlayer(message.getNickFirstPlayer());
        clientController.getCurrentState().nextState(message);
    }

    /**
     * This method visits a {@link CreatedGameMessage}: it notifies {@link ClientController} to change its state.
     * @param message the {@link CreatedGameMessage} to visit
     */
    @Override
    public void visit(CreatedGameMessage message) {
        clientController.getCurrentState().nextState(message);
    }

    /**
     * This method visits a {@link AvailableGamesMessage}: it notifies {@link ClientController} to change its state.
     * @param message the {@link AvailableGamesMessage} to visit
     */
    @Override
    public void visit(AvailableGamesMessage message) {
        this.clientController.getCurrentState().nextState(message);
    }

    /**
     * This method visits a {@link BeginFinalRoundMessage}: it notifies {@link ClientController} to change its state.
     * @param message the {@link GameConfigurationMessage} to visit
     */
    @Override
    public void visit(BeginFinalRoundMessage message) {
        clientController.getCurrentState().nextState(message);
    }

    /**
     * This method visits a {@link CreatedPlayerMessage}: it notifies {@link ClientController} to change its state.
     * @param message the {@link CreatedPlayerMessage} to visit
     */
    @Override
    public void visit(CreatedPlayerMessage message) {
        clientController.getCurrentState().nextState(message);
    }

    /**
     * This method visits a {@link DisconnectedPlayerMessage}: it updates {@link LocalModel}
     * and notifies {@link ClientController} to change its state.
     * @param message the {@link DisconnectedPlayerMessage} to visit
     */
    @Override
    public void visit(DisconnectedPlayerMessage message) {
        waitForLocalModel();
        this.localModel.setPlayerInactive(message.getRemovedNick());
        clientController.getCurrentState().nextState(message);
    }

    /**
     * This method visits a {@link JoinedGameMessage}: it updates {@link LocalModel}
     * and notifies {@link ClientController} to change its state.
     * @param message the {@link JoinedGameMessage} to visit
     */
    @Override
    public void visit(JoinedGameMessage message) {
        clientController.getCurrentState().nextState(message);
    }

    /**
     * This method visits a {@link PlayerReconnectedToGameMessage}: it updates {@link LocalModel}
     * and notifies {@link ClientController} to change its state.
     * @param message the {@link PlayerReconnectedToGameMessage} to visit
     */
    @Override
    public  void visit(PlayerReconnectedToGameMessage message) {
        waitForLocalModel();
        this.localModel.setPlayerActive(message.getPlayerName());
        clientController.getCurrentState().nextState(message);
    }

    /**
     * This method visits a {@link GameHandlingErrorMessage}: it notifies {@link ClientController} to change its state.
     * @param message the {@link GameHandlingErrorMessage} to visit
     */
    @Override
    public void visit(GameHandlingErrorMessage message) {
        clientController.getCurrentState().nextState(message);
    }

    /**
     * This method visits a {@link DisconnectFromGameMessage}: it notifies {@link ClientController} to change its state.
     * @param message the {@link DisconnectFromGameMessage} to visit
     */
    @Override
    public void visit(DisconnectFromGameMessage message) {
        clientController.getCurrentState().nextState(message);
    }

    /**
     * This method visits a {@link DisconnectFromServerMessage}: it notifies {@link DisconnectFromServerMessage} to change its state.
     * @param message the {@link GameConfigurationMessage} to visit
     */
    @Override
    public void visit(DisconnectFromServerMessage message) {
        clientController.getCurrentState().nextState(message);
    }

    /**
     * This method visits a {@link TurnStateMessage}: it notifies {@link ClientController} to change its state.
     * @param message the {@link TurnStateMessage} to visit
     */
    @Override
    public void visit(TurnStateMessage message) {
        clientController.getCurrentState().nextState(message);
    }

    /**
     * This method visits a {@link NetworkHandlingErrorMessage}: it notifies {@link ClientController} to change its state.
     * @param message the {@link NetworkHandlingErrorMessage} to visit
     */
    @Override
    public void visit(NetworkHandlingErrorMessage message) {
        clientController.getCurrentState().nextState(message);
    }

}