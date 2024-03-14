package it.polimi.ingsw.gc19.Model.Deck;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.gc19.Model.Card.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Deck<cardType extends Card>{

    private ArrayList<cardType> cardsInDeck;
    private HashMap<String, cardType> initialCardInDeck;
    private  int initialLenOfDeck;

    public Deck(String filename) {
        try {cardsInDeck = init(filename);}
        catch(IOException ignored) {}
        initialCardInDeck = new HashMap<>();
        for(cardType card : cardsInDeck)
            initialCardInDeck.put(card.getCardCode(), card);
        initialLenOfDeck = cardsInDeck.size();
    }

    private ArrayList<cardType> init(String filename) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        File f = new File(filename);

        ArrayList<cardType> a;

        a = objectMapper.readValue(f, new TypeReference<ArrayList<cardType>>(){});

        return a;
    }

    ArrayList<cardType> getArray() {
        return cardsInDeck;
    }

    public int getInitialLenOfDeck(){
        return this.initialLenOfDeck;
    }

    public cardType pickACard() throws EmptyDeckException{
        if(this.isEmpty()){
            throw new EmptyDeckException("You can't pick a card. Deck is empty!");
        }
        return this.cardsInDeck.remove(new Random().nextInt(cardsInDeck.size()));
    }

    public String getInfoCard(String name) throws CardNotFoundException{
        return Optional.of(initialCardInDeck.get(name).getCardDescription())
                .orElseThrow(() -> new CardNotFoundException("Requested card not found."));
    }

    public ArrayList<String> getInfoAllCards(){
        return initialCardInDeck.values()
                .stream().map(Card::getCardDescription)
                .collect(Collectors
                        .toCollection(ArrayList::new));
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
