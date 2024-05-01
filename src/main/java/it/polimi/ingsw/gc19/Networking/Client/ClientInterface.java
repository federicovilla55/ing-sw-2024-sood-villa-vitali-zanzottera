package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

import java.util.*;

public interface ClientInterface extends ConfigurableClient, NetworkManagementInterface{
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
