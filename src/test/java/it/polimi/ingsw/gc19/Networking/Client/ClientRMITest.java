package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.ClientRMI;
import it.polimi.ingsw.gc19.Networking.Server.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedColorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedPickCardMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.GameConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OwnStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.TableConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.AvailableColorsMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.NewPlayerConnectedToGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkError;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.MainServerRMI;
import it.polimi.ingsw.gc19.View.GameLocalView.ActionParser;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ClientRMITest {
    private static MainServerRMI mainServerRMI;
    private static VirtualMainServer virtualMainServer;
    private static Registry registry;

    // Hashmap to save the get the anchor for the placeCard.
    private HashMap<ClientInterface, PlayableCard> clientsAnchors;
    private TestClassClientRMI client1, client2, client3, client4, client5;

    @BeforeAll
    public static void setUpServer() throws IOException, NotBoundException {
        ServerApp.startRMI(Settings.DEFAULT_RMI_SERVER_PORT);
        mainServerRMI = ServerApp.getMainServerRMI();
        registry = LocateRegistry.getRegistry("localhost");
        virtualMainServer = (VirtualMainServer) registry.lookup(Settings.mainRMIServerName);
    }

    @BeforeEach
    public void setUpTest() throws RemoteException {
        MessageHandler messageHandler1 = new MessageHandler(new ActionParser());
        MessageHandler messageHandler2 = new MessageHandler(new ActionParser());
        MessageHandler messageHandler3 = new MessageHandler(new ActionParser());
        MessageHandler messageHandler4 = new MessageHandler(new ActionParser());
        MessageHandler messageHandler5 = new MessageHandler(new ActionParser());

        this.client1 = new TestClassClientRMI(virtualMainServer, messageHandler1, "client1");
        this.client2 = new TestClassClientRMI(virtualMainServer, messageHandler2, "client2");
        this.client3 = new TestClassClientRMI(virtualMainServer, messageHandler3, "client3");
        this.client4 = new TestClassClientRMI(virtualMainServer, messageHandler4, "client4");
        this.client5 = new TestClassClientRMI(virtualMainServer, messageHandler5, "client5");

        clientsAnchors = new HashMap<>();
    }

    @AfterEach
    public void resetClients(){
        this.client1.disconnect();
        this.client2.disconnect();
        this.client3.disconnect();
        this.client4.disconnect();
        this.client5.disconnect();
        mainServerRMI.killClientHandlers();
        mainServerRMI.resetServer();
    }

    @AfterAll
    public static void tearDownServer() {
        ServerApp.unexportRegistry();
    }

    @Test
    public void testClientCreation(){
        this.client1.connect();
        assertMessageEquals(this.client1, new CreatedPlayerMessage("client1"));
    }

    @Test
    public void testCreateClient(){
        this.client1.connect();
        assertMessageEquals(this.client1, new CreatedPlayerMessage("client1"));
        this.client2.connect();

        assertMessageEquals(this.client2, new CreatedPlayerMessage("client2"));
        assertNull(this.client1.getMessage());
        this.client3.connect();

        assertMessageEquals(this.client3, new CreatedPlayerMessage("client3"));
        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());
        this.client4.connect();

        assertMessageEquals(this.client4, new CreatedPlayerMessage("client4"));
        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());
        assertNull(this.client3.getMessage());

        this.client1.connect();

        assertMessageEquals(this.client1, new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER, null));
        assertNull(this.client2.getMessage());
        assertNull(this.client3.getMessage());
        assertNull(this.client4.getMessage());

        //Create new client with other name
        this.client5.setNickname("client1");
        this.client5.connect();

        assertMessageEquals(this.client5, new GameHandlingError(Error.PLAYER_NAME_ALREADY_IN_USE, null));
    }

    @Test
    public void testCreateGame(){
        this.client1.connect();

        this.client1.createGame("game1", 2);

        assertMessageEquals(this.client1, new CreatedGameMessage("game1"));

        //Player already registered to some games

        this.client1.createGame("game2", 2);

        assertMessageEquals(this.client1, new GameHandlingError(Error.PLAYER_ALREADY_REGISTERED_TO_SOME_GAME, null));

        //Player already registered and game name equal

        this.client1.createGame("game1", 2);

        assertMessageEquals(this.client1, new GameHandlingError(Error.PLAYER_ALREADY_REGISTERED_TO_SOME_GAME, null));

        //Game name already in use
        this.client2.connect();

        this.client2.createGame("game1", 2);

        assertMessageEquals(this.client2, new GameHandlingError(Error.GAME_NAME_ALREADY_IN_USE, null));
    }

    @Test
    public void testMultiplePlayerInGame() {
        this.client1.connect();

        this.client1.createGame("game3", 3);

        assertMessageEquals(this.client1, new CreatedGameMessage("game3"));


        this.client2.connect();

        this.client2.joinGame("game3");

        assertMessageEquals(this.client2, new JoinedGameMessage("game3"));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage("client2"));


        this.client3.connect();

        this.client3.joinGame("game3");

        assertMessageEquals(this.client3, new JoinedGameMessage("game3"));
        assertMessageEquals(List.of(this.client2, this.client1), new NewPlayerConnectedToGameMessage("client3"));


        this.client3.sendChatMessage(new ArrayList<>(List.of("client1", "client2")), "Message in chat");
        assertMessageEquals(new ArrayList<>(List.of(this.client1, this.client2)), new NotifyChatMessage("client3", "Message in chat"));


        this.client3.chooseColor(Color.BLUE);
        assertMessageEquals(new ArrayList<>(List.of(this.client3, this.client2, this.client1)), new AcceptedColorMessage("client3", Color.BLUE));
        assertMessageEquals(new ArrayList<>(List.of(this.client2, this.client1)), new AvailableColorsMessage(new ArrayList<>(List.of(Color.GREEN, Color.YELLOW, Color.RED))));
    }

    @Test
    public void testFirePlayersAndGames(){

        this.client1.connect();
        this.client2.connect();
        this.client3.connect();
        this.client4.connect();

        this.client1.createGame("game13", 4, 1);
        this.client2.joinGame("game13");
        this.client3.joinGame("game13");
        this.client4.joinGame("game13");


        allPlayersChooseColor(this.client1, this.client2, this.client3, this.client4);

        allPlayersChoosePrivateGoal(this.client1, this.client2, this.client3, this.client4);

        allPlayersPlacedInitialCard(this.client1, this.client2, this.client3, this.client4);

        this.client3.disconnect();
        this.client4.disconnect();

        // client1 turn
        this.client1.placeCard("resource_23", "initial_05", Direction.UP_RIGHT, CardOrientation.DOWN);
        this.client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyFirstTurn(this.client2, client2, PlayableCardType.RESOURCE);

        this.client1.placeCard("resource_01", "initial_05", Direction.UP_LEFT, CardOrientation.UP);
        this.client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(this.client2, client2, PlayableCardType.RESOURCE);

        this.client1.placeCard("gold_39", "resource_01", Direction.UP_LEFT, CardOrientation.DOWN);
        this.client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(this.client2, client2, PlayableCardType.RESOURCE);

        this.client1.placeCard("gold_23", "resource_23", Direction.UP_RIGHT, CardOrientation.UP);
        this.client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(this.client2, client2, PlayableCardType.RESOURCE);

        this.client1.placeCard("gold_40", "gold_23", Direction.UP_LEFT, CardOrientation.DOWN);
        this.client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(this.client2, client2, PlayableCardType.RESOURCE);

        this.client1.placeCard("resource_05", "gold_39", Direction.UP_RIGHT, CardOrientation.DOWN);
        this.client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(this.client2, client2, PlayableCardType.RESOURCE);

        this.client1.placeCard("resource_03", "resource_05", Direction.UP_RIGHT, CardOrientation.DOWN);
        this.client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(this.client2, client2, PlayableCardType.RESOURCE);

        this.client1.placeCard("gold_06", "resource_05", Direction.DOWN_RIGHT, CardOrientation.UP);
        this.client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(this.client2, client2, PlayableCardType.RESOURCE);

        this.client1.placeCard("gold_20", "gold_23", Direction.DOWN_RIGHT, CardOrientation.DOWN);
        this.client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(this.client2, client2, PlayableCardType.RESOURCE);

        this.client1.placeCard("resource_08", "gold_20", Direction.DOWN_RIGHT, CardOrientation.DOWN);
        this.client1.pickCardFromTable(PlayableCardType.RESOURCE, 1);

        dummyTurn(this.client2, client2, PlayableCardType.RESOURCE);

        this.client1.placeCard("resource_21", "gold_20", Direction.UP_RIGHT, CardOrientation.DOWN);
        this.client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(this.client2, client2, PlayableCardType.RESOURCE);

        this.client1.placeCard("gold_28", "resource_08", Direction.DOWN_RIGHT, CardOrientation.UP);
        this.client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(this.client2, client2, PlayableCardType.RESOURCE);

        this.client1.placeCard("resource_30", "gold_28", Direction.UP_RIGHT, CardOrientation.UP);
        this.client1.pickCardFromTable(PlayableCardType.GOLD, 0);

        dummyTurn(this.client2, client2, PlayableCardType.RESOURCE);

        this.client1.placeCard("resource_39", "resource_21", Direction.UP_RIGHT, CardOrientation.UP);
        this.client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(this.client2, client2, PlayableCardType.RESOURCE);

        //assertFalse(gameController.getGameAssociated().getFinalCondition());
        this.client1.placeCard("gold_24", "resource_21", Direction.DOWN_RIGHT, CardOrientation.UP);
        // client1 reached 20 points: final condition should be true, but not in final round
        //assertTrue(gameController.getGameAssociated().getFinalCondition());
        //assertFalse(gameController.getGameAssociated().isFinalRound());
        this.client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        //assertTrue(gameController.getGameAssociated().getFinalCondition());
        //assertFalse(gameController.getGameAssociated().isFinalRound());
        dummyTurn(this.client2, client2, PlayableCardType.RESOURCE);

        // now it should be the final round:
        //assertTrue(gameController.getGameAssociated().getFinalCondition());
        //assertTrue(gameController.getGameAssociated().isFinalRound());

        this.client1.placeCard("resource_28", "resource_39", Direction.UP_RIGHT, CardOrientation.UP);
        this.client1.pickCardFromTable(PlayableCardType.RESOURCE, 1);

        dummyTurn(this.client2, client2, PlayableCardType.RESOURCE);

        // game should end and declare client1 the winner
        //assertEquals(GameState.END, gameController.getGameAssociated().getGameState());
        //assertEquals(this.client1, gameController.getGameAssociated().getWinnerPlayers().getFirst().getName());
        //assertEquals(1, gameController.getGameAssociated().getWinnerPlayers().size());


        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertMessageEquals(List.of(this.client1, this.client2), new DisconnectGameMessage("game13"));
    }

    @Test
    public void testPlayerCanJoinFullGame(){
        this.client1.connect();

        this.client1.createGame("game5", 2);

        assertMessageEquals(this.client1, new CreatedGameMessage("game5"));


        this.client2.connect();

        this.client2.joinGame("game5");

        assertMessageEquals(this.client2, new JoinedGameMessage("game5"));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage("client2"));


        this.client3.connect();

        this.client3.joinGame("game5");

        assertMessageEquals(this.client3, new GameHandlingError(Error.GAME_NOT_ACCESSIBLE, null));
    }


    @Test
    public void testMultipleGames(){
        this.client1.connect();

        this.client1.createGame("game8", 2);

        assertMessageEquals(this.client1, new CreatedGameMessage("game8"));

        client1.waitForMessage(TableConfigurationMessage.class);


        this.client2.connect();

        this.client2.joinGame("game8");

        assertMessageEquals(this.client2, new JoinedGameMessage("game8"));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage("client2"));


        this.client1.waitForMessage(TableConfigurationMessage.class);
        this.client2.waitForMessage(GameConfigurationMessage.class);

        this.client3.connect();

        this.client3.createGame("game9", 2);

        assertMessageEquals(this.client3, new CreatedGameMessage("game9"));


        this.client4.connect();

        this.client4.joinGame("game9");

        assertMessageEquals(this.client4, new JoinedGameMessage("game9"));

        assertMessageEquals(this.client3, new NewPlayerConnectedToGameMessage("client4"));

        this.client3.sendChatMessage(new ArrayList<>(List.of("client3", "client4")), "Message in chat");
        assertMessageEquals(new ArrayList<>(List.of(this.client3, this.client4)), new NotifyChatMessage("client3", "Message in chat"));

    }


    @Test
    public void testJoinFirstAvailableGames() throws RemoteException {
        this.client1.connect();
        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message = this.client1.getMessage();
        String token1 = ((CreatedPlayerMessage) message).getToken();
        this.client1.setToken(token1);

        this.client1.createGame("game4", 2);

        assertMessageEquals(this.client1, new CreatedGameMessage("game4"));

        client1.waitForMessage(GameConfigurationMessage.class);


        this.client2.connect();
        client2.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message2 = this.client2.getMessage();
        String token2 = ((CreatedPlayerMessage) message2).getToken();
        this.client2.setToken(token2);

        this.client2.joinGame("game4");
        assertNotNull(this.client2);

        assertMessageEquals(this.client2, new JoinedGameMessage("game4"));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage("client2"));


        this.client1.waitForMessage(TableConfigurationMessage.class);
        this.client2.waitForMessage(GameConfigurationMessage.class);

        this.client3.connect();
        client3.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message3 = this.client3.getMessage();
        String token3 = ((CreatedPlayerMessage) message3).getToken();
        this.client3.setToken(token3);

        this.client3.createGame("game7", 2);

        assertMessageEquals(this.client3, new CreatedGameMessage("game7"));


        this.client4.connect();
        client4.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message4 = this.client4.getMessage();
        String token4 = ((CreatedPlayerMessage) message4).getToken();
        this.client4.setToken(token4);

        this.client4.joinFirstAvailableGame();

        assertMessageEquals(this.client4, new JoinedGameMessage("game7"));

        assertMessageEquals(this.client3, new NewPlayerConnectedToGameMessage("client4"));


        this.client5 = new TestClassClientRMI(virtualMainServer, new MessageHandler(new ActionParser()) ,"client5");
        this.client5.connect();

        this.client5.joinFirstAvailableGame();
        assertMessageEquals(new GameHandlingError(Error.NO_GAMES_FREE_TO_JOIN, null));

    }


    @Test
    public void testDisconnectionWhileInLobby() throws RemoteException {

        this.client1.connect();
        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message = this.client1.getMessage();
        String token1 = ((CreatedPlayerMessage) message).getToken();
        this.client1.setToken(token1);

        this.client2.connect();
        client2.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message2 = this.client2.getMessage();
        String token2 = ((CreatedPlayerMessage) message2).getToken();
        this.client2.setToken(token1);

        this.client2.createGame("game11", 2);

        this.client2.stopSendingHeartbeat();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        TestClassClientRMI client6 = new TestClassClientRMI(virtualMainServer, new MessageHandler(new ActionParser()),"client2");
        client6.connect();
        assertMessageEquals(client6, new GameHandlingError(Error.PLAYER_NAME_ALREADY_IN_USE, null));


        this.client2.reconnect();

        assertMessageEquals(this.client2, new JoinedGameMessage("game11"));

        this.client1.stopSendingHeartbeat();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        this.client1.reconnect();

        this.client1.startSendingHeartbeat();

        assertMessageEquals(this.client1, new AvailableGamesMessage(List.of("game11")));

        this.client1.reconnect();

        assertMessageEquals(this.client1, new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER, null));

        this.client1.startSendingHeartbeat();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        TestClassClientRMI client8 = new TestClassClientRMI(virtualMainServer, new MessageHandler(new ActionParser()) ,"client8");
        client8.connect();
        client8.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message8 = client8.getMessage();
        String token8 = ((CreatedPlayerMessage) message8).getToken();
        client8.setToken(token8);
        client8.reconnect();
        assertMessageEquals(client8, new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER, null));
        client8.disconnect();
    }

    @Test
    public void testDisconnectionWhileInGame() throws RemoteException {
        this.client1.connect();
        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message = this.client1.getMessage();
        String token1 = ((CreatedPlayerMessage) message).getToken();
        this.client1.setToken(token1);

        this.client1.createGame("game6", 2);

        assertMessageEquals(this.client1, new CreatedGameMessage("game6"));


        this.client2.connect();
        client2.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message2 = this.client2.getMessage();
        String token2 = ((CreatedPlayerMessage) message2).getToken();
        this.client2.setToken(token2);

        this.client2.joinGame("game6");

        assertMessageEquals(this.client2, new JoinedGameMessage("game6"));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage("client2"));


        this.client2.stopSendingHeartbeat();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        client2.reconnect();

        this.client2.stopSendingHeartbeat();

        //Situation: client 2 has disconnected from game
        TestClassClientRMI client6 = new TestClassClientRMI(virtualMainServer, new MessageHandler(new ActionParser()),"client6");
        assertMessageEquals(client2, new JoinedGameMessage("game6"));

        this.client2.sendChatMessage(new ArrayList<>(List.of("client1", "client2")), "Chat message after disconnection!");
        assertMessageEquals(new ArrayList<>(List.of(this.client1, this.client2)), new NotifyChatMessage("client2", "Chat message after disconnection!"));

    }

    @Test
    public void testReconnection() throws RemoteException {
        this.client1.connect();

        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message = this.client1.getMessage();
        String token1 = ((CreatedPlayerMessage) message).getToken();

        this.client1.createGame("game15", 2);

        client1.waitForMessage(GameConfigurationMessage.class);

        this.client2.connect();
        this.client2.joinGame("game15");

        client1.waitForMessage(TableConfigurationMessage.class);

        this.client1.stopSendingHeartbeat();

        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        TestClassClientRMI client7 = new TestClassClientRMI(virtualMainServer, new MessageHandler(new ActionParser()),this.client1.getNickname());
        client7.setToken(token1);
        client7.setNickname("client1");
        client7.reconnect();

        assertMessageEquals(client7, new JoinedGameMessage("game15"));

        client7.sendChatMessage(new ArrayList<>(List.of("client2")), "Send chat message after reconnection");

        assertMessageEquals(this.client2, new NotifyChatMessage("client1", "Send chat message after reconnection"));
    }

    private void assertMessageEquals(TestClassClientRMI receiver, MessageToClient message) {
        assertMessageEquals(List.of(receiver), message);
    }

    private void assertMessageEquals(MessageToClient message, TestClassClientRMI... receivers) {
        ArrayList<TestClassClientRMI> receiversName = Arrays.stream(receivers).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        assertMessageEquals(receiversName, message);
    }

    private void assertMessageEquals(List<TestClassClientRMI> receivers, MessageToClient message) {
        List<String> receiversName;
        receiversName = receivers.stream().map(TestClassClientRMI::getNickname).toList();
        message.setHeader(receiversName);
        for (TestClassClientRMI receiver : receivers) {
            receiver.waitForMessage(message.getClass());
            assertEquals(message, receiver.getMessage(message.getClass()));
        }
    }

    private void dummyTurn(ClientInterface clientInterface, CommonClientMethodsForTests client ,PlayableCardType cardType){
        dummyPlace(clientInterface, client);
        clientInterface.pickCardFromDeck(cardType);
    }

    private void dummyFirstTurn(ClientInterface clientInterface, CommonClientMethodsForTests client,PlayableCardType cardType){
        dummyFirstPlace(clientInterface, client);
        clientInterface.pickCardFromDeck(cardType);
    }

    private void dummyFirstPlace(ClientInterface clientInterface, CommonClientMethodsForTests client){
        client.waitForMessage(OwnStationConfigurationMessage.class);
        OwnStationConfigurationMessage latestMessage = (OwnStationConfigurationMessage) client.getMessage(OwnStationConfigurationMessage.class);

        clientInterface.placeCard(latestMessage.getCardsInHand().getFirst().getCardCode(), latestMessage.getInitialCard().getCardCode(), Direction.UP_RIGHT, CardOrientation.DOWN);
        clientsAnchors.put(clientInterface, latestMessage.getCardsInHand().getFirst());
    }

    private void dummyPlace(ClientInterface clientInterface, CommonClientMethodsForTests client){
        AcceptedPickCardMessage latestMessage;
        do {
            client.waitForMessage(AcceptedPickCardMessage.class);
            latestMessage = (AcceptedPickCardMessage) client.getMessage(AcceptedPickCardMessage.class);
        } while (!latestMessage.getNick().equals(clientInterface.getNickname()));

        clientInterface.placeCard(latestMessage.getPickedCard().getCardCode(), clientsAnchors.get(clientInterface).getCardCode(), Direction.UP_RIGHT, CardOrientation.DOWN);
        clientsAnchors.put(clientInterface, latestMessage.getPickedCard());
    }

    private void allPlayersPlacedInitialCard(ClientInterface client1, ClientInterface client2, ClientInterface client3, ClientInterface client4){
        client1.placeInitialCard(CardOrientation.DOWN);
        client2.placeInitialCard(CardOrientation.DOWN);
        client3.placeInitialCard(CardOrientation.UP);
        client4.placeInitialCard(CardOrientation.DOWN);
    }

    private void allPlayersChoosePrivateGoal(ClientInterface client1, ClientInterface client2, ClientInterface client3, ClientInterface client4){
        client1.choosePrivateGoalCard(0);
        client2.choosePrivateGoalCard(1);
        client3.choosePrivateGoalCard(0);
        client4.choosePrivateGoalCard(1);
    }

    private void allPlayersChooseColor(ClientInterface client1, ClientInterface client2, ClientInterface client3, ClientInterface client4){
        client1.chooseColor(Color.RED);
        client2.chooseColor(Color.GREEN);
        client3.chooseColor(Color.BLUE);
        client4.chooseColor(Color.YELLOW);
    }

    private void allPlayersPlacedInitialCard(VirtualGameServer virtualGameServer1, VirtualGameServer virtualGameServer2) throws RemoteException {
        virtualGameServer1.placeInitialCard(CardOrientation.DOWN);
        virtualGameServer2.placeInitialCard(CardOrientation.DOWN);
    }

    private void allPlayersChoosePrivateGoal(VirtualGameServer virtualGameServer1, VirtualGameServer virtualGameServer2) throws RemoteException {
        virtualGameServer1.choosePrivateGoalCard(0);
        virtualGameServer2.choosePrivateGoalCard(1);
    }

    private void allPlayersChooseColor(VirtualGameServer virtualGameServer1, VirtualGameServer virtualGameServer2) throws RemoteException {
        virtualGameServer1.chooseColor(Color.RED);
        virtualGameServer2.chooseColor(Color.GREEN);
    }


}
