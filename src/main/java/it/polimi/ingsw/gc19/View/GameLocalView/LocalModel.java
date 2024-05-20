package it.polimi.ingsw.gc19.View.GameLocalView;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Chat.Message;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.LocalModelEvents;
import it.polimi.ingsw.gc19.View.Listeners.ListenersManager;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupEvent;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedPlacePlayableCardMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.TableConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OwnStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedPickCardFromTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The class is used by the client to save the information of the current game.
 * The class is created and saves the data contained in various messages sent
 * by the server after each action.
 * It is used to save the data that the client will see in its view.
 * Other classes specifically created for this reason that are generated
 * and used through this class are:
 * {@link LocalStationPlayer}
 * {@link OtherStation}
 * {@link PersonalStation}
 * {@link LocalTable}
 */

public class LocalModel {
    /**
     * The attribute contains the Local Stations ({@link LocalStationPlayer})
     * of each player of the game the client is currently playing in.
     * The Map contains both the personal and the other player stations.
     */
    private final ConcurrentHashMap<String, LocalStationPlayer> playerStations;

    /**
     * The map contains the state of each player in a game: for each
     * player that is in the game a {@link State} is saved to denote if
     * that player currently active or inactive.
     */
    private final ConcurrentHashMap<String, State> playerState;
    private String nickname;
    private String gameName;

    /**
     * The attribute denotes the table that the client will see.
     * After the message containing the table configuration is received
     * ({@link TableConfigurationMessage})
     * the table is created and contains the information that the client can use to
     * see the public goal cards or the cards to pick.
     */
    private LocalTable table;

    /**
     * The first player of the game, so the player that will start
     * placing and picking cards when the game starts.
     */
    private String firstPlayer;

    /**
     * Object used as a thread lock to do not
     * permit concurrent modifications of the table.
     */
    private final Object lockTable;

    /**
     * Attribute containing the number of players that can play in the game
     * the client joined.
     */
    private int numPlayers;

    /**
     * Map containing all {@link PlayableCard} that
     * have been placed or picked so far.
     */
    private final ConcurrentHashMap<String, PlayableCard> previousPlayableCards;

    /**
     * Map containing the private Goal cards and the public Goal cards
     * ({@link GoalCard}).
     */
    private final ConcurrentHashMap<String, GoalCard> previousGoalCards;

    /**
     * A list containing the available colors that the client can
     * choose as the color of its pawn for the game.
     */
    private List<Color> availableColors;

    /**
     * List of {@link Message} containing the
     * Chat messages arrived so far.
     */
    private final ArrayList<Message> messages;

    /**
     * Attribute used to handle the listeners that listen and notify
     * the view of changes or events generated during the game.
     */
    private ListenersManager listenersManager;

    /**
     * At the end of the game the winners, represented with their
     * String nickname, will be saved in this attribute.
     */
    private ArrayList<String> winners;

    public LocalModel(){
        playerStations = new ConcurrentHashMap<>();
        playerState = new ConcurrentHashMap<>();
        previousPlayableCards = new ConcurrentHashMap<>();
        previousGoalCards = new ConcurrentHashMap<>();
        lockTable = new Object();
        messages = new ArrayList<>();
        winners = new ArrayList<>();
    }

    /**
     * Method used to set the ListenersManager, used to handle the listeners
     * that listen and notify the view of changes or events generated
     * during the game.
     * @param listenersManager, the ListenersManager that will notify the view.
     */
    public void setListenersManager(ListenersManager listenersManager){
        this.listenersManager = listenersManager;
    }

