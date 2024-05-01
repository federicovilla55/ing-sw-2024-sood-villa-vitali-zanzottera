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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LocalModel {
    private PersonalStation personalStation;
    private ConcurrentHashMap<String, OtherStation> otherStations; // use station
    private ConcurrentHashMap<String, State> otherPlayerState; // use personal too
    private List<String> availableGames;
    private String nickname; // use nickname for methods in hashmap
    private String gameName;

    private LocalTable table;
    private String firstPlayer;
    private final Object lockTable;
    private final Object lockPersonal;
    private final Object lockOther;
    private int numPlayers;

    private final ConcurrentHashMap<String, PlayableCard> previousPlayableCards;
    private final ConcurrentHashMap<String, GoalCard> previousGoalCards;

    private List<Color> availableColors;

    private final ArrayList<Message> messages;

    public LocalModel(){
        personalStation = null;
        otherStations = new ConcurrentHashMap<>();
        otherPlayerState = new ConcurrentHashMap<>();
        previousPlayableCards = new ConcurrentHashMap<>();
        previousGoalCards = new ConcurrentHashMap<>();
        lockTable = new Object();
        lockPersonal = new Object();
        lockOther = new Object();

        messages = new ArrayList<>();
    }

    public void setPersonalStation(PersonalStation localStation) {
        synchronized (this.lockPersonal) {
            this.personalStation = localStation;
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
        synchronized (this.lockPersonal) {
            return this.personalStation;
        }
    }

    public void setOtherStations(String nickname, OtherStation otherStation) {
        System.out.println("Nickname: " + nickname);
        synchronized (this.lockOther) {
            this.otherStations.put(nickname, otherStation);
        }
        synchronized (this.lockOther) {
            this.otherPlayerState.put(nickname, State.ACTIVE);
        }
    }

    public void setPlayerInactive(String nickname){
        synchronized (this.lockOther) {
            this.otherPlayerState.put(nickname, State.INACTIVE);
        }
    }

    public void setPlayerActive(String nickname){
        synchronized (this.lockOther) {
            this.otherPlayerState.put(nickname, State.ACTIVE);
        }
    }

    public void setPrivateGoal(GoalCard goalCard){
        synchronized (this.lockPersonal){
            this.personalStation.setPrivateGoalCard(goalCard);
        }
    }



    public void setPrivateGoalCard(int cardIdx){
        synchronized (this.lockPersonal){
            this.personalStation.setPrivateGoalCard(cardIdx);
        }
    }


    public void setColor(Color color){
        synchronized (this.lockPersonal) {
            this.personalStation.setColor(color);
        }
    }

    public void placeCardOtherStation(String nickname, String anchorCode, PlayableCard cardToPlace, Direction direction, CardOrientation orientation) {
        synchronized (this.lockOther){
            Tuple<Integer, Integer> coord = this.otherStations.get(nickname).getCoord(anchorCode);
            System.out.println("Coord placeOtherStation: " + coord);
            this.otherStations.get(nickname).placeCard(cardToPlace, new Tuple<>(
                    coord.x() + direction.getX(), coord.y() + direction.getY()));
        }
        synchronized (this.lockOther) {
            this.otherPlayerState.put(nickname, State.ACTIVE);
        }
    }

    public void placeInitialCardOtherStation(String nickname, PlayableCard initialCard){
        synchronized (this.lockOther) {
            System.out.println(nickname + otherStations.keySet());
            if (!this.otherStations.containsKey(nickname))
                System.out.println("Nome non contenuto nella mappa..." + nickname);
            this.otherStations.get(nickname).placeInitialCard(initialCard);
        }
        synchronized (this.lockOther) {
            this.otherPlayerState.put(nickname, State.ACTIVE);
        }
    }

    public boolean isCardPlaceableOtherStation(String nickname, PlayableCard anchor, PlayableCard cardToPlace, Direction direction){
        synchronized (this.lockOther) {
            return this.otherStations.get(nickname).cardIsPlaceable(anchor, cardToPlace, direction);
        }
    }

    public void placeCardPersonalStation(String anchorCode, PlayableCard cardToPlace, Direction direction, CardOrientation orientation){
        synchronized (this.lockPersonal) {
            Tuple<Integer, Integer> coord = this.personalStation.getCoord(anchorCode);
            System.out.println("Coord placePersonalStation: " + coord);
            this.personalStation.placeCard(cardToPlace, new Tuple<>(
                    coord.x() + direction.getX(), coord.y() + direction.getY()));
        }
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void placeInitialCardPersonalStation(PlayableCard initialCard){
        synchronized (this.lockPersonal) {
            this.personalStation.placeInitialCard(initialCard);
        }
    }

    public boolean isCardPlaceablePersonalStation(PlayableCard cardToPlace, PlayableCard anchor, Direction direction){
        synchronized (this.lockPersonal) {
            return this.personalStation.cardIsPlaceable(anchor, cardToPlace, direction);
        }
    }

    public void updateCardsInHand(PlayableCard playableCard){
        synchronized (this.lockPersonal) {
            this.personalStation.updateCardsInHand(playableCard);
        }
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
    }

    public ConcurrentHashMap<String, OtherStation> getOtherStations() {
        synchronized (this.lockOther) {
            return new ConcurrentHashMap<>(this.otherStations);
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
    }

    public LocalTable getTable() {
        synchronized (this.lockTable) {
            return table;
        }
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

    public void setAvailableGames(List<String> availableGames) {
        this.availableGames = availableGames;
    }

    public List<String> getAvailableGames() {
        return this.availableGames;
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
            return messages;
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

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameName() {
        return gameName;
    }
}
