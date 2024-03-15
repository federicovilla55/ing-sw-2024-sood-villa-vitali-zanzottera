package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import it.polimi.ingsw.gc19.Costants.ImportantConstants;
import it.polimi.ingsw.gc19.Model.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Model.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Station.Station;
import it.polimi.ingsw.gc19.Model.Tuple.Tuple;

import java.util.List;

@JsonTypeName("pattern")
class PatternEffect implements GoalEffect{

    private final int cardValue;
    private final List<Tuple<Integer, Integer>> moves;
    private final List<Symbol> requiredSymbol;
    @JsonCreator
    protected PatternEffect(@JsonProperty("value") int cardValue,
                            @JsonProperty("moves") List<Tuple<Integer, Integer>> moves,
                            @JsonProperty("required") List<Symbol> requiredSymbol){
        this.cardValue = cardValue;
        this.moves = moves;
        this.requiredSymbol = requiredSymbol;
    }

    @Override
    public int countPoints(Station station){
        boolean[][] usedCards = new boolean[ImportantConstants.gridDimension][ImportantConstants.gridDimension];
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

        for(int i = 0; i < ImportantConstants.gridDimension; i++){
            for(int k = 0; k < ImportantConstants.gridDimension; k++){
                found = true;
                nextI = i;
                nextK = k;
                numOfCard = 0;
                while(found){
                    try{
                        currentCard = station.getCardSchema().getCard(nextI, nextK).get(); //rivedere
                        if((currentCard.getCardType() != PlayableCardType.INITIAL) && (currentCard.getPermanentResources().getFirst() == requiredSymbol.get(numOfCard)) && usedCards[nextI][nextK]){
                            nextI = nextI + moves.get(numOfCard).x();
                            nextK = nextK + moves.get(numOfCard).y();
                            usedCards[nextI][nextK] = false;
                            numOfCard++;
                        }
                    }
                    catch(Exception ignored){
                        while(numOfCard > 0){
                            usedCards[nextI][nextK] = true;
                            nextI = nextI - moves.get(numOfCard).x();
                            nextK = nextK - moves.get(numOfCard).y();
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
        return "Points per pattern: " + this.cardValue +
               "Pattern moves required starting from a card: " + this.moves.toString() +
               "Patter resource required: " + this.requiredSymbol.toString();
    }

}