    /**
     * To set the personal station after a
     * {@link OwnStationConfigurationMessage}
     * is arrived.
     * @param localStation, PersonalStation instance created by the
     *                      {@link MessageHandler}
     *                      after the message cited above is arrived.
     */
    public void setPersonalStation(PersonalStation localStation) {
        System.out.println("qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq");
        synchronized (this.playerStations) {
            this.playerStations.put(this.nickname, localStation);
            this.playerStations.notifyAll();
        }
        synchronized (previousGoalCards){
            GoalCard goalCard = localStation.getPrivateGoalCardInStation();
            if(goalCard != null) {
                previousGoalCards.put(goalCard.getCardCode(), goalCard);
            }
            GoalCard[] goalCards = localStation.getPrivateGoalCardsInStation();
            if(goalCards[0] != null && goalCards[1] != null){
                previousGoalCards.put(goalCards[0].getCardCode(), goalCards[0]);
                previousGoalCards.put(goalCards[1].getCardCode(), goalCards[1]);
            }
        }
        synchronized (previousPlayableCards){
            for(PlayableCard card : localStation.getCardsInHand()){
                previousPlayableCards.put(card.getCardCode(), card);
            }
        }

        synchronized (this.playerState){
            this.playerState.put(localStation.ownerPlayer, State.ACTIVE);
        }
        this.addCardsFromStationToMap(localStation);

        this.listenersManager.notifyStationListener(localStation);
    }

    /**
     * Message used to add the cards contained in a station to the map
     * that contains all the Cards that have been played so far.
     * @param station, LocalStationPlayer from which retrieve the cards.
     */
    private void addCardsFromStationToMap(LocalStationPlayer station){
        synchronized (previousPlayableCards){
            for(Tuple<PlayableCard, Tuple<Integer, Integer>> cardAndPos : station.getPlacedCardSequence()){
                PlayableCard card = cardAndPos.x();

                previousPlayableCards.put(card.getCardCode(), card);
            }
        }
    }

    /**
     * To return the Personal Station.
     * @return the element of the playerStations whose key is equal
     * to the nickname of the client, so its PersonalStation.
     */
    public PersonalStation getPersonalStation() {
        synchronized (playerStations) {
            while (!this.playerStations.containsKey(nickname)){
                try{
                    this.playerStations.wait();
                }
                catch (InterruptedException interruptedException){
                    Thread.currentThread().interrupt();
                }
            }
            return (PersonalStation) this.playerStations.get(this.nickname);
        }
    }

    /**
     * To add the station of another player to the map containing all the Stations.
     * @param nickname the nickname whose map we want to add.
     * @param otherStation the station we want to save.
     */
    public void setOtherStations(String nickname, OtherStation otherStation) {
        synchronized (playerStations) {
            this.playerStations.put(nickname, otherStation);
        }
        synchronized (playerState) {
            this.playerState.put(nickname, State.ACTIVE);
        }
        this.listenersManager.notifyStationListener(otherStation);
    }

    /**
     * To change the state {@link State} of a player to INACTIVE.
     * @param nickname, the nickname of the player we want to set inactive.
     */
    public void setPlayerInactive(String nickname){
        synchronized (playerState) {
            this.playerState.put(nickname, State.INACTIVE);
        }
        this.listenersManager.notifyLocalModelListener(LocalModelEvents.DISCONNECTED_PLAYER, this, nickname);
    }

    /**
     * To set the state {@link State} of a player to ACTIVE.
     * @param nickname the nickname of the player to set its state to ACTIVE.
     */
    public void setPlayerActive(String nickname){
        if(this.playerState.get(nickname) == State.INACTIVE) {
            this.listenersManager.notifyLocalModelListener(LocalModelEvents.RECONNECTED_PLAYER, this, nickname);
        }
        else{
            this.listenersManager.notifyLocalModelListener(LocalModelEvents.NEW_PLAYER_CONNECTED, this, nickname);
        }
        synchronized (playerState) {
            this.playerState.put(nickname, State.ACTIVE);
        }
    }

    /**
     * To choose a private Goal card {@link GoalCard} for the personal station.
     * @param goalCard the GoalCard the client has chosen.
     */
    public void setPrivateGoal(GoalCard goalCard) {
        synchronized (playerStations) {
            this.playerStations.get(this.nickname).setPrivateGoalCard(goalCard);
        }

        this.listenersManager.notifySetupListener(SetupEvent.ACCEPTED_PRIVATE_GOAL_CARD);

        if(finishedLocalSetup()){
            this.listenersManager.notifySetupListener(SetupEvent.COMPLETED);
        }
    }

