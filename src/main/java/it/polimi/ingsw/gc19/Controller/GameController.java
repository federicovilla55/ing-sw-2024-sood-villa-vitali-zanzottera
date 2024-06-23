package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.CardNotFoundException;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Chat.Message;
import it.polimi.ingsw.gc19.Model.Deck.EmptyDeckException;
import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Model.Game.Player;
import it.polimi.ingsw.gc19.Model.Game.PlayerNotFoundException;
import it.polimi.ingsw.gc19.Model.MessageFactory;
import it.polimi.ingsw.gc19.Model.Station.InvalidAnchorException;
import it.polimi.ingsw.gc19.Model.Station.InvalidCardException;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.OtherAcceptedPickCardFromDeckMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.OwnAcceptedPickCardFromDeckMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedPickCardFromTable;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.EndGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.GamePausedMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.GameResumedMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.ErrorType;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.AvailableColorsMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.DisconnectedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.PlayerReconnectedToGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
import it.polimi.ingsw.gc19.Utils.Tuple;


import java.util.*;
import java.util.stream.Collectors;


/**
 * This class is the controller of a single game. It is a Controller relative to Model-View-Controller design pattern.
 * The idea of this class is to implement an Observer of the view, and to obtain from it events (user input), extract from
 * the view necessary information, and call methods on the model (gameAssociated)
 */
public class GameController{

    /**
     * A {@link Timer} used to stop game when only one player remains connected
     */
    private Timer stopGameTimer;

    /**
     * Connected {@link MessageFactory}
     */
    private final MessageFactory messageFactory;

    /**
     * Timeout in seconds before the paused game is ended
     */
    private final long timeout;

    /**
     * List of nicknames of all connected clients
     */
    private final Map<String, ClientHandler> connectedClients;

    /**
     * This attribute is the model of the game
     */
    private final Game gameAssociated;

    /**
     * This constructor creates a GameController to manage a game
     * @param gameAssociated the game managed by the controller
     */
    public GameController(Game gameAssociated) {
        this(gameAssociated,180);
    }

    /**
     * This constructor creates a GameController to manage a game
     * @param gameAssociated the game managed by the controller
     * @param timeout seconds before paused game is ended
     */
    public GameController(Game gameAssociated, long timeout) {
        this.stopGameTimer = new Timer();
        this.messageFactory = new MessageFactory();
        this.gameAssociated = gameAssociated;
        gameAssociated.setMessageFactory(this.messageFactory);
        this.connectedClients = new HashMap<>();
        this.timeout = timeout;
    }

    public Game getGameAssociated() {
        return gameAssociated;
    }

    public ArrayList<String> getConnectedClients() {
        return new ArrayList<>(connectedClients.keySet());
    }

    /**
     * This method adds a client with given nickname to the game
     * @param nickname the name of the client to add
     */
    public synchronized void addClient(String nickname, ClientHandler clientHandler) {
        try {
            this.gameAssociated.getPlayerByName(nickname);
            //player already present in game
            this.messageFactory.sendMessageToAllGamePlayersExcept(new PlayerReconnectedToGameMessage(nickname), nickname);
            if(!this.connectedClients.containsKey(nickname)) {
                this.connectedClients.put(nickname, clientHandler);
                clientHandler.setGameController(this);
                messageFactory.attachObserver(nickname, clientHandler);
                // send to connected client all info needed
                this.gameAssociated.sendCurrentStateToPlayer(nickname);
                if(this.gameAssociated.getGameState().equals(GameState.PAUSE)) {
                    // if there is only one client, and it was not the active player, make it the active player and turn state to PLACE
                    if(this.connectedClients.size()==1 && !this.gameAssociated.getActivePlayer().getName().equals(nickname)) {
                        this.gameAssociated.setActivePlayer(
                                this.gameAssociated.getPlayerByName(nickname)
                        );
                        this.gameAssociated.setTurnState(TurnState.PLACE);
                        this.messageFactory.sendMessageToAllGamePlayers(
                                new TurnStateMessage(this.gameAssociated.getActivePlayer().getName(), this.gameAssociated.getTurnState())
                        );
                    }
                    else if(this.connectedClients.size()>=2) {
                        //if the game is in pause and there are 2 or more clients connected, unpause game and stop timer task
                        this.gameAssociated.setGameState(GameState.PLAYING);
                        this.stopGameTimer.cancel();
                        this.messageFactory.sendMessageToAllGamePlayers(new GameResumedMessage(
                                this.gameAssociated.getTurnState(),
                                this.gameAssociated.getActivePlayer().getName()
                        ));
                    }
                }
                if(this.gameAssociated.getGameState() == GameState.END){
                    this.messageFactory.sendMessageToPlayer(nickname, new EndGameMessage(this.gameAssociated.getWinnerNicks(), this.gameAssociated.getFinalPoints()).setHeader(nickname));
                }
            }
        } catch (PlayerNotFoundException e) {
            //new player
            if(this.gameAssociated.getNumJoinedPlayer() < this.gameAssociated.getNumPlayers()) {
                this.connectedClients.put(nickname, clientHandler);
                clientHandler.setGameController(this);
                messageFactory.attachObserver(nickname, clientHandler);
                this.gameAssociated.createNewPlayer(nickname);
            }
        }
    }

