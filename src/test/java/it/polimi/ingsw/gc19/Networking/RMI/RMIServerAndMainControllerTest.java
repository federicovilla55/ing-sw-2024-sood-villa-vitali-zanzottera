package it.polimi.ingsw.gc19.Networking.RMI;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedColorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedPickCardMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.GameConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OwnStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.TableConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.AvailableColorsMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.DisconnectedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.NewPlayerConnectedToGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkError;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.ServerApp;
import it.polimi.ingsw.gc19.Networking.Server.ServerSettings;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.VirtualGameServer;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.VirtualMainServer;
import org.junit.jupiter.api.*;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class RMIServerAndMainControllerTest {
    private static VirtualMainServer virtualMainServer;
    private Client client1, client2, client3, client4, client5;
    private ArrayList<Client> stressTestClients;
    private long maxTimeBeforeDisconnection, maxTimeBeforeReEnteringLobby, prevHeartBeatSetting;

    @BeforeEach
    public void setUpTest() throws RemoteException, NotBoundException {
        maxTimeBeforeDisconnection = ServerSettings.TIME_TO_WAIT_BEFORE_IN_GAME_CLIENT_DISCONNECTION;
        prevHeartBeatSetting = ServerSettings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS;
        maxTimeBeforeReEnteringLobby = ServerSettings.TIME_TO_WAIT_BEFORE_IN_GAME_CLIENT_DISCONNECTION;

        ServerSettings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS = 1;
        ServerSettings.TIME_TO_WAIT_BEFORE_IN_GAME_CLIENT_DISCONNECTION = 3;
        ServerSettings.TIME_TO_WAIT_BEFORE_CLIENT_HANDLER_KILL = 20;

        ServerApp.startRMI();

        Registry registry = LocateRegistry.getRegistry("localhost");
        virtualMainServer = (VirtualMainServer) registry.lookup(ServerSettings.MAIN_RMI_SERVER_NAME);

        this.client1 = new Client(virtualMainServer, "client1");
        this.client2 = new Client(virtualMainServer, "client2");
        this.client3 = new Client(virtualMainServer, "client3");
        this.client4 = new Client(virtualMainServer, "client4");
        this.client5 = new Client(virtualMainServer, "client5");

        this.stressTestClients = overloadTest(5);
    }

    @AfterEach
    public void tearDown() throws RemoteException {
        this.client1.disconnect();
        this.client1.destroyHeartBeatThread();
        this.client2.disconnect();
        this.client2.destroyHeartBeatThread();
        this.client3.disconnect();
        this.client3.destroyHeartBeatThread();
        this.client4.disconnect();
        this.client4.destroyHeartBeatThread();
        this.client5.disconnect();
        this.client5.destroyHeartBeatThread();

        this.killStressTestClients();

        ServerApp.stopRMI();

        ServerSettings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS = prevHeartBeatSetting;
        ServerSettings.TIME_TO_WAIT_BEFORE_IN_GAME_CLIENT_DISCONNECTION = maxTimeBeforeReEnteringLobby;
        ServerSettings.TIME_TO_WAIT_BEFORE_CLIENT_HANDLER_KILL = maxTimeBeforeDisconnection;
    }

    private static ArrayList<Client> overloadTest(int numberOfClients) throws RemoteException {
        ArrayList<Client> stressTestClients = new ArrayList<>();
        for(int i = 0; i < numberOfClients; i++){
            Client client = new Client(virtualMainServer, "client overload " + Integer.toString(i));
            client.connect();
            stressTestClients.add(client);
        }
        return stressTestClients;
    }

    private void killStressTestClients() throws RemoteException {
        for(Client c : this.stressTestClients){
            c.disconnect();
            c.stopSendingHeartBeat();
        }
    }

    @Test
    public void testCreateClient() throws RemoteException {
        this.client1.connect();

        assertMessageEquals(this.client1, new CreatedPlayerMessage(this.client1.getName()));
        this.client2.connect();

        assertMessageEquals(this.client2, new CreatedPlayerMessage(this.client2.getName()));
        assertNull(this.client1.getMessage());
        this.client3.connect();

        assertMessageEquals(this.client3, new CreatedPlayerMessage(this.client3.getName()));
        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());
        this.client4.connect();

        assertMessageEquals(this.client4, new CreatedPlayerMessage(this.client4.getName()));
        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());
        assertNull(this.client3.getMessage());

        //Create new client with other name
        this.client5.setName("client1");
        this.client5.connect();

        assertMessageEquals(this.client5, new GameHandlingErrorMessage(Error.PLAYER_NAME_ALREADY_IN_USE, null));
    }

    @Test
    public void testCreateGame() throws RemoteException {
        this.client1.connect();

        this.client1.newGame("game1", 2);

        assertMessageEquals(this.client1, new CreatedGameMessage("game1").setHeader(this.client1.getName()));

        //Player already registered to some games

        this.client1.newGame("game2", 2);

        assertMessageEquals(this.client1, new GameHandlingErrorMessage(Error.PLAYER_ALREADY_REGISTERED_TO_SOME_GAME, null));

        //Player already registered and game name equal

        this.client1.newGame("game1", 2);

        assertMessageEquals(this.client1, new GameHandlingErrorMessage(Error.PLAYER_ALREADY_REGISTERED_TO_SOME_GAME, null));

        //Game name already in use
        this.client2.connect();

        this.client2.newGame("game1", 2);

        assertMessageEquals(this.client2, new GameHandlingErrorMessage(Error.GAME_NAME_ALREADY_IN_USE, null));
    }

    @Test
    public void testMultiplePlayerInGame() throws RemoteException {
        this.client1.connect();

        this.client1.newGame("game3", 3);

        assertMessageEquals(this.client1, new CreatedGameMessage("game3").setHeader(this.client1.getName()));


        this.client2.connect();

        this.client2.joinGame("game3");

        assertMessageEquals(this.client2, new JoinedGameMessage("game3").setHeader(this.client2.getName()));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getName()));


        this.client3.connect();

        VirtualGameServer gameServer3 = this.client3.joinGame("game3");

        assertMessageEquals(this.client3, new JoinedGameMessage("game3").setHeader(this.client3.getName()));
        assertMessageEquals(new NewPlayerConnectedToGameMessage(this.client3.getName()), this.client2, this.client1);


        gameServer3.sendChatMessage(new ArrayList<>(List.of(this.client1.getName(), this.client2.getName())), "Message in chat");
        assertMessageEquals(new ArrayList<>(List.of(this.client1, this.client2)), new NotifyChatMessage(this.client3.getName(), "Message in chat"));


        gameServer3.chooseColor(Color.BLUE);
        assertMessageEquals(new ArrayList<>(List.of(this.client3, this.client2, this.client1)), new AcceptedColorMessage(this.client3.getName(), Color.BLUE));
        assertMessageEquals(new ArrayList<>(List.of(this.client2, this.client1)), new AvailableColorsMessage(new ArrayList<>(List.of(Color.GREEN, Color.YELLOW, Color.RED))));
    }

    @Test
    public void testPlayerCanJoinFullGame() throws RemoteException {
        this.client1.connect();

        this.client1.newGame("game5", 2);

        assertMessageEquals(this.client1, new CreatedGameMessage("game5").setHeader(this.client1.getName()));


        this.client2.connect();

        this.client2.joinGame("game5");

        assertMessageEquals(this.client2, new JoinedGameMessage("game5").setHeader(this.client2.getName()));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getName()));


        this.client3.connect();

       this.client3.joinGame("game5");

        assertMessageEquals(this.client3, new GameHandlingErrorMessage(Error.GAME_NOT_ACCESSIBLE, null));
    }

    @Test
    public void testMultipleGames() throws RemoteException {
        this.client1.connect();

        this.client1.newGame("game8", 2);

        assertMessageEquals(this.client1, new CreatedGameMessage("game8").setHeader(this.client1.getName()));

        client1.waitForMessage(TableConfigurationMessage.class);
        client1.clearQueue();


        this.client2.connect();

        this.client2.joinGame("game8");

        assertMessageEquals(this.client2, new JoinedGameMessage("game8").setHeader(this.client2.getName()));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getName()));


        this.client1.waitForMessage(TableConfigurationMessage.class);
        this.client2.waitForMessage(GameConfigurationMessage.class);
        this.client1.clearQueue();
        this.client2.clearQueue();

        this.client3.connect();

        VirtualGameServer gameServer3 = this.client3.newGame("game9", 2);

        assertMessageEquals(this.client3, new CreatedGameMessage("game9").setHeader(this.client3.getName()));


        this.client4.connect();

        this.client4.joinGame("game9");

        assertMessageEquals(this.client4, new JoinedGameMessage("game9").setHeader(this.client4.getName()));

        assertMessageEquals(this.client3, new NewPlayerConnectedToGameMessage(this.client4.getName()));


        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());

        gameServer3.sendChatMessage(new ArrayList<>(List.of(this.client3.getName(), this.client4.getName())), "Message in chat");
        assertMessageEquals(new ArrayList<>(List.of(this.client3, this.client4)), new NotifyChatMessage(this.client3.getName(), "Message in chat"));

        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());
    }

    @Test
    public void testJoinFirstAvailableGames() throws RemoteException {
        this.client1.connect();

        VirtualGameServer gameServer1 = this.client1.joinFirstAvailableGame();
        assertMessageEquals(this.client1, new GameHandlingErrorMessage(Error.NO_GAMES_FREE_TO_JOIN, null));
        assertNull(gameServer1);

        gameServer1 = this.client1.newGame("game4", 2);
        assertNotNull(gameServer1);

        assertMessageEquals(this.client1, new CreatedGameMessage("game4"));

        client1.waitForMessage(GameConfigurationMessage.class);
        client1.clearQueue();


        this.client2.connect();

        VirtualGameServer gameServer2 = this.client2.joinGame("game4");
        assertNotNull(gameServer2);

        assertMessageEquals(this.client2, new JoinedGameMessage("game4"));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getName()));


        this.client1.waitForMessage(TableConfigurationMessage.class);
        this.client2.waitForMessage(GameConfigurationMessage.class);
        this.client1.clearQueue();
        this.client2.clearQueue();


        this.client3.connect();

        VirtualGameServer gameServer3 = this.client3.newGame("game7", 2);
        assertNotNull(gameServer3);

        assertMessageEquals(this.client3, new CreatedGameMessage("game7").setHeader(this.client3.getName()));


        this.client4.connect();

        VirtualGameServer gameServer4 = this.client4.joinFirstAvailableGame();
        assertNotNull(gameServer4);

        assertMessageEquals(this.client4, new JoinedGameMessage("game7").setHeader(this.client4.getName()));

        assertMessageEquals(this.client3, new NewPlayerConnectedToGameMessage(this.client4.getName()));


        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());

        this.client5 = new Client(virtualMainServer, "client5");
        this.client5.connect();

        VirtualGameServer gameServer5 = this.client5.joinFirstAvailableGame();
        assertNull(gameServer5);
        assertMessageEquals(new GameHandlingErrorMessage(Error.NO_GAMES_FREE_TO_JOIN, null));

    }

    @Test
    public void testDisconnectionWhileInLobby() throws RemoteException {

        this.client1.connect();

        this.client2.connect();

        this.client2.newGame("game11", 2);

        this.client2.stopSendingHeartBeat();

        waitingThread(5000);

        Client client6 = new Client(virtualMainServer, this.client2.getName());
        client6.connect();
        assertMessageEquals(client6, new GameHandlingErrorMessage(Error.PLAYER_NAME_ALREADY_IN_USE, null));


        this.client2.reconnect();

        assertMessageEquals(this.client2, new JoinedGameMessage("game11"));

        this.client1.stopSendingHeartBeat();

        waitingThread(5000);

        this.client1.reconnect();

        this.client1.startSendingHeartBeat();

        assertMessageEquals(this.client1, new AvailableGamesMessage(List.of("game11")));

        this.client1.reconnect();

        assertMessageEquals(this.client1, new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER, null));

        this.client1.stopSendingHeartBeat();
        waitingThread(5000);
        Client client7 = new Client(virtualMainServer, this.client1.getName());
        client7.setToken("fake token");
        client7.reconnect();
        assertMessageEquals(client7, new NetworkHandlingErrorMessage(NetworkError.COULD_NOT_RECONNECT, null));

        Client client8 = new Client(virtualMainServer, this.client1.getName());
        client8.connect();
        assertMessageEquals(client8, new GameHandlingErrorMessage(Error.PLAYER_NAME_ALREADY_IN_USE, null));
        client8.setToken("fake token");
        client8.reconnect();
    }

    @Test
    public void testDisconnectionWhileInGame() throws RemoteException {
        this.client1.connect();

        VirtualGameServer gameServer1 = this.client1.newGame("game6", 2);
        assertNotNull(gameServer1);

        assertMessageEquals(this.client1, new CreatedGameMessage("game6"));


        this.client2.connect();

        VirtualGameServer gameServer2 = this.client2.joinGame("game6");
        assertNotNull(gameServer2);

        assertMessageEquals(this.client2, new JoinedGameMessage("game6"));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getName()));


        this.client2.stopSendingHeartBeat();

        waitingThread(5000);

        gameServer2 = client2.reconnect();

        this.client2.startSendingHeartBeat();

        //Situation: client 2 has disconnected from game
        Client client6 = new Client(virtualMainServer, this.client2.getName());
        assertMessageEquals(client2, new JoinedGameMessage("game6"));

        client6.reconnect();
        assertMessageEquals(client6, new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER, null));

        gameServer2.sendChatMessage(new ArrayList<>(List.of(this.client1.getName(), this.client2.getName())), "Chat message after disconnection!");
        assertMessageEquals(new ArrayList<>(List.of(this.client1, this.client2)), new NotifyChatMessage(this.client2.getName(), "Chat message after disconnection!"));

    }

    @Test
    public void testFirePlayersAndGames() throws RemoteException {

        this.client1.connect();
        this.client2.connect();
        this.client3.connect();
        this.client4.connect();

        VirtualGameServer gameServer1 = this.client1.newGame("game13", 4, 1);
        VirtualGameServer gameServer2 = this.client2.joinGame("game13");
        VirtualGameServer gameServer3 = this.client3.joinGame("game13");
        VirtualGameServer gameServer4 = this.client4.joinGame("game13");

        assertNotNull(gameServer1);
        assertNotNull(gameServer2);
        assertNotNull(gameServer3);
        assertNotNull(gameServer4);

        allPlayersChooseColor(gameServer1, gameServer2, gameServer3, gameServer4);

        allPlayersChoosePrivateGoal(gameServer1, gameServer2, gameServer3, gameServer4);

        allPlayersPlacedInitialCard(gameServer1, gameServer2, gameServer3, gameServer4);

        this.client3.disconnect();
        this.client4.disconnect();

        // client1 turn
        gameServer1.placeCard("resource_23", "initial_05", Direction.UP_RIGHT, CardOrientation.DOWN);
        gameServer1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyFirstTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("resource_01", "initial_05", Direction.UP_LEFT, CardOrientation.UP);
        gameServer1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("gold_39", "resource_01", Direction.UP_LEFT, CardOrientation.DOWN);
        gameServer1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("gold_23", "resource_23", Direction.UP_RIGHT, CardOrientation.UP);
        gameServer1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("gold_40", "gold_23", Direction.UP_LEFT, CardOrientation.DOWN);
        gameServer1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("resource_05", "gold_39", Direction.UP_RIGHT, CardOrientation.DOWN);
        gameServer1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("resource_03", "resource_05", Direction.UP_RIGHT, CardOrientation.DOWN);
        gameServer1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("gold_06", "resource_05", Direction.DOWN_RIGHT, CardOrientation.UP);
        gameServer1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("gold_20", "gold_23", Direction.DOWN_RIGHT, CardOrientation.DOWN);
        gameServer1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("resource_08", "gold_20", Direction.DOWN_RIGHT, CardOrientation.DOWN);
        gameServer1.pickCardFromTable(PlayableCardType.RESOURCE, 1);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("resource_21", "gold_20", Direction.UP_RIGHT, CardOrientation.DOWN);
        gameServer1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("gold_28", "resource_08", Direction.DOWN_RIGHT, CardOrientation.UP);
        gameServer1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("resource_30", "gold_28", Direction.UP_RIGHT, CardOrientation.UP);
        gameServer1.pickCardFromTable(PlayableCardType.GOLD, 0);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        gameServer1.placeCard("resource_39", "resource_21", Direction.UP_RIGHT, CardOrientation.UP);
        gameServer1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        //assertFalse(gameController.getGameAssociated().getFinalCondition());
        gameServer1.placeCard("gold_24", "resource_21", Direction.DOWN_RIGHT, CardOrientation.UP);
        // client1 reached 20 points: final condition should be true, but not in final round
        //assertTrue(gameController.getGameAssociated().getFinalCondition());
        //assertFalse(gameController.getGameAssociated().isFinalRound());
        gameServer1.pickCardFromTable(PlayableCardType.GOLD, 1);

        //assertTrue(gameController.getGameAssociated().getFinalCondition());
        //assertFalse(gameController.getGameAssociated().isFinalRound());
        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        // now it should be the final round:
        //assertTrue(gameController.getGameAssociated().getFinalCondition());
        //assertTrue(gameController.getGameAssociated().isFinalRound());

        gameServer1.placeCard("resource_28", "resource_39", Direction.UP_RIGHT, CardOrientation.UP);
        gameServer1.pickCardFromTable(PlayableCardType.RESOURCE, 1);

        dummyTurn(gameServer2, client2, PlayableCardType.RESOURCE);

        // game should end and declare client1 the winner
        //assertEquals(GameState.END, gameController.getGameAssociated().getGameState());
        //assertEquals(gameServer1, gameController.getGameAssociated().getWinnerPlayers().getFirst().getName());
        //assertEquals(1, gameController.getGameAssociated().getWinnerPlayers().size());

        client1.clearQueue();
        client2.clearQueue();

        waitingThread(4000);
        //assertEquals(this.client2.getIncomingMessages().size(), 1);
        //assertEquals(this.client1.getIncomingMessages().size(), 1);
        assertMessageEquals(List.of(this.client2, this.client1), new DisconnectFromGameMessage("game13"));

        waitingThread(2000);
        gameServer1.sendChatMessage(new ArrayList<String>(List.of(this.client2.getName())), "After game end!");
        waitingThread(500);
        //assertNull(client2.getMessage());
        //assertMessageEquals(this.client1, new GameHandlingError(Error.GAME_NOT_FOUND, null));

    }

    @Test
    public void testRequestAvailableGames() throws RemoteException {
        client1.connect();
        client2.connect();

        client1.newGame("game25", 3);

        client2.clearQueue();
        client2.requestAvailableGames();
        assertMessageEquals(client2, new AvailableGamesMessage(List.of("game25")).setHeader(this.client2.getName()));

        client3.connect();
        client3.newGame("game26", 2);
        client2.clearQueue();
        client2.requestAvailableGames();
        assertMessageEquals(client2, new AvailableGamesMessage(List.of("game25", "game26")).setHeader(this.client2.getName()));
    }

    @Test
    public void testReconnection() throws RemoteException {
        this.client1.connect();

        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message = this.client1.getMessage(CreatedPlayerMessage.class);
        String token1 = ((CreatedPlayerMessage) message).getToken();

        VirtualGameServer gameServer1 = this.client1.newGame("game15", 2);

        client1.waitForMessage(GameConfigurationMessage.class);
        client1.clearQueue();

        this.client2.connect();
        VirtualGameServer gameServer2 = this.client2.joinGame("game15");

        client1.waitForMessage(TableConfigurationMessage.class);
        client1.clearQueue();

        this.client1.stopSendingHeartBeat();

        waitingThread(1100);

        Client client7 = new Client(virtualMainServer, this.client1.getName());
        client7.setToken(token1);
        VirtualGameServer gameServer7 = client7.reconnect();

        assertMessageEquals(client7, new JoinedGameMessage("game15"));

        assertNotEquals(gameServer1, gameServer7);


        gameServer7.sendChatMessage(new ArrayList<>(List.of(this.client2.getName())), "Send chat message after reconnection");

        assertMessageEquals(this.client2, new NotifyChatMessage(client7.getName(), "Send chat message after reconnection"));
        assertNull(this.client1.getMessage());

        gameServer2.sendChatMessage(new ArrayList<>(List.of(this.client2.getName(), client1.getName())), "Send chat message after reconnection");

        assertMessageEquals(List.of(this.client2, client7), new NotifyChatMessage(this.client2.getName(), "Send chat message after reconnection"));
        assertNull(this.client1.getMessage());
    }

    @Test
    public void testInactiveClientKiller() throws RemoteException {
        client1.connect();
        waitingThread(500);
        client1.stopSendingHeartBeat();
        waitingThread(25 * 1000);
        Client client5 = new Client(virtualMainServer, this.client1.getName());
        client5.connect();
        assertMessageEquals(client5, new CreatedPlayerMessage(client5.getName()));
        client5.disconnect();
    }

    @Test
    public void testExitFromGame() throws RemoteException {
        client1.connect();
        client2.connect();
        client1.newGame("game30", 3, 1);
        client2.joinGame("game30");
        assertMessageEquals(client2, new JoinedGameMessage("game30"));
        waitingThread(500);
        client1.exitFromGame();
        assertMessageEquals(client1, new DisconnectFromGameMessage("game30"));
        assertMessageEquals(client2, new DisconnectedPlayerMessage(this.client1.getName()));
        client1.newGame("game30", 4, 2);
        assertMessageEquals(this.client1, new GameHandlingErrorMessage(Error.GAME_NAME_ALREADY_IN_USE, null));
        client1.newGame("game31", 3);
        assertMessageEquals(this.client1, new CreatedGameMessage("game31"));
        client2.exitFromGame();
        assertMessageEquals(client2, new DisconnectFromGameMessage("game30"));
        waitingThread(500);
        client2.newGame("game35", 2, 3);
        assertMessageEquals(client2, new CreatedGameMessage("game35"));
        client1.clearQueue();
        client1.joinFirstAvailableGame();
        assertMessageEquals(client1, new GameHandlingErrorMessage(Error.PLAYER_ALREADY_REGISTERED_TO_SOME_GAME, null));
        client1.exitFromGame();
        assertMessageEquals(client1, new DisconnectFromGameMessage("game31"));
        client1.joinGame("game35");
        assertMessageEquals(client2, new JoinedGameMessage("game35"));
        client3.connect();
        client3.joinGame("game35");
        assertMessageEquals(client3, new GameHandlingErrorMessage(Error.GAME_NOT_ACCESSIBLE, null));
        client1.exitFromGame();
        assertMessageEquals(client1, new DisconnectFromGameMessage("game35"));
        client3.exitFromGame();
        client2.exitFromGame();
        assertMessageEquals(client2, new DisconnectFromGameMessage("game35"));
        client2.newGame("game35", 2, 3);
        assertMessageEquals(client2, new CreatedGameMessage("game35"));
        client1.clearQueue();
    }

    private void dummyTurn(VirtualGameServer virtualGameServer, Client client, PlayableCardType cardType) throws RemoteException {
        dummyPlace(virtualGameServer, client);
        virtualGameServer.pickCardFromDeck(cardType);
    }

    private void dummyFirstTurn(VirtualGameServer virtualGameServer, Client client, PlayableCardType cardType) throws RemoteException {
        dummyFirstPlace(virtualGameServer, client);
        virtualGameServer.pickCardFromDeck(cardType);
    }

    private void dummyFirstPlace(VirtualGameServer virtualGameServer, Client client) throws RemoteException {
        client.waitForMessage(OwnStationConfigurationMessage.class);
        OwnStationConfigurationMessage latestMessage = (OwnStationConfigurationMessage) client.getMessage(OwnStationConfigurationMessage.class);

        client.setAnchorCard(latestMessage.getInitialCard());
        client.setCardToPlace(latestMessage.getCardsInHand().getFirst());

        virtualGameServer.placeCard(client.getCardToPlace().getCardCode(), client.getAnchorCard().getCardCode(), Direction.UP_RIGHT, CardOrientation.DOWN);
    }

    private void dummyPlace(VirtualGameServer virtualGameServer, Client client) throws RemoteException {
        AcceptedPickCardMessage latestMessage;
        do {
            client.waitForMessage(AcceptedPickCardMessage.class);
            latestMessage = (AcceptedPickCardMessage) client.getMessage(AcceptedPickCardMessage.class);
        } while (!latestMessage.getNick().equals(client.getName()));

        client.setAnchorCard(client.getCardToPlace());
        client.setCardToPlace(latestMessage.getPickedCard());

        virtualGameServer.placeCard(client.getCardToPlace().getCardCode(), client.getAnchorCard().getCardCode(), Direction.UP_RIGHT, CardOrientation.DOWN);
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

    private void assertMessageEquals(Client receiver, MessageToClient message) {
        assertMessageEquals(List.of(receiver), message);
    }

    private void assertMessageEquals(MessageToClient message, Client... receivers) {
        ArrayList<Client> receiversName = Arrays.stream(receivers).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        assertMessageEquals(receiversName, message);
    }

    private void assertMessageEquals(List<Client> receivers, MessageToClient message) {
        List<String> receiversName;
        receiversName = receivers.stream().map(Client::getName).toList();
        message.setHeader(receiversName);
        for (Client receiver : receivers) {
            receiver.waitForMessage(message.getClass());
            assertEquals(message, receiver.getMessage(message.getClass()));
        }
    }

    private void clearQueue(List<Client> clients) {
        for (Client player : clients) {
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

class Client extends UnicastRemoteObject implements VirtualClient, Serializable {

    private final Deque<MessageToClient> incomingMessages;
    private final VirtualMainServer virtualMainServer;
    private VirtualGameServer virtualGameServer;
    private String name;
    private Boolean sendHeartBeat;
    private String token;
    private PlayableCard cardToPlace;
    private PlayableCard anchorCard;
    private final ScheduledFuture<?> heartBeatThread;

    public Client(VirtualMainServer virtualMainServer, String name) throws RemoteException {
        super();
        this.virtualMainServer = virtualMainServer;
        virtualMainServer.registerClient(this);
        this.name = name;
        this.incomingMessages = new ArrayDeque<>();
        this.sendHeartBeat = true;
        this.token = null;
        heartBeatThread = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::heartBeat, 0, 1000 * ServerSettings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS / 2, TimeUnit.MILLISECONDS);
    }

    private synchronized void heartBeat() {
            try {
                if (Client.this.sendHeartBeat) {
                    virtualMainServer.heartBeat(Client.this);
                    //System.out.println("send heartbeat " + Client.this.name);
                }
            } catch (RemoteException e) {}
    }

    public Deque<MessageToClient> getIncomingMessages(){
        return this.incomingMessages;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public PlayableCard getAnchorCard() {
        return anchorCard;
    }

    public void setAnchorCard(PlayableCard anchorCard) {
        this.anchorCard = anchorCard;
    }

    public PlayableCard getCardToPlace() {
        return cardToPlace;
    }

    public void setCardToPlace(PlayableCard cardToPlace) {
        this.cardToPlace = cardToPlace;
    }

    public synchronized void stopSendingHeartBeat() {
        this.sendHeartBeat = false;
        this.notify();
    }

    public synchronized void startSendingHeartBeat() {
        this.sendHeartBeat = true;
        this.notify();
    }

    public void setVirtualGameServer(VirtualGameServer virtualGameServer) {
        this.virtualGameServer = virtualGameServer;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void pushUpdate(MessageToClient message) {
        if (message instanceof CreatedPlayerMessage) {
            this.token = ((CreatedPlayerMessage) message).getToken();
        }
        synchronized (this.incomingMessages) {
            this.incomingMessages.add(message);
            this.incomingMessages.notifyAll();
        }
    }

    public void waitForMessage(Class<? extends MessageToClient> messageToClientClass) {
        synchronized (this.incomingMessages) {
            while (this.incomingMessages.stream().noneMatch(messageToClientClass::isInstance)) {
                try {
                    this.incomingMessages.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public MessageToClient getMessage() {
        return getMessage(MessageToClient.class);
    }

    public MessageToClient getMessage(Class<? extends MessageToClient> messageToClientClass) {
        synchronized (this.incomingMessages) {
            while (!this.incomingMessages.isEmpty()) {
                MessageToClient res = this.incomingMessages.remove();
                if (messageToClientClass.isInstance(res)) return res;
            }
        }
        return null;
    }

    public void clearQueue() {
        synchronized (this.incomingMessages) {
            this.incomingMessages.clear();
        }
    }

    public void connect() throws RemoteException {
        this.virtualMainServer.newConnection(this, name);
        startSendingHeartBeat();
    }

    public VirtualGameServer newGame(String gameName, int numOfPlayer) throws RemoteException {
        return this.virtualMainServer.createGame(this, gameName, name, numOfPlayer);
    }

    public VirtualGameServer newGame(String gameName, int numOfPlayer, long randomSeed) throws RemoteException {
        return this.virtualMainServer.createGame(this, gameName, name, numOfPlayer, randomSeed);
    }

    public VirtualGameServer joinGame(String game) throws RemoteException {
        return this.virtualMainServer.joinGame(this, game, name);
    }

    public VirtualGameServer joinFirstAvailableGame() throws RemoteException {
        return this.virtualMainServer.joinFirstAvailableGame(this, this.name);
    }

    public void sendChatMessage(ArrayList<String> receivers, String message) throws RemoteException {
        this.virtualGameServer.sendChatMessage(receivers, message);
    }

    public void disconnect() throws RemoteException {
        this.virtualMainServer.disconnect(this, name);
    }

    public void destroyHeartBeatThread() {
        heartBeatThread.cancel(true);
    }

    public VirtualGameServer reconnect() throws RemoteException {
        return this.virtualMainServer.reconnect(this, name, token);
    }

    public void requestAvailableGames() throws RemoteException{
        this.virtualMainServer.requestAvailableGames(this, this.name);
    }

    public void exitFromGame() throws RemoteException{
        this.virtualMainServer.disconnectFromGame(this, this.name);
    }

}