    /**
     * To set the color of the pawn in the personal station.
     * @param color, the color of the pawn the client chose.
     */
    public void setColor(Color color){
        synchronized (this.playerStations) {
            this.playerStations.get(this.nickname).setChosenColor(color);
        }
        this.availableColors.remove(color);

        this.listenersManager.notifySetupListener(SetupEvent.ACCEPTED_COLOR);
        if(finishedLocalSetup()){
            this.listenersManager.notifySetupListener(SetupEvent.COMPLETED);
        }
    }

    /**
     * To control if the client has already chosen its pawn color.
     * @return a boolean that is true if the {@link Color} associated
     * to the Client's personal station is not null.
     */
    public boolean isColorChosen(){
        synchronized (this.playerStations){
            return (this.playerStations.get(this.nickname).getChosenColor() != null);
        }
    }

    /**
     * This method is called after a {@link AcceptedPlacePlayableCardMessage} is received
     * Method used to place a card in a Station ({@link LocalStationPlayer}) given:
     * @param nickname, the name of the player in which station we want to place the card.
     * @param anchorCode, the anchor card code of the card we want to place the card from.
     * @param cardToPlace, the card we want to place.
     * @param direction, the direction in which we want to place the card, given the anchor card.
     */
    public void placeCard(String nickname, String anchorCode, PlayableCard cardToPlace, Direction direction) {
        synchronized (this.playerStations) {
            this.playerStations.get(nickname).placeCard(cardToPlace, anchorCode, direction);
        }
        synchronized (playerState) {
            this.playerState.put(nickname, State.ACTIVE);
        }

        synchronized (previousPlayableCards) {
            previousPlayableCards.put(cardToPlace.getCardCode(), cardToPlace);
        }

        if(this.nickname.equals(nickname)) {
            this.listenersManager.notifyStationListener((PersonalStation) this.playerStations.get(nickname));
        }
        else {
            this.listenersManager.notifyStationListener((OtherStation) this.playerStations.get(nickname));
        }
    }

    /**
     * To place the initial card given:
     * @param nickname the nickname of the player that wants its initial card to be placed.
     * @param initialCard the {@link PlayableCard} containing the initialcard the user wants to place.
     */
    public void placeInitialCard(String nickname, PlayableCard initialCard){
        synchronized (playerStations) {
            this.playerStations.get(nickname).placeInitialCard(initialCard);
        }
        synchronized (playerState) {
            this.playerState.put(nickname, State.ACTIVE);
        }

        synchronized (previousPlayableCards) {
            previousPlayableCards.put(initialCard.getCardCode(), initialCard);
        }

        if(this.nickname.equals(nickname)) {
            this.listenersManager.notifySetupListener(SetupEvent.ACCEPTED_INITIAL_CARD);
        }

        if(finishedLocalSetup()){
            this.listenersManager.notifySetupListener(SetupEvent.COMPLETED);
        }
    }

    /**
     * To check if the client has finished its local setup actions: choosing color,
     * setting the private goal and placing the initial card.
     * @return a boolean containing True if the setup phase is ended.
     */
    private boolean finishedLocalSetup(){
        return this.getPersonalStation().getChosenColor() != null && this.getPersonalStation().getPrivateGoalCardInStation() != null && !this.getPersonalStation().getPlacedCardSequence().isEmpty();
    }

    /**
     * To set the nickname of the Client.
     * @param nickname the nickname of the client.
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * To get the nickname of the client.
     * @return the nickname of the client.
     */
    public String getNickname() {
        return this.nickname;
    }

    /**
     * To ask if a card is placeable in the personal station of the client.
     * @param cardToPlace, the card the client wants to place.
     * @param anchor, the anchor from which the client wants to place the card.
     * @param direction, the direction of the cardToPlace from the anchor.
     * @return a boolean containing True only if the card is placeable.
     */
    public boolean isCardPlaceablePersonalStation(PlayableCard cardToPlace, PlayableCard anchor, Direction direction){
        return this.playerStations.get(this.nickname).cardIsPlaceable(anchor, cardToPlace, direction);
    }

