package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageHandler;
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
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkError;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
import it.polimi.ingsw.gc19.Networking.Server.ServerApp;
import it.polimi.ingsw.gc19.Networking.Server.ServerSettings;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ClientTCPTest {

    private TestClassClientTCP client1, client2, client3, client4;
    private HashMap<TestClassClientTCP, PlayableCard> clientsAnchors;
    private long prevHeartBeatSetting, maxTimeBeforeReEnteringLobby, maxTimeBeforeDisconnection;

    @BeforeEach
    public void setUp() throws IOException {
        maxTimeBeforeDisconnection = ServerSettings.TIME_TO_WAIT_BEFORE_IN_GAME_CLIENT_DISCONNECTION;
        prevHeartBeatSetting = ServerSettings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS;
        maxTimeBeforeReEnteringLobby = ServerSettings.TIME_TO_WAIT_BEFORE_IN_GAME_CLIENT_DISCONNECTION;

        ServerSettings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS = 1;
        ServerSettings.TIME_TO_WAIT_BEFORE_IN_GAME_CLIENT_DISCONNECTION = 3;
        ServerSettings.TIME_TO_WAIT_BEFORE_CLIENT_HANDLER_KILL = 20;

        ServerApp.startTCP();

        ClientController clientController1 = new ClientController();
        ClientController clientController2 = new ClientController();
        ClientController clientController3 = new ClientController();
        ClientController clientController4 = new ClientController();

        this.client1 = new TestClassClientTCP(new MessageHandler(clientController1), clientController1);
        clientController1.setClientInterface(client1);
        this.client2 = new TestClassClientTCP(new MessageHandler(clientController2), clientController2);
        clientController2.setClientInterface(client2);
        this.client3 = new TestClassClientTCP(new MessageHandler(clientController3), clientController3);
        clientController3.setClientInterface(client3);
        this.client4 = new TestClassClientTCP(new MessageHandler(clientController4), clientController4);
        clientController4.setClientInterface(client4);

        clientsAnchors = new HashMap<>();
    }

    @AfterEach
    public void tearDown(){
        this.client1.stopClient();
        this.client2.stopClient();
        this.client3.stopClient();
        this.client4.stopClient();

        ServerApp.stopTCP();

        ServerSettings.MAX_DELTA_TIME_BETWEEN_HEARTBEATS = prevHeartBeatSetting;
        ServerSettings.TIME_TO_WAIT_BEFORE_IN_GAME_CLIENT_DISCONNECTION = maxTimeBeforeReEnteringLobby;
        ServerSettings.TIME_TO_WAIT_BEFORE_CLIENT_HANDLER_KILL = maxTimeBeforeDisconnection;
    }


    @Test
    public void testCreatePlayer(){
        this.client1.connect("client1");
        assertMessageEquals(this.client1, new CreatedPlayerMessage("client1"));
        this.client2.connect("client2");
        assertMessageEquals(this.client2, new CreatedPlayerMessage("client2"));
        this.client3.connect("client3");
        assertMessageEquals(this.client3, new CreatedPlayerMessage("client3"));
        this.client4.connect("client4");
        assertMessageEquals(this.client4, new CreatedPlayerMessage("client4"));

        this.client1.connect("client5");
        assertMessageEquals(this.client1, new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER, null));

        this.client1.disconnect();
        this.client2.disconnect();
        this.client3.disconnect();
        this.client4.disconnect();
    }

    @Test
    public void testMultiplePlayerInGame(){
        this.client1.connect("client1");
        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message1 = this.client1.getMessage();
        String token1 = ((CreatedPlayerMessage) message1).getToken();
        this.client1.configure("client1", token1);

        this.client1.createGame("game3", 3);

        assertMessageEquals(this.client1, new CreatedGameMessage("game3"));

        this.client2.connect("client2");
        client2.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message2 = this.client2.getMessage();
        String token2 = ((CreatedPlayerMessage) message2).getToken();
        this.client2.configure("client2", token2);

        this.client2.joinGame("game3", false);

        assertMessageEquals(this.client2, new JoinedGameMessage("game3"));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage("client2"));

        this.client3.connect("client3");
        client3.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message3 = this.client3.getMessage();
        String token3 = ((CreatedPlayerMessage) message3).getToken();
        this.client3.configure("client3", token3);

        this.client3.joinGame("game3", false);

        waitingThread(500);

        assertMessageEquals(this.client3, new JoinedGameMessage("game3"));
        assertMessageEquals(new NewPlayerConnectedToGameMessage("client3"), this.client2, this.client1);


        client3.sendChatMessage(new ArrayList<>(List.of(this.client1.getNickname(), this.client2.getNickname())), "Message in chat");
        assertMessageEquals(new ArrayList<>(List.of(this.client1, this.client2)), new NotifyChatMessage(this.client3.getNickname(), "Message in chat"));

        client3.chooseColor(Color.BLUE);
        assertMessageEquals(new ArrayList<>(List.of(this.client3, this.client2, this.client1)), new AcceptedColorMessage(this.client3.getNickname(), Color.BLUE));
        assertMessageEquals(new ArrayList<>(List.of(this.client2, this.client1)), new AvailableColorsMessage(new ArrayList<>(List.of(Color.GREEN, Color.YELLOW, Color.RED))));

        this.client1.disconnect();
        this.client2.disconnect();
        this.client3.disconnect();
    }


    @Test
    public void testJoinFirstAvailableGames() throws IOException {
        this.client1.connect("client1");
        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message1 = this.client1.getMessage();
        String token1 = ((CreatedPlayerMessage) message1).getToken();
        this.client1.configure("client1", token1);

        this.client1.createGame("game4", 2, 1);

        assertMessageEquals(this.client1, new CreatedGameMessage("game4"));

        client1.waitForMessage(GameConfigurationMessage.class);

        this.client2.connect("client2");
        client2.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message2 = this.client2.getMessage();
        String token2 = ((CreatedPlayerMessage) message2).getToken();
        this.client2.configure("client2", token2);

        this.client2.joinGame("game4", false);

        assertMessageEquals(this.client2, new JoinedGameMessage("game4"));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getNickname()));

        this.client1.waitForMessage(TableConfigurationMessage.class);
        this.client2.waitForMessage(GameConfigurationMessage.class);

        this.client3.connect("client3");
        client3.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message3 = this.client3.getMessage();
        String token3 = ((CreatedPlayerMessage) message3).getToken();
        this.client3.configure("client3", token3);

        this.client3.createGame("game7", 2, 1);

        assertMessageEquals(this.client3, new CreatedGameMessage("game7"));

        this.client4.connect("client4");
        client4.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message4 = this.client4.getMessage();
        String token4 = ((CreatedPlayerMessage) message4).getToken();
        this.client4.configure("client4", token4);

        this.client4.joinFirstAvailableGame();

        assertMessageEquals(this.client4, new JoinedGameMessage("game7"));

        assertMessageEquals(this.client3, new NewPlayerConnectedToGameMessage(this.client4.getNickname()));

        ClientController clientController5 = new ClientController();
        TestClassClientTCP client5 = new TestClassClientTCP(new MessageHandler(clientController5), clientController5);
        clientController5.setClientInterface(client5);
        client5.connect("client5");

        client5.joinFirstAvailableGame();
        assertMessageEquals(new GameHandlingErrorMessage(Error.NO_GAMES_FREE_TO_JOIN, null));

        client5.disconnect();

        this.client1.disconnect();
        this.client2.disconnect();
        this.client3.disconnect();
        this.client4.disconnect();
    }


    @Test
    public void testMultipleReconnection(){
        this.client1.connect("client1");

        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message1 = this.client1.getMessage();
        String token1 = ((CreatedPlayerMessage) message1).getToken();
        this.client1.configure("client1", token1);

        //this.client1.reconnect();
        //assertMessageEquals(this.client1, new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER,null));

        //this.client1.reconnect();
        //assertMessageEquals(this.client1, new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER,null));

        this.client1.connect("client1");
        assertMessageEquals(this.client1, new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER,null));

        //this.client1.reconnect();
        //assertMessageEquals(this.client1, new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER,null));

        this.client1.stopSendingHeartbeat();
        waitingThread(3000);
        //this.client1.startSendingHeartbeat();
        this.client1.reconnect();
        this.client1.waitForMessage(MessageToClient.class);

        this.client1.createGame("game25", 3, 1);
        assertMessageEquals(this.client1, new CreatedGameMessage("game25"));
        this.client2.connect("client2");
        client2.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message2 = this.client2.getMessage();
        String token2 = ((CreatedPlayerMessage) message2).getToken();
        this.client2.configure("client2", token2);

        //this.client2.startSendingHeartbeat();
        this.client3.connect("client3");
        client3.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message3 = this.client3.getMessage();
        String token3 = ((CreatedPlayerMessage) message3).getToken();
        this.client3.configure("client3", token3);

        //this.client3.startSendingHeartbeat();
        this.client2.joinGame("game25", false);
        assertMessageEquals(this.client2, new JoinedGameMessage("game25"));
        this.client3.joinGame("game25", false);
        assertMessageEquals(this.client3, new JoinedGameMessage("game25"));

        this.client1.stopSendingHeartbeat();
        waitingThread(5000);

        this.client1.sendChatMessage(new ArrayList<>(List.of(this.client2.getNickname(), "client3")), "Chat message after heartbeat dead!");
        assertMessageEquals(this.client1, new GameHandlingErrorMessage(Error.GAME_NOT_FOUND, null));

        this.client1.disconnect();
        this.client2.disconnect();
        this.client3.disconnect();
    }


    @Test
    public void testDisconnectionWhileInLobby(){
        this.client1.connect("client1");
        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message1 = this.client1.getMessage();
        String token1 = ((CreatedPlayerMessage) message1).getToken();
        this.client1.configure("client1", token1);

        this.client2.connect("client2");
        client2.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message2 = this.client2.getMessage();
        String token2 = ((CreatedPlayerMessage) message2).getToken();
        this.client2.configure("client2", token2);

        this.client2.createGame("game11", 2, 1);
        this.client2.stopSendingHeartbeat();

        waitingThread(5000);

        this.client2.reconnect();
        this.client2.startSendingHeartbeat();

        assertMessageEquals(this.client2, new JoinedGameMessage("game11"));

        this.client1.stopSendingHeartbeat();

        waitingThread(5000);

        this.client1.reconnect();
        this.client1.startSendingHeartbeat();

        waitingThread(500);

        assertMessageEquals(this.client1, new AvailableGamesMessage(List.of("game11")));

        this.client1.stopSendingHeartbeat();
        waitingThread(5000);

        this.client1.reconnect();
        this.client1.startSendingHeartbeat();
        assertMessageEquals(client1, new AvailableGamesMessage(List.of("game11")));

        client1.disconnect();

        //client8 has disconnected so, it has deleted the token file
        assertThrows(IllegalStateException.class, () -> client1.reconnect());
        this.client2.disconnect();
        this.client1.stopClient();
    }

    @Test
    public void testExitFromGame(){
        this.client1.connect("client1");
        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message1 = this.client1.getMessage();
        String token1 = ((CreatedPlayerMessage) message1).getToken();
        this.client1.configure("client1", token1);

        this.client2.connect("client2");
        client2.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message2 = this.client2.getMessage();
        String token2 = ((CreatedPlayerMessage) message2).getToken();
        this.client2.configure("client2", token2);

        this.client1.createGame("game11", 3);
        waitingThread(1000);
        this.client2.joinFirstAvailableGame();
        waitingThread(1000);
        this.client1.logoutFromGame();
        assertMessageEquals(this.client2, new DisconnectedPlayerMessage(this.client1.getNickname()));
        this.client2.logoutFromGame();

        this.client3.connect("client3");
        client3.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message3 = this.client3.getMessage();
        String token3 = ((CreatedPlayerMessage) message3).getToken();

        this.client3.configure("client3", token3);
        this.client3.availableGames();
        assertMessageEquals(this.client3, new AvailableGamesMessage(List.of()));
        client1.disconnect();
        client2.disconnect();
        client3.disconnect();
    }

    @Test
    public void testPlayerCanJoinFullGame(){
        this.client1.connect("client1");
        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message1 = this.client1.getMessage();
        String token1 = ((CreatedPlayerMessage) message1).getToken();
        this.client1.configure("client1", token1);

        this.client1.createGame("game5", 2, 1);
        assertMessageEquals(this.client1, new CreatedGameMessage("game5").setHeader(this.client1.getNickname()));

        this.client2.connect("client2");
        client2.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message2 = this.client2.getMessage();
        String token2 = ((CreatedPlayerMessage) message2).getToken();
        this.client2.configure("client2", token2);

        this.client2.joinGame("game5", false);
        assertMessageEquals(this.client2, new JoinedGameMessage("game5").setHeader(this.client2.getNickname()));
        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getNickname()));

        this.client3.connect("client3");
        client3.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message3 = this.client3.getMessage();
        String token3 = ((CreatedPlayerMessage) message3).getToken();
        this.client3.configure("client3", token3);

        this.client3.joinGame("game5", false);
        assertMessageEquals(this.client3, new GameHandlingErrorMessage(Error.GAME_NOT_ACCESSIBLE, null));

        this.client1.disconnect();
        this.client2.disconnect();
        this.client3.disconnect();
    }


    @Test
    public void testMultipleGames(){
        this.client1.connect("client1");
        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message1 = this.client1.getMessage();
        String token1 = ((CreatedPlayerMessage) message1).getToken();
        this.client1.configure("client1", token1);

        this.client1.createGame("game8", 2, 1);
        assertMessageEquals(this.client1, new CreatedGameMessage("game8"));
        client1.waitForMessage(TableConfigurationMessage.class);

        this.client2.connect("client2");
        client2.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message2 = this.client2.getMessage();
        String token2 = ((CreatedPlayerMessage) message2).getToken();
        this.client2.configure("client2", token2);

        this.client2.joinGame("game8", false);
        assertMessageEquals(this.client2, new JoinedGameMessage("game8").setHeader(this.client2.getNickname()));
        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getNickname()));

        this.client1.waitForMessage(TableConfigurationMessage.class);
        this.client2.waitForMessage(GameConfigurationMessage.class);

        this.client3.connect("client3");
        client3.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message3 = this.client3.getMessage();
        String token3 = ((CreatedPlayerMessage) message3).getToken();
        this.client3.configure("client3", token3);

        this.client3.createGame("game9", 2, 1);
        assertMessageEquals(this.client3, new CreatedGameMessage("game9").setHeader(this.client3.getNickname()));

        this.client4.connect("client4");
        client4.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message4 = this.client4.getMessage();
        String token4 = ((CreatedPlayerMessage) message4).getToken();
        this.client4.configure("client4", token4);

        this.client4.joinGame("game9", false);
        assertMessageEquals(this.client4, new JoinedGameMessage("game9").setHeader(this.client4.getNickname()));
        assertMessageEquals(this.client3, new NewPlayerConnectedToGameMessage(this.client4.getNickname()));

        this.client3.sendChatMessage(new ArrayList<>(List.of(this.client3.getNickname(), this.client4.getNickname())), "Message in chat");
        assertMessageEquals(new ArrayList<>(List.of(this.client3, this.client4)), new NotifyChatMessage(this.client3.getNickname(), "Message in chat"));

        this.client1.disconnect();
        this.client2.disconnect();
        this.client3.disconnect();
        this.client4.disconnect();
    }


    @Test
    public void testCreateClientAfterDisconnection() throws IOException {
        ClientController clientController7 = new ClientController();
        TestClassClientTCP client7 = new TestClassClientTCP(new MessageHandler(clientController7), clientController7);
        clientController7.setClientInterface(client7);
        client7.connect("client7");
        client7.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message7 = client7.getMessage();
        String token7 = ((CreatedPlayerMessage) message7).getToken();
        client7.configure("client7", token7);

        client7.createGame("game18", 2, 1);
        waitingThread(1500);

        client7.disconnect();

        ClientController clientController8 = new ClientController();
        TestClassClientTCP client8 = new TestClassClientTCP(new MessageHandler(clientController8), clientController8);
        clientController8.setClientInterface(client8);
        client8.connect(client7.getNickname());
        client8.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message8 = client8.getMessage();
        String token8 = ((CreatedPlayerMessage) message8).getToken();
        client8.configure(client7.getNickname(), token8);

        client8.joinFirstAvailableGame();
        assertMessageEquals(client8, new GameHandlingErrorMessage(Error.NO_GAMES_FREE_TO_JOIN, null));
        client8.disconnect();
    }

    @Test
    public void testFirePlayersAndGames(){
        this.client1.connect("client1");
        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message1 = this.client1.getMessage();
        String token1 = ((CreatedPlayerMessage) message1).getToken();
        this.client1.configure("client1", token1);

        this.client2.connect("client2");
        client2.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message2 = this.client2.getMessage();
        String token2 = ((CreatedPlayerMessage) message2).getToken();
        this.client2.configure("client2", token2);

        this.client3.connect("client3");
        client3.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message3 = this.client3.getMessage();
        String token3 = ((CreatedPlayerMessage) message3).getToken();
        this.client3.configure("client3", token3);

        this.client4.connect("client4");
        client4.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message4 = this.client4.getMessage();
        String token4 = ((CreatedPlayerMessage) message4).getToken();
        this.client4.configure("client4", token4);

        this.client1.createGame("game2", 4, 1);
        this.client2.joinGame("game2", true);
        this.client3.joinGame("game2", true);
        this.client4.joinGame("game2", true);

        allPlayersChooseColor(client1, client2, client3, client4);
        allPlayersChoosePrivateGoal(client1, client2, client3, client4);
        allPlayersPlacedInitialCard(client1, client2, client3, client4);

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

        waitingThread(4000);

        assertMessageEquals(List.of(this.client2, this.client1), new DisconnectFromGameMessage("game2"));

        this.client1.disconnect();
        this.client2.disconnect();
    }

    @Test
    public void testDisconnectionWhileInGame(){
        this.client1.connect("client1");
        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message = this.client1.getMessage();
        String token1 = ((CreatedPlayerMessage) message).getToken();
        this.client1.configure("client1", token1);

        this.client1.createGame("game6", 2,1);

        assertMessageEquals(this.client1, new CreatedGameMessage("game6"));

        this.client2.connect("client2");
        client2.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message2 = this.client2.getMessage();
        String token2 = ((CreatedPlayerMessage) message2).getToken();
        this.client2.configure("client2", token2);

        this.client2.joinGame("game6", false);

        assertMessageEquals(this.client2, new JoinedGameMessage("game6"));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getNickname()));

        this.client2.stopSendingHeartbeat();
        waitingThread(5000);
        client2.reconnect();
        this.client2.startSendingHeartbeat();

        //Situation: client 2 has disconnected from game
        assertMessageEquals(client2, new JoinedGameMessage("game6"));

        this.client2.sendChatMessage(new ArrayList<>(List.of(this.client1.getNickname(), this.client2.getNickname())), "Chat message after disconnection!");
        assertMessageEquals(new ArrayList<>(List.of(this.client1, this.client2)), new NotifyChatMessage(this.client2.getNickname(), "Chat message after disconnection!"));

        this.client1.disconnect();
        this.client2.disconnect();
    }

    @Test
    public void testCreateGame(){
        //Client1 tries to create a game without having registered his player
        this.client1.createGame("game1", 3, 1);
        assertMessageEquals(this.client1, new NetworkHandlingErrorMessage(NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER, null));

        this.client1.connect("client1");
        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message = this.client1.getMessage();
        String token1 = ((CreatedPlayerMessage) message).getToken();
        this.client1.configure("client1", token1);

        this.client1.createGame("game1", 3, 1);
        assertMessageEquals(this.client1, new CreatedGameMessage("game1"));

        this.client2.connect("client2");
        client2.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message2 = this.client2.getMessage();
        String token2 = ((CreatedPlayerMessage) message2).getToken();
        this.client2.configure("client2", token2);

        this.client2.createGame("game1", 2, 1);
        assertMessageEquals(this.client2, new GameHandlingErrorMessage(Error.GAME_NAME_ALREADY_IN_USE, null));

        this.client2.joinGame("game1", false);
        assertMessageEquals(this.client2, new JoinedGameMessage("game1"));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getNickname()));

        this.client3.joinGame("game1", false);
        assertMessageEquals(this.client3, new NetworkHandlingErrorMessage(NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER, null));

        this.client3.connect("client3");
        client3.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message3 = this.client3.getMessage();
        String token3 = ((CreatedPlayerMessage) message3).getToken();
        this.client3.configure("client3", token3);

        this.client3.joinGame("game1", false);
        assertMessageEquals(this.client3, new JoinedGameMessage("game1"));
        assertMessageEquals(List.of(this.client2, this.client1), new NewPlayerConnectedToGameMessage(this.client3.getNickname()));

        this.client1.disconnect();
        this.client2.disconnect();
        this.client3.disconnect();
    }


    @Test
    public void testReconnection() throws IOException {
        this.client1.connect("client1");
        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message = this.client1.getMessage();
        String token1 = ((CreatedPlayerMessage) message).getToken();
        this.client1.configure("client1", token1);

        this.client1.createGame("game15", 2, 1);

        client1.waitForMessage(GameConfigurationMessage.class);

        this.client2.connect("client2");
        client2.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message2 = this.client2.getMessage();
        String token2 = ((CreatedPlayerMessage) message2).getToken();
        this.client2.configure("client2", token2);

        this.client2.joinGame("game15", true);

        client1.waitForMessage(TableConfigurationMessage.class);

        this.client1.stopSendingHeartbeat();

        waitingThread(5000);

        ClientController clientController7 = new ClientController();
        TestClassClientTCP client7 = new TestClassClientTCP(new MessageHandler(clientController7), clientController7);
        clientController7.setClientInterface(client7);
        client7.configure("client1", token1);
        client7.reconnect();

        assertMessageEquals(client7, new JoinedGameMessage("game15"));

        client7.sendChatMessage(new ArrayList<>(List.of(this.client2.getNickname())), "Send chat message after reconnection");

        assertMessageEquals(this.client2, new NotifyChatMessage(client7.getNickname(), "Send chat message after reconnection"));

        client2.sendChatMessage(new ArrayList<>(List.of(this.client2.getNickname(), client1.getNickname())), "Send chat message after reconnection!");

        assertMessageEquals(List.of(this.client2, client7), new NotifyChatMessage(this.client2.getNickname(), "Send chat message after reconnection!"));

        client7.sendChatMessage(new ArrayList<>(List.of(this.client2.getNickname())), "Reconnected client 1 message!");
        assertMessageEquals(this.client2, new NotifyChatMessage(client7.getNickname(), "Reconnected client 1 message!"));

        client7.disconnect();

        client2.disconnect();
    }

    private void dummyTurn(TestClassClientTCP client, PlayableCardType cardType){
        dummyPlace(client);
        assertMessageEquals(List.of(this.client1, client2), new TurnStateMessage(this.client2.getNickname(), TurnState.DRAW));
        client.pickCardFromDeck(cardType);
    }

    private void dummyFirstTurn(TestClassClientTCP client, PlayableCardType cardType){
        dummyFirstPlace(client);
        assertMessageEquals(List.of(this.client1, client2), new TurnStateMessage(this.client2.getNickname(), TurnState.DRAW));
        client.pickCardFromDeck(cardType);
    }

    private void dummyFirstPlace(TestClassClientTCP client){
        client.waitForMessage(OwnStationConfigurationMessage.class);
        OwnStationConfigurationMessage latestMessage = (OwnStationConfigurationMessage) client.getMessage(OwnStationConfigurationMessage.class);

        // remove other client turns
        assertMessageWithHeaderEquals(client, new TurnStateMessage("client1", TurnState.PLACE), "client1", "client2", "client3", "client4");
        assertMessageWithHeaderEquals(client, new TurnStateMessage("client1", TurnState.DRAW), "client1", "client2");

        assertMessageEquals(List.of(this.client1, client2), new TurnStateMessage(this.client2.getNickname(), TurnState.PLACE));

        client.placeCard(latestMessage.getCardsInHand().getFirst().getCardCode(), latestMessage.getInitialCard().getCardCode(), Direction.UP_RIGHT, CardOrientation.DOWN);

        clientsAnchors.put(client, latestMessage.getCardsInHand().getFirst());
    }

    private void dummyPlace(TestClassClientTCP client){
        AcceptedPickCardMessage latestMessage;
        do {
            System.out.println(client.getNickname());
            client.waitForMessage(AcceptedPickCardMessage.class);
            latestMessage = (AcceptedPickCardMessage) client.getMessage(AcceptedPickCardMessage.class);
        } while (!latestMessage.getNick().equals(client.getNickname()));

        assertMessageWithHeaderEquals(client, new TurnStateMessage("client1", TurnState.PLACE), "client1", "client2");
        assertMessageWithHeaderEquals(client, new TurnStateMessage("client1", TurnState.DRAW), "client1", "client2");

        assertMessageEquals(List.of(this.client1, client2), new TurnStateMessage(this.client2.getNickname(), TurnState.PLACE));

        client.placeCard(latestMessage.getPickedCard().getCardCode(), clientsAnchors.get(client).getCardCode(), Direction.UP_RIGHT, CardOrientation.DOWN);
        clientsAnchors.put(client, latestMessage.getPickedCard());
    }

    private void allPlayersPlacedInitialCard(TestClassClientTCP client1, TestClassClientTCP client2, TestClassClientTCP client3, TestClassClientTCP client4){
        client1.placeInitialCard(CardOrientation.DOWN);
        client2.placeInitialCard(CardOrientation.DOWN);
        client3.placeInitialCard(CardOrientation.UP);
        client4.placeInitialCard(CardOrientation.DOWN);
    }

    private void allPlayersChoosePrivateGoal(TestClassClientTCP client1, TestClassClientTCP client2, TestClassClientTCP client3, TestClassClientTCP client4) {
        client1.choosePrivateGoalCard(0);
        client2.choosePrivateGoalCard(1);
        client3.choosePrivateGoalCard(0);
        client4.choosePrivateGoalCard(1);
    }

    private void allPlayersChooseColor(TestClassClientTCP client1, TestClassClientTCP client2, TestClassClientTCP client3, TestClassClientTCP client4){
        client1.chooseColor(Color.RED);
        client2.chooseColor(Color.GREEN);
        client3.chooseColor(Color.BLUE);
        client4.chooseColor(Color.YELLOW);
    }

    private void assertMessageEquals(TestClassClientTCP receiver, MessageToClient message) {
        assertMessageEquals(List.of(receiver), message);
    }

    private void assertMessageEquals(MessageToClient message, TestClassClientTCP ... receivers) {
        ArrayList<TestClassClientTCP> receiversName = Arrays.stream(receivers).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        assertMessageEquals(receiversName, message);
    }

    private void assertMessageEquals(List<TestClassClientTCP> receivers, MessageToClient message) {
        List<String> receiversName;
        receiversName = receivers.stream().map(TestClassClientTCP::getNickname).toList();
        message.setHeader(receiversName);
        for (TestClassClientTCP receiver : receivers) {
            receiver.waitForMessage(message.getClass());
            assertEquals(message, receiver.getMessage(message.getClass()));
        }
    }

    private void assertMessageWithHeaderEquals(TestClassClientTCP receiver, MessageToClient message, String ... header) {
        assertMessageWithHeaderEquals(List.of(receiver), message, header);
    }

    private void assertMessageWithHeaderEquals(List<TestClassClientTCP> receivers, MessageToClient message, String ... header) {
        message.setHeader(Arrays.stream(header).toList());
        for (TestClassClientTCP receiver : receivers) {
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
