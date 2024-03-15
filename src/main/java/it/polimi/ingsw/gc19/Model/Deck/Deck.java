package it.polimi.ingsw.gc19.Model.Deck;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.gc19.Model.Card.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Deck<cardType extends Card>{

    private final ArrayList<cardType> cardsInDeck;
    private  int initialLenOfDeck;

    public Deck(Stream<cardType> cardsInDeck) {
        this.cardsInDeck = cardsInDeck.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private ArrayList<cardType> init(String filename) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        File f = new File(filename);

        ArrayList<cardType> a;

        a = objectMapper.readValue(f, new TypeReference<ArrayList<cardType>>(){});

        return a;
    }

    public void shuffleDeck(){
        Collections.shuffle(this.cardsInDeck);
    }

    ArrayList<cardType> getArray() {return cardsInDeck;}

    public int getInitialLenOfDeck(){
        return this.initialLenOfDeck;
    }

    public cardType pickACard() throws EmptyDeckException{
        if(this.isEmpty()){
            throw new EmptyDeckException("You can't pick a card. Deck is empty!");
        }
        return this.cardsInDeck.remove(new Random().nextInt(cardsInDeck.size()));
    }

    public void insertCard(cardType card){
        this.cardsInDeck.addFirst(card);
    }

    public boolean isEmpty(){
        return this.cardsInDeck.isEmpty();
    }

    public boolean cardIsInDeck(cardType cardToSearch){
        return this.cardsInDeck.contains(cardToSearch);
    }

    public int numberOfCardInDeck(){
        return this.cardsInDeck.size();
    }

}
