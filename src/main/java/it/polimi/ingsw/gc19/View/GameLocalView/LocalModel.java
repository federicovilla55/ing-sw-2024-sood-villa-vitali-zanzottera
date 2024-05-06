package it.polimi.ingsw.gc19.View.GameLocalView;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Chat.Message;
import it.polimi.ingsw.gc19.Model.Game.Player;
import it.polimi.ingsw.gc19.Model.Station.InvalidAnchorException;
import it.polimi.ingsw.gc19.Model.Station.InvalidCardException;
import it.polimi.ingsw.gc19.Model.Station.Station;
import it.polimi.ingsw.gc19.Utils.Tuple;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public void addChatPropertyChangeListener(PropertyChangeListener listener) {
        propertyChanger.addPropertyChangeListener("chat", listener);
    }

    public void removeChatPropertyChangeListener(PropertyChangeListener listener) {
        propertyChanger.removePropertyChangeListener("chat", listener);
    }

    public void addPersonalStationPropertyChangeListener(PropertyChangeListener listener) {
        propertyChanger.addPropertyChangeListener("personalStation", listener);
    }

    public void removePersonalStationPropertyChangeListener(PropertyChangeListener listener) {
        propertyChanger.removePropertyChangeListener("personalStation", listener);
    }

    public void addTablePropertyChangeListener(PropertyChangeListener listener) {
        propertyChanger.addPropertyChangeListener("table", listener);
    }

    public void removeTablePropertyChangeListener(PropertyChangeListener listener) {
        propertyChanger.removePropertyChangeListener("table", listener);
    }

    public void addOtherStationsPropertyChangeListener(PropertyChangeListener listener) {
        propertyChanger.addPropertyChangeListener("otherStations", listener);
    }

    public void removeOtherStationsPropertyChangeListener(PropertyChangeListener listener) {
        propertyChanger.removePropertyChangeListener("otherStations", listener);
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
        synchronized (playerStations) {
            this.playerStations.get(this.nickname).setChosenColor(color);
        }
        this.updatePersonalStation();
    }

    public void placeCard(String nickname, String anchorCode, PlayableCard cardToPlace, Direction direction, CardOrientation orientation) {
        synchronized (playerStations){
            Tuple<Integer, Integer> coords = this.playerStations.get(nickname).getCoord(anchorCode);
            this.playerStations.get(nickname).placeCard(cardToPlace, new Tuple<>(
                    coords.x() + direction.getX(), coords.y() + direction.getY()));
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

    public boolean isCardPlaceable(String nickname, PlayableCard anchor, PlayableCard cardToPlace, Direction direction){
        synchronized (playerStations) {
            return this.playerStations.get(nickname).cardIsPlaceable(anchor, cardToPlace, direction);
        }
    }

    public void placeCardPersonalStation(String anchorCode, PlayableCard cardToPlace, Direction direction, CardOrientation orientation){
        placeCard(this.nickname, anchorCode, cardToPlace, direction, orientation);
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void placeInitialCardPersonalStation(PlayableCard initialCard){
        placeInitialCard(this.nickname, initialCard);
    }

    public boolean isCardPlaceablePersonalStation(PlayableCard cardToPlace, PlayableCard anchor, Direction direction){
        return isCardPlaceable(this.nickname, anchor, cardToPlace, direction);
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

    public ConcurrentHashMap<String, LocalStationPlayer> getOtherStations() {
        synchronized (playerStations) {
            ConcurrentHashMap<String, LocalStationPlayer> otherStations =
                    new ConcurrentHashMap<>(this.playerStations);
            playerStations.remove(this.nickname);
            return otherStations;
        }
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

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameName() {
        return gameName;
    }
}
