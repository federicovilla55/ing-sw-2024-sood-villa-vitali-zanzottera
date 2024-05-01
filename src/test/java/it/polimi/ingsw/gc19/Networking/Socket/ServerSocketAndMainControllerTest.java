package it.polimi.ingsw.gc19.Networking.Socket;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Client.Message.Action.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.Chat.PlayerChatMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.Heartbeat.ClientHeartBeatMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.GameConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OwnStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.TableConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkError;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
import it.polimi.ingsw.gc19.Networking.Server.ServerApp;
import it.polimi.ingsw.gc19.Networking.Server.ServerSettings;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ServerSocketAndMainControllerTest {

    private Client client1, client2, client3, client4;
    private ArrayList<Client> stressTestClient;

    @BeforeEach
    public void setUp(){
        ServerSettings.TIME_TO_WAIT_BEFORE_CLIENT_HANDLER_KILL = 20;
        ServerApp.startTCP(ServerSettings.DEFAULT_TCP_SERVER_PORT);
        this.client1 = new Client("client1");
        this.client2 = new Client("client2");
        this.client3 = new Client("client3");
        this.client4 = new Client("client4");
        this.stressTestClient = overloadTest(5);
    }

    @AfterEach
    public void tearDown(){
        this.client1.disconnect();
        this.client1.stopClient();
        this.client2.disconnect();
        this.client2.stopClient();
        this.client3.disconnect();
        this.client3.stopClient();
        this.client4.disconnect();
        this.client4.stopClient();

        for(Client c : this.stressTestClient){
            c.disconnect();
            c.stopClient();
        }

        ServerApp.stopTCP();

        ServerSettings.TIME_TO_WAIT_BEFORE_CLIENT_HANDLER_KILL = 60 * 20;
    }

    private static ArrayList<Client> overloadTest(int numberOfClients){
        ArrayList<Client> stressTestClient = new ArrayList<>();
        for(int i = 0; i < numberOfClients; i++){
            Client client = new Client("client overload " + Integer.toString(i));
            client.createPlayer();
            stressTestClient.add(client);
        }
        return stressTestClient;
    }

    @Test
    public void testCreatePlayer(){
        this.client1.createPlayer();
        assertMessageEquals(this.client1, new CreatedPlayerMessage(this.client1.getName()));
        this.client2.createPlayer();
        assertMessageEquals(this.client2, new CreatedPlayerMessage((this.client2.getName())));
        this.client3.createPlayer();
        assertMessageEquals(this.client3, new CreatedPlayerMessage((this.client3.getName())));
        this.client4.createPlayer();
        assertMessageEquals(this.client4, new CreatedPlayerMessage((this.client4.getName())));

        this.client1.createPlayer();
        assertMessageEquals(this.client1, new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER, null));
    }

    @Test
    public void testMultiplePlayerInGame(){
        this.client1.createPlayer();
        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message1 = this.client1.getMessage(CreatedPlayerMessage.class);
        String token1 = ((CreatedPlayerMessage) message1).getToken();
        this.client1.setToken(token1);

        assertNotNull(client1.getToken());

        this.client1.createGame("game3", 3, 1);

        assertMessageEquals(this.client1, new CreatedGameMessage("game3"));

        this.client2.createPlayer();

        this.client2.joinGame("game3", false);

        assertMessageEquals(this.client2, new JoinedGameMessage("game3").setHeader(this.client2.getName()));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getName()));


        this.client3.createPlayer();

        this.client3.joinGame("game3", false);

        waitingThread(500);

        assertMessageEquals(this.client3, new JoinedGameMessage("game3").setHeader(this.client3.getName()));
        assertMessageEquals(new NewPlayerConnectedToGameMessage(this.client3.getName()), this.client2, this.client1);


        client3.sendChatMessage(new ArrayList<>(List.of(this.client1.getName(), this.client2.getName())), "Message in chat");
        assertMessageEquals(new ArrayList<>(List.of(this.client1, this.client2)), new NotifyChatMessage(this.client3.getName(), "Message in chat"));


        client3.chooseColor(Color.BLUE);
        assertMessageEquals(new ArrayList<>(List.of(this.client3, this.client2, this.client1)), new AcceptedColorMessage(this.client3.getName(), Color.BLUE));
        assertMessageEquals(new ArrayList<>(List.of(this.client2, this.client1)), new AvailableColorsMessage(new ArrayList<>(List.of(Color.GREEN, Color.YELLOW, Color.RED))));
    }

    @Test
    public void testJoinFirstAvailableGames(){
        this.client1.createPlayer();
        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message1 = this.client1.getMessage(CreatedPlayerMessage.class);
        String token1 = ((CreatedPlayerMessage) message1).getToken();
        this.client1.setToken(token1);

        assertNotNull(client1.getToken());

        this.client1.createGame("game4", 2, 1);

        assertMessageEquals(this.client1, new CreatedGameMessage("game4"));

        client1.waitForMessage(GameConfigurationMessage.class);
        client1.clearQueue();


        this.client2.createPlayer();

        this.client2.joinGame("game4", false);

        assertMessageEquals(this.client2, new JoinedGameMessage("game4"));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getName()));


        this.client1.waitForMessage(TableConfigurationMessage.class);
        this.client2.waitForMessage(GameConfigurationMessage.class);
        this.client1.clearQueue();
        this.client2.clearQueue();


        this.client3.createPlayer();

        this.client3.createGame("game7", 2, 1);

        assertMessageEquals(this.client3, new CreatedGameMessage("game7").setHeader(this.client3.getName()));


        this.client4.createPlayer();

        this.client4.joinFirstAvailableGame();

        assertMessageEquals(this.client4, new JoinedGameMessage("game7"));

        assertMessageEquals(this.client3, new NewPlayerConnectedToGameMessage(this.client4.getName()));


        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());

        Client client5 = new Client("client5");
        client5.createPlayer();

        client5.joinFirstAvailableGame();
        assertMessageEquals(new GameHandlingError(Error.NO_GAMES_FREE_TO_JOIN, null));

        client5.disconnect();

    }

    @Test
    public void testMultipleReconnection(){
        this.client1.createPlayer();

        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message1 = this.client1.getMessage(CreatedPlayerMessage.class);
        String token1 = ((CreatedPlayerMessage) message1).getToken();
        this.client1.setToken(token1);

        this.client1.reconnect();
        assertMessageEquals(this.client1, new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER,null));

        this.client1.reconnect();
        assertMessageEquals(this.client1, new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER,null));

        this.client1.createPlayer();
        assertMessageEquals(this.client1, new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER,null));

        this.client1.reconnect();
        assertMessageEquals(this.client1, new NetworkHandlingErrorMessage(NetworkError.CLIENT_ALREADY_CONNECTED_TO_SERVER,null));

        this.client1.clearQueue();
        this.client1.stopSendingHeartBeat();
        waitingThread(3000);
        this.client1.startSendingHeartBeat();
        this.client1.reconnect();
        this.client1.waitForMessage(MessageToClient.class);

        this.client1.createGame("game25", 3, 1);
        assertMessageEquals(this.client1, new CreatedGameMessage("game25"));
        this.client2.createPlayer();
        this.client2.startSendingHeartBeat();
        this.client3.createPlayer();
        this.client3.startSendingHeartBeat();
        this.client2.joinGame("game25", false);
        assertMessageEquals(this.client2, new JoinedGameMessage("game25"));
        this.client3.joinGame("game25", false);
        assertMessageEquals(this.client3, new JoinedGameMessage("game25"));

        this.client1.clearQueue();
        this.client1.stopSendingHeartBeat();
        waitingThread(2500);

        this.client1.sendChatMessage(List.of(this.client2.getName(), this.client3.getName()), "Chat message after heartbeat dead!");
        assertMessageEquals(this.client1, new GameHandlingError(Error.GAME_NOT_FOUND, null));
    }

    @Test
    public void testDisconnectionWhileInLobby(){

        this.client1.createPlayer();

        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message1 = this.client1.getMessage(CreatedPlayerMessage.class);
        String token1 = ((CreatedPlayerMessage) message1).getToken();
        this.client1.setToken(token1);

        this.client2.createPlayer();

        client2.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message2 = this.client2.getMessage(CreatedPlayerMessage.class);
        String token2 = ((CreatedPlayerMessage) message2).getToken();
        this.client2.setToken(token2);

        this.client2.createGame("game11", 2, 1);

        this.client2.stopSendingHeartBeat();

        waitingThread(5000);

        Client client6 = new Client(this.client2.getName());
        client6.createPlayer();
        assertMessageEquals(client6, new GameHandlingError(Error.PLAYER_NAME_ALREADY_IN_USE, null));

        this.client2.reconnect();

        assertMessageEquals(this.client2, new JoinedGameMessage("game11"));

        this.client1.stopSendingHeartBeat();

        waitingThread(5000);

        this.client1.reconnect();

        this.client1.startSendingHeartBeat();

        waitingThread(500);

        assertMessageEquals(this.client1, new AvailableGamesMessage(List.of("game11")));

        this.client1.reconnect();

        this.client1.stopSendingHeartBeat();
        waitingThread(5000);
        Client client7 = new Client(this.client1.getName());
        client7.reconnect();
        assertMessageEquals(client7, new NetworkHandlingErrorMessage(NetworkError.COULD_NOT_RECONNECT, null));

        client7.disconnect();

        Client client8 = new Client(this.client1.getName());
        client8.setToken(token1);
        client8.reconnect();
        assertMessageEquals(this.client1, new AvailableGamesMessage(List.of("game11")));
        client8.disconnect();
    }

    @Test
    public void testPlayerCanJoinFullGame(){
        this.client1.createPlayer();
        this.client1.createGame("game5", 2, 1);
        assertMessageEquals(this.client1, new CreatedGameMessage("game5").setHeader(this.client1.getName()));


        this.client2.createPlayer();
        this.client2.joinGame("game5", false);
        assertMessageEquals(this.client2, new JoinedGameMessage("game5").setHeader(this.client2.getName()));
        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getName()));


        this.client3.createPlayer();
        assertMessageEquals(this.client3, new CreatedPlayerMessage(this.client3.getName()));
        this.client3.joinGame("game5", false);
        assertMessageEquals(this.client3, new GameHandlingError(Error.GAME_NOT_ACCESSIBLE, null));
    }

    @Test
    public void testMultipleGames(){
        this.client1.createPlayer();
        this.client1.createGame("game8", 2, 1);
        assertMessageEquals(this.client1, new CreatedGameMessage("game8"));
        client1.waitForMessage(TableConfigurationMessage.class);
        client1.clearQueue();

        this.client2.createPlayer();
        this.client2.joinGame("game8", false);
        assertMessageEquals(this.client2, new JoinedGameMessage("game8").setHeader(this.client2.getName()));
        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getName()));

        this.client1.waitForMessage(TableConfigurationMessage.class);
        this.client2.waitForMessage(GameConfigurationMessage.class);
        this.client1.clearQueue();
        this.client2.clearQueue();

        this.client3.createPlayer();
        assertMessageEquals(this.client3, new CreatedPlayerMessage(this.client3.getName()));
        this.client3.createGame("game9", 2, 1);
        assertMessageEquals(this.client3, new CreatedGameMessage("game9").setHeader(this.client3.getName()));

        this.client4.createPlayer();
        this.client4.joinGame("game9", false);
        assertMessageEquals(this.client4, new JoinedGameMessage("game9").setHeader(this.client4.getName()));
        assertMessageEquals(this.client3, new NewPlayerConnectedToGameMessage(this.client4.getName()));


        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());

        this.client3.sendChatMessage(new ArrayList<>(List.of(this.client3.getName(), this.client4.getName())), "Message in chat");
        assertMessageEquals(new ArrayList<>(List.of(this.client3, this.client4)), new NotifyChatMessage(this.client3.getName(), "Message in chat"));

        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());
    }

    @Test
    public void testCreateClientAfterDisconnection(){
        Client client7 = new Client("client7");
        client7.createPlayer();

        client7.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message7 = client7.getMessage(CreatedPlayerMessage.class);
        String token7 = ((CreatedPlayerMessage) message7).getToken();

        client7.createGame("game18", 2, 1);
        waitingThread(1500);
        
        client7.disconnect();
        
        Client client8 = new Client(client7.getName());
        client8.createPlayer();
        client8.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message8 = client8.getMessage(CreatedPlayerMessage.class);
        String token8 = ((CreatedPlayerMessage) message8).getToken();

        client8.joinFirstAvailableGame();
        assertMessageEquals(client8, new GameHandlingError(Error.NO_GAMES_FREE_TO_JOIN, null));
    }

    @Test
    public void testFirePlayersAndGames(){

        this.client1.createPlayer();
        this.client2.createPlayer();
        this.client3.createPlayer();
        this.client4.createPlayer();

        this.client1.createGame("game2", 4, 1);
        this.client2.joinGame("game2", true);
        this.client3.joinGame("game2", true);
        this.client4.joinGame("game2", true);

        allPlayersChooseColor(client1, client2, client3, client4);
        allPlayersChoosePrivateGoal(client1, client2, client3, client4);
        allPlayersPlacedInitialCard(client1, client2, client3, client4);

        assertMessageWithHeaderEquals(this.client1,  new StartPlayingGameMessage(this.client1.getName()), "client1", "client2", "client3", "client4");

        assertMessageWithHeaderEquals(this.client1, new TurnStateMessage(this.client1.getName(), TurnState.PLACE), "client1", "client2", "client3", "client4");

        this.client3.disconnect();
        assertMessageWithHeaderEquals(this.client1, new DisconnectedPlayerMessage("client3"), "client1", "client2", "client4");
        this.client4.disconnect();
        assertMessageWithHeaderEquals(this.client1, new DisconnectedPlayerMessage("client4"), "client1", "client2");


        client1.placeCard("resource_23", "initial_05", Direction.UP_RIGHT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyFirstTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_01", "initial_05", Direction.UP_LEFT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("gold_39", "resource_01", Direction.UP_LEFT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("gold_23", "resource_23", Direction.UP_RIGHT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("gold_40", "gold_23", Direction.UP_LEFT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_05", "gold_39", Direction.UP_RIGHT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_03", "resource_05", Direction.UP_RIGHT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("gold_06", "resource_05", Direction.DOWN_RIGHT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("gold_20", "gold_23", Direction.DOWN_RIGHT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_08", "gold_20", Direction.DOWN_RIGHT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_21", "gold_20", Direction.UP_RIGHT, CardOrientation.DOWN);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("gold_28", "resource_08", Direction.DOWN_RIGHT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_30", "gold_28", Direction.UP_RIGHT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 0);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_39", "resource_21", Direction.UP_RIGHT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        //assertFalse(gameController.getGameAssociated().getFinalCondition());
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("gold_24", "resource_21", Direction.DOWN_RIGHT, CardOrientation.UP);
        // client1 reached 20 points: final condition should be true, but not in final round
        //assertTrue(gameController.getGameAssociated().getFinalCondition());
        //assertFalse(gameController.getGameAssociated().isFinalRound());
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.GOLD, 1);

        //assertTrue(gameController.getGameAssociated().getFinalCondition());
        //assertFalse(gameController.getGameAssociated().isFinalRound());
        dummyTurn(client2, PlayableCardType.RESOURCE);

        // now it should be the final round:
        //assertTrue(gameController.getGameAssociated().getFinalCondition());
        //assertTrue(gameController.getGameAssociated().isFinalRound());

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.PLACE), "client1", "client2");
        client1.placeCard("resource_28", "resource_39", Direction.UP_RIGHT, CardOrientation.UP);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getName(), TurnState.DRAW), "client1", "client2");
        client1.pickCardFromTable(PlayableCardType.RESOURCE, 1);

        dummyTurn(client2, PlayableCardType.RESOURCE);

        // game should end and declare client1 the winner
        //assertEquals(GameState.END, gameController.getGameAssociated().getGameState());
        //assertEquals(client1, gameController.getGameAssociated().getWinnerPlayers().getFirst().getName());
        //assertEquals(1, gameController.getGameAssociated().getWinnerPlayers().size());


        waitingThread(4000);

        assertMessageEquals(List.of(this.client2, this.client1), new DisconnectFromGameMessage("game2"));
    }

    @Test
    public void testDisconnectionWhileInGame(){

        this.client1.createPlayer();
        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message = this.client1.getMessage(CreatedPlayerMessage.class);
        String token1 = ((CreatedPlayerMessage) message).getToken();
        this.client1.setToken(token1);
        this.client1.startSendingHeartBeat();

        this.client1.createGame("game6", 2,1);

        assertMessageEquals(this.client1, new CreatedGameMessage("game6"));

        this.client2.createPlayer();
        client2.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message2 = this.client2.getMessage(CreatedPlayerMessage.class);
        String token2 = ((CreatedPlayerMessage) message2).getToken();
        this.client2.setToken(token2);

        this.client2.joinGame("game6", false);

        assertMessageEquals(this.client2, new JoinedGameMessage("game6"));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getName()));


        this.client2.stopSendingHeartBeat();

        waitingThread(5000);

        client2.reconnect();

        this.client2.startSendingHeartBeat();

        //Situation: client 2 has disconnected from game
        assertMessageEquals(client2, new JoinedGameMessage("game6"));

        Client client6 = new Client(this.client2.getName());
        client6.reconnect();
        assertMessageEquals(client6, new NetworkHandlingErrorMessage(NetworkError.COULD_NOT_RECONNECT, null));

        this.client2.sendChatMessage(new ArrayList<>(List.of(this.client1.getName(), this.client2.getName())), "Chat message after disconnection!");
        assertMessageEquals(new ArrayList<>(List.of(this.client1, this.client2)), new NotifyChatMessage(this.client2.getName(), "Chat message after disconnection!"));

        client6.disconnect();
    }

    @Test
    public void testCreateGame(){
        //Client1 tries to create a game without having registered his player
        this.client1.createGame("game1", 3, 1);
        assertMessageEquals(this.client1, new NetworkHandlingErrorMessage(NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER, null));

        this.client1.createPlayer();
        assertMessageEquals(this.client1, new CreatedPlayerMessage(this.client1.getName()));
        this.client1.createGame("game1", 3, 1);
        assertMessageEquals(this.client1, new CreatedGameMessage("game1"));

        this.client2.createPlayer();
        assertMessageEquals(this.client2, new CreatedPlayerMessage(this.client2.getName()));
        this.client2.createGame("game1", 2, 1);
        assertMessageEquals(this.client2, new GameHandlingError(Error.GAME_NAME_ALREADY_IN_USE, null));

        this.client2.joinGame("game1", false);
        assertMessageEquals(this.client2, new JoinedGameMessage("game1"));

        assertMessageEquals(this.client1, new NewPlayerConnectedToGameMessage(this.client2.getName()));

        this.client3.joinGame("game1", false);
        assertMessageEquals(this.client3, new NetworkHandlingErrorMessage(NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER, null));

        this.client3.createPlayer();
        this.client3.joinGame("game1", false);
        assertMessageEquals(this.client3, new JoinedGameMessage("game1"));
        assertMessageEquals(List.of(this.client2, this.client1), new NewPlayerConnectedToGameMessage(this.client3.getName()));
    }

    @Test
    public void testReconnection(){
        this.client1.createPlayer();

        client1.waitForMessage(CreatedPlayerMessage.class);
        MessageToClient message = this.client1.getMessage(CreatedPlayerMessage.class);
        String token1 = ((CreatedPlayerMessage) message).getToken();

        this.client1.createGame("game15", 2, 1);

        client1.waitForMessage(GameConfigurationMessage.class);
        client1.clearQueue();

        this.client2.createPlayer();
        this.client2.joinGame("game15", true);

        client1.waitForMessage(TableConfigurationMessage.class);
        client1.clearQueue();

        this.client1.stopSendingHeartBeat();

        this.client1.closeSocket();

        waitingThread(5000);

        Client client7 = new Client(this.client1.getName());
        client7.setToken(token1);
        client7.reconnect();

        assertMessageEquals(client7, new JoinedGameMessage("game15"));

        assertMessageEquals(client2, new PlayerReconnectedToGameMessage("client1"));

        client7.sendChatMessage(new ArrayList<>(List.of(this.client2.getName())), "Send chat message after reconnection");

        assertMessageEquals(this.client2, new NotifyChatMessage(client7.getName(), "Send chat message after reconnection"));
        assertNull(this.client1.getMessage());

        client2.sendChatMessage(new ArrayList<>(List.of(this.client2.getName(), client1.getName())), "Send chat message after reconnection!");

        assertMessageEquals(List.of(this.client2, client7), new NotifyChatMessage(this.client2.getName(), "Send chat message after reconnection!"));
        assertNull(this.client1.getMessage());

        client7.sendChatMessage(new ArrayList<>(List.of(this.client2.getName())), "Reconnected client 1 message!");
        assertMessageEquals(this.client2, new NotifyChatMessage(client7.getName(), "Reconnected client 1 message!"));

        client7.disconnect();
    }

    @Test
    public void testRequestAvailableGames(){
        client1.createPlayer();
        client2.createPlayer();

        client1.createGame("game25", 3);

        waitingThread(500);

        client2.clearQueue();
        client2.requestAvailableGames();
        assertMessageEquals(client2, new AvailableGamesMessage(List.of("game25")).setHeader(this.client2.getName()));

        client3.createPlayer();
        client3.createGame("game26", 2);
        waitingThread(500);
        client2.clearQueue();
        client2.requestAvailableGames();
        assertMessageEquals(client2, new AvailableGamesMessage(List.of("game25", "game26")).setHeader(this.client2.getName()));
    }

    @Test
    public void testInactiveClientKiller(){
        System.out.println(ServerSettings.TIME_TO_WAIT_BEFORE_CLIENT_HANDLER_KILL);
        client1.createPlayer();
        waitingThread(500);
        client1.stopSendingHeartBeat();
        waitingThread(25 * 1000);
        Client client5 = new Client(this.client1.getName());
        client5.createPlayer();
        assertMessageEquals(client5, new CreatedPlayerMessage(client5.getName()));
        client5.disconnect();
    }

    @Test
    public void testExitFromGame(){
        client1.createPlayer();
        client2.createPlayer();
        client1.createGame("game30", 3, 1);
        client2.joinGame("game30", true);
        waitingThread(500);
        client1.exitFromGame();
        assertMessageEquals(client1, new DisconnectFromGameMessage("game30"));
        assertMessageEquals(client2, new DisconnectedPlayerMessage(this.client1.getName()));
        client1.createGame("game30", 4, 2);
        assertMessageEquals(this.client1, new GameHandlingError(Error.GAME_NAME_ALREADY_IN_USE, null));
        client1.createGame("game31", 3);
        assertMessageEquals(this.client1, new CreatedGameMessage("game31"));
        client2.exitFromGame();
        assertMessageEquals(client2, new DisconnectFromGameMessage("game30"));
        waitingThread(500);
        client2.createGame("game35", 2, 3);
        assertMessageEquals(client2, new CreatedGameMessage("game35"));
        client1.clearQueue();
        client1.joinFirstAvailableGame();
        assertMessageEquals(client1, new GameHandlingError(Error.PLAYER_ALREADY_REGISTERED_TO_SOME_GAME, null));
        client1.exitFromGame();
        assertMessageEquals(client1, new DisconnectFromGameMessage("game31"));
        client1.joinGame("game35", true);
        client3.createPlayer();
        client3.joinGame("game35", false);
        assertMessageEquals(client3, new GameHandlingError(Error.GAME_NOT_ACCESSIBLE, null));
        client1.exitFromGame();
        assertMessageEquals(client1, new DisconnectFromGameMessage("game35"));
        client3.exitFromGame();
        client2.exitFromGame();
        assertMessageEquals(client2, new DisconnectFromGameMessage("game35"));
        client2.createGame("game35", 2, 3);
        assertMessageEquals(client2, new CreatedGameMessage("game35"));
        client1.clearQueue();
    }

    private void dummyTurn(Client client, PlayableCardType cardType){
        dummyPlace(client);
        assertMessageEquals(List.of(this.client1, client2), new TurnStateMessage(this.client2.getName(), TurnState.DRAW));
        client.pickCardFromDeck(cardType);
    }

    private void dummyFirstTurn(Client client, PlayableCardType cardType){
        dummyFirstPlace(client);
        assertMessageEquals(List.of(this.client1, client2), new TurnStateMessage(this.client2.getName(), TurnState.DRAW));
        client.pickCardFromDeck(cardType);
    }

    private void dummyFirstPlace(Client client){
        client.waitForMessage(OwnStationConfigurationMessage.class);
        OwnStationConfigurationMessage latestMessage = (OwnStationConfigurationMessage) client.getMessage(OwnStationConfigurationMessage.class);

        client.setAnchorCard(latestMessage.getInitialCard());
        client.setCardToPlace(latestMessage.getCardsInHand().getFirst());

        // remove other client turns
        assertMessageWithHeaderEquals(client, new TurnStateMessage("client1", TurnState.PLACE), "client1", "client2", "client3", "client4");
        assertMessageWithHeaderEquals(client, new TurnStateMessage("client1", TurnState.DRAW), "client1", "client2");

        assertMessageEquals(List.of(this.client1, client2), new TurnStateMessage(this.client2.getName(), TurnState.PLACE));

        client.placeCard(client.getCardToPlace().getCardCode(), client.getAnchorCard().getCardCode(), Direction.UP_RIGHT, CardOrientation.DOWN);
    }

    private void dummyPlace(Client client){
        AcceptedPickCardMessage latestMessage;
        do {
            client.waitForMessage(AcceptedPickCardMessage.class);
            latestMessage = (AcceptedPickCardMessage) client.getMessage(AcceptedPickCardMessage.class);
        } while (!latestMessage.getNick().equals(client.getName()));

        client.setAnchorCard(client.getCardToPlace());
        client.setCardToPlace(latestMessage.getPickedCard());

        assertMessageWithHeaderEquals(client, new TurnStateMessage("client1", TurnState.PLACE), "client1", "client2");
        assertMessageWithHeaderEquals(client, new TurnStateMessage("client1", TurnState.DRAW), "client1", "client2");

        assertMessageEquals(List.of(this.client1, client2), new TurnStateMessage(this.client2.getName(), TurnState.PLACE));

        client.placeCard(client.getCardToPlace().getCardCode(), client.getAnchorCard().getCardCode(), Direction.UP_RIGHT, CardOrientation.DOWN);
    }

    private void allPlayersPlacedInitialCard(Client client1, Client client2, Client client3, Client client4){
        client1.placeInitialCard(CardOrientation.DOWN);
        client2.placeInitialCard(CardOrientation.DOWN);
        client3.placeInitialCard(CardOrientation.UP);
        client4.placeInitialCard(CardOrientation.DOWN);
    }

    private void allPlayersChoosePrivateGoal(Client client1, Client client2, Client client3, Client client4) {
        client1.choosePrivateGoalCard(0);
        client2.choosePrivateGoalCard(1);
        client3.choosePrivateGoalCard(0);
        client4.choosePrivateGoalCard(1);
    }

    private void allPlayersChooseColor(Client client1, Client client2, Client client3, Client client4){
        client1.chooseColor(Color.RED);
        client2.chooseColor(Color.GREEN);
        client3.chooseColor(Color.BLUE);
        client4.chooseColor(Color.YELLOW);
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

    private void assertMessageWithHeaderEquals(Client receiver, MessageToClient message, String ... header) {
        assertMessageWithHeaderEquals(List.of(receiver), message, header);
    }

    private void assertMessageWithHeaderEquals(List<Client> receivers, MessageToClient message, String ... header) {
        message.setHeader(Arrays.stream(header).toList());
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

class Client{

    private final Socket socket;
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;
    private final Deque<MessageToClient> incomingMessages;
    private String name;
    private Boolean sendHeartBeat;
    private String token;
    private PlayableCard cardToPlace;
    private PlayableCard anchorCard;
    private final ScheduledExecutorService heartBeatThread = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService receiverThread = Executors.newSingleThreadExecutor();

    public Client(String name){
        try{
            this.socket = new Socket(ServerSettings.DEFAULT_SERVER_IP, ServerSettings.DEFAULT_TCP_SERVER_PORT);
            this.outputStream = new ObjectOutputStream(this.socket.getOutputStream());
            this.inputStream = new ObjectInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.name = name;
        this.incomingMessages = new ArrayDeque<>();
        this.sendHeartBeat = true;
        this.token = null;

        this.heartBeatThread.scheduleAtFixedRate(this::heartBeat, 0, 400, TimeUnit.MILLISECONDS);
        this.receiverThread.submit(this::receiveMessages);
    }

    public void sendMessage(MessageToServer message){
        boolean sent = false;
        int numOfTry = 0;
        while(!Thread.interrupted() && !sent && !socket.isClosed() && numOfTry < 25) {
            try {
                synchronized (this.outputStream) {
                    this.outputStream.writeObject(message);
                    finalizeSending();
                }
                sent = true;
            } catch (Exception e) {
                numOfTry++;
            }
        }
    }


    public void closeSocket(){
        try{
            socket.close();
        }
        catch (IOException ioException){

        }
    }

    public void receiveMessages(){
        MessageToClient incomingMessage;
        while (!Thread.interrupted()){
            try{
                incomingMessage = (MessageToClient) this.inputStream.readObject();
            } catch (IOException  | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            synchronized (this.incomingMessages){
                this.incomingMessages.add(incomingMessage);
                this.incomingMessages.notifyAll();
            }
        }
    }

    private void finalizeSending() throws IOException {
        synchronized (this.outputStream){
            this.outputStream.flush();
            this.outputStream.reset();
        }
    }

    private synchronized void heartBeat() {
        if (this.sendHeartBeat && !Thread.interrupted()) {
            this.sendMessage(new ClientHeartBeatMessage(this.name));
        }
    }

    public void stopClient(){
        try {
            this.socket.close();
        }
        catch (IOException ioException){
            throw new RuntimeException(ioException);
        }

        this.receiverThread.shutdownNow();
        this.heartBeatThread.shutdownNow();
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
        this.notifyAll();
    }

    public synchronized void startSendingHeartBeat() {
        this.sendHeartBeat = true;
        this.notifyAll();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int waitAndNotifyTypeOfMessage(Class<? extends MessageToClient> messageToClientClass1, Class<? extends MessageToClient> messageToClientClass2) {
        synchronized (this.incomingMessages) {
            while (this.incomingMessages.stream().noneMatch(messageToClientClass1::isInstance) && this.incomingMessages.stream().noneMatch(messageToClientClass2::isInstance)) {
                try {
                    this.incomingMessages.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if(this.incomingMessages.stream().noneMatch(messageToClientClass1::isInstance)){
                return 1;
            }
            return 0;
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

    public void createPlayer(){
        this.sendMessage(new NewUserMessage(this.name));
        this.startSendingHeartBeat();
    }

    public void disconnect(){
        this.sendMessage(new DisconnectMessage(this.name));
        this.stopSendingHeartBeat();
    }

    public void createGame(String gameName, int numOfPlayers, long randomSeed){
        this.sendMessage(new CreateNewGameMessage(this.name, gameName, numOfPlayers, randomSeed));
    }

    public void createGame(String gameName, int numOfPlayers){
        this.sendMessage(new CreateNewGameMessage(this.name, gameName, numOfPlayers, null));
    }

    public void requestAvailableGames(){
        this.sendMessage(new RequestAvailableGamesMessage(this.name));
    }

    public void joinGame(String gameName, boolean wait){
        boolean found = false;
        if(wait) {
            while (!found) {
                this.sendMessage(new JoinGameMessage(gameName, this.name));
                if (waitAndNotifyTypeOfMessage(GameHandlingError.class, JoinedGameMessage.class) == 1) {
                    found = true;
                }
                else {
                    getMessage(GameHandlingError.class);
                }
            }
        }
        else{
            this.sendMessage(new JoinGameMessage(gameName, this.name));
        }
    }
    
    public void pickCardFromDeck(PlayableCardType type){
        this.sendMessage(new PickCardFromDeckMessage(name, type));
    }
    
    public void pickCardFromTable(PlayableCardType type, int position){
        this.sendMessage(new PickCardFromTableMessage(name, type, position));
    }
    
    public void placeCard(String toPlace, String anchorCode, Direction direction, CardOrientation orientation){
        this.sendMessage(new PlaceCardMessage(name, toPlace, anchorCode, direction, orientation));
    }
    
    public void placeInitialCard(CardOrientation orientation){
        this.sendMessage(new DirectionOfInitialCardMessage(name, orientation));
    }
    
    public void chooseColor(Color color){
        this.sendMessage(new ChosenColorMessage(name, color));
    }
    
    public void choosePrivateGoalCard(int cardIdx){
        this.sendMessage(new ChosenGoalCardMessage(name, cardIdx));
    }

    public void sendChatMessage(List<String> receivers, String message){
        this.sendMessage(new PlayerChatMessage(new ArrayList<>(receivers), name, message));
    }

    public void reconnect(){
        this.sendMessage(new ReconnectToServerMessage(this.name, this.token));
    }

    public void joinFirstAvailableGame(){
        this.sendMessage(new JoinFirstAvailableGameMessage(this.name));
    }

    public void exitFromGame(){
        this.sendMessage(new RequestGameExitMessage(this.name));
    }

}
