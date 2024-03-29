package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.CardNotFoundException;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Deck.EmptyDeckException;
import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Model.Game.Player;
import it.polimi.ingsw.gc19.Model.Game.PlayerNotFoundException;
import it.polimi.ingsw.gc19.Model.Station.InvalidAnchorException;
import it.polimi.ingsw.gc19.Model.Station.InvalidCardException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * This class is the controller of a single game. It is a Controller relative to Model-View-Controller design pattern.
 * The idea of this class is to implement an Observer of the view, and to obtain from it events (user input), extract from
 * the view necessary information, and call methods on the model (gameAssociated)
 */
public class GameController {
    /**
     * Timeout in seconds before the paused game is ended
     */
    final long timeout;

    /**
     * List of nicknames of all connected clients
     */
    final List<String> connectedClients;

    /**
     * This attribute is the model of the game
     */
    final Game gameAssociated;

    /**
     * This constructor creates a GameController to manage a game
     * @param gameAssociated the game managed by the controller
     */
    GameController(Game gameAssociated) {
        this(gameAssociated,60);
    }

    /**
     * This constructor creates a GameController to manage a game
     * @param gameAssociated the game managed by the controller
     * @param timeout seconds before paused game is ended
     */
    GameController(Game gameAssociated, long timeout) {
        this.gameAssociated = gameAssociated;
        this.connectedClients = new ArrayList<>();
        this.timeout = timeout;
    }

    /**
     * This method adds a client with given nickname to the game
     * @param nickname the name of the client to add
     */
    public synchronized void addClient(String nickname) {
        try {
            this.gameAssociated.getPlayerByName(nickname);
            //player already present in game
            if(!this.connectedClients.contains(nickname)) {
                this.connectedClients.add(nickname);
                if(this.gameAssociated.getGameState().equals(GameState.PAUSE)) {
                    // if there is only one client, and it was not the active player, make it the active player and turn state to PLACE
                    if(this.connectedClients.size()==1 && !this.gameAssociated.getActivePlayer().getName().equals(nickname)) {
                        this.gameAssociated.setActivePlayer(
                                this.gameAssociated.getPlayerByName(nickname)
                        );
                        this.gameAssociated.setTurnState(TurnState.PLACE);
                    }
                    else if(this.connectedClients.size()>=2) {
                        //the game is in pause and there are 2 or more clients connected
                        this.gameAssociated.setGameState(GameState.PLAYING);
                    }
                }
            }
        } catch (PlayerNotFoundException e) {
            //new player
            if(this.gameAssociated.getNumJoinedPlayer() < this.gameAssociated.getNumPlayers()) {
                this.gameAssociated.createNewPlayer(nickname);
                this.connectedClients.add(nickname);
            }
        }
    }

