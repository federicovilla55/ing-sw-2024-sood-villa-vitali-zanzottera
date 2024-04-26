package it.polimi.ingsw.gc19.View.GameLocalView;

import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Utils.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OtherStation extends LocalStationPlayer{
    // mano giocatore (solo retro carte)
    ArrayList<PlayableCardType> backCardHand;
    public OtherStation(String nicknameOwner, Color chosenColor, Map<Symbol, Integer> visibleSymbols, int numPoints, List<Tuple<PlayableCard, Tuple<Integer, Integer>>> placedCardSequence) {
        super(nicknameOwner, chosenColor, visibleSymbols, numPoints, placedCardSequence);
    }

    public ArrayList<PlayableCardType> getBackCardHand() {
        synchronized (this.backCardHand) {
            return backCardHand;
        }
    }

    public void addBackCard(PlayableCardType cardToAdd){
        synchronized (this.backCardHand){
            backCardHand.add(cardToAdd);
        }
    }

    public void setBackCardHand(ArrayList<PlayableCardType> backCardHand) {
        this.backCardHand = backCardHand;
    }
}
