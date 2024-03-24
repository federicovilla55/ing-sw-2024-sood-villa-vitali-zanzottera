package it.polimi.ingsw.gc19.Model.Game;

import it.polimi.ingsw.gc19.Controller.JSONParser;
import it.polimi.ingsw.gc19.Enums.GameState;
import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.CardNotFoundException;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Model.Deck.Deck;
import it.polimi.ingsw.gc19.Model.Deck.EmptyDeckException;
import it.polimi.ingsw.gc19.Model.Station.Station;

import java.io.IOException;
import java.lang.reflect.MalformedParametersException;
import java.util.*;
import java.util.stream.Stream;

/**
 * This class represents a game session.
 * Players, Decks and the Table can be accessed and managed through the class.
 */
public class Game {
    private TurnState turnState;
    private GameState gameState;
    /**
     * This Attribute contains a list of the players associated with the game.
     */
    private final ArrayList<Player> players;

    /**
     * This attribute contains the number of players, as specified by the player
     * who created the game.
     */
    private final int numPlayers;

    /**
     * This attribute contains the active player; the player who is currently playing.
     */
    private Player activePlayer;

    /**
     * This attribute contains the first player; the player who will start playing after
     * the game starts.
     */
    private Player firstPlayer;

    /**
     * This attribute associate every playable card code to the corresponding PlayableCard object.
     */
    private final HashMap<String, PlayableCard> stringPlayableCardHashMap;

    /**
     * This attribute associate every goal card code to the corresponding GoalCard object.
     */
    private final HashMap<String, GoalCard> stringGoalCardHashMap;

    /**
     * This attribute represents the deck composed of goal cards.
     */
    private final Deck<GoalCard> goalDeck;

    /**
     * This attribute represents the deck composed of initial cards.
     */
    private final Deck<PlayableCard> initialDeck;

    /**
     * This attribute represents the deck composed of resource cards.
     */
    private final Deck<PlayableCard> resourceDeck;

    /**
     * This attribute represents the deck composed of gold cards.
     */
    private final Deck<PlayableCard> goldDeck;

    /**
     * This attribute represents the two gold cards on the table.
     */
    private final PlayableCard[] goldCardsOnTable;

    /**
     * This attribute represents the two resource cards on the table.
     */
    private final PlayableCard[] resourceCardsOnTable;

    /**
     * This attribute represents the two goal cards on the table.
     */
    private final GoalCard[] publicGoalCardsOnTable;

    private boolean finalRound;

    /**
     * Constructs a new game session with the specified number of players.
     * Initializes decks, cards on table, players and other variables.
     *
     * @param numPlayers The number of players in the game.
     * @throws IOException if there's an I/O error while reading card files.
     */
    public Game(int numPlayers) throws IOException{
        /**
         * This attribute identifies the available colors from which players can
         * choose the color of their pawn.
         */
        ArrayList<Color> availableColors = new ArrayList<>(Arrays.asList(Color.BLUE, Color.GREEN, Color.YELLOW, Color.RED));
        this.players = new ArrayList<>();
        this.numPlayers = numPlayers;

        this.stringGoalCardHashMap = new HashMap<>();
        this.stringPlayableCardHashMap = new HashMap<>();
        JSONParser.readPlayableCardFromFile().forEach(c -> this.stringPlayableCardHashMap.put(c.getCardCode(), c));
        JSONParser.readGoalCardFromFile().forEach(c -> this.stringGoalCardHashMap.put(c.getCardCode(), c));

        this.goalDeck = new Deck<>(this.stringGoalCardHashMap.values().stream());
        this.initialDeck = new Deck<>(this.stringPlayableCardHashMap.values().stream().filter(c -> c.getCardType() == PlayableCardType.INITIAL));
        this.resourceDeck = new Deck<>(this.stringPlayableCardHashMap.values().stream().filter(c -> c.getCardType() == PlayableCardType.RESOURCE));
        this.goldDeck = new Deck<>(this.stringPlayableCardHashMap.values().stream().filter(c -> c.getCardType() == PlayableCardType.GOLD));

        this.goalDeck.shuffleDeck();
        this.initialDeck.shuffleDeck();
        this.goldDeck.shuffleDeck();
        this.resourceDeck.shuffleDeck();

        this.finalRound = false;

        this.goldCardsOnTable = new PlayableCard[]{goldDeck.pickACard(), goldDeck.pickACard()};
        this.resourceCardsOnTable = new PlayableCard[]{resourceDeck.pickACard(), resourceDeck.pickACard()};
        this.publicGoalCardsOnTable = new GoalCard[]{goalDeck.pickACard(), goalDeck.pickACard()};

        this.gameState = GameState.SETUP;
        this.turnState = null;
    }

    /**
     * The method retrieves a playable card from the deck based on its code.
     * Each card code is composed of two parts: the type of
     * the card and the card number.
     *
     * @param code The code of the card to retrieve.
     * @return Optional containing the playable card, if found.
     */
    public Optional<PlayableCard> getPlayableCardFromCode(String code){
        return Optional.of(this.stringPlayableCardHashMap.get(code));
    }

