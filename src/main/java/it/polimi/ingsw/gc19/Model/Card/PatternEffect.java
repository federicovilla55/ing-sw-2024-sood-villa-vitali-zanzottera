package it.polimi.ingsw.gc19.Model.Card;

import it.polimi.ingsw.gc19.Costants.ImportantConstants;
import it.polimi.ingsw.gc19.Model.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Model.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Station.Station;
import it.polimi.ingsw.gc19.Tuple.Tuple;

import java.util.List;

public class PatternEffect implements GoalEffect{

    private final int cardValue;
    private final List<Tuple<Integer, Integer>> moves;
    private final List<Symbol> requiredSymbol;

    protected PatternEffect(int cardValue, List<Tuple<Integer, Integer>> moves, List<Symbol> requiredSymbol){
        this.cardValue = cardValue;
        this.moves = moves;
        this.requiredSymbol = requiredSymbol;
    }

    @Override
    public int countPoints(Station station){
        boolean[][] usedCards = new boolean[40][40];
        PlayableCard currentCard;
        int numPattern;
        boolean found;
        int nextI, nextK, numOfCard;

        for(int i = 0; i < ImportantConstants.gridDimension; i++){
            for(int k =0; k < ImportantConstants.gridDimension; k++){
                usedCards[i][k] = true;
            }
        }

        numPattern = 0;

        for(int i = 0; i < ImportantConstants.gridDimension;i++){
            for(int k = 0; k < ImportantConstants.gridDimension; k++){
                found = true;
                nextI = i;
                nextK = k;
                numOfCard = 0;
                while(found){
                    try{
                        currentCard = station.getCardSchema().getCard(nextI, nextK);
                        if((currentCard.getCardType() != PlayableCardType.INITIAL) && (currentCard.getPermanentResources().getFirst() == requiredSymbol.get(numOfCard)) && usedCards[nextI][nextK]){
                            nextI = nextI + moves.get(numOfCard).getX();
                            nextK = nextK + moves.get(numOfCard).getY();
                            usedCards[nextI][nextK] = false;
                            numOfCard++;
                        }
                    }
                    catch(Exception ignored){
                        while(numOfCard > 0){
                            usedCards[nextI][nextK] = true;
                            nextI = nextI - moves.get(numOfCard).getX();
                            nextK = nextK - moves.get(numOfCard).getY();
                            numOfCard--;
                        }
                        found = false;
                    };
                }
                numPattern++;
            }
        }
        return numPattern * this.cardValue;
    }

    @Override
    public String getEffectDescription(){
        return "Type: goal card based on card pattern" +
               "Points per pattern: " + this.cardValue +
               "Pattern moves required starting from a card: " + this.moves.toString() +
               "Patter resource required: " + this.requiredSymbol.toString();
    }

}
