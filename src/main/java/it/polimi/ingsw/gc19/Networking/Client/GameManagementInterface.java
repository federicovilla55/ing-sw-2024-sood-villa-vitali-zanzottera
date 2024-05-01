package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;

import java.util.ArrayList;

public interface GameManagementInterface {
    void createGame(String gameName, int numPlayers);

    void createGame(String gameName, int numPlayers, int seed);

    void joinGame(String gameName);

    void joinFirstAvailableGame();

    void placeCard(String cardToInsert, String anchorCard, Direction directionToInsert, CardOrientation orientation);

    void sendChatMessage(ArrayList<String> UsersToSend, String messageToSend);

    void placeInitialCard(CardOrientation cardOrientation);

    void pickCardFromTable(PlayableCardType type, int position);

    void pickCardFromDeck(PlayableCardType type);

    void chooseColor(Color color);

    void choosePrivateGoalCard(int cardIdx);

    void availableGames();

    void logoutFromGame();
}
