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
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalModel;

import java.util.List;
import java.util.Map;

/**
 * Handles incoming messages from the server to the client by implementing the AllMessageVisitor interface (design patter visitor).
 */
public class MessageHandler implements AllMessageVisitor {
    private ClientInterface client;

    private LocalModel localModel;

    public MessageHandler(ClientInterface client){
        this.client = client;
        this.localModel = new LocalModel();
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
    public void visit(AcceptedPlacePlayableCardMessage message) {
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

    @Override
    public void visit(NetworkHandlingErrorMessage message) {

    }
}

// @todo: for now those class are here so that a MessageHandler can be used
// without having to care about the network used (RMI or TCP). Maybe the
// Table and Station class can be moved to the same package that contains
// the logic for the client-side game handling.
class ViewTable{
    private PlayableCard resource1;
    private PlayableCard resource2;
    private PlayableCard gold1;
    private PlayableCard gold2;
    private GoalCard publicGoal1;
    private  GoalCard publicGoal2;
    private Symbol nextSeedOfResourceDeck;
    private Symbol nextSeedOfGoldDeck;

    ViewTable(PlayableCard resource1, PlayableCard resource2, PlayableCard gold1, PlayableCard gold2,
          GoalCard publicGoal1, GoalCard publicGoal2, Symbol nextSeedOfResourceDeck, Symbol nextSeedOfGoldDeck){
        this.resource1 = resource1;
        this.resource2 = resource2;
        this.gold1 = gold1;
        this.gold2 = gold2;
        this.publicGoal1 = publicGoal1;
        this.publicGoal2 = publicGoal2;
        this.nextSeedOfResourceDeck = nextSeedOfResourceDeck;
        this.nextSeedOfGoldDeck = nextSeedOfGoldDeck;
    }
}

class ViewStation{
    private String nick;
    private Color color;
    private List<PlayableCard> cardsInHand;
    private Map<Symbol, Integer> visibleSymbols;
    private GoalCard privateGoalCard;
    private int numPoints;
    private PlayableCard initialCard;
    private GoalCard goalCard1;
    private GoalCard goalCard2;
    private List<Tuple<PlayableCard, Tuple<Integer,Integer>>> placedCardSequence;

    ViewStation(String nick, Color color, List<PlayableCard> cardsInHand, Map<Symbol, Integer> visibleSymbols, GoalCard privateGoalCard, int numPoints,
            PlayableCard initialCard, GoalCard goalCard1, GoalCard goalCard2, List<Tuple<PlayableCard,Tuple<Integer,Integer>>> placedCardSequence){
        this.nick = nick;
        this.color = color;
        this.cardsInHand = cardsInHand;
        this.visibleSymbols = visibleSymbols;
        this.privateGoalCard = privateGoalCard;
        this.numPoints = numPoints;
        this.initialCard = initialCard;
        this.goalCard1 = goalCard1;
        this.goalCard2 = goalCard2;
        this.placedCardSequence = placedCardSequence;
    }
}