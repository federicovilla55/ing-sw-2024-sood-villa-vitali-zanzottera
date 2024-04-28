package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

import java.util.*;

public interface ClientInterface {
    void connect();

    void createGame(String gameName, int numPlayers);

    void createGame(String gameName, int numPlayers, int seed);

    void joinGame(String gameName);

    void joinFirstAvailableGame();

    void reconnect();

    void disconnect();

    void placeCard(String cardToInsert, String anchorCard, Direction directionToInsert, CardOrientation orientation);

    void sendChatMessage(ArrayList<String> UsersToSend, String messageToSend);

    void placeInitialCard(CardOrientation cardOrientation);

    void pickCardFromTable(PlayableCardType type, int position);

    void pickCardFromDeck(PlayableCardType type);

    void chooseColor(Color color);

    void choosePrivateGoalCard(int cardIdx);

    void availableGames();

    void setToken(String token);

    void setNickname(String nickname);

    String getNickname();

    void logout();

}