    /**
     * To add a playable card in the personal station {@link PersonalStation} hand.
     * @param playableCard, the playable {@link PlayableCard}
     *                      card the client wants to add to its hand.
     */
    public void updateCardsInHand(PlayableCard playableCard){
        synchronized (this.playerStations) {
            ((PersonalStation) this.playerStations.get(this.nickname)).updateCardsInHand(playableCard);
        }

        synchronized (previousPlayableCards) {
            previousPlayableCards.put(playableCard.getCardCode(), playableCard);
        }

        this.listenersManager.notifyStationListener((PersonalStation) this.playerStations.get(this.nickname));
    }

    /**
     * After a message {@link AcceptedPickCardFromTable} arrived.
     * To update the cards in table after a card is picked, given:
     * @param playableCard, the card that is now in the Table.
     * @param playableCardType, the type of the card that is now in the top of the deck.
     * @param position, the position from which the card was picked.
     */
    public void updateCardsInTable(PlayableCard playableCard, PlayableCardType playableCardType, int position){
        synchronized (this.lockTable) {
            if (playableCardType == PlayableCardType.GOLD) {
                if (position == 0) {
                    this.table.setGold1(playableCard);
                } else {
                    this.table.setGold2(playableCard);
                }
            }
            if (playableCardType == PlayableCardType.RESOURCE) {
                if (position == 0) {
                    this.table.setResource1(playableCard);
                } else {
                    this.table.setResource2(playableCard);
                }
            }
        }

        synchronized (previousPlayableCards){
            previousPlayableCards.put(playableCard.getCardCode(), playableCard);
        }
        
        this.listenersManager.notifyTableListener(table);
    }

    /**
     * To return the stations of the other players.
     * @return an hashmap containing the stations of the other players and their nicknames.
     */
    public ConcurrentHashMap<String, OtherStation> getOtherStations() {
        ConcurrentHashMap<String, OtherStation> otherStations =
                new ConcurrentHashMap<>();
        synchronized (playerStations) {
            for(LocalStationPlayer station : playerStations.values()){
                if(!station.getOwnerPlayer().equals(this.nickname)){
                    otherStations.put(station.getOwnerPlayer(), (OtherStation) station);
                }
            }
        }
        return otherStations;
    }

    /**
     * To return all the stations of all the players in the game.
     * @return a hashmap containing the stations of the players in the game given their nickname.
     */
    public ConcurrentHashMap<String, LocalStationPlayer> getStations() {
        ConcurrentHashMap<String, LocalStationPlayer> allStations =
                new ConcurrentHashMap<>();
        synchronized (playerStations) {
            for(LocalStationPlayer station : playerStations.values()){
                allStations.put(station.getOwnerPlayer(), station);
            }
        }
        return allStations;
    }

    /**
     * To set the table after a message {@link TableConfigurationMessage} is received.
     * @param table, the table created by the {@link MessageHandler}
     */
    public void setTable(LocalTable table) {
        synchronized (this.lockTable) {
            this.table = table;
        }
        synchronized (previousGoalCards){
            GoalCard[] goalCards = {table.getPublicGoal1(), table.getPublicGoal2()};
            for (GoalCard goalCard : goalCards) {
                previousGoalCards.put(goalCard.getCardCode(), goalCard);
            }
        }

        synchronized (previousPlayableCards){
            PlayableCard[] resources = {table.getResource1(), table.getResource2(), table.getGold1(), table.getGold2()};
            for (PlayableCard card : resources) {
                if (card != null) {
                    previousPlayableCards.put(card.getCardCode(), card);
                }
            }
        }

        this.listenersManager.notifyTableListener(table);
    }

    /**
     * @return the table fo the game.
     */
    public LocalTable getTable() {
        synchronized (this.lockTable) {
            return table;
        }
    }

    /**
     * To return the information of the next card the player can pick from the deck:
     * @param type, the type of the card.
     * @param symbol, the symbols of the card.
     */
    public void setNextSeedOfDeck(PlayableCardType type, Symbol symbol) {
        switch (type) {
            case RESOURCE -> { this.getTable().setNextSeedOfResourceDeck(symbol); }
            case GOLD -> { this.getTable().setNextSeedOfGoldDeck(symbol); }
        }

        this.listenersManager.notifyTableListener(table);
    }

    /**
     * To set the first player in the game. The first player is the one with the black pawn.
     * @param firstPlayer, the nickname of the first player.
     */
    public void setFirstPlayer(String firstPlayer) {
        this.firstPlayer = firstPlayer;

    }

    /**
     * @return a String containing the nickname of the first player of the game.
     */
    public String getFirstPlayer() {
        return firstPlayer;
    }

    /**
     * To set the:
     * @param numPlayers, the number of players in the game.
     */
    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }

    /**
     * To return:
     * @return the number of players in the game.
     */
    public int getNumPlayers() {
        return numPlayers;
    }

    /**
     * To set the
     * @param availableColors, the list of colors from which the client
     *                         can choose the color of its pawn.
     */
    public void setAvailableColors(List<Color> availableColors) {
        this.availableColors = new ArrayList<>(availableColors);
        if(!this.isColorChosen()) {
            this.listenersManager.notifySetupListener(SetupEvent.AVAILABLE_COLOR);
        }
    }

    /**
     * To return
     * @return the list of available colors.
     */
    public List<Color> getAvailableColors() {
        return availableColors;
    }

    /**
     * To add a message to the list containing the chat messages.
     * @param message, a new received message.
     */
    public void updateMessages(Message message){
        synchronized (this.messages){
            messages.add(message);
        }
        this.listenersManager.notifyChatListener(messages);
    }

    /**
     * To return
     * @return the list of chat messages sent so far.
     */
    public ArrayList<Message> getMessages() {
        synchronized (this.messages) {
            return this.messages;
        }
    }

    /**
     * To return the goal card previously chosen or available in the table.
     * @param cardCode, the code of the goal card we want to have information from.
     * @return the goalcard whoose card code was given as a parameter.
     */
    public GoalCard getGoalCard(String cardCode){
        synchronized (this.previousGoalCards){
            return previousGoalCards.get(cardCode);
        }
    }

    /**
     * To get a placed or given playable card given its code.
     * @param cardCode, the code of the available card.
     * @return the Playable card.
     */
    public PlayableCard getPlayableCard(String cardCode){
        synchronized (this.previousPlayableCards){
            return previousPlayableCards.get(cardCode);
        }
    }

    /**
     * To return
     * @return the number of active players.
     */
    public int getNumActivePlayers(){
        return (int) playerState.values().stream()
                .filter(e -> e == State.ACTIVE)
                .count();
    }

    /**
     * To set the number of active points of a given player.
     * @param nickname the player whose points will be updated.
     * @param numPoints the new number of points for the player.
     */
    public void setNumPoints(String nickname, int numPoints){
        synchronized (this.playerStations){
            this.playerStations.get(nickname).setNumPoints(numPoints);
        }
    }

    /**
     * To set the visible symbols of a certain station.
     * @param nickname the nickname whose station we want to update.
     * @param visibleSymbols a map containing for each symbol the numer of times that it is visible in the station.
     */
    public void setVisibleSymbols(String nickname, Map<Symbol, Integer> visibleSymbols){
        synchronized (this.playerStations){
            this.playerStations.get(nickname).setVisibleSymbols(new HashMap<>(visibleSymbols));
        }
        if(nickname.equals(this.getNickname())) {
            this.listenersManager.notifyStationListener(this.getPersonalStation());
        }
        else {
            this.listenersManager.notifyStationListener(this.getOtherStations().get(nickname));
        }
    }

    /**
     * To return the state of a player given its nickname.
     * @param nickname, the nickname from which we want to retrieve the state.
     * @return the state of the player. {@link State}
     */
    public State getPlayerState(String nickname){
        synchronized (this.playerState){
            return playerState.get(nickname);
        }
    }

    /**
     * To set the name of a game.
     * @param gameName, the name of the game the client has joined or created.
     */
    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    /**
     * To return
     * @return the name of the game the client has joined or created.
     */
    public String getGameName() {
        return gameName;
    }

    /**
     * To set the game winners.
     * @param winners a List of strings containing the nicknames of the
     *                player that win the game.
     */
    public void setWinners(List<String> winners) {
        this.winners = new ArrayList<>(winners);
    }

    /**
     * To return
     * @return the list of nicknames of the players who won the game.
     */
    public ArrayList<String> getWinners() {
        return winners;
    }
}
