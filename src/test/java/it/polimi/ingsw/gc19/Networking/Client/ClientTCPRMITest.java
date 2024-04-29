package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedColorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedPickCardMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.GameConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OwnStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.TableConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.AvailableColorsMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.DisconnectedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.NewPlayerConnectedToGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.StartPlayingGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkError;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
import it.polimi.ingsw.gc19.Networking.Server.ServerApp;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.MainServerRMI;
import it.polimi.ingsw.gc19.Networking.Server.Settings;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.VirtualMainServer;
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

public class ClientTCPRMITest {
    private static VirtualMainServer virtualMainServer;
    private static Registry registry;
    private static MainServerRMI mainServerRMI;

    // Hashmap to save the get the anchor for the placeCard.
    private HashMap<ClientInterface, PlayableCard> clientsAnchors;

    private TestClassClientRMI client1, client3, client5;
    private TestClassClientTCP client2, client4;

    private ActionParser actionParser1, actionParser2, actionParser3, actionParser4, actionParser5;

    @BeforeEach
    public void setUpTest() throws IOException, NotBoundException {
        ServerApp.startRMI(Settings.DEFAULT_RMI_SERVER_PORT);
        ServerApp.startTCP(Settings.DEFAULT_TCP_SERVER_PORT);
        mainServerRMI = ServerApp.getMainServerRMI();
        registry = LocateRegistry.getRegistry("localhost");
        virtualMainServer = (VirtualMainServer) registry.lookup(Settings.mainRMIServerName);

        actionParser1 = new ActionParser("client1");
        actionParser2 = new ActionParser("client1");
        actionParser3 = new ActionParser("client1");
        actionParser4 = new ActionParser("client1");
        actionParser5 = new ActionParser("client1");

        this.client1 = new TestClassClientRMI(virtualMainServer, new MessageHandler(actionParser1),"client1", actionParser1);
        this.client2 = new TestClassClientTCP("client2", new MessageHandler(actionParser1), actionParser2);
        this.client3 = new TestClassClientRMI(virtualMainServer, new MessageHandler(actionParser3),"client3", actionParser3);
        this.client4 = new TestClassClientTCP("client4", new MessageHandler(actionParser4), actionParser4);
        this.client5 = new TestClassClientRMI(virtualMainServer, new MessageHandler(actionParser5) ,"client5", actionParser5);
        clientsAnchors = new HashMap<>();
    }

    @AfterEach
    public void tearDown(){
        this.client1.stopClient();
        this.client2.stopClient();
        this.client3.stopClient();
        this.client4.stopClient();
        this.client5.stopClient();
        ServerApp.stopRMI();
        ServerApp.stopTCP();
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

        this.client1.disconnect();
        this.client2.disconnect();
        this.client3.disconnect();
        this.client4.disconnect();
        this.client5.disconnect();
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

        this.client1.disconnect();
        this.client2.disconnect();
    }


    @Test
    public void testMultiplePlayerInGame(){
        this.client1.connect();

        this.client1.createGame("game3", 3);

        assertMessageEquals(this.client1, new CreatedGameMessage("game3"));

        //this.client1.setVirtualMainServer(virtualMainServer);
        this.client2.connect();
        this.client2.joinGame("game3");
        assertMessageEquals(this.client2, new JoinedGameMessage("game3"));
        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage("client2"));

        //this.client1.setVirtualMainServer(virtualMainServer);
        this.client3.connect();

        this.client3.joinGame("game3");

        assertMessageEquals(this.client3, new JoinedGameMessage("game3"));
        assertMessageEquals(List.of(this.client2, this.client1), new NewPlayerConnectedToGameMessage("client3"));

        client3.sendChatMessage(new ArrayList<>(List.of("client1", "client2")), "Message in chat");
        assertMessageEquals(new ArrayList<>(List.of(this.client1, this.client2)), new NotifyChatMessage("client3", "Message in chat"));

