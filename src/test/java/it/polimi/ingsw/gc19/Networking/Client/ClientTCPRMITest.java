package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.ClientRMI;
import it.polimi.ingsw.gc19.Networking.Client.ClientTCP.ClientTCP;
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
import it.polimi.ingsw.gc19.Networking.Server.VirtualGameServer;
import it.polimi.ingsw.gc19.Networking.Server.VirtualMainServer;
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
    private HashMap<VirtualGameServer, PlayableCard> clientsAnchorsRMI;
    private HashMap<ClientTCP, PlayableCard> clientsAnchorsTCP;

    private ClientRMI client1, client3, client5;
    private ClientTCP client2, client4;

    @BeforeAll
    public static void setUpServer() throws IOException, NotBoundException {
        ServerApp.startRMI(Settings.DEFAULT_RMI_SERVER_PORT);
        ServerApp.startTCP(Settings.DEFAULT_TCP_SERVER_PORT);
        mainServerRMI = ServerApp.getMainServerRMI();
        registry = LocateRegistry.getRegistry("localhost");
        virtualMainServer = (VirtualMainServer) registry.lookup(Settings.mainRMIServerName);
    }

    @BeforeEach
    public void setUpTest() throws RemoteException {
        this.client1 = new ClientRMI(virtualMainServer, "client1");
        this.client2 = new ClientTCP("client2");
        this.client3 = new ClientRMI(virtualMainServer, "client3");
        this.client4 = new ClientTCP("client4");
        this.client5 = new ClientRMI(virtualMainServer, "client5");
        clientsAnchorsRMI = new HashMap<>();
        clientsAnchorsTCP = new HashMap<>();
    }

    @Test
    public void testClientCreation() throws InterruptedException {
        this.client1.connect();
        assertMessageEquals(this.client1, new CreatedPlayerMessage("client1"));
    }

    @Test
    public void testCreateClient() throws RemoteException, InterruptedException {
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
    public void testCreateGame() throws RemoteException {
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
    public void testMultiplePlayerInGame() throws RemoteException {
        this.client1.connect();

        this.client1.createGame("game3", 3);

        assertMessageEquals(this.client1, new CreatedGameMessage("game3"));


        this.client2.connect();

        this.client2.joinGame("game3");

        assertMessageEquals(this.client2, new JoinedGameMessage("game3"));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage("client2"));


        this.client3.connect();

        this.client3.joinGame("game3");
        VirtualGameServer gameServer3 = this.client3.getVirtualGameServer();

        assertMessageEquals(this.client3, new JoinedGameMessage("game3"));
        assertMessageEquals(List.of(this.client2, this.client1), new NewPlayerConnectedToGameMessage("client3"));


        gameServer3.sendChatMessage(new ArrayList<>(List.of("client1", "client2")), "Message in chat");
        assertMessageEquals(new ArrayList<>(List.of(this.client1, this.client2)), new NotifyChatMessage("client3", "Message in chat"));


        gameServer3.chooseColor(Color.BLUE);
        assertMessageEquals(new ArrayList<>(List.of(this.client3, this.client2, this.client1)), new AcceptedColorMessage("client3", Color.BLUE));
        assertMessageEquals(new ArrayList<>(List.of(this.client2, this.client1)), new AvailableColorsMessage(new ArrayList<>(List.of(Color.GREEN, Color.YELLOW, Color.RED))));
    }

    @Test
    public void testFirePlayersAndGames() throws RemoteException {

        this.client1.connect();
        this.client2.connect();
        this.client3.connect();
        this.client4.connect();

        this.client1.createGame("game13", 4, 1);
        VirtualGameServer gameServer1 = this.client1.getVirtualGameServer();
        this.client2.joinGame("game13");
        this.client3.joinGame("game13");
        this.client4.joinGame("game13");
        assertMessageEquals(this.client2, new JoinedGameMessage("game13"));
        VirtualGameServer gameServer3 = this.client3.getVirtualGameServer();
        assertMessageEquals(this.client4, new JoinedGameMessage("game13"));


        assertNotNull(gameServer1);
        assertNotNull(gameServer3);

        allPlayersChooseColor(gameServer1, client2, gameServer3, client4);

        allPlayersChoosePrivateGoal(gameServer1, client2, gameServer3, client4);

        allPlayersPlacedInitialCard(gameServer1, client2, gameServer3, client4);

        assertMessageWithHeaderEquals(this.client1,  new StartPlayingGameMessage(this.client1.getNickname()), "client1", "client2", "client3", "client4");

        assertMessageWithHeaderEquals(this.client1, new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2", "client3", "client4");

        this.client3.disconnect();

        assertMessageWithHeaderEquals(this.client1, new DisconnectedPlayerMessage("client3"), "client1", "client2", "client4");

        this.client4.disconnect();
        assertMessageWithHeaderEquals(this.client1, new DisconnectedPlayerMessage("client4"), "client1", "client2");


        client1.placeCard("resource_23", "initial_05", Direction.UP_RIGHT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyFirstTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_01", "initial_05", Direction.UP_LEFT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("gold_39", "resource_01", Direction.UP_LEFT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("gold_23", "resource_23", Direction.UP_RIGHT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("gold_40", "gold_23", Direction.UP_LEFT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_05", "gold_39", Direction.UP_RIGHT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_03", "resource_05", Direction.UP_RIGHT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("gold_06", "resource_05", Direction.DOWN_RIGHT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("gold_20", "gold_23", Direction.DOWN_RIGHT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_08", "gold_20", Direction.DOWN_RIGHT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_21", "gold_20", Direction.UP_RIGHT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("gold_28", "resource_08", Direction.DOWN_RIGHT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_30", "gold_28", Direction.UP_RIGHT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_39", "resource_21", Direction.UP_RIGHT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

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
        dummyTurn(client2, PlayableCardType.RESOURCE);

        // now it should be the final round:
        //assertTrue(gameController.getGameAssociated().getFinalCondition());
        //assertTrue(gameController.getGameAssociated().isFinalRound());

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_28", "resource_39", Direction.UP_RIGHT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        // game should end and declare client1 the winner
        //assertEquals(GameState.END, gameController.getGameAssociated().getGameState());
        //assertEquals(client1, gameController.getGameAssociated().getWinnerPlayers().getFirst().getNickname());
        //assertEquals(1, gameController.getGameAssociated().getWinnerPlayers().size());


        waitingThread(4000);

        assertMessageEquals(List.of(this.client2, this.client1), new DisconnectGameMessage("game13"));
    }

    @Test
    public void testPlayerCanJoinFullGame() throws RemoteException {
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
    public void testMultipleGames() throws RemoteException {
        this.client1.connect();

        this.client1.createGame("game8", 2);

        assertMessageEquals(this.client1, new CreatedGameMessage("game8"));

        client1.waitForMessage(TableConfigurationMessage.class);
        client1.clearMessages();


        this.client2.connect();

        this.client2.joinGame("game8");

        assertMessageEquals(this.client2, new JoinedGameMessage("game8"));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage("client2"));


        this.client1.waitForMessage(TableConfigurationMessage.class);
        this.client2.waitForMessage(GameConfigurationMessage.class);
        this.client1.clearMessages();
        this.client2.clearQueue();

        this.client3.connect();

        this.client3.createGame("game9", 2);

        VirtualGameServer gameServer3 = this.client3.getVirtualGameServer();

        assertMessageEquals(this.client3, new CreatedGameMessage("game9"));


        this.client4.connect();

        this.client4.joinGame("game9");

        assertMessageEquals(this.client4, new JoinedGameMessage("game9"));

        assertMessageEquals(this.client3, new NewPlayerConnectedToGameMessage("client4"));


        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());

        gameServer3.sendChatMessage(new ArrayList<>(List.of("client3", "client4")), "Message in chat");
        assertMessageEquals(new ArrayList<>(List.of(this.client3, this.client4)), new NotifyChatMessage("client3", "Message in chat"));

        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());
    }

    @Test
    public void testJoinFirstAvailableGames() throws RemoteException {
        this.client1.connect();

        this.client1.createGame("game4", 2);
        VirtualGameServer gameServer1 = this.client1.getVirtualGameServer();
        assertNotNull(gameServer1);

        assertMessageEquals(this.client1, new CreatedGameMessage("game4"));

        client1.waitForMessage(GameConfigurationMessage.class);
        client1.clearMessages();


        this.client2.connect();

        this.client2.joinGame("game4");
        assertMessageEquals(this.client2, new JoinedGameMessage("game4"));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage("client2"));


        this.client1.waitForMessage(TableConfigurationMessage.class);
        this.client2.waitForMessage(GameConfigurationMessage.class);
        this.client1.clearMessages();
        this.client2.clearQueue();


        this.client3.connect();

        this.client3.createGame("game7", 2);
        VirtualGameServer gameServer3 = this.client3.getVirtualGameServer();
        assertNotNull(gameServer3);

        assertMessageEquals(this.client3, new CreatedGameMessage("game7"));


        this.client4.connect();

        this.client4.joinFirstAvailableGame();
        assertMessageEquals(this.client4, new JoinedGameMessage("game7"));

        assertMessageEquals(this.client3, new NewPlayerConnectedToGameMessage("client4"));


        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());

        this.client5 = new ClientRMI(virtualMainServer, "client5");
        this.client5.connect();

        this.client5.joinFirstAvailableGame();
        VirtualGameServer gameServer5 = this.client5.getVirtualGameServer();
        assertNull(gameServer5);

    }

    @Test
    public void testDisconnectionWhileInLobby() throws RemoteException {

        this.client1.connect();

        this.client2.connect();

        this.client2.createGame("game11", 2);

        this.client2.stopSendingHeartbeat();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        ClientRMI client6 = new ClientRMI(virtualMainServer, "client2");
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

        ClientRMI client7 = new ClientRMI(virtualMainServer, "client7");
        // @todo: how is the reconnect running without any values in names and token (null values only)???
        client7.reconnect();
        assertMessageEquals(client7, new NetworkHandlingErrorMessage(NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER, null));

        ClientRMI client8 = new ClientRMI(virtualMainServer, "client8");
        client8.connect();
        client8.reconnect();
        assertMessageEquals(client8, new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER, null));
    }

    @Test
    public void testDisconnectionWhileInGame() throws RemoteException {
        this.client1.connect();

        this.client1.createGame("game6", 2);
        VirtualGameServer gameServer1 = this.client1.getVirtualGameServer();
        assertNotNull(gameServer1);

        assertMessageEquals(this.client1, new CreatedGameMessage("game6"));


        this.client2.connect();

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
        assertMessageEquals(this.client2, new JoinedGameMessage("game6"));


        this.client2.stopSendingHeartbeat();

        //Situation: client 2 has disconnected from game
        ClientRMI client6 = new ClientRMI(virtualMainServer, "client6");
        client6.reconnect();
        assertMessageEquals(client6, new NetworkHandlingErrorMessage(NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER, null));

        client2.sendChatMessage(new ArrayList<>(List.of("client1", "client2")), "Chat message after disconnection!");
        assertMessageEquals(new ArrayList<>(List.of(this.client1, this.client2)), new NotifyChatMessage("client2", "Chat message after disconnection!"));

    }

    @Test
    public void testReconnection() throws RemoteException {
        this.client1.connect();

        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message = this.client1.getMessage();
        String token1 = ((CreatedPlayerMessage) message).getToken();

        this.client1.createGame("game15", 2);
        VirtualGameServer gameServer1 = this.client1.getVirtualGameServer();

        client1.waitForMessage(GameConfigurationMessage.class);
        client1.clearMessages();

        this.client2.connect();
        this.client2.joinGame("game15");

        assertMessageEquals(this.client2, new JoinedGameMessage("game15"));

        client1.waitForMessage(TableConfigurationMessage.class);
        client1.clearMessages();

        this.client1.stopSendingHeartbeat();

        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        ClientRMI client7 = new ClientRMI(virtualMainServer, "client7");
        client7.setToken(token1);
        client7.setNickname("client1");
        client7.reconnect();
        VirtualGameServer gameServer7 = client7.getVirtualGameServer();

        assertMessageEquals(client7, new JoinedGameMessage("game15"));

        assertNotEquals(gameServer1, gameServer7);


        gameServer7.sendChatMessage(new ArrayList<>(List.of("client2")), "Send chat message after reconnection");

        assertMessageEquals(this.client2, new NotifyChatMessage("client1", "Send chat message after reconnection"));
        assertNull(this.client1.getMessage());
    }

    @AfterEach
    public void resetClients() throws RemoteException {
        this.client1.disconnect();
        this.client2.disconnect();
        this.client2.stopClient();
        this.client3.disconnect();
        this.client4.disconnect();
        this.client4.stopClient();
        mainServerRMI.resetServer();
    }


    @AfterAll
    public static void tearDownServer() {
        ServerApp.unexportRegistry();
        ServerApp.stopTCP();
    }


    private void assertMessageEquals(ClientRMI receiver, MessageToClient message) {
        assertMessageEquals(List.of(receiver), message);
    }

    private void assertMessageEquals(MessageToClient message, ClientRMI... receivers) {
        ArrayList<ClientInterface> receiversName = Arrays.stream(receivers).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        assertMessageEquals(receiversName, message);
    }

    private void assertMessageEquals(List<ClientInterface> receivers, MessageToClient message) {
        List<String> receiversName;
        receiversName = receivers.stream().map(ClientInterface::getNickname).toList();
        message.setHeader(receiversName);
        for (ClientInterface receiver : receivers) {
            receiver.waitForMessage(message.getClass());
            assertEquals(message, receiver.getMessage(message.getClass()));
        }
    }

    private void dummyTurn(VirtualGameServer virtualGameServer, ClientRMI client, PlayableCardType cardType) throws RemoteException {
        dummyPlace(virtualGameServer, client);
        virtualGameServer.pickCardFromDeck(cardType);
    }

    private void dummyFirstTurn(VirtualGameServer virtualGameServer, ClientRMI client, PlayableCardType cardType) throws RemoteException {
        dummyFirstPlace(virtualGameServer, client);
        virtualGameServer.pickCardFromDeck(cardType);
    }

    private void dummyFirstPlace(VirtualGameServer virtualGameServer, ClientRMI client) throws RemoteException {
        client.waitForMessage(OwnStationConfigurationMessage.class);
        OwnStationConfigurationMessage latestMessage = (OwnStationConfigurationMessage) client.getMessage(OwnStationConfigurationMessage.class);

        virtualGameServer.placeCard(latestMessage.getCardsInHand().getFirst().getCardCode(), latestMessage.getInitialCard().getCardCode(), Direction.UP_RIGHT, CardOrientation.DOWN);
        clientsAnchorsRMI.put(virtualGameServer, latestMessage.getCardsInHand().getFirst());
    }

    private void dummyPlace(VirtualGameServer virtualGameServer, ClientRMI client) throws RemoteException {
        AcceptedPickCardMessage latestMessage;
        do {
            client.waitForMessage(AcceptedPickCardMessage.class);
            latestMessage = (AcceptedPickCardMessage) client.getMessage(AcceptedPickCardMessage.class);
        } while (!latestMessage.getNick().equals(client.getNickname()));

        virtualGameServer.placeCard(latestMessage.getPickedCard().getCardCode(), clientsAnchorsRMI.get(virtualGameServer).getCardCode(), Direction.UP_RIGHT, CardOrientation.DOWN);
        clientsAnchorsRMI.put(virtualGameServer, latestMessage.getPickedCard());
    }

    private void allPlayersPlacedInitialCard(VirtualGameServer virtualGameServer1, VirtualGameServer virtualGameServer2, VirtualGameServer virtualGameServer3, VirtualGameServer virtualGameServer4) throws RemoteException {
        virtualGameServer1.placeInitialCard(CardOrientation.DOWN);
        virtualGameServer2.placeInitialCard(CardOrientation.DOWN);
        virtualGameServer3.placeInitialCard(CardOrientation.UP);
        virtualGameServer4.placeInitialCard(CardOrientation.DOWN);
    }

    private void allPlayersChoosePrivateGoal(VirtualGameServer virtualGameServer1, VirtualGameServer virtualGameServer2, VirtualGameServer virtualGameServer3, VirtualGameServer virtualGameServer4) throws RemoteException {
        virtualGameServer1.choosePrivateGoalCard(0);
        virtualGameServer2.choosePrivateGoalCard(1);
        virtualGameServer3.choosePrivateGoalCard(0);
        virtualGameServer4.choosePrivateGoalCard(1);
    }

    private void allPlayersChooseColor(VirtualGameServer virtualGameServer1, VirtualGameServer virtualGameServer2, VirtualGameServer virtualGameServer3, VirtualGameServer virtualGameServer4) throws RemoteException {
        virtualGameServer1.chooseColor(Color.RED);
        virtualGameServer2.chooseColor(Color.GREEN);
        virtualGameServer3.chooseColor(Color.BLUE);
        virtualGameServer4.chooseColor(Color.YELLOW);
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

    private void dummyTurn(ClientTCP client, PlayableCardType cardType) throws RemoteException {
        dummyPlace(client);
        assertMessageEquals(List.of(this.client1, client2), new TurnStateMessage(this.client2.getNickname(), TurnState.DRAW));
        client.pickCardFromDeck(cardType);
    }

    private void dummyFirstTurn(ClientTCP client, PlayableCardType cardType){
        dummyFirstPlace(client);
        assertMessageEquals(List.of(this.client1, client2), new TurnStateMessage(this.client2.getNickname(), TurnState.DRAW));
        client.pickCardFromDeck(cardType);
    }

    private void dummyFirstPlace(ClientTCP client){
        client.waitForMessage(OwnStationConfigurationMessage.class);
        OwnStationConfigurationMessage latestMessage = (OwnStationConfigurationMessage) client.getMessage(OwnStationConfigurationMessage.class);

        // remove other client turns
        assertMessageWithHeaderEquals(client, new TurnStateMessage("client1", TurnState.PLACE), "client1", "client2", "client3", "client4");
        assertMessageWithHeaderEquals(client, new TurnStateMessage("client1", TurnState.DRAW), "client1", "client2");

        assertMessageEquals(List.of(this.client1, client2), new TurnStateMessage(this.client2.getNickname(), TurnState.PLACE));

        client.placeCard(latestMessage.getCardsInHand().getFirst().getCardCode(), latestMessage.getInitialCard().getCardCode(), Direction.UP_RIGHT, CardOrientation.DOWN);

        clientsAnchorsTCP.put(client, latestMessage.getCardsInHand().getFirst());
    }

    private void dummyPlace(ClientTCP client){
        AcceptedPickCardMessage latestMessage;
        do {
            System.out.println(client.getNickname());
            client.waitForMessage(AcceptedPickCardMessage.class);
            latestMessage = (AcceptedPickCardMessage) client.getMessage(AcceptedPickCardMessage.class);
        } while (!latestMessage.getNick().equals(client.getNickname()));

        assertMessageWithHeaderEquals(client, new TurnStateMessage("client1", TurnState.PLACE), "client1", "client2");
        assertMessageWithHeaderEquals(client, new TurnStateMessage("client1", TurnState.DRAW), "client1", "client2");

        assertMessageEquals(List.of(this.client1, client2), new TurnStateMessage(this.client2.getNickname(), TurnState.PLACE));

        System.out.println("Cosa: " + latestMessage.getPickedCard().getCardCode());
        System.out.println("Cosa: " + clientsAnchorsTCP.get(client).getCardCode());
        client.placeCard(latestMessage.getPickedCard().getCardCode(), clientsAnchorsTCP.get(client).getCardCode(), Direction.UP_RIGHT, CardOrientation.DOWN);
        clientsAnchorsTCP.put(client, latestMessage.getPickedCard());
    }

    private void allPlayersPlacedInitialCard(ClientTCP client1, ClientTCP client2, ClientTCP client3, ClientTCP client4){
        client1.placeInitialCard(CardOrientation.DOWN);
        client2.placeInitialCard(CardOrientation.DOWN);
        client3.placeInitialCard(CardOrientation.UP);
        client4.placeInitialCard(CardOrientation.DOWN);
    }

    private void allPlayersChoosePrivateGoal(ClientTCP client1, ClientTCP client2, ClientTCP client3, ClientTCP client4) {
        client1.choosePrivateGoalCard(0);
        client2.choosePrivateGoalCard(1);
        client3.choosePrivateGoalCard(0);
        client4.choosePrivateGoalCard(1);
    }

    private void allPlayersChooseColor(ClientTCP client1, ClientTCP client2, ClientTCP client3, ClientTCP client4){
        client1.chooseColor(Color.RED);
        client2.chooseColor(Color.GREEN);
        client3.chooseColor(Color.BLUE);
        client4.chooseColor(Color.YELLOW);
    }

    private void allPlayersChooseColor(VirtualGameServer gameServer1, ClientTCP client2, VirtualGameServer gameServer3, ClientTCP client4) throws RemoteException {
        gameServer1.chooseColor(Color.RED);
        client2.chooseColor(Color.GREEN);
        gameServer3.chooseColor(Color.BLUE);
        client4.chooseColor(Color.YELLOW);
    }
    private void allPlayersChoosePrivateGoal(VirtualGameServer gameServer1, ClientTCP client2, VirtualGameServer gameServer3, ClientTCP client4) throws RemoteException {
        gameServer1.choosePrivateGoalCard(0);
        client2.choosePrivateGoalCard(1);
        gameServer3.choosePrivateGoalCard(0);
        client4.choosePrivateGoalCard(1);
    }

    private void allPlayersPlacedInitialCard(VirtualGameServer gameServer1, ClientTCP client2, VirtualGameServer gameServer3, ClientTCP client4) throws RemoteException{
        gameServer1.placeInitialCard(CardOrientation.DOWN);
        client2.placeInitialCard(CardOrientation.DOWN);
        gameServer3.placeInitialCard(CardOrientation.UP);
        client4.placeInitialCard(CardOrientation.DOWN);
    }


    private void assertMessageEquals(ClientTCP receiver, MessageToClient message) {
        assertMessageEquals(List.of(receiver), message);
    }

    private void assertMessageEquals(MessageToClient message, ClientTCP... receivers) {
        ArrayList<ClientInterface> receiversName = Arrays.stream(receivers).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        assertMessageEquals(receiversName, message);
    }


    private void assertMessageWithHeaderEquals(ClientInterface receiver, MessageToClient message, String ... header) {
        assertMessageWithHeaderEquals(List.of(receiver), message, header);
    }

    private void assertMessageWithHeaderEquals(List<ClientInterface> receivers, MessageToClient message, String ... header) {
        message.setHeader(Arrays.stream(header).toList());
        for (ClientInterface receiver : receivers) {
            receiver.waitForMessage(message.getClass());
            assertEquals(message, receiver.getMessage(message.getClass()));
        }
    }

    private void clearQueue(List<ClientTCP> clients) {
        for (ClientTCP player : clients) {
            player.clearQueue();
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
