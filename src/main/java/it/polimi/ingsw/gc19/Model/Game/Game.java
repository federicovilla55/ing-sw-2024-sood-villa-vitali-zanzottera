package it.polimi.ingsw.gc19.Model.Game;

import it.polimi.ingsw.gc19.Controller.ClientPlayer;
import it.polimi.ingsw.gc19.Controller.JSONParser;
import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.CardNotFoundException;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Deck.Deck;
import it.polimi.ingsw.gc19.Model.Deck.EmptyDeckException;
import it.polimi.ingsw.gc19.Model.Enums.Color;
import it.polimi.ingsw.gc19.Model.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Model.Player.NameAlreadyInUseException;
import it.polimi.ingsw.gc19.Model.Player.Player;
import it.polimi.ingsw.gc19.Model.Player.PlayerNotFoundException;
import it.polimi.ingsw.gc19.Model.Station.Station;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.MalformedParametersException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Game {
    private final ArrayList<Player> players;
    private int numPlayers;
    private Player activePlayer;
    private Player firstPlayer;
    private ArrayList<Color> availableColors;
    private final HashMap<String, PlayableCard> stringPlayableCardHashMap;
    private final HashMap<String, GoalCard> stringGoalCardHashMap;
    private final Deck<GoalCard> goalDeck;
    private final Deck<PlayableCard> initialDeck;
    private final Deck<PlayableCard> resourceDeck;
    private final Deck<PlayableCard> goldDeck;
    private PlayableCard[] goldCardsOnTable;
    private PlayableCard[] resourceCardsOnTable;
    private GoalCard[] publicGoalCardsOnTable;

    public Game(int numPlayers) throws IOException{
        this.availableColors = new ArrayList<>(Arrays.asList(Color.BLUE, Color.GREEN, Color.YELLOW, Color.RED));
        this.players = new ArrayList<>();
        this.numPlayers = numPlayers;

        this.stringGoalCardHashMap = new HashMap<>();
        this.stringPlayableCardHashMap = new HashMap<>();
        JSONParser.readPlayableCardFromFile().forEach(c -> this.stringPlayableCardHashMap.put(c.getCardCode(), c));
        JSONParser.readGoalCardFromFile().forEach(c -> this.stringGoalCardHashMap.put(c.getCardCode(), c));

        this.goalDeck = new Deck<>(this.stringGoalCardHashMap.values().stream());
        //this.shuffleDeck();
        this.initialDeck = new Deck<>(this.stringPlayableCardHashMap.values().stream().filter(c -> c.getCardType() == PlayableCardType.INITIAL));
        this.resourceDeck = new Deck<>(this.stringPlayableCardHashMap.values().stream().filter(c -> c.getCardType() == PlayableCardType.RESOURCE));
        this.goldDeck = new Deck<>(this.stringPlayableCardHashMap.values().stream().filter(c -> c.getCardType() == PlayableCardType.GOLD));

        try{
            this.goldCardsOnTable = new PlayableCard[]{goldDeck.pickACard(), goldDeck.pickACard()};
            this.resourceCardsOnTable = new PlayableCard[]{resourceDeck.pickACard(), resourceDeck.pickACard()};
            this.publicGoalCardsOnTable = new GoalCard[]{goalDeck.pickACard(), goalDeck.pickACard()};
        }catch(EmptyDeckException ignored){}

    }

    public Optional<PlayableCard> getPlayableCardFromCode(String code){
        return Optional.of(this.stringPlayableCardHashMap.get(code));
    }

    public Optional<Card> getGoalCardFromCode(String code){
        return Optional.of(this.stringGoalCardHashMap.get(code));
    }

    public String getInfoCard(String code){
        return Stream.concat(this.stringGoalCardHashMap.values().stream(), this.stringPlayableCardHashMap.values().stream())
                     .filter(c -> c.getCardCode().equals(code))
                     .map(Card::getCardDescription)
                     .findAny()
                     .orElse("Card code is not valid!");
    }

    public ArrayList<String> getInfoAllCards(){
        return Stream.concat(this.stringGoalCardHashMap.values().stream(), this.stringPlayableCardHashMap.values().stream())
                     .map(Card::getCardDescription)
                     .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public void startGame(){
        this.setFirstPlayer();
        this.activePlayer = this.firstPlayer;
    }

    public Player getFirstPlayer(){
        return this.firstPlayer;
    }

    private void setFirstPlayer(){
        if(players.size() == numPlayers){
            // Assign the first player to the one at the random index
            Random random = new Random();
            this.firstPlayer = players.get(random.nextInt(players.size()));
        }
    }

    public Player getActivePlayer(){
        return this.activePlayer;
    }

    public Player getNextPlayer(){
        return this.players.get((this.players.indexOf(this.activePlayer) + 1) % this.numPlayers);
    }

    public void createNewPlayer(String name, ClientPlayer Client) throws NameAlreadyInUseException {
        //  case in which two players have chosen the same name.
        for(Player p : players){
            if(p.getName().equals(name)){
                throw new NameAlreadyInUseException("Nickname already used.");
            }
        }

        Player player = new Player(name);
        players.add(player);
    }
    public void removePlayer(Player p){
        players.remove(p);
    }
    public Player getPlayerByName(String name) throws PlayerNotFoundException{
        for(Player p : players){
            if(p.getName().equals(name)){
                return p;
            }
        }
        throw new PlayerNotFoundException("A player with the given name was not found.");
    }

    /**
     * This method returns a card from the deck of specified type. If the deck is empty, throws an exception
     * @param type The type of card to pick from the respective deck
     * @return The card from a specified deck
     * @throws EmptyDeckException when the deck is empty, return this exception
     */
    public PlayableCard pickCardFromDeck(PlayableCardType type) throws EmptyDeckException, MalformedParametersException {
        Deck<PlayableCard> deck;
        deck = switch (type) {
            case RESOURCE -> {
                yield this.resourceDeck;
            }
            case GOLD -> {
                yield this.goldDeck;
            }
            default -> throw new MalformedParametersException("type must be RESOURCE or GOLD");
        };
        return deck.pickACard();
    }
    /**
     * This method returns a card of the given type at the given position. The card is removed from the position, and if no card
     * is present the exception NoCardException is thrown. Then a card from the same deck type is drawn (if the deck
     * is not empty) and placed at the given position.
     * @param type The type of card to pick, either RESOURCE or GOLD
     * @param position The position 0 -> left 1 -> right
     * @return The card in the specified position if present
     * @throws CardNotFoundException when there is no card, this exception is thrown
     */
    public PlayableCard pickCardFromTable(PlayableCardType type, int position) throws CardNotFoundException, MalformedParametersException {
        Deck<PlayableCard> deck;
        PlayableCard[] cardsOnTable = switch (type) {
            case RESOURCE -> {
                deck = resourceDeck;
                yield this.resourceCardsOnTable;
            }
            case GOLD -> {
                deck = goldDeck;
                yield this.goldCardsOnTable;
            }
            default -> throw new MalformedParametersException("type must be RESOURCE or GOLD");
        };
        PlayableCard result = cardsOnTable[position];
        if(result==null) {
            throw new CardNotFoundException("Card not found");
        }
        try {
            cardsOnTable[position] = deck.pickACard();
        } catch (EmptyDeckException e) {
            cardsOnTable[position] = null;
        }
        return result;
    }

    public void updateGoalPoints(){
        // For each player updates his station points
        // for each goal card (public and private)
        for(Player p : players){
            Station station = p.getPlayerStation();
            station.updatePoints(this.publicGoalCardsOnTable[0]);
            station.updatePoints(this.publicGoalCardsOnTable[1]);
            station.updatePoints(station.getPrivateGoalCard());
        }
    }

    public int getNumPlayers(){
        return numPlayers;
    }
    public int getNumJoinedPlayer()
    {
        return players.size();
    }

    public String NotifyPlayer(Player playerToNotify)
    {
        return playerToNotify.getName();
    }

}
