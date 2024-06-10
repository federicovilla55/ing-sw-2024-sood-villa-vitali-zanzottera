package it.polimi.ingsw.gc19.Model.Game;

import it.polimi.ingsw.gc19.Controller.JSONParser;
import it.polimi.ingsw.gc19.Model.MessageFactory;
import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.CardNotFoundException;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Chat.Chat;
import it.polimi.ingsw.gc19.Model.Deck.Deck;
import it.polimi.ingsw.gc19.Model.Deck.EmptyDeckException;
import it.polimi.ingsw.gc19.Model.Publisher;
import it.polimi.ingsw.gc19.Model.Station.Station;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.GameConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.AvailableColorsMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.NewPlayerConnectedToGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.StartPlayingGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OtherStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OwnStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.TableConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
import it.polimi.ingsw.gc19.Utils.Tuple;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents a game session.
 * Players, Decks and the Table can be accessed and managed through the class.
 */
public class Game extends Publisher{

    /**
     *  This attribute represents the name of this game
     */
    private final String gameName;

    /**
     *  This attribute represents the {@link TurnState}
     */
    private TurnState turnState;
    /**
     *  This attribute represents the {@link GameState}
     */
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

    /**
     * This attribute represents the condition of final round.
     */
    private boolean finalRound;

    /**
     * This attribute represents if the game has already reached a final condition.
     */
    private boolean finalCondition;

    /**
     * This attribute represents the rng used to shuffle cards and select first player.
     */
    private final Random rng;

    /**
     * This attribute represents the chat.
     */
    private final Chat chat;

    /**
     * Gets players.
     *
     * @return the players
     */
    public ArrayList<Player> getPlayers() {
        return this.players;
    }

    /**
     * Gets available colors.
     *
     * @return the available colors
     */
    public List<Color> getAvailableColors() {
        ArrayList<Color> availableColors = new ArrayList<>(List.of(Color.values()));
        for(Player p : this.players){
            availableColors.remove(p.getColor());
        }
        return availableColors;
    }


    /**
     * Compute final scoreboard list.
     *
     * @return the list containing players by descending order of points
     */
    public List<Player> computeFinalScoreboard() {

        Comparator<Player> byGoalPoints = Comparator.comparing((Player p) -> p.getStation().getPointsFromGoals()).reversed();
        Comparator<Player> byStationPoints = Comparator.comparing((Player p) -> p.getStation().getNumPoints()).reversed();

        List<Player> sortedPlayers;
        sortedPlayers = players.stream()
                .sorted(byGoalPoints.thenComparing(byStationPoints))
                .collect(Collectors.toList());

        return sortedPlayers;
    }

    /**
     * Constructs a new game session with the specified number of players.
     * Initializes decks, cards on table, players and other variables.
     *
     * @param numPlayers The number of players in the game.
     * @param gameName   the game name
     * @throws IOException if there's an I/O error while reading card files.
     */
    public Game(int numPlayers, String gameName) throws IOException {
        this(numPlayers, gameName, new Random().nextLong());
    }


    /**
     * This constructor is called directly for testing, removing randomness by fixing the seed of the rng
     *
     * @param numPlayers The number of players in the game.
     * @param gameName   the game name
     * @param randomSeed the random seed used to shuffle card and to select first player
     * @throws IOException if there's an I/O error while reading card files.
     */
    public Game(int numPlayers, String gameName, long randomSeed) throws IOException{

        super();
        this.gameName = gameName;
        this.rng = new Random(randomSeed);
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

        this.goalDeck.shuffleDeck(this.rng);
        this.initialDeck.shuffleDeck(this.rng);
        this.goldDeck.shuffleDeck(this.rng);
        this.resourceDeck.shuffleDeck(this.rng);

        this.chat = new Chat();

        this.finalCondition = false;
        this.finalRound = false;

        this.goldCardsOnTable = new PlayableCard[]{goldDeck.pickACard(), goldDeck.pickACard()};
        this.resourceCardsOnTable = new PlayableCard[]{resourceDeck.pickACard(), resourceDeck.pickACard()};
        this.publicGoalCardsOnTable = new GoalCard[]{goalDeck.pickACard(), goalDeck.pickACard()};

        this.gameState = GameState.SETUP;
        this.turnState = null;
    }

    @Override
    public void setMessageFactory(MessageFactory messageFactory) {
        super.setMessageFactory(messageFactory);
        this.chat.setMessageFactory(messageFactory);
    }

    /**
     * Check if this game has the player of specified nickname.
     *
     * @param nick the nick
     * @return the boolean
     */
    public boolean hasPlayer(String nick){
        return this.getPlayers().stream()
                   .anyMatch(p -> p.getName().equals(nick));
    }

