package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.CardNotFoundException;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Deck.EmptyDeckException;
import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Model.Game.NameAlreadyInUseException;
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
        this.gameAssociated = gameAssociated;
        this.connectedClients = new ArrayList<>();
    }

    public void addClient(String nickname) {
        try {
            this.gameAssociated.getPlayerByName(nickname);
            //player already present in game
            if(!this.connectedClients.contains(nickname)) {
                this.connectedClients.add(nickname);
            }
        } catch (PlayerNotFoundException e) {
            //new player
            if(this.gameAssociated.getNumJoinedPlayer()<this.gameAssociated.getNumPlayers()) {
                try {
                    this.gameAssociated.createNewPlayer(nickname);
                } catch (NameAlreadyInUseException ignored) {}
                this.connectedClients.add(nickname);
                //if num of players reached, start game
                if(this.gameAssociated.allPlayersChooseInitialGoalColor()) {
                    this.gameAssociated.startGame();
                }
            }
        }
    }

    public void removeClient(String nickname) {
        if(this.connectedClients.remove(nickname)) {
            if (this.gameAssociated.getActivePlayer().getName().equals(nickname)) {
                this.gameAssociated.setTurnState(TurnState.PLACE);
                this.setNextPlayer();
            }
            if (this.connectedClients.size() == 1 && !this.gameAssociated.getGameState().equals(GameState.SETUP)) {
                this.gameAssociated.setGameState(GameState.PAUSE);
            }
        }
    }

    public void chooseColor(String nickname, Color color) {
        if(!this.gameAssociated.getGameState().equals(GameState.SETUP)) return;

        try {
            if(this.gameAssociated.getPlayerByName(nickname).getColor()==null) {
                this.gameAssociated.getPlayerByName(nickname).setColor(color);
            }
            if(this.gameAssociated.allPlayersChooseInitialGoalColor()) {
                this.gameAssociated.startGame();
            }
        } catch (PlayerNotFoundException ignored) {}
    }

    public void choosePrivateGoal(String nickname, int cardIdx) {
        if(!this.gameAssociated.getGameState().equals(GameState.SETUP)) return;

        try {
            if(this.gameAssociated.getPlayerByName(nickname).getPlayerStation().getPrivateGoalCard()==null) {
                this.gameAssociated.getPlayerByName(nickname).getPlayerStation().setPrivateGoalCard(cardIdx);
            }
            if(this.gameAssociated.allPlayersChooseInitialGoalColor()) {
                this.gameAssociated.startGame();
            }
        } catch (PlayerNotFoundException ignored) {}
    }

    public void placeInitialCard(String nickname, CardOrientation cardOrientation) {
        if(!this.gameAssociated.getGameState().equals(GameState.SETUP)) return;

        try {
            if(!this.gameAssociated.getPlayerByName(nickname).getPlayerStation().getInitialCardIsPlaced()) {
                this.gameAssociated.getPlayerByName(nickname).getPlayerStation().placeInitialCard(cardOrientation);
            }
            if(this.gameAssociated.allPlayersChooseInitialGoalColor()) {
                this.gameAssociated.startGame();
            }
        } catch (PlayerNotFoundException ignored) {}
    }

    public void placeCard(String nickname, String cardCode, String anchorCode, Direction direction) {
        if(
                !this.gameAssociated.getGameState().equals(GameState.PLAYING) &&
                        !this.gameAssociated.getTurnState().equals(TurnState.PLACE) ||
                !this.gameAssociated.getActivePlayer().getName().equals(nickname)) return;

        Optional<PlayableCard> anchor = this.gameAssociated.getPlayableCardFromCode(anchorCode);
        Optional<PlayableCard> toPlace = this.gameAssociated.getPlayableCardFromCode(cardCode);

        if(anchor.isPresent() && toPlace.isPresent()) {
            try {
                this.gameAssociated.getActivePlayer().getPlayerStation()
                        .placeCard(anchor.get(), toPlace.get(), direction);
            } catch (InvalidCardException e) {
                // given card to place is not valid
                return;
            } catch (InvalidAnchorException e) {
                // anchor card is not valid
                return;
            }

            if(this.gameAssociated.getActivePlayer().getPlayerStation().getNumPoints() >= 20)
                this.gameAssociated.setFinalRound(true);

            if(this.gameAssociated.drawableCardsArePresent())
                this.gameAssociated.setTurnState(TurnState.PLACE);
            else
                this.setNextPlayer();
        }
    }

    public void drawCardFromDeck(String nickname, PlayableCardType type)
    {
        if(
                !this.gameAssociated.getGameState().equals(GameState.PLAYING) &&
                        !this.gameAssociated.getTurnState().equals(TurnState.DRAW) ||
                        !this.gameAssociated.getActivePlayer().getName().equals(nickname)) return;

        PlayableCard card = null;
        try {
             card = this.gameAssociated.pickCardFromDeck(type);
        }
        catch (EmptyDeckException e) { return; }

        this.gameAssociated.getActivePlayer().getPlayerStation()
                .addCardInHand(
                        card
                );
        this.gameAssociated.setTurnState(TurnState.PLACE);
        this.setNextPlayer();
    }

    public void drawCardFromTable(String nickname, PlayableCardType type, int position)
    {
        if(
                !this.gameAssociated.getGameState().equals(GameState.PLAYING) &&
                        !this.gameAssociated.getTurnState().equals(TurnState.DRAW) ||
                        !this.gameAssociated.getActivePlayer().getName().equals(nickname)) return;
        PlayableCard card = null;
        try {
            card = this.gameAssociated.pickCardFromTable(type, position);
        }
        catch (CardNotFoundException e) { return; }

        this.gameAssociated.getActivePlayer().getPlayerStation()
                .addCardInHand(
                        card
                );
        this.gameAssociated.setTurnState(TurnState.PLACE);
        this.setNextPlayer();
    }

    private void setNextPlayer()
    {
        Player selectedPlayer;
        do {
            selectedPlayer = this.gameAssociated.getNextPlayer();
            if(selectedPlayer.equals(this.gameAssociated.getFirstPlayer()) && this.gameAssociated.getFinalRound()) {
                this.calculateFinalResult();
                this.gameAssociated.setGameState(GameState.END);
                return;
            }
            this.gameAssociated.setActivePlayer(selectedPlayer);
        } while(!this.connectedClients.contains(selectedPlayer.getName()));

    }

    private void calculateFinalResult()
    {
        this.gameAssociated.updateGoalPoints();
    }
}
