package it.polimi.ingsw.gc19.View.GameLocalView;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Chat.Message;
import it.polimi.ingsw.gc19.Utils.Tuple;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
    private PropertyChangeSupport propertyChanger = new PropertyChangeSupport(this);

    public LocalModel(){
        playerStations = new ConcurrentHashMap<>();
        playerState = new ConcurrentHashMap<>();
        previousPlayableCards = new ConcurrentHashMap<>();
        previousGoalCards = new ConcurrentHashMap<>();
        lockTable = new Object();
        messages = new ArrayList<>();
    }

    public void updateChat() {
        fireChatPropertyChange();
    }

    public void updatePersonalStation() {
        firePersonalStationPropertyChange();
    }

    public void updateOtherStations() {
        fireOtherStationsPropertyChange();
    }

    public void updateTable() {
        fireTablePropertyChange();
    }

    public void addAllPropertyChangeListener(PropertyChangeListener listener){
        addTablePropertyChangeListener(listener);
        addOtherStationsPropertyChangeListener(listener);
        addChatPropertyChangeListener(listener);
        addPersonalStationPropertyChangeListener(listener);
    }

    public void addChatPropertyChangeListener(PropertyChangeListener listener) {
        propertyChanger.addPropertyChangeListener("chat", listener);
    }

    public void addPersonalStationPropertyChangeListener(PropertyChangeListener listener) {
        propertyChanger.addPropertyChangeListener("personalStation", listener);
    }

    public void addTablePropertyChangeListener(PropertyChangeListener listener) {
        propertyChanger.addPropertyChangeListener("table", listener);
    }

    public void addOtherStationsPropertyChangeListener(PropertyChangeListener listener) {
        propertyChanger.addPropertyChangeListener("otherStations", listener);
    }

    private void fireChatPropertyChange() {
        propertyChanger.firePropertyChange("chat", null, this.getMessages());
    }

    private void firePersonalStationPropertyChange() {
        propertyChanger.firePropertyChange("personalStation", null, this.getPersonalStation());
    }

    private void fireTablePropertyChange() {
        propertyChanger.firePropertyChange("table", null, this.getTable());
    }

    private void fireOtherStationsPropertyChange() {
        propertyChanger.firePropertyChange("otherStations", null, this.getOtherStations());
    }

    public void setPersonalStation(PersonalStation localStation) {
        synchronized (this.playerStations) {
            this.playerStations.put(this.nickname, localStation);
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

        this.updatePersonalStation();
    }

    public void addCardsFromStationToMap(LocalStationPlayer station){
        synchronized (previousPlayableCards){
            for(Tuple<PlayableCard, Tuple<Integer, Integer>> cardAndPos : station.getPlacedCardSequence()){
                PlayableCard card = cardAndPos.x();

                previousPlayableCards.put(card.getCardCode(), card);
            }
        }
    }

    public PersonalStation getPersonalStation() {
        synchronized (playerStations) {
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
        this.updateOtherStations();
    }

    public void setPlayerInactive(String nickname){
        synchronized (playerState) {
            this.playerState.put(nickname, State.INACTIVE);
        }
    }

    public void setPlayerActive(String nickname){
        synchronized (playerState) {
            this.playerState.put(nickname, State.ACTIVE);
        }
    }

    public void setPrivateGoal(GoalCard goalCard){
        synchronized (playerStations){
            this.playerStations.get(this.nickname).setPrivateGoalCard(goalCard);
        }
        this.updatePersonalStation();
    }


    public void setPrivateGoalCard(int cardIdx){
        synchronized (playerStations){
            this.playerStations.get(this.nickname).setPrivateGoalCard(cardIdx);
        }
        this.updatePersonalStation();
    }

    public void setColor(Color color){
        synchronized (this.playerStations) {
            this.playerStations.get(this.nickname).setChosenColor(color);
        }
        this.updatePersonalStation();
    }

    public void placeCard(String nickname, String anchorCode, PlayableCard cardToPlace, Direction direction) {
        synchronized (this.playerStations) {
            Tuple<Integer, Integer> coords = this.playerStations.get(nickname).getCoord(anchorCode);
            this.playerStations.get(nickname).placeCard(cardToPlace, anchorCode, direction);
        }
        synchronized (playerState) {
            this.playerState.put(nickname, State.ACTIVE);
        }

        if(this.nickname.equals(nickname)) {
            this.updatePersonalStation();
        } else {
            this.updateOtherStations();
        }
    }

    public void placeInitialCard(String nickname, PlayableCard initialCard){
        synchronized (playerStations) {
            this.playerStations.get(nickname).placeInitialCard(initialCard);
        }
        synchronized (playerState) {
            this.playerState.put(nickname, State.ACTIVE);
        }
        if(this.nickname.equals(nickname)) {
            this.updatePersonalStation();
        } else {
            this.updateOtherStations();
        }
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
        this.updatePersonalStation();
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
        
        this.updateTable();
    }

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

        this.updateTable();
    }

    public LocalTable getTable() {
        synchronized (this.lockTable) {
            return table;
        }
    }

    public void setFirstPlayer(String firstPlayer) {
        if(firstPlayer == null) return;

        this.firstPlayer = firstPlayer;

        // I suppose that there is an element in the GUI/TUI
        // that will contain the black pawn if the player
        // is the first one. Because I don't know if it
        // is the personal station or not I have to decide
        // at run time.
        if(this.firstPlayer.equals(this.nickname)){
            this.updatePersonalStation();
        } else {
            this.updateOtherStations();
        }
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
    }

    public List<Color> getAvailableColors() {
        return availableColors;
    }

    public void updateMessages(String messageContent, String sender, List<String> receivers){
        synchronized (this.messages){
            messages.add(new Message(messageContent, sender, String.valueOf(receivers)));
        }
        updateChat();
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
                .filter(e -> e==State.ACTIVE)
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

}