    /**
     * This method removes a client with given nickname from the game
     * @param nickname the name of the client to remove
     */
    public synchronized void removeClient(String nickname) {
        ClientHandler clientHandlerToRemove = this.connectedClients.remove(nickname);
        if(clientHandlerToRemove != null) {
            clientHandlerToRemove.setGameController(null);
            this.messageFactory.removeObserver(nickname);
            this.messageFactory.sendMessageToAllGamePlayers(new DisconnectedPlayerMessage(nickname));
            if (this.gameAssociated.getActivePlayer() != null && this.gameAssociated.getActivePlayer().getName().equals(nickname)){
                // the client disconnected was the active player: turn goes to next player unless no other client is connected
                // if he has already placed, a card is picked to end its turn
                if(this.gameAssociated.getTurnState().equals(TurnState.DRAW)) {
                    Iterator<Tuple<PlayableCardType,Integer>> iterator = List.of(
                            new Tuple<>(PlayableCardType.RESOURCE, 0),
                            new Tuple<>(PlayableCardType.RESOURCE, 1),
                            new Tuple<>(PlayableCardType.GOLD, 0),
                            new Tuple<>(PlayableCardType.GOLD, 1)
                    ).iterator();
                    while(this.gameAssociated.getTurnState().equals(TurnState.DRAW) && iterator.hasNext()) {
                        Tuple<PlayableCardType,Integer> current = iterator.next();
                        this.drawCardFromTable(nickname, current.x(), current.y());
                    }
                }
                else {
                    if(!this.connectedClients.isEmpty()) {
                        this.gameAssociated.setTurnState(TurnState.PLACE);
                        try {
                            this.setNextPlayer();
                        } catch (GameFinishedException e) {
                            //do not send TurnStateMessage
                            return;
                        }
                        this.messageFactory.sendMessageToAllGamePlayers(
                                new TurnStateMessage(this.gameAssociated.getActivePlayer().getName(), this.gameAssociated.getTurnState())
                        );
                    }
                }
            }

            if (this.connectedClients.size() == 1 && this.gameAssociated.getGameState().equals(GameState.PLAYING)) {
                // only a client is connected while game is playing: pause game
                this.gameAssociated.setGameState(GameState.PAUSE);
                stopGameTimer = new Timer();
                stopGameTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        stopGame();
                    }
                }, timeout * 1000);
                this.messageFactory.sendMessageToAllGamePlayers(new GamePausedMessage());
            }
        }
    }

    /**
     * This method sets a color for the given player
     * @param nickname the name of the player
     * @param color the chosen color
     */
    public synchronized void chooseColor(String nickname, Color color) {
        if(!this.connectedClients.containsKey(nickname)) return;

        if(!this.gameAssociated.getGameState().equals(GameState.SETUP)){
            this.messageFactory.sendMessageToPlayer(nickname,
                                                    new RefusedActionMessage(ErrorType.INVALID_GAME_STATE, "You cannot choose your color while game is in state " +
                                                            this.gameAssociated.getGameState().toString().toLowerCase()));
            return;
        }

        if(!this.gameAssociated.getAvailableColors().contains(color)){
            this.messageFactory.sendMessageToPlayer(nickname,
                    new RefusedActionMessage(ErrorType.COLOR_ALREADY_CHOSEN, "The color " +
                            color + " was already taken"));
            return;
        }

        if(this.gameAssociated.getPlayerByName(nickname).getColor()==null) {
            this.gameAssociated.getPlayerByName(nickname).setColor(color);
            this.messageFactory.sendMessageToAllGamePlayersExcept(new AvailableColorsMessage(new ArrayList<>(this.gameAssociated.getAvailableColors())), nickname);
        }
        else{
            this.messageFactory.sendMessageToPlayer(nickname, new RefusedActionMessage(ErrorType.COLOR_ALREADY_CHOSEN, "You have already chosen your color!"));
        }

        if(this.gameAssociated.allPlayersChooseInitialGoalColor()){
            this.gameAssociated.startGame();
        }
    }

    /**
     * This method choose which private goal between the given two for the given player
     * @param nickname the player name
     * @param cardIdx an index, 0 or 1, representing which card is being chosen
     */
    public synchronized void choosePrivateGoal(String nickname, int cardIdx) {
        if(!this.connectedClients.containsKey(nickname)) return;
        if(cardIdx<0||cardIdx>=2){
            this.messageFactory.sendMessageToPlayer(nickname, new RefusedActionMessage(ErrorType.INVALID_GOAL_CARD_ERROR,
                                                                                       "Goal card chosen is not valid!"));
            return;
        }

        if(this.gameAssociated.getGameState() != GameState.SETUP){
            this.messageFactory.sendMessageToPlayer(nickname, new RefusedActionMessage(ErrorType.INVALID_GAME_STATE,
                                                                                       "You cannot choose you goal card when game state is " + this.gameAssociated.getGameState().toString().toLowerCase()));
            return;
        }

        if(this.gameAssociated.getPlayerByName(nickname).getStation().getPrivateGoalCard()==null) {
            this.gameAssociated.getPlayerByName(nickname).getStation().setPrivateGoalCard(cardIdx);
        }
        else{
            this.messageFactory.sendMessageToPlayer(nickname, new RefusedActionMessage(ErrorType.GOAL_CARD_ALREADY_CHOSEN, "You have already chosen you private goal card!"));
        }

        if(this.gameAssociated.allPlayersChooseInitialGoalColor()) {
            this.gameAssociated.startGame();
        }
    }

    /**
     * This method place the initial card for the given player, choosing if the card has to be placed upward or downward
     * @param nickname the player name
     * @param cardOrientation the orientation of the card of type {@link CardOrientation}
     */
    public synchronized void placeInitialCard(String nickname, CardOrientation cardOrientation) {
        if(!this.connectedClients.containsKey(nickname)) return;

        if(this.gameAssociated.getGameState() != GameState.SETUP){
            this.messageFactory.sendMessageToPlayer(nickname, new RefusedActionMessage(ErrorType.INVALID_GAME_STATE,
                                                                                       "You cannot place initial card when game state is " + this.gameAssociated.getGameState().toString().toLowerCase()));
            return;
        }

        if(!this.gameAssociated.getPlayerByName(nickname).getStation().getInitialCardIsPlaced()) {
            this.gameAssociated.getPlayerByName(nickname).getStation().placeInitialCard(cardOrientation);
        }
        else{
            this.messageFactory.sendMessageToPlayer(nickname, new RefusedActionMessage(ErrorType.GENERIC, "You have already chosen initial card orientation!"));
        }

        if(this.gameAssociated.allPlayersChooseInitialGoalColor()) {
            this.gameAssociated.startGame();
        }
    }

    /**
     * This method place a card for the given player, using another card (anchor) and the place direction
     *
     * @param nickname   the player name
     * @param cardCode   the code of the card to place
     * @param anchorCode the code of the anchor card
     * @param direction  the direction where to put the card, of type Direction
     * @param cardOrientation the {@link CardOrientation} chosen by player
     */
    public synchronized void placeCard(String nickname, String cardCode, String anchorCode, Direction direction, CardOrientation cardOrientation) {
        if(!this.gameAssociated.getGameState().equals(GameState.PLAYING)) {
            this.messageFactory.sendMessageToPlayer(nickname, new RefusedActionMessage(ErrorType.INVALID_GAME_STATE,
                                                                                       "You cannot place a card when game is in " + this.gameAssociated.getGameState().toString().toLowerCase() + " state!"));
            return;
        }

        if(!this.gameAssociated.getTurnState().equals(TurnState.PLACE)){
            this.messageFactory.sendMessageToPlayer(nickname, new RefusedActionMessage(ErrorType.INVALID_TURN_STATE,
                                                                                       "You cannot place a card when your turn is in " + this.gameAssociated.getTurnState().toString().toLowerCase() + " state!"));
            return;
        }

        if(!this.gameAssociated.getActivePlayer().getName().equals(nickname)){
            this.messageFactory.sendMessageToPlayer(nickname, new RefusedActionMessage(ErrorType.NOT_YOUR_TURN,
                                                                                       "You cannot place a card when is " + this.gameAssociated.getActivePlayer().getName() + " state!"));
            return;
        }

        Optional<PlayableCard> anchor = this.gameAssociated.getPlayableCardFromCode(anchorCode);
        Optional<PlayableCard> toPlace = this.gameAssociated.getPlayableCardFromCode(cardCode);

        if(anchor.isPresent() && toPlace.isPresent()) {
            try {
                boolean checkPlacing = this.gameAssociated.getActivePlayer().getStation()
                                                          .placeCard(anchor.get(), toPlace.get(), direction, cardOrientation);
                if(!checkPlacing) return;
            } catch (InvalidCardException e) {
                //Message
                this.messageFactory.sendMessageToPlayer(nickname,
                                                        new RefusedActionMessage(ErrorType.INVALID_CARD_ERROR, "Attention, card " + cardCode + " cannot be placed because you haven't it in your station!"));
                //Message
                return;
            } catch (InvalidAnchorException e) {
                this.messageFactory.sendMessageToPlayer(nickname,
                                                        new RefusedActionMessage(ErrorType.INVALID_ANCHOR_ERROR, "Attention, " + anchorCode + " is invalid!"));
                return;
            }

            if(this.gameAssociated.getActivePlayer().getStation().getNumPoints() >= 20)
                this.gameAssociated.setFinalCondition(true);

            if(this.gameAssociated.drawableCardsArePresent())
                this.gameAssociated.setTurnState(TurnState.DRAW);
            else {
                // if there are no cards left to draw on table, the turn goes to next player
                try {
                    this.setNextPlayer();
                } catch (GameFinishedException e) {
                    //do not send TurnStateMessage
                    return;
                }
            }

            this.messageFactory.sendMessageToAllGamePlayers(
                    new TurnStateMessage(this.gameAssociated.getActivePlayer().getName(), this.gameAssociated.getTurnState())
            );
        }
    }

    /**
     * This method draws a card from deck for the given player, choosing between {@link PlayableCardType} decks
     * (only <code>PlayableCardType.GOLD</code> and <code>PlayableCardType.RESOURCE</code>).
     * @param nickname the player name
     * @param type the {@link PlayableCardType} chosen, only <code>PlayableCardType.GOLD</code>
     *             and <code>PlayableCardType.RESOURCE</code> are admissible types
     */
    public synchronized void drawCardFromDeck(String nickname, PlayableCardType type) {
        if (checkDrawConditions(nickname)) return;

        PlayableCard card = null;
        try {
            card = this.gameAssociated.pickCardFromDeck(type);
        }
        catch (EmptyDeckException e){
            this.messageFactory.sendMessageToPlayer(nickname, new RefusedActionMessage(ErrorType.EMPTY_DECK, "You cannot pick a card from deck " + type.toString().toLowerCase()  + " because is empty!"));
            return;
        }

        this.gameAssociated.getActivePlayer().getStation().updateCardsInHand(card);
        this.messageFactory.sendMessageToPlayer(nickname, new OwnAcceptedPickCardFromDeckMessage(nickname,
                                                                                                 card, type, gameAssociated.getDeckFromType(type).getNextCard().map(PlayableCard::getSeed).orElse(null)));
        this.messageFactory.sendMessageToAllGamePlayersExcept(new OtherAcceptedPickCardFromDeckMessage(nickname,
                                                                                                       new Tuple<>(card.getSeed(),card.getCardType()), type, gameAssociated.getDeckFromType(type).getNextCard().map(PlayableCard::getSeed).orElse(null)),nickname);

        this.gameAssociated.setTurnState(TurnState.PLACE);
        try {
            this.setNextPlayer();
        } catch (GameFinishedException e) {
            //do not send TurnStateMessage
            return;
        }

        this.messageFactory.sendMessageToAllGamePlayers(
                new TurnStateMessage(this.gameAssociated.getActivePlayer().getName(), this.gameAssociated.getTurnState())
        );
    }

    /**
     * This method draws a card from a table position for the given player, choosing between {@link PlayableCardType} decks
     * (only <code>PlayableCardType.GOLD</code> and <code>PlayableCardType.RESOURCE</code>).
     * @param nickname the player name
     * @param type the {@link PlayableCardType} chosen, only <code>PlayableCardType.GOLD</code>
     *             and <code>PlayableCardType.RESOURCE</code> are admissible types
     * @param position an integer between 0 and 1, representing the position of the chosen card
     */
    public synchronized void drawCardFromTable(String nickname, PlayableCardType type, int position) {
        if (checkDrawConditions(nickname)) return;

        PlayableCard card = null;
        try {
            card = this.gameAssociated.pickCardFromTable(type, position);
        }
        catch (CardNotFoundException e) {
            this.messageFactory.sendMessageToPlayer(nickname, new RefusedActionMessage(ErrorType.EMPTY_TABLE_SLOT,
                                                                                       "You cannot pick a card from table in position " + position + " because slot is empty!"));
            return;
        }
        catch (IllegalArgumentException illegalArgumentException){
            this.messageFactory.sendMessageToPlayer(nickname, new RefusedActionMessage(ErrorType.INVALID_CARD_ERROR,
                                                                                       illegalArgumentException.getMessage()));

            return;
        }

        this.gameAssociated.getActivePlayer().getStation().updateCardsInHand(card);
        this.messageFactory.sendMessageToAllGamePlayers(new AcceptedPickCardFromTable(nickname,
                                                                                      card, gameAssociated.getDeckFromType(type).getNextCard().map(PlayableCard::getSeed).orElse(null),
                                                                                      position, type, gameAssociated.getPlayableCardsOnTable(type)[position]));

        if(!this.gameAssociated.drawableCardsArePresent()) {
            this.gameAssociated.setFinalCondition(true);
        }

        this.gameAssociated.setTurnState(TurnState.PLACE);
        try {
            this.setNextPlayer();
        } catch (GameFinishedException e) {
            //do not send TurnStateMessage
            return;
        }


        this.messageFactory.sendMessageToAllGamePlayers(
                new TurnStateMessage(this.gameAssociated.getActivePlayer().getName(), this.gameAssociated.getTurnState())
        );
    }

    /**
     * This method checks if the player with a given nickname can draw a card. This is possible if it is the player's turn and
     * the player is in {@link TurnState#DRAW} turn state. Moreover, the game should be in the status of {@link GameState#PLAYING}.
     * @param nickname the nickname of the player to check if he can draw
     * @return If all these conditions are met, this method returns false. If a condition is not met, this method returns true
     */
    private boolean checkDrawConditions(String nickname) {
        if(!this.gameAssociated.getGameState().equals(GameState.PLAYING)) {
            this.messageFactory.sendMessageToPlayer(nickname, new RefusedActionMessage(ErrorType.INVALID_GAME_STATE,
                                                                                       "You cannot pick a card when game is in " + this.gameAssociated.getGameState().toString().toLowerCase() + " state!"));
            return true;
        }

        if(!this.gameAssociated.getTurnState().equals(TurnState.DRAW)){
            this.messageFactory.sendMessageToPlayer(nickname, new RefusedActionMessage(ErrorType.INVALID_TURN_STATE,
                                                                                       "You cannot pick a card when your turn is in " + this.gameAssociated.getTurnState().toString().toLowerCase() + " state!"));
            return true;
        }

        if(!this.gameAssociated.getActivePlayer().getName().equals(nickname)){
            this.messageFactory.sendMessageToPlayer(nickname, new RefusedActionMessage(ErrorType.NOT_YOUR_TURN,
                                                                                       "You cannot place a card when is " + this.gameAssociated.getActivePlayer().getName() + " state!"));
            return true;
        }
        return false;
    }

    /**
     * This method sets the next player, checking if this player is connected to the game or not.
     * If the player is not connected, the turn will go to the successive player
     */
    private synchronized void setNextPlayer() throws GameFinishedException {
        if(this.gameAssociated.getGameState().equals(GameState.END)) {
            throw new GameFinishedException();
        }

        Player selectedPlayer;
        do {
            selectedPlayer = this.gameAssociated.getNextPlayer();
            if(selectedPlayer.equals(this.gameAssociated.getFirstPlayer()) && this.gameAssociated.getFinalCondition()) {
                if(this.gameAssociated.isFinalRound()){
                    this.endGame();
                    throw new GameFinishedException();
                }
                else {
                    this.gameAssociated.setFinalRound(true);
                }
            }
            this.gameAssociated.setActivePlayer(selectedPlayer);
        } while(!this.connectedClients.containsKey(selectedPlayer.getName()));

    }

    /**
     * This method tells the game to update points using public and private goal cards
     */
    private synchronized void calculateFinalResult(){
        this.gameAssociated.updateGoalPoints();
    }

    /**
     * Ends the game
     */
    private synchronized void endGame() {
        this.calculateFinalResult();
        List<Player> sortedPlayers = this.gameAssociated.computeFinalScoreboard();

        Map<String, Integer> scoreboard = sortedPlayers.stream().collect(Collectors.toMap(Player::getName, p -> p.getStation().getNumPoints()));

        List<Player> winnerPlayers = new ArrayList<>();

        Player p;
        do {
            p = sortedPlayers.removeFirst();
            winnerPlayers.add(p);
        }while(!sortedPlayers.isEmpty()
                && sortedPlayers.getFirst().getStation().getNumPoints() == p.getStation().getNumPoints()
                && sortedPlayers.getFirst().getStation().getPointsFromGoals() == p.getStation().getPointsFromGoals()
                && sortedPlayers.getFirst().getNumberOfSatisfiedGoals() == p.getNumberOfSatisfiedGoals());

        this.gameAssociated.setGameState(GameState.END);

        this.gameAssociated.setWinnerNicks(winnerPlayers.stream().map(Player::getName).collect(Collectors.toCollection(ArrayList::new)));
        this.gameAssociated.setFinalPoints(scoreboard);

        this.messageFactory.sendMessageToAllGamePlayers(
                new EndGameMessage(this.gameAssociated.getWinnerNicks(), this.gameAssociated.getFinalPoints())
        );

        MainController.getMainController().fireGameAndPlayer(getGameAssociated().getGameName());
    }

    /**
     * This method is called when there is a timeout and at most only a client is connected
     */
    private synchronized void stopGame() {
        if(this.gameAssociated.getGameState().equals(GameState.PAUSE)) {
            if (this.connectedClients.size() == 1) {
                this.gameAssociated.setGameState(GameState.END);
                //Notify winner

                this.gameAssociated.setFinalPoints(new HashMap<>());
                this.gameAssociated.setWinnerNicks(new ArrayList<>(this.connectedClients.keySet()));

                this.messageFactory.sendMessageToAllGamePlayers(
                        // the Map is empty because there is no score to update
                        new EndGameMessage(this.gameAssociated.getWinnerNicks(), new HashMap<>())
                );

                MainController.getMainController().fireGameAndPlayer(getGameAssociated().getGameName());

                return;
            }
            this.gameAssociated.setGameState(GameState.END);

            this.gameAssociated.setFinalPoints(new HashMap<>());
            this.getGameAssociated().setWinnerNicks(new ArrayList<>());

            this.messageFactory.sendMessageToAllGamePlayers(
                    new EndGameMessage(new ArrayList<>(), this.gameAssociated.getFinalPoints())
            );

            MainController.getMainController().fireGameAndPlayer(getGameAssociated().getGameName());
        }
    }

    /**
     * This method is used to send a chat message to some players of the game.
     * @param receivers an <code>ArrayList&lt;String&gt;</code> containing all names of players to which
     *                  message must be sent.
     * @param senderNick the nickname of the player who sent the message
     * @param message the message to be sent
     */
    public synchronized void sendChatMessage(ArrayList<String> receivers, String senderNick, String message){
        if(!this.gameAssociated.hasPlayer(senderNick) ||
                !new HashSet<>(this.gameAssociated.getPlayers().stream().map(Player::getName).collect(Collectors.toList())).containsAll(receivers)){
            return;
        }
        this.gameAssociated.getChat().pushMessage(new Message(message, senderNick, receivers));
    }

}