    /**
     * Get chat.
     *
     * @return the chat
     */
    public Chat getChat(){
        return this.chat;
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
     * @return The description of the card, if the card is found, otherwise an error message.
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
        if(this.activePlayer!=null) {
            this.gameState = GameState.PLAYING;
            this.turnState = TurnState.PLACE;
            this.getMessageFactory().sendMessageToAllGamePlayers(new StartPlayingGameMessage(this.activePlayer.getName()));
            this.getMessageFactory().sendMessageToAllGamePlayers(new TurnStateMessage(this.activePlayer.getName(), this.turnState));
        }
    }

    /**
     * Get first player.
     *
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
            this.firstPlayer = players.get(this.rng.nextInt(players.size()));
        }
    }

    /**
     * Get active player that is currently playing.
     *
     * @return The current active player.
     */
    public Player getActivePlayer(){
        return this.activePlayer;
    }

    /**
     * Get next player.
     *
     * @return The next player in sequence. The turns are made based on the player indices in the ArrayList players.
     */
    public Player getNextPlayer(){
        return this.players.get((this.players.indexOf(this.activePlayer) + 1) % this.numPlayers);
    }

    /**
     * Creates a new player with the given name.
     *
     * @param name The name of the new player.
     * @throws NameAlreadyInUseException if the name is already in use.
     */
    public void createNewPlayer(String name) throws NameAlreadyInUseException {
        //  case in which two players have chosen the same name.
        for(Player p : players){
            if(p.getName().equals(name)){
                throw new NameAlreadyInUseException("Nickname already used.");
            }
        }

        Player player = new Player(name, this.initialDeck.pickACard(), this.goalDeck.pickACard(), this.goalDeck.pickACard());
        player.setMessageFactory(this.getMessageFactory());

        player.getStation().updateCardsInHand(pickCardFromDeck(PlayableCardType.RESOURCE));
        player.getStation().updateCardsInHand(pickCardFromDeck(PlayableCardType.RESOURCE));
        player.getStation().updateCardsInHand(pickCardFromDeck(PlayableCardType.GOLD));

        players.add(player);

        //Notify to all player different from the current that another player has joined the game
        this.getMessageFactory().sendMessageToAllGamePlayersExcept(new NewPlayerConnectedToGameMessage(player.getName()), player.getName());
        //Give to all other players this player station and table config because cards from deck were picked
        this.getMessageFactory().sendMessageToAllGamePlayersExcept(new OtherStationConfigurationMessage(
                player.getName(),
                player.getColor(),
                player.getStation().getCardsInHand().stream().map(c -> new Tuple<>(c.getSeed(), c.getCardType())).toList(),
                player.getStation().getVisibleSymbolsInStation(),
                player.getStation().getNumPoints(),
                player.getStation().getPlacedCardSequence()
        ), player.getName());
        if (GameState.SETUP.equals(this.gameState))
            this.getMessageFactory().sendMessageToAllGamePlayersExcept(new TableConfigurationMessage(
                    this.resourceCardsOnTable[0], this.resourceCardsOnTable[1],
                    this.goldCardsOnTable[0], this.goldCardsOnTable[1],
                    this.publicGoalCardsOnTable[0], this.publicGoalCardsOnTable[1],
                    this.resourceDeck.getNextCard().map(PlayableCard::getSeed).orElse(null),
                    this.goldDeck.getNextCard().map(PlayableCard::getSeed).orElse(null)), player.getName());

        sendCurrentStateToPlayer(player.getName());
    }

    /**
     * Send current game state to player.
     *
     * @param nickname the nickname of the player
     */
    public void sendCurrentStateToPlayer(String nickname) {
        //Send to player its own station
        sendCurrentOwnStationState(nickname);

        //Send to player available colors if gameState is "SETUP"
        if(GameState.SETUP.equals(this.gameState))
            sendCurrentAvailableColors(nickname);

        //Send to the player the current state of table
        sendCurrentTableState(nickname);

        //Send to player others station
        sendCurrentOthersStationState(nickname);

        //Send to player game and turn state
        this.getMessageFactory().sendMessageToPlayer(nickname, new GameConfigurationMessage(
                this.gameState,
                this.turnState,
                this.getFirstPlayer() != null ? this.getFirstPlayer().getName() : null,
                this.getActivePlayer() != null ? this.getActivePlayer().getName() : null,
                this.finalRound,
                this.numPlayers
        ));
    }

    /**
     * Send current available colors to player.
     *
     * @param nickname the nickname of the player
     */
    public void sendCurrentAvailableColors(String nickname) {
        this.getMessageFactory().sendMessageToPlayer(nickname, new AvailableColorsMessage(this.getAvailableColors()));
    }