    /**
     * This method removes a client with given nickname from the game
     * @param nickname the name of the client to remove
     */
    public synchronized void removeClient(String nickname) {
        if(this.connectedClients.remove(nickname)) {

            if (this.gameAssociated.getActivePlayer() != null && this.gameAssociated.getActivePlayer().getName().equals(nickname)) {
                // the client disconnected was the active player: turn goes to next player unless no other client is connected
                if(!this.connectedClients.isEmpty()) {
                    this.gameAssociated.setTurnState(TurnState.PLACE);
                    this.setNextPlayer();
                }
            }

            if (this.connectedClients.size() == 1 && this.gameAssociated.getGameState().equals(GameState.PLAYING)) {
                // only a client is connected while game is playing: pause game
                this.gameAssociated.setGameState(GameState.PAUSE);
                new Thread(() -> {
                    try {
                        Thread.sleep(timeout * 1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    this.stopGame();
                }).start();
            }
        }
    }

    /**
     * This method sets a color for the given player
     * @param nickname the name of the player
     * @param color the chosen color
     */
    public synchronized void chooseColor(String nickname, Color color) {
        if(!this.gameAssociated.getGameState().equals(GameState.SETUP)
            || !this.connectedClients.contains(nickname)) return;

        if(this.gameAssociated.getPlayerByName(nickname).getColor()==null
        && this.gameAssociated.getAvailableColors().contains(color)) {
            this.gameAssociated.getPlayerByName(nickname).setColor(color);
            this.gameAssociated.removeAvailableColor(color);
        }
        if(this.gameAssociated.allPlayersChooseInitialGoalColor()) {
            this.gameAssociated.startGame();
        }
    }

    /**
     * This method choose which private goal between the given two for the given player
     * @param nickname the player name
     * @param cardIdx an index, 0 or 1, representing which card is being chosen
     */
    public synchronized void choosePrivateGoal(String nickname, int cardIdx) {
        if(cardIdx<0||cardIdx>=2) return;
        if(!this.gameAssociated.getGameState().equals(GameState.SETUP)
                || !this.connectedClients.contains(nickname)) return;

        if(this.gameAssociated.getPlayerByName(nickname).getPlayerStation().getPrivateGoalCard()==null) {
            this.gameAssociated.getPlayerByName(nickname).getPlayerStation().setPrivateGoalCard(cardIdx);
        }
        if(this.gameAssociated.allPlayersChooseInitialGoalColor()) {
            this.gameAssociated.startGame();
        }
    }

    /**
     * This method place the initial card for the given player, choosing if the card has to be placed upward or downward
     * @param nickname the player name
     * @param cardOrientation the orientation of the card of type CardOrientation (UP, DOWN)
     */
    public synchronized void placeInitialCard(String nickname, CardOrientation cardOrientation) {
        if(!this.gameAssociated.getGameState().equals(GameState.SETUP)
                || !this.connectedClients.contains(nickname)) return;

        if(!this.gameAssociated.getPlayerByName(nickname).getPlayerStation().getInitialCardIsPlaced()) {
            this.gameAssociated.getPlayerByName(nickname).getPlayerStation().placeInitialCard(cardOrientation);
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
     * @param cardOrientation UP or DOWN card orientation
     */
    public synchronized void placeCard(String nickname, String cardCode, String anchorCode, Direction direction, CardOrientation cardOrientation) {
        if(
                !this.gameAssociated.getGameState().equals(GameState.PLAYING) ||
                        !this.gameAssociated.getTurnState().equals(TurnState.PLACE) ||
                !this.gameAssociated.getActivePlayer().getName().equals(nickname)) return;

        Optional<PlayableCard> anchor = this.gameAssociated.getPlayableCardFromCode(anchorCode);
        Optional<PlayableCard> toPlace = this.gameAssociated.getPlayableCardFromCode(cardCode);

        if(anchor.isPresent() && toPlace.isPresent()) {
            try {
                boolean checkPlacing = this.gameAssociated.getActivePlayer().getPlayerStation()
                        .placeCard(anchor.get(), toPlace.get(), direction, cardOrientation);
                if(!checkPlacing) return;
            } catch (InvalidCardException e) {
                // given card to place is not valid
                return;
            } catch (InvalidAnchorException e) {
                // anchor card is not valid
                return;
            }

            if(this.gameAssociated.getActivePlayer().getPlayerStation().getNumPoints() >= 20)
                this.gameAssociated.setFinalCondition(true);

            if(this.gameAssociated.drawableCardsArePresent())
                this.gameAssociated.setTurnState(TurnState.DRAW);
            else
                // if there are no cards left to draw on table, the turn goes to next player
                this.setNextPlayer();
        }
    }

    /**
     * This method draws a card from deck for the given player, choosing between the ResourceDeck and the GoldDeck
     * @param nickname the player name
     * @param type type CardType, only RESOURCE and GOLD are admissible types
     */
    public synchronized void drawCardFromDeck(String nickname, PlayableCardType type)
    {
        if(
                !this.gameAssociated.getGameState().equals(GameState.PLAYING) ||
                        !this.gameAssociated.getTurnState().equals(TurnState.DRAW) ||
                        !this.gameAssociated.getActivePlayer().getName().equals(nickname)) return;

        PlayableCard card = null;
        try {
             card = this.gameAssociated.pickCardFromDeck(type);
        }
        catch (EmptyDeckException e) { return; }

        this.gameAssociated.getActivePlayer().getPlayerStation()
                .updateCardsInHand(
                        card
                );

        this.gameAssociated.setTurnState(TurnState.PLACE);
        this.setNextPlayer();
    }

    /**
     * This method draws a card from a table position for the given player, choosing between the ResourceDeck and the GoldDeck
     * @param nickname the player name
     * @param type type CardType, only RESOURCE and GOLD are admissible types
     * @param position an integer between 0 and 1, representing the position of the chosen card
     */
    public synchronized void drawCardFromTable(String nickname, PlayableCardType type, int position)
    {
        if(
                !this.gameAssociated.getGameState().equals(GameState.PLAYING) ||
                        !this.gameAssociated.getTurnState().equals(TurnState.DRAW) ||
                        !this.gameAssociated.getActivePlayer().getName().equals(nickname)) return;
        PlayableCard card = null;
        try {
            card = this.gameAssociated.pickCardFromTable(type, position);
        }
        catch (CardNotFoundException e) { return; }

        this.gameAssociated.getActivePlayer().getPlayerStation()
                .updateCardsInHand(
                        card
                );

        if(!this.gameAssociated.drawableCardsArePresent()) {
            this.gameAssociated.setFinalCondition(true);
        }

        this.gameAssociated.setTurnState(TurnState.PLACE);
        this.setNextPlayer();
    }

    /**
     * This method sets the next player, checking if this player is connected to the game or not.
     * If the player is not connected, the turn will go to the successive player
     */
    private synchronized void setNextPlayer()
    {
        Player selectedPlayer;
        do {
            selectedPlayer = this.gameAssociated.getNextPlayer();
            if(selectedPlayer.equals(this.gameAssociated.getFirstPlayer()) && this.gameAssociated.getFinalCondition()) {
                if(this.gameAssociated.isFinalRound()) {
                    this.endGame();
                    return;
                }
                else {
                    this.gameAssociated.setFinalRound(true);
                }
            }
            this.gameAssociated.setActivePlayer(selectedPlayer);
        } while(!this.connectedClients.contains(selectedPlayer.getName()));

    }

    /**
     * This method tells the game to update points using public and private goal cards
     */
    private synchronized void calculateFinalResult()
    {
        this.gameAssociated.updateGoalPoints();
    }

    private synchronized void endGame() {
        this.calculateFinalResult();
        this.gameAssociated.computeWinnerPlayers();
        this.gameAssociated.setGameState(GameState.END);
    }

    // this method is called when there is a timeout and at most only a client is connected
    private synchronized void stopGame() {
        if(this.gameAssociated.getGameState().equals(GameState.PAUSE)) {
            if (this.connectedClients.size() == 1) {
                this.gameAssociated.addWinnerPlayer(
                        this.gameAssociated.getPlayerByName(this.connectedClients.getFirst())
                );
            }
            this.gameAssociated.setGameState(GameState.END);
        }
    }
}