    /**
     * The method retrieves a goal card from the
     * goal card hashmap given its code.
     *
     * @param code The code of the card to retrieve.
     * @return Optional containing the goal card, if found.
     */
    public Optional<GoalCard> getGoalCardFromCode(String code){
        return Optional.of(this.stringGoalCardHashMap.get(code));
    }

    /**
     * The method retrieves the description of a card based
     * on its unique code. The description consist of a String.
     *
     * @param code The code of the card to retrieve.
     * @return The description of the card, if the card is found,
     * otherwise an error message.
     */
    public String getInfoCard(String code){
        return Stream.concat(this.stringGoalCardHashMap.values().stream(), this.stringPlayableCardHashMap.values().stream())
                     .filter(c -> c.getCardCode().equals(code))
                     .map(Card::getCardDescription)
                     .findAny()
                     .orElse("Card code is not valid!");
    }

    /**
     * The method retrieves the descriptions of all cards
     * from the four decks. For each deck and for each card
     * the getCardDescription is called.
     *
     * @return List of Strings with the card descriptions.
     */
    public ArrayList<String> getInfoAllCards(){
        return Stream.concat(this.stringGoalCardHashMap.values().stream(), this.stringPlayableCardHashMap.values().stream())
                     .map(Card::getCardDescription)
                     .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * The method starts the game session.
     * It sets the first player and the current active player.
     */
    public void startGame(){
        this.setFirstPlayer();
        this.activePlayer = this.getFirstPlayer();
        this.gameState = GameState.PLAYING;
        this.turnState = TurnState.PLACE;
    }

    /**
     * @return The first player.
     */
    public Player getFirstPlayer(){
        return this.firstPlayer;
    }

    /**
     * The method randomly sets the first player.
     */
    private void setFirstPlayer(){
        if(players.size() == numPlayers){
            // Assign the first player to the one at the random index
            Random random = new Random();
            this.firstPlayer = players.get(random.nextInt(players.size()));
        }
    }

    /**
     * @return The current active player.
     */
    public Player getActivePlayer(){
        return this.activePlayer;
    }

    /**
     * @return The next player in sequence.
     * The turns are made based on the player indices in the ArrayList players.
     */
    public Player getNextPlayer(){
        return this.players.get((this.players.indexOf(this.activePlayer) + 1) % this.numPlayers);
    }

    /**
     * Creates a new player with the given name.
     *
     * @param name   The name of the new player.
     * @throws NameAlreadyInUseException if the name is already in use.
     */
    public void createNewPlayer(String name) throws NameAlreadyInUseException {
        //  case in which two players have chosen the same name.
        for(Player p : players){
            if(p.getName().equals(name)){
                throw new NameAlreadyInUseException("Nickname already used.");
            }
        }

        Player player = null;
        player = new Player(name, this.initialDeck.pickACard(), this.goalDeck.pickACard(), this.goalDeck.pickACard());

        players.add(player);
    }

    /**
     * The method removes a specified player from the game.
     *
     * @param p The player to be removed.
     */
    public void removePlayer(Player p){
        players.remove(p);
    }

    /**
     * The method retrieves a player given its name.
     *
     * @param name The unique name of the player.
     * @return The player class associated with the specified name.
     * @throws PlayerNotFoundException if no player with the given name is found.
     */
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
            case RESOURCE -> this.resourceDeck;
            case GOLD -> this.goldDeck;
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
        for(Player p : players){
            Station station = p.getPlayerStation();
            station.updatePoints(this.publicGoalCardsOnTable[0]);
            station.updatePoints(this.publicGoalCardsOnTable[1]);
            station.updatePoints(station.getPrivateGoalCard());
        }
    }

    /**
     * @return the number of players to play this game, as specified
     * by the player when the game was created.
     */
    public int getNumPlayers(){
        return numPlayers;
    }

    /**
     *
     * @return the number of players who have joined the game yet.
     */
    public int getNumJoinedPlayer()
    {
        return players.size();
    }

    public boolean allPlayersChooseInitialGoalColor() {
        boolean flag = true;
        if(getNumJoinedPlayer()<getNumPlayers()) return false;
        for(Player player : this.players) {
            if (
                    player.getColor() == null ||
                    player.getPlayerStation().getPrivateGoalCard() == null ||
                    !player.getPlayerStation().getInitialCardIsPlaced()
            ) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    public boolean drawableCardsArePresent() {
        return !this.goldDeck.isEmpty() ||
                !this.resourceDeck.isEmpty() ||
                this.goldCardsOnTable[0] != null ||
                this.goldCardsOnTable[1] != null ||
                this.resourceCardsOnTable[0] != null ||
                this.resourceCardsOnTable[1] != null;
    }

    public void setActivePlayer(Player activePlayer) {
        this.activePlayer = activePlayer;
    }

    public TurnState getTurnState() {
        return turnState;
    }

    public void setTurnState(TurnState turnState) {
        this.turnState = turnState;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public boolean getFinalRound() {
        return finalRound;
    }

    public void setFinalRound(boolean finalRound) {
        this.finalRound = finalRound;
    }

}