    /**
     * Send current own station state to player.
     *
     * @param nickname the nickname of
     */
    public void sendCurrentOwnStationState(String nickname) {
        this.getMessageFactory().sendMessageToPlayer(nickname, new OwnStationConfigurationMessage(nickname, getPlayerByName(nickname).getColor(),
                getPlayerByName(nickname).getStation().getCardsInHand(),
                getPlayerByName(nickname).getStation().getVisibleSymbolsInStation(),
                getPlayerByName(nickname).getStation().getPrivateGoalCard(),
                getPlayerByName(nickname).getStation().getNumPoints(),
                getPlayerByName(nickname).getStation().getInitialCard(),
                getPlayerByName(nickname).getStation().getPrivateGoalCardInStation(0),
                getPlayerByName(nickname).getStation().getPrivateGoalCardInStation(1),
                getPlayerByName(nickname).getStation().getPlacedCardSequence()));
    }

    /**
     * Send current others station states to a player.
     *
     * @param receiver the nickname of the player to which the stations are sent
     */
    public void sendCurrentOthersStationState(String receiver) {

        for(String nickname : players.stream().map(Player::getName).toList()) {
            if(!nickname.equals(receiver)) {
                this.getMessageFactory().sendMessageToPlayer(receiver, new OtherStationConfigurationMessage(
                        nickname,
                        getPlayerByName(nickname).getColor(),
                        getPlayerByName(nickname).getStation().getCardsInHand().stream().map(c -> new Tuple<>(c.getSeed(), c.getCardType())).toList(),
                        getPlayerByName(nickname).getStation().getVisibleSymbolsInStation(),
                        getPlayerByName(nickname).getStation().getNumPoints(),
                        getPlayerByName(nickname).getStation().getPlacedCardSequence()
                        ));
            }
        }
    }

    /**
     * Send current table state to player.
     *
     * @param nickname the nickname of the player
     */
    public void sendCurrentTableState(String nickname) {
        this.getMessageFactory().sendMessageToPlayer(nickname,
                                                     new TableConfigurationMessage(this.resourceCardsOnTable[0], this.resourceCardsOnTable[1],
                                                                                          this.goldCardsOnTable[0], this.goldCardsOnTable[1],
                                                                                          this.publicGoalCardsOnTable[0], this.publicGoalCardsOnTable[1],
                                                                                          this.resourceDeck.getNextCard().map(PlayableCard::getSeed).orElse(null), this.goldDeck.getNextCard().map(PlayableCard::getSeed).orElse(null)));
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
     * Get deck of specified type.
     *
     * @param type the type of the deck, only RESOURCE or GOLD allowed
     * @return the deck
     * @throws IllegalArgumentException if the type is not RESOURCE or GOLD
     */
    public Deck<PlayableCard> getDeckFromType(PlayableCardType type) throws IllegalArgumentException {
        Deck<PlayableCard> deck;
        deck = switch (type) {
            case RESOURCE -> this.resourceDeck;
            case GOLD -> this.goldDeck;
            default -> throw new IllegalArgumentException("type must be RESOURCE or GOLD");
        };
        return deck;
    }

    /**
     * This method returns a card from the deck of specified type. If the deck is empty, throws an exception
     *
     * @param type The type of card to pick from the respective deck
     * @return The card from a specified deck
     * @throws EmptyDeckException       when the deck is empty, return this exception
     * @throws IllegalArgumentException if the type is not RESOURCE or GOLD
     */
    public PlayableCard pickCardFromDeck(PlayableCardType type) throws EmptyDeckException, IllegalArgumentException {
        Deck<PlayableCard> deck;
        deck = switch (type) {
            case RESOURCE -> this.resourceDeck;
            case GOLD -> this.goldDeck;
            default -> throw new IllegalArgumentException("type must be RESOURCE or GOLD");
        };
        return deck.pickACard();
    }

    /**
     * This method returns a card of the given type at the given position. The card is removed from the position, and if no card
     * is present the exception NoCardException is thrown. Then a card from the same deck type is drawn (if the deck
     * is not empty) and placed at the given position.
     *
     * @param type     The type of card to pick, either RESOURCE or GOLD
     * @param position The position 0 -> left 1 -> right
     * @return The card in the specified position if present
     * @throws CardNotFoundException    when there is no card, this exception is thrown
     * @throws IllegalArgumentException if the type is not RESOURCE or GOLD
     */
    public PlayableCard pickCardFromTable(PlayableCardType type, int position) throws CardNotFoundException, IllegalArgumentException {
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
            default -> throw new IllegalArgumentException("type must be RESOURCE or GOLD");
        };

        PlayableCard result = null;
        try {
            result = cardsOnTable[position];
            if(result==null) {
                throw new CardNotFoundException("Card not found");
            }
            cardsOnTable[position] = deck.pickACard();
        } catch (EmptyDeckException e) {
            cardsOnTable[position] = null;
        }
        catch (ArrayIndexOutOfBoundsException e){
            throw new IllegalArgumentException("position must be 0 or 1");
        }

        return result;
    }

