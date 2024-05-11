package it.polimi.ingsw.gc19.View.GameLocalView;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Chat.Message;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.LocalModelEvents;
import it.polimi.ingsw.gc19.View.Listeners.ListenersManager;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalModel {
    private final ConcurrentHashMap<String, LocalStationPlayer> playerStations;
    private final ConcurrentHashMap<String, State> playerState;
    private String nickname;
    private String gameName;
    private LocalTable table;
    private String firstPlayer;
    private final Object lockTable;
    private int numPlayers;
    private final ConcurrentHashMap<String, PlayableCard> previousPlayableCards;
    private final ConcurrentHashMap<String, GoalCard> previousGoalCards;
    private List<Color> availableColors;
    private final ArrayList<Message> messages;
    private ListenersManager listenersManager;
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

    public void setListenersManager(ListenersManager listenersManager){
        this.listenersManager = listenersManager;
    }

    public void setPersonalStation(PersonalStation localStation) {
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

    private void addCardsFromStationToMap(LocalStationPlayer station){
        synchronized (previousPlayableCards){
            for(Tuple<PlayableCard, Tuple<Integer, Integer>> cardAndPos : station.getPlacedCardSequence()){
                PlayableCard card = cardAndPos.x();

                previousPlayableCards.put(card.getCardCode(), card);
            }
        }
    }

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

    public void setOtherStations(String nickname, OtherStation otherStation) {
        synchronized (playerStations) {
            this.playerStations.put(nickname, otherStation);
        }
        synchronized (playerState) {
            this.playerState.put(nickname, State.ACTIVE);
        }
        this.listenersManager.notifyStationListener(otherStation);
    }

    public void setPlayerInactive(String nickname){
        synchronized (playerState) {
            this.playerState.put(nickname, State.INACTIVE);
        }
        this.listenersManager.notifyLocalModelListener(LocalModelEvents.DISCONNECTED_PLAYER, this, nickname);
    }

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

    public void setPrivateGoal(GoalCard goalCard) {
        synchronized (playerStations) {
            this.playerStations.get(this.nickname).setPrivateGoalCard(goalCard);
        }

        this.listenersManager.notifySetupListener(SetupEvent.ACCEPTED_PRIVATE_GOAL_CARD);

        if(finishedLocalSetup()){
            this.listenersManager.notifySetupListener(SetupEvent.COMPLETED);
        }
    }

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

    public boolean isColorChosen(){
        synchronized (this.playerStations){
            return (this.playerStations.get(this.nickname).getChosenColor() != null);
        }
    }

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

    private boolean finishedLocalSetup(){
        return this.getPersonalStation().getChosenColor() != null && this.getPersonalStation().getPrivateGoalCardInStation() != null && !this.getPersonalStation().getPlacedCardSequence().isEmpty();
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return this.nickname;
    }

    public boolean isCardPlaceablePersonalStation(PlayableCard cardToPlace, PlayableCard anchor, Direction direction){
        return this.playerStations.get(this.nickname).cardIsPlaceable(anchor, cardToPlace, direction);
    }

    public void updateCardsInHand(PlayableCard playableCard){
        synchronized (this.playerStations) {
            ((PersonalStation) this.playerStations.get(this.nickname)).updateCardsInHand(playableCard);
        }

        synchronized (previousPlayableCards) {
            previousPlayableCards.put(playableCard.getCardCode(), playableCard);
        }

        this.listenersManager.notifyStationListener((PersonalStation) this.playerStations.get(this.nickname));
    }

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

    public ConcurrentHashMap<String, OtherStation> getStations() {
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

    public LocalTable getTable() {
        synchronized (this.lockTable) {
            return table;
        }
    }

    public void setNextSeedOfDeck(PlayableCardType type, Symbol symbol) {
        switch (type) {
            case RESOURCE -> { this.getTable().setNextSeedOfResourceDeck(symbol); }
            case GOLD -> { this.getTable().setNextSeedOfGoldDeck(symbol); }
        }

        this.listenersManager.notifyTableListener(table);
    }

    public void setFirstPlayer(String firstPlayer) {
        this.firstPlayer = firstPlayer;

    }

    public String getFirstPlayer() {
        return firstPlayer;
    }

    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public void setAvailableColors(List<Color> availableColors) {
        this.availableColors = availableColors;
        if(!this.isColorChosen()) {
            this.listenersManager.notifySetupListener(SetupEvent.AVAILABLE_COLOR);
        }
    }

    public List<Color> getAvailableColors() {
        return availableColors;
    }

    public void updateMessages(Message message){
        synchronized (this.messages){
            messages.add(message);
        }
        this.listenersManager.notifyChatListener(messages);
    }

    public ArrayList<Message> getMessages() {
        synchronized (this.messages) {
            return new ArrayList<>(messages);
        }
    }

    public GoalCard getGoalCard(String cardCode){
        synchronized (this.previousGoalCards){
            return previousGoalCards.get(cardCode);
        }
    }

    public PlayableCard getPlayableCard(String cardCode){
        synchronized (this.previousPlayableCards){
            return previousPlayableCards.get(cardCode);
        }
    }

    public int getNumActivePlayers(){
        return (int) playerState.values().stream()
                .filter(e -> e == State.ACTIVE)
                .count();
    }

    public void setNumPoints(String nickname, int numPoints){
        synchronized (this.playerStations){
            this.playerStations.get(nickname).setNumPoints(numPoints);
        }
    }

    public void setVisibleSymbols(String nickname, Map<Symbol, Integer> visibleSymbols){
        synchronized (this.playerStations){
            this.playerStations.get(nickname).setVisibleSymbols(new HashMap<>(visibleSymbols));
        }
        if(nickname.equals(this.getNickname())) {
            this.listenersManager.notifyStationListener(this.getPersonalStation());
        }
        else {
            this.listenersManager.notifyStationListener((OtherStation) this.playerStations.get(nickname));
        }
    }

    public State getPlayerState(String nickname){
        synchronized (this.playerState){
            return playerState.get(nickname);
        }
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameName() {
        return gameName;
    }

    public void setWinners(List<String> winners) {
        this.winners = new ArrayList<>(winners);
    }

    public ArrayList<String> getWinners() {
        return winners;
    }
}
