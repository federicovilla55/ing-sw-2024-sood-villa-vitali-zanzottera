package it.polimi.ingsw.gc19.View.GameLocalView;

import it.polimi.ingsw.gc19.Costants.ImportantConstants;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Game.Player;
import it.polimi.ingsw.gc19.Utils.Tuple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class LocalStationPlayer {
    protected final String ownerPlayer;
    protected int numPoints;
    protected final PlayableCard[][] cardSchema;
    protected Map<Symbol, Integer> visibleSymbols;

    protected final List<Tuple<PlayableCard, Tuple<Integer,Integer>>> placedCardSequence;

    protected Color chosenColor;

    public LocalStationPlayer(String nicknameOwner){
        this.ownerPlayer = nicknameOwner;
        this.cardSchema = new PlayableCard[ImportantConstants.gridDimension][ImportantConstants.gridDimension];
        this.chosenColor = null;
        this.visibleSymbols = new HashMap<>();
        this.numPoints = 0;
        this.placedCardSequence = null;

        reconstructSchema();
    }

    public LocalStationPlayer(String nicknameOwner, Color chosenColor, Map<Symbol, Integer> visibleSymbols,
                              int numPoints, List<Tuple<PlayableCard,Tuple<Integer,Integer>>> placedCardSequence){
        this.cardSchema = new PlayableCard[ImportantConstants.gridDimension][ImportantConstants.gridDimension];

        this.ownerPlayer = nicknameOwner;
        this.chosenColor = chosenColor;
        this.visibleSymbols = visibleSymbols;
        this.numPoints = numPoints;
        this.placedCardSequence = placedCardSequence;

        reconstructSchema();
    }

    public void reconstructSchema() {
        // Reset card schema
        for (int i = 0; i < ImportantConstants.gridDimension; i++) {
            for (int k = 0; k < ImportantConstants.gridDimension; k++) {
                cardSchema[i][k] = null;
            }
        }

        // Reconstruct card schema and overlap from placedCardSequence
        for (Tuple<PlayableCard, Tuple<Integer,Integer>> placement : placedCardSequence) {
            PlayableCard card = placement.x();
            Tuple<Integer, Integer> position = placement.y();
            int rowIndex = position.x();
            int colIndex = position.y();

            // Update card schema with the placed card
            cardSchema[rowIndex][colIndex] = card;
        }
    }

    public String getOwnerPlayer() {
        return ownerPlayer;
    }

    public int getNumPoints() {
        return numPoints;
    }

    public Map<Symbol, Integer> getVisibleSymbols() {
        return visibleSymbols;
    }

    public Color getChosenColor() {
        return chosenColor;
    }

    public void setNumPoints(int numPoints) {
        this.numPoints = numPoints;
    }

    public void setVisibleSymbols(Map<Symbol, Integer> visibleSymbols){
         this.visibleSymbols = new HashMap<>(visibleSymbols);
    }

    public void placeCard(PlayableCard placedCard, Tuple<Integer, Integer> position){
        // up or down is decided by the placedCard.cardState attribute.
        placedCardSequence.add(new Tuple<>(placedCard, position));

        cardSchema[position.x()][position.y()] = placedCard;
    }

    public void placeInitialCard(PlayableCard initialCard){
        this.placedCardSequence.add(new Tuple<>(initialCard, new Tuple<>((ImportantConstants.gridDimension / 2), (ImportantConstants.gridDimension / 2))));
        this.cardSchema[ImportantConstants.gridDimension / 2][ImportantConstants.gridDimension / 2] = initialCard;
        System.out.println(new Tuple<>(ImportantConstants.gridDimension / 2, ImportantConstants.gridDimension / 2));
    }

    public Tuple<Integer, Integer> getCoord(String cardCode){
        for(Tuple<PlayableCard, Tuple<Integer,Integer>> t : placedCardSequence){
            if(t.x().getCardCode().equals(cardCode)){
                return t.y();
            }
        }
        return new Tuple<>(-1, -1);
    }

    public boolean cardIsPlaceable(PlayableCard anchor, PlayableCard cardToPlace, Direction direction){
        return true;
    }
}