    /**
     * Update goal points. Public goals and private goal are used.
     */
    public void updateGoalPoints(){
        for(Player p : players){
            int pointsFromGoals = 0;
            Station station = p.getStation();
            pointsFromGoals += station.updatePoints(this.publicGoalCardsOnTable[0]);
            pointsFromGoals += station.updatePoints(this.publicGoalCardsOnTable[1]);
            pointsFromGoals += station.updatePoints(station.getPrivateGoalCard());
            p.getStation().setPointsFromGoals(pointsFromGoals);
        }
    }

    /**
     * Get number of players.
     *
     * @return the number of players to play this game, as specified by the player when the game was created.
     */
    public int getNumPlayers(){
        return numPlayers;
    }

    /**
     * Gets number of joined player.
     *
     * @return the number of players who have joined the game yet.
     */
    public int getNumJoinedPlayer()
    {
        return players.size();
    }

    /**
     * All players choose initial goal color boolean.
     *
     * @return the boolean
     */
    public boolean allPlayersChooseInitialGoalColor() {
        boolean flag = true;
        if(getNumJoinedPlayer()<getNumPlayers()) return false;
        for(Player player : this.players) {
            if (
                    player.getColor() == null ||
                    player.getStation().getPrivateGoalCard() == null ||
                    !player.getStation().getInitialCardIsPlaced()
            ) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    /**
     * Check if there are cards to draw.
     *
     * @return the boolean
     */
    public boolean drawableCardsArePresent() {
        return !this.goldDeck.isEmpty() ||
                !this.resourceDeck.isEmpty() ||
                this.goldCardsOnTable[0] != null ||
                this.goldCardsOnTable[1] != null ||
                this.resourceCardsOnTable[0] != null ||
                this.resourceCardsOnTable[1] != null;
    }

    /**
     * Sets active player.
     *
     * @param activePlayer the active player
     */
    public void setActivePlayer(Player activePlayer) {
        this.activePlayer = activePlayer;
    }

    /**
     * Gets current turn state.
     *
     * @return the turn state
     */
    public TurnState getTurnState() {
        return turnState;
    }

    /**
     * Sets current turn state.
     *
     * @param turnState the turn state
     */
    public void setTurnState(TurnState turnState) {
        this.turnState = turnState;
    }

    /**
     * Gets current game state.
     *
     * @return the game state
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Sets current game state.
     *
     * @param gameState the game state
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Gets final condition.
     *
     * @return the final condition
     */
    public boolean getFinalCondition() {
        return finalCondition;
    }

    /**
     * Sets final condition.
     *
     * @param finalCondition the final condition
     */
    public void setFinalCondition(boolean finalCondition) {
        this.finalCondition = finalCondition;
    }

    /**
     * Sets final round.
     *
     * @param finalRound the final round
     */
    public void setFinalRound(boolean finalRound) {
        this.finalRound = finalRound;
        this.getMessageFactory().sendMessageToAllGamePlayers(
                new GameConfigurationMessage(
                        this.gameState,
                        this.turnState,
                        this.firstPlayer.getName(),
                        this.activePlayer.getName(),
                        this.finalRound,
                        this.numPlayers
                )
        );
    }

    /**
     * Gets the finalRound boolean.
     *
     * @return the boolean
     */
    public boolean isFinalRound() {
        return finalRound;
    }

    /**
     * Get public goal cards on table.
     *
     * @return the goal cards
     */
    public GoalCard[] getPublicGoalCardsOnTable() {
        return Arrays.copyOf(this.publicGoalCardsOnTable,this.publicGoalCardsOnTable.length);
    }

    /**
     * Get resource cards on table.
     *
     * @return the playable cards
     */
    public PlayableCard[] getResourceCardsOnTable() {
        return Arrays.copyOf(this.resourceCardsOnTable,this.resourceCardsOnTable.length);
    }

    /**
     * Get gold cards on table.
     *
     * @return the playable cards
     */
    public PlayableCard[] getGoldCardsOnTable() {
        return Arrays.copyOf(this.goldCardsOnTable,this.goldCardsOnTable.length);
    }

    /**
     * Get playable cards on table.
     *
     * @param type the type
     * @return the playable cards
     */
    public PlayableCard[] getPlayableCardsOnTable(PlayableCardType type) {
        return
                switch (type) {
                    case RESOURCE -> {
                        yield this.resourceCardsOnTable;
                    }
                    case GOLD -> {
                        yield this.goldCardsOnTable;
                    }
                    default -> throw new IllegalArgumentException();
                };
    }

    /**
     * Gets game name.
     *
     * @return the game name
     */
    public String getGameName() {
        return gameName;
    }
}