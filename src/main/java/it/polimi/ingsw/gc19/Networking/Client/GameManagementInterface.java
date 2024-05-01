package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;

import java.util.ArrayList;

/**
 * The interface is implemented by all clients independently by their connection type (RMI or socket).
 * It aims to create a common way that the client can use to interact in the network.
 * By using common methods both for RMI and socket, a higher level of abstraction can be obtained,
 * without requiring to create different methods for RMI and socket in the methods that needs to
 * interact with the network.
 */
public interface GameManagementInterface {
    /**
     * The method is used to create a new game with the given name and number of players.
     * @param gameName The name of the game that will be created.
     * @param numPlayers The number of players to play with.
     */
    void createGame(String gameName, int numPlayers);

    /**
     * The method is used to create a new game with the given name,
     * number of players and seed.
     * @param gameName The name of the game that will be created.
     * @param numPlayers The number of players for the game.
     * @param seed The seed for the game.
     */
    void createGame(String gameName, int numPlayers, int seed);

    /**
     * The method is used to join a game with the given game name.
     * @param gameName The name of the game to join.
     */
    void joinGame(String gameName);

    /**
     * The method is used to join the first available game.
     */
    void joinFirstAvailableGame();

    /**
     * The method is used to reconnect the client to the server.
     */
    void reconnect();

    /**
     * The method is used to explicitly disconnect the client from the server.
     */
    void disconnect();

    /**
     * The method is used to send a request to place a card in the personal table.
     * @param cardToInsert the card that needs to be placed.
     * @param anchorCard the card from which to place the card.
     * @param directionToInsert the direction from the anchorCard in which we want to place the card.
     * @param orientation the orientation in which we want to place cardToInsert.
     */
    void placeCard(String cardToInsert, String anchorCard, Direction directionToInsert, CardOrientation orientation);

    /**
     * The method is used to send a message to the chat.
     * @param UsersToSend a list containing the nickname of the users to whom we want to send the message.
     * @param messageToSend the message we want to send.
     */
    void sendChatMessage(ArrayList<String> UsersToSend, String messageToSend);

    /**
     * The method is used to place the initial card at the beginning of the game.
     * @param cardOrientation the orientation in which we want to place the initial card.
     */
    void placeInitialCard(CardOrientation cardOrientation);

    /**
     * The method is used to pick a card from the common table.
     * @param type the type of card we want to pick (RESOURCE/GOLD).
     * @param position the position in the table we want to take the card from.
     */
    void pickCardFromTable(PlayableCardType type, int position);

    /**
     * The method is used to pick a card from a deck.
     * @param type the type of card we want to pick (RESOURCE/GOLD).
     */
    void pickCardFromDeck(PlayableCardType type);

    /**
     * To choose a color for the personal pawn at the beginning of the game.
     * @param color the selected color.
     */
    void chooseColor(Color color);

    /**
     * To choose the private goal card at the beginning of the game.
     * @param cardIdx which of the two proposed goal card we want to choose.
     */
    void choosePrivateGoalCard(int cardIdx);

    /**
     * To get the games free to join.
     */
    void availableGames();

    /**
     * To get the personal nickname.
     * @return a string representing the public nickname of the player.
     */
    String getNickname();

    /**
     * To explicitly disconnect and log out of the game.
     */
    void logoutFromGame();
}
