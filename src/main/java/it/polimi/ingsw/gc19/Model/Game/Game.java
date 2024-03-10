package it.polimi.ingsw.gc19.Model.Game;

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

import java.lang.reflect.MalformedParametersException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Game {
    private ArrayList<Player> players;
    private int numPlayers;
    private Player activePlayer;
    private Player firstPlayer;
    private ArrayList<Color> availableColors;
    private Deck<GoalCard> goalDeck;
    private Deck<PlayableCard> initialDeck;
    private Deck<PlayableCard> resourceDeck;
    private Deck<PlayableCard> goldDeck;
    private PlayableCard[] goldcardsOnTable;
    private PlayableCard[] resourceCardsOnTable;
    private GoalCard[] pulicGoalCardsOnTable;

    private String name;

    public Game(int numPlayers){
        availableColors = new ArrayList<>(Arrays.asList(Color.BLUE, Color.GREEN, Color.YELLOW, Color.RED));
        players = new ArrayList<Player>();
        firstPlayer = null;
        this.numPlayers = numPlayers;
        // @todo: insert json filenames
        goalDeck = new Deck<GoalCard>("goal_cards_file_path");
        initialDeck = new Deck<PlayableCard>("initial_cards_file_path");
        resourceDeck = new Deck<PlayableCard>("resource_cards_file_path");
        goldDeck = new Deck<PlayableCard>("gold_cards_file_path");

        fillCardsOnTable();
    }
    public void startGame(){
        // @todo: keep track of turns and game states.
    }

    public void createNewPlayer(String name) throws NameAlreadyInUseException {
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
    public Player getActivePlayer(){
        return this.activePlayer;
    }
    public String getInfoCard(String name) throws CardNotFoundException{
        // Using card name format: "type_number_orientation.jpg"
        // ex. goal_01_back.jpg

        String cardType = name.split("_", 2)[0];
        switch (cardType){
            case "goal" -> {
                return goalDeck.getInfoCard(name);
            }
            case "initial" -> {
                return initialDeck.getInfoCard(name);
            }
            case "resources" -> {
                return resourceDeck.getInfoCard(name);
            }
            case "gold" -> {
                return goldDeck.getInfoCard(name);
            }
            default -> {
                throw new CardNotFoundException("Requested card not found.");
            }
        }
    }
    public ArrayList<String> getInfoAllCards(){
        ArrayList<String> infos = new ArrayList<>(goalDeck.getInfoAllCards());
        infos.addAll(initialDeck.getInfoAllCards());
        infos.addAll(resourceDeck.getInfoAllCards());
        infos.addAll(goldDeck.getInfoAllCards());
        return infos;
    }
    public void setActivePlayer(Player p){
        this.activePlayer = p;
    }

    public Player getFirstPlayer(){
        return this.firstPlayer;
    }

    public void setFirstPlayer(){
        if(firstPlayer == null && players.size() == numPlayers){
            // Assign the first player to the one at the random index
            Random random = new Random();
            this.firstPlayer = players.get(random.nextInt(players.size()));
        }
    }

    private void fillCardsOnTable() {
        try{
            goldcardsOnTable = new PlayableCard[]{goldDeck.pickACard(), goldDeck.pickACard()};
            resourceCardsOnTable = new PlayableCard[]{resourceDeck.pickACard(), resourceDeck.pickACard()};
            pulicGoalCardsOnTable = new GoalCard[]{goalDeck.pickACard(), goalDeck.pickACard()};
        }catch (EmptyDeckException e){
            // @todo: how to handle exceptions?
            e.printStackTrace();
        }
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
                yield resourceDeck;
            }
            case GOLD -> {
                yield goldDeck;
            }
            default -> throw new MalformedParametersException("type must be RESOURCE or GOLD");
        };
        return deck.pickACard();
    }
    /**
     * This method returns a card of the given type at the given position. The card is removed from the position, and if no card
     * is present the exception NoCardException is thrown. Then a card from the same deck type is drawn (if the deck
     * is not empty) and placed at the given position.
     * @param type The type of card to pick, either RESOUCE or GOLD
     * @param position The position 0 -> left 1 -> right
     * @return The card in the specified position if present
     * @throws CardNotFoundException when there is no card, this exception is thrown
     */
    public PlayableCard pickCardFromTable(PlayableCardType type, int position) throws CardNotFoundException, MalformedParametersException {
        Deck<PlayableCard> deck;
        PlayableCard[] cardsOnTable;
        switch (type) {
            case RESOURCE:
                deck = resourceDeck; cardsOnTable = resourceCardsOnTable;
                break;
            case GOLD:
                deck = goldDeck; cardsOnTable = goldcardsOnTable;
                break;
            default:
                throw new MalformedParametersException("type must be RESOURCE or GOLD");
        }
        PlayableCard result = cardsOnTable[position];
        if(result==null) {
            throw new CardNotFoundException();
        }
        try {
            cardsOnTable[position] = deck.pickACard();
        } catch (EmptyDeckException e) {
            cardsOnTable[position] = null;
        }
        return result;
    }

    public String getName() {
        return this.name;
    }

    public void updateGoalPoints(){
        // For each player updates his station points
        // for each goal card (public and private)
        for(Player p : players){
            Station station = p.getPlayerStation();
            station.updatePoints(pulicGoalCardsOnTable[0]);
            station.updatePoints(pulicGoalCardsOnTable[1]);
            station.updatePoints(station.getPrivateGoalCard());
        }
    }

}