        client3.chooseColor(Color.BLUE);
        assertMessageEquals(new ArrayList<>(List.of(this.client3, this.client2, this.client1)), new AcceptedColorMessage("client3", Color.BLUE));
        assertMessageEquals(new ArrayList<>(List.of(this.client2, this.client1)), new AvailableColorsMessage(new ArrayList<>(List.of(Color.GREEN, Color.YELLOW, Color.RED))));

        this.client1.disconnect();
        this.client2.disconnect();
        this.client3.disconnect();
    }


    @Test
    public void testDisconnectionAndReconnection() throws IOException {
        TestClassClientRMI client6 = new TestClassClientRMI(virtualMainServer, new MessageHandler(new ActionParser()),"client6", new ActionParser());
        client6.connect();
        client6.startSendingHeartbeat();
        waitingThread(2500);
        client6.disconnect();
        TestClassClientTCP client7 = new TestClassClientTCP(client6.getNickname(), new MessageHandler(new ActionParser()), new ActionParser());
        client7.connect();
        assertMessageEquals(client7, new CreatedPlayerMessage(client7.getNickname()));

        client6.disconnect();
        client7.disconnect();
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
        assertMessageEquals(this.client2, new JoinedGameMessage("game13"));
        assertMessageEquals(this.client3, new JoinedGameMessage("game13"));
        assertMessageEquals(this.client4, new JoinedGameMessage("game13"));

        allPlayersChooseColor(client1, client2, client3, client4);

        allPlayersChoosePrivateGoal(client1, client2, client3, client4);

        allPlayersPlacedInitialCard(client1, client2, client3, client4);

        assertMessageWithHeaderEquals(this.client1, new StartPlayingGameMessage(this.client1.getNickname()), "client1", "client2", "client3", "client4");

        assertMessageWithHeaderEquals(this.client1, new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2", "client3", "client4");

        this.client3.disconnect();
        assertMessageWithHeaderEquals(this.client1, new DisconnectedPlayerMessage("client3"), "client1", "client2", "client4");

        this.client4.disconnect();
        assertMessageWithHeaderEquals(this.client1, new DisconnectedPlayerMessage("client4"), "client1", "client2");

        client1.placeCard("resource_23", "initial_05", Direction.UP_RIGHT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyFirstTurn(client2, client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_01", "initial_05", Direction.UP_LEFT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(client2, client2,PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("gold_39", "resource_01", Direction.UP_LEFT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(client2, client2,PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("gold_23", "resource_23", Direction.UP_RIGHT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(client2, client2,PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("gold_40", "gold_23", Direction.UP_LEFT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, client2,PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_05", "gold_39", Direction.UP_RIGHT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, client2,PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_03", "resource_05", Direction.UP_RIGHT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, client2,PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("gold_06", "resource_05", Direction.DOWN_RIGHT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(client2, client2,PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("gold_20", "gold_23", Direction.DOWN_RIGHT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, client2,PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_08", "gold_20", Direction.DOWN_RIGHT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 1);

        dummyTurn(client2, client2,PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_21", "gold_20", Direction.UP_RIGHT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, client2,PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("gold_28", "resource_08", Direction.DOWN_RIGHT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, client2,PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_30", "gold_28", Direction.UP_RIGHT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 0);

        dummyTurn(client2, client2,PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_39", "resource_21", Direction.UP_RIGHT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(client2, client2,PlayableCardType.RESOURCE);

        //assertFalse(gameController.getGameAssociated().getFinalCondition());
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("gold_24", "resource_21", Direction.DOWN_RIGHT, CardOrientation.UP);
        // client1 reached 20 points: final condition should be true, but not in final round
        //assertTrue(gameController.getGameAssociated().getFinalCondition());
        //assertFalse(gameController.getGameAssociated().isFinalRound());
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        //assertTrue(gameController.getGameAssociated().getFinalCondition());
        //assertFalse(gameController.getGameAssociated().isFinalRound());
        dummyTurn(client2, client2,PlayableCardType.RESOURCE);

        // now it should be the final round:
        //assertTrue(gameController.getGameAssociated().getFinalCondition());
        //assertTrue(gameController.getGameAssociated().isFinalRound());

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_28", "resource_39", Direction.UP_RIGHT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 1);

        dummyTurn(client2, client2,PlayableCardType.RESOURCE);

        // game should end and declare client1 the winner
        //assertEquals(GameState.END, gameController.getGameAssociated().getGameState());
        //assertEquals(client1, gameController.getGameAssociated().getWinnerPlayers().getFirst().getNickname());
        //assertEquals(1, gameController.getGameAssociated().getWinnerPlayers().size());


        waitingThread(4000);

        assertMessageEquals(List.of(this.client1, this.client2), new DisconnectFromGameMessage("game13"));

        this.client1.disconnect();
        this.client2.disconnect();
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

        this.client1.disconnect();
        this.client2.disconnect();
        this.client3.disconnect();
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

        client3.sendChatMessage(new ArrayList<>(List.of("client3", "client4")), "Message in chat");
        assertMessageEquals(new ArrayList<>(List.of(this.client3, this.client4)), new NotifyChatMessage("client3", "Message in chat"));

        this.client1.disconnect();
        this.client2.disconnect();
        this.client3.disconnect();
        this.client4.disconnect();
    }

    @Test
    public void testJoinFirstAvailableGames() throws RemoteException {
        this.client1.connect();

        this.client1.createGame("game4", 2);

        assertMessageEquals(this.client1, new CreatedGameMessage("game4"));

        client1.waitForMessage(GameConfigurationMessage.class);

        this.client2.connect();

        this.client2.joinGame("game4");
        assertMessageEquals(this.client2, new JoinedGameMessage("game4"));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage("client2"));


        this.client1.waitForMessage(TableConfigurationMessage.class);
        this.client2.waitForMessage(GameConfigurationMessage.class);

        this.client3.connect();

        this.client3.createGame("game7", 2);

        assertMessageEquals(this.client3, new CreatedGameMessage("game7"));


        this.client4.connect();

        this.client4.joinFirstAvailableGame();
        assertMessageEquals(this.client4, new JoinedGameMessage("game7"));

        assertMessageEquals(this.client3, new NewPlayerConnectedToGameMessage("client4"));

        this.client5 = new TestClassClientRMI(virtualMainServer, new MessageHandler(new ActionParser()),"client5", new ActionParser());
        this.client5.connect();

        this.client5.joinFirstAvailableGame();

        this.client1.disconnect();
        this.client2.disconnect();
        this.client3.disconnect();
        this.client4.disconnect();
        this.client5.disconnect();
    }


    @Test
    public void testDisconnectionWhileInLobby() throws RemoteException {
        this.client1.connect();
        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message1 = this.client1.getMessage();
        String token1 = ((CreatedPlayerMessage) message1).getToken();
        this.client1.setToken(token1);

        this.client2.connect();
        client2.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message2 = this.client2.getMessage();
        String token2 = ((CreatedPlayerMessage) message2).getToken();
        this.client2.setToken(token2);

        this.client2.createGame("game11", 2);

        this.client2.stopSendingHeartbeat();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        TestClassClientRMI client6 = new TestClassClientRMI(virtualMainServer, new MessageHandler(new ActionParser()),"client2", new ActionParser());
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

        TestClassClientRMI client8 = new TestClassClientRMI(virtualMainServer, new MessageHandler(new ActionParser()),"client8", new ActionParser());
        client8.connect();
        client8.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message8 = client8.getMessage();
        String token8 = ((CreatedPlayerMessage) message8).getToken();
        client8.setToken(token8);
        client8.reconnect();
        assertMessageEquals(client8, new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER, null));

        this.client1.disconnect();
        this.client2.disconnect();
        client8.disconnect();
    }


    @Test
    public void testDisconnectionWhileInGame() throws RemoteException {
        this.client1.connect();

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
        client2.startSendingHeartbeat();
        waitingThread(500);
        assertMessageEquals(this.client2, new JoinedGameMessage("game6"));

        this.client2.stopSendingHeartbeat();

        //Situation: client 2 has disconnected from game
        TestClassClientRMI client6 = new TestClassClientRMI(virtualMainServer, new MessageHandler(new ActionParser()),"client6", new ActionParser());
        //Client6 tries to reconnect with no token
        client6.setToken("fake token");
        client6.reconnect();
        assertMessageEquals(client6, new NetworkHandlingErrorMessage(NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER, null));

        client2.sendChatMessage(new ArrayList<>(List.of("client1", "client2")), "Chat message after disconnection!");
        assertMessageEquals(new ArrayList<>(List.of(this.client1, this.client2)), new NotifyChatMessage("client2", "Chat message after disconnection!"));

        client6.disconnect();
        this.client1.disconnect();
        this.client2.disconnect();
    }

    @Test
    public void testExitFromGame(){
        this.client1.connect();
        this.client2.connect();
        this.client1.createGame("game11", 3);
        waitingThread(500);
        this.client2.joinFirstAvailableGame();
        this.client1.logoutFromGame();
        assertMessageEquals(this.client2, new DisconnectedPlayerMessage(this.client1.getNickname()));
        this.client2.logoutFromGame();
        this.client3.connect();
        this.client3.availableGames();
        assertMessageEquals(this.client3, new AvailableGamesMessage(List.of()));
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

        assertMessageEquals(this.client2, new JoinedGameMessage("game15"));

        client1.waitForMessage(TableConfigurationMessage.class);

        this.client1.stopSendingHeartbeat();

        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        TestClassClientRMI client7 = new TestClassClientRMI(virtualMainServer, new MessageHandler(new ActionParser()), "client7", new ActionParser());
        client7.setNickname("client1");
        client7.setToken(token1);
        client7.reconnect();

        assertMessageEquals(client7, new JoinedGameMessage("game15"));

        client7.sendChatMessage(new ArrayList<>(List.of("client2")), "Send chat message after reconnection");

        assertMessageEquals(this.client2, new NotifyChatMessage("client1", "Send chat message after reconnection"));

        this.client1.disconnect();
        this.client2.disconnect();
        client7.disconnect();
    }

    private void assertMessageEquals(List<CommonClientMethodsForTests> receivers, MessageToClient message) {
        List<String> receiversName = receivers.stream().map(CommonClientMethodsForTests::getNickname).toList();
        message.setHeader(receiversName);
        for (CommonClientMethodsForTests receiver : receivers) {
            receiver.waitForMessage(message.getClass());
            assertEquals(message, receiver.getMessage(message.getClass()));
        }
    }

    private void dummyFirstPlace(ClientInterface client, CommonClientMethodsForTests commonAction){
        commonAction.waitForMessage(OwnStationConfigurationMessage.class);
        OwnStationConfigurationMessage latestMessage = (OwnStationConfigurationMessage) commonAction.getMessage(OwnStationConfigurationMessage.class);

        // remove other client turns
        assertMessageWithHeaderEquals(commonAction, new TurnStateMessage("client1", TurnState.PLACE), "client1", "client2", "client3", "client4");
        assertMessageWithHeaderEquals(commonAction, new TurnStateMessage("client1", TurnState.DRAW), "client1", "client2");

        assertMessageEquals(List.of(this.client1, client2), new TurnStateMessage(this.client2.getNickname(), TurnState.PLACE));

        client.placeCard(latestMessage.getCardsInHand().getFirst().getCardCode(), latestMessage.getInitialCard().getCardCode(), Direction.UP_RIGHT, CardOrientation.DOWN);
        clientsAnchors.put(client, latestMessage.getCardsInHand().getFirst());
    }

    private void dummyTurn(ClientInterface client, CommonClientMethodsForTests commonAction, PlayableCardType cardType){
        dummyPlace(client, commonAction);
        assertMessageEquals(List.of(this.client1, client2), new TurnStateMessage(this.client2.getNickname(), TurnState.DRAW));
        client.pickCardFromDeck(cardType);
    }

    private void dummyFirstTurn(ClientInterface client, CommonClientMethodsForTests commonAction, PlayableCardType cardType){
        dummyFirstPlace(client, commonAction);
        assertMessageEquals(List.of(this.client1, client2), new TurnStateMessage(this.client2.getNickname(), TurnState.DRAW));
        client.pickCardFromDeck(cardType);
    }

    private void dummyPlace(ClientInterface client, CommonClientMethodsForTests commonAction){
        AcceptedPickCardMessage latestMessage;
        do {
            commonAction.waitForMessage(AcceptedPickCardMessage.class);
            latestMessage = (AcceptedPickCardMessage) commonAction.getMessage(AcceptedPickCardMessage.class);
        } while (!latestMessage.getNick().equals(client.getNickname()));

        assertMessageWithHeaderEquals(commonAction, new TurnStateMessage("client1", TurnState.PLACE), "client1", "client2");
        assertMessageWithHeaderEquals(commonAction, new TurnStateMessage("client1", TurnState.DRAW), "client1", "client2");

        assertMessageEquals(List.of(this.client1, client2), new TurnStateMessage(this.client2.getNickname(), TurnState.PLACE));

        client.placeCard(latestMessage.getPickedCard().getCardCode(), clientsAnchors.get(client).getCardCode(), Direction.UP_RIGHT, CardOrientation.DOWN);
        clientsAnchors.put(client, latestMessage.getPickedCard());
    }

    private void allPlayersChooseColor(ClientInterface clientInterface1, ClientInterface clientInterface2, ClientInterface clientInterface3, ClientInterface clientInterface4){
        clientInterface1.chooseColor(Color.RED);
        clientInterface2.chooseColor(Color.GREEN);
        clientInterface3.chooseColor(Color.BLUE);
        clientInterface4.chooseColor(Color.YELLOW);
    }

    private void allPlayersChoosePrivateGoal(ClientInterface clientInterface1, ClientInterface clientInterface2, ClientInterface clientInterface3, ClientInterface clientInterface4){
        clientInterface1.choosePrivateGoalCard(0);
        clientInterface2.choosePrivateGoalCard(1);
        clientInterface3.choosePrivateGoalCard(0);
        clientInterface4.choosePrivateGoalCard(1);
    }

    private void allPlayersPlacedInitialCard(ClientInterface clientInterface1, ClientInterface clientInterface2, ClientInterface clientInterface3, ClientInterface clientInterface4){
        clientInterface1.placeInitialCard(CardOrientation.DOWN);
        clientInterface2.placeInitialCard(CardOrientation.DOWN);
        clientInterface3.placeInitialCard(CardOrientation.UP);
        clientInterface4.placeInitialCard(CardOrientation.DOWN);
    }

    private void assertMessageEquals(CommonClientMethodsForTests receiver, MessageToClient message) {
        assertMessageEquals(List.of(receiver), message);
    }

    private void assertMessageEquals(MessageToClient message, CommonClientMethodsForTests... receivers) {
        ArrayList<CommonClientMethodsForTests> receiversName = Arrays.stream(receivers).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        assertMessageEquals(receiversName, message);
    }


    private void assertMessageWithHeaderEquals(CommonClientMethodsForTests receiver, MessageToClient message, String ... header) {
        assertMessageWithHeaderEquals(List.of(receiver), message, header);
    }

    private void assertMessageWithHeaderEquals(List<CommonClientMethodsForTests> receivers, MessageToClient message, String ... header) {
        message.setHeader(Arrays.stream(header).toList());
        for (CommonClientMethodsForTests receiver : receivers) {
            receiver.waitForMessage(message.getClass());
            assertEquals(message, receiver.getMessage(message.getClass()));
        }
    }

    private void waitingThread(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
