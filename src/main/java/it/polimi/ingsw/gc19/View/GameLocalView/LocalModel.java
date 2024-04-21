package it.polimi.ingsw.gc19.View.GameLocalView;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Game.Player;
import it.polimi.ingsw.gc19.Model.Station.InvalidAnchorException;
import it.polimi.ingsw.gc19.Model.Station.InvalidCardException;
import it.polimi.ingsw.gc19.Model.Station.Station;
import it.polimi.ingsw.gc19.Model.Tuple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocalModel {
    private Station personalStation;
    private HashMap<String, Station> otherStations;
    private HashMap<String, State> otherPlayerState;
    private List<String> availableGames;
    private String nickname;
    private LocalTable table;
    private String firstPlayer;
    private int numPlayers;

    public LocalModel(){
        personalStation = null;
        otherStations = null;
    }

    public void setPersonalStation(Station localStation) {
        this.personalStation = localStation;
    }

    public Station getPersonalStation() {
        return this.personalStation;
    }

    public void setOtherStations(String nickname, Station otherStation) {
        this.otherStations.put(nickname, otherStation);
        this.otherPlayerState.put(nickname, State.ACTIVE);
    }

    public void setPlayerInactive(String nickname){
        this.otherPlayerState.put(nickname, State.INACTIVE);
    }

    public void setPlayerActive(String nickname){
        this.otherPlayerState.put(nickname, State.ACTIVE);
    }

    public void setPrivateGoal(GoalCard goalCard){
        this.personalStation.setPrivateGoalCard(goalCard);
    }

    public void setColor(Color color){
        this.personalStation.getOwnerPlayer().setColor(color);
    }

    public void placeCardOtherStation(String nickname, PlayableCard anchor, PlayableCard cardToPlace, Direction direction, CardOrientation orientation){
        try {
            this.otherStations.get(nickname).updateCardsInHand(cardToPlace);
            this.otherStations.get(nickname).placeCard(anchor, cardToPlace, direction, orientation);
            this.otherPlayerState.put(nickname, State.ACTIVE);
        } catch (InvalidCardException | InvalidAnchorException ignored) {
        }
    }

    public void placeInitialCardOtherStation(String nickname, CardOrientation orientation){
        this.otherStations.get(nickname).placeInitialCard(orientation);
        this.otherPlayerState.put(nickname, State.ACTIVE);
    }

    public boolean isCardPlaceableOtherStation(String nickname, PlayableCard anchor, PlayableCard cardToPlace, Direction direction){
        try {
            return this.otherStations.get(nickname).cardIsPlaceable(anchor, cardToPlace, direction);
        } catch (InvalidCardException | InvalidAnchorException ignored) {
        }
        return false;
    }

    public void placeCardPersonalStation(PlayableCard anchor, PlayableCard cardToPlace, Direction direction, CardOrientation orientation){
        try {
            PlayableCard anchorCard = null; // @todo: how to get the anchor card from its code.
            this.personalStation.placeCard(anchor, cardToPlace, direction, orientation);
        } catch (InvalidCardException | InvalidAnchorException ignored) {
        }
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void placeInitialCardPersonalStation(CardOrientation orientation){
        this.personalStation.placeInitialCard(orientation);
    }

    public boolean isCardPlaceablePersonalStation(PlayableCard anchor, PlayableCard cardToPlace, Direction direction){
        try {
            return this.personalStation.cardIsPlaceable(anchor, cardToPlace, direction);
        } catch (InvalidCardException | InvalidAnchorException ignored) {
        }
        return false;
    }

    public void updateCardsInHand(PlayableCard playableCard){
        this.personalStation.updateCardsInHand(playableCard);
    }

    public void updateCardsInTable(PlayableCard playableCard, PlayableCardType playableCardType, int position){
        if(playableCard.getCardType() == PlayableCardType.GOLD){
            if(position == 0) {
                this.table.setGold1(playableCard);
            } else {
                this.table.setGold2(playableCard);
            }
        }
        if(playableCard.getCardType() == PlayableCardType.RESOURCE){
            if(position == 0) {
                this.table.setResource1(playableCard);
            } else {
                this.table.setResource2(playableCard);
            }
        }

    }

    public HashMap<String, Station> getOtherStations() {
        return this.otherStations;
    }

    public void setTable(LocalTable table) {
        this.table = table;
    }

    public LocalTable getTable() {
        return table;
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

    public void setAvailableGames(List<String> availableGames) {
        this.availableGames = availableGames;
    }

    public List<String> getAvailableGames() {
        return this.availableGames;
    }
}
