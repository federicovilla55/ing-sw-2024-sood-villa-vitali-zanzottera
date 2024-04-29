package it.polimi.ingsw.gc19.View.ActionParser;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Client.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.Action.ChosenColorMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.Action.PickCardFromDeckMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedPickCardMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.OwnAcceptedPickCardFromDeckMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OwnStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.DisconnectFromGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkError;
import it.polimi.ingsw.gc19.Networking.Server.Message.Network.NetworkHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
import it.polimi.ingsw.gc19.Networking.Server.ServerApp;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.MainServerRMI;
import it.polimi.ingsw.gc19.Networking.Server.ServerRMI.VirtualMainServer;
import it.polimi.ingsw.gc19.Networking.Server.Settings;
import it.polimi.ingsw.gc19.View.GameLocalView.ActionParser;
import it.polimi.ingsw.gc19.View.GameLocalView.ViewState;
import org.junit.jupiter.api.*;

import javax.swing.text.View;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ActionParserTest {

    private static VirtualMainServer virtualMainServer;
    private static Registry registry;
    private static MainServerRMI mainServerRMI;

    private HashMap<ClientInterface, PlayableCard> clientsAnchors;

    private TestClassClientRMI client1, client3, client5;
    private TestClassClientTCP client2, client4;

    private ActionParser actionParser1, actionParser2, actionParser3, actionParser4, actionParser5;

    @BeforeAll
    public static void setUpServer() throws IOException, NotBoundException {
        ServerApp.startRMI(Settings.DEFAULT_RMI_SERVER_PORT);
        ServerApp.startTCP(Settings.DEFAULT_TCP_SERVER_PORT);
        mainServerRMI = ServerApp.getMainServerRMI();
        registry = LocateRegistry.getRegistry("localhost");
        virtualMainServer = (VirtualMainServer) registry.lookup(Settings.mainRMIServerName);
    }

    @BeforeEach
    public void setUpTest() throws IOException {
        this.client1 = new TestClassClientRMI(virtualMainServer, new MessageHandler(new ActionParser()),"client1", new ActionParser());
        this.client2 = new TestClassClientTCP("client2", new MessageHandler(new ActionParser()), new ActionParser());
        this.client3 = new TestClassClientRMI(virtualMainServer, new MessageHandler(new ActionParser()),"client3", new ActionParser());
        this.client4 = new TestClassClientTCP("client4", new MessageHandler(new ActionParser()), new ActionParser());
        this.client5 = new TestClassClientRMI(virtualMainServer, new MessageHandler(new ActionParser()) ,"client5", new ActionParser());
        clientsAnchors = new HashMap<>();

        this.client1.getMessageHandler().setClient(this.client1);
        this.client2.getMessageHandler().setClient(this.client2);
        this.client3.getMessageHandler().setClient(this.client3);
        this.client4.getMessageHandler().setClient(this.client4);
        this.client5.getMessageHandler().setClient(this.client5);

        actionParser1 = this.client1.getMessageHandler().getActionParser();
        actionParser2 = this.client2.getMessageHandler().getActionParser();
        actionParser3 = this.client3.getMessageHandler().getActionParser();
        actionParser4 = this.client4.getMessageHandler().getActionParser();
        actionParser5 = this.client5.getMessageHandler().getActionParser();
    }

    @Test
    public void testNotPlayer(){
        assertEquals(actionParser1.getState(), ViewState.NOTPLAYER);
        assertEquals(actionParser2.getState(), ViewState.NOTPLAYER);
        assertEquals(actionParser3.getState(), ViewState.NOTPLAYER);
        assertEquals(actionParser4.getState(), ViewState.NOTPLAYER);
        assertEquals(actionParser5.getState(), ViewState.NOTPLAYER);
    }

    @Test
    public void testNotGame() throws RemoteException {
        actionParser1.parseAction("create_player(client1)");
        assertEquals(actionParser1.getState(), ViewState.WAIT);
        assertMessageEquals(client1, new CreatedPlayerMessage(client1.getNickname()));
        actionParser1.viewState.nextState(new CreatedPlayerMessage(client1.getNickname()));
        assertEquals(actionParser1.getState(), ViewState.NOTGAME);

        actionParser2.parseAction("create_player(client2)");
        assertEquals(actionParser2.getState(), ViewState.WAIT);
        assertMessageEquals(client2, new CreatedPlayerMessage(client2.getNickname()));
        actionParser2.viewState.nextState(new CreatedPlayerMessage(client2.getNickname()));
        assertEquals(actionParser2.getState(), ViewState.NOTGAME);

        TestClassClientRMI client6 = new TestClassClientRMI(virtualMainServer, new MessageHandler(new ActionParser()) ,"client6", new ActionParser());
        client6.getMessageHandler().setClient(client6);
        ActionParser actionParser6 = client6.getMessageHandler().getActionParser();
        actionParser6.parseAction("create_player(client2)");
        assertEquals(actionParser6.getState(), ViewState.WAIT);
        assertMessageEquals(client6, new GameHandlingError(Error.PLAYER_NAME_ALREADY_IN_USE, ""));
        actionParser6.viewState.nextState(new GameHandlingError(Error.PLAYER_NAME_ALREADY_IN_USE, ""));
        assertEquals(actionParser6.getState(), ViewState.NOTPLAYER);

    }

    @Test
    public void testJoinGame(){
        actionParser1.parseAction("create_player(client1)");
        assertEquals(actionParser1.getState(), ViewState.WAIT);
        assertMessageEquals(client1, new CreatedPlayerMessage(client1.getNickname()));
        actionParser1.viewState.nextState(new CreatedPlayerMessage(client1.getNickname()));
        assertEquals(actionParser1.getState(), ViewState.NOTGAME);

        actionParser2.parseAction("create_player(client2)");
        assertEquals(actionParser2.getState(), ViewState.WAIT);
        assertMessageEquals(client2, new CreatedPlayerMessage(client2.getNickname()));
        actionParser2.viewState.nextState(new CreatedPlayerMessage(client2.getNickname()));
        assertEquals(actionParser2.getState(), ViewState.NOTGAME);

        actionParser1.parseAction("create_game(GAME1, 2)");
        assertEquals(actionParser1.getState(), ViewState.WAIT);
        assertMessageEquals(client1, new CreatedGameMessage("GAME1"));
        actionParser1.viewState.nextState(new CreatedGameMessage("GAME1"));
        assertEquals(actionParser1.getState(), ViewState.SETUP);

        actionParser2.parseAction("join_first_game()");
        assertEquals(actionParser2.getState(), ViewState.WAIT);
        assertMessageEquals(client2, new JoinedGameMessage("GAME1"));
        actionParser2.viewState.nextState(new JoinedGameMessage("GAME1"));
        assertEquals(actionParser2.getState(), ViewState.SETUP);
    }

    @Test
    public void testSetup(){
        actionParser1.parseAction("create_player(client1)");
        assertEquals(actionParser1.getState(), ViewState.WAIT);
        assertMessageEquals(client1, new CreatedPlayerMessage(client1.getNickname()));
        actionParser1.viewState.nextState(new CreatedPlayerMessage(client1.getNickname()));
        assertEquals(actionParser1.getState(), ViewState.NOTGAME);

        actionParser2.parseAction("create_player(client2)");
        assertEquals(actionParser2.getState(), ViewState.WAIT);
        assertMessageEquals(client2, new CreatedPlayerMessage(client2.getNickname()));
        actionParser2.viewState.nextState(new CreatedPlayerMessage(client2.getNickname()));
        assertEquals(actionParser2.getState(), ViewState.NOTGAME);

        actionParser3.parseAction("create_player(client3)");
        assertEquals(actionParser3.getState(), ViewState.WAIT);
        assertMessageEquals(client3, new CreatedPlayerMessage(client3.getNickname()));
        actionParser3.viewState.nextState(new CreatedPlayerMessage(client3.getNickname()));
        assertEquals(actionParser3.getState(), ViewState.NOTGAME);

        actionParser4.parseAction("create_player(client4)");
        assertEquals(actionParser4.getState(), ViewState.WAIT);
        assertMessageEquals(client4, new CreatedPlayerMessage(client4.getNickname()));
        actionParser4.viewState.nextState(new CreatedPlayerMessage(client4.getNickname()));
        assertEquals(actionParser4.getState(), ViewState.NOTGAME);

        actionParser1.parseAction("create_game_seed(GAME1, 4, 1)");
        assertEquals(actionParser1.getState(), ViewState.WAIT);
        assertMessageEquals(client1, new CreatedGameMessage("GAME1"));
        actionParser1.viewState.nextState(new CreatedGameMessage("GAME1"));
        assertEquals(actionParser1.getState(), ViewState.SETUP);

        actionParser2.parseAction("join_first_game()");
        assertEquals(actionParser2.getState(), ViewState.WAIT);
        assertMessageEquals(client2, new JoinedGameMessage("GAME1"));
        actionParser2.viewState.nextState(new JoinedGameMessage("GAME1"));
        assertEquals(actionParser2.getState(), ViewState.SETUP);

        actionParser3.parseAction("join_first_game()");
        assertEquals(actionParser3.getState(), ViewState.WAIT);
        assertMessageEquals(client3, new JoinedGameMessage("GAME1"));
        actionParser3.viewState.nextState(new JoinedGameMessage("GAME1"));
        assertEquals(actionParser3.getState(), ViewState.SETUP);

        actionParser4.parseAction("join_first_game()");
        assertEquals(actionParser4.getState(), ViewState.WAIT);
        assertMessageEquals(client4, new JoinedGameMessage("GAME1"));
        actionParser4.viewState.nextState(new JoinedGameMessage("GAME1"));
        assertEquals(actionParser4.getState(), ViewState.SETUP);

        allPlayersPlacedInitialCard(actionParser1, actionParser2, actionParser3, actionParser4);
        allPlayersChooseColor(actionParser1, actionParser2, actionParser3, actionParser4);
        allPlayersChoosePrivateGoal(actionParser1, actionParser2, actionParser3, actionParser4);

        assertMessageWithHeaderEquals(this.client1,  new StartPlayingGameMessage(this.client1.getNickname()), "client1", "client2", "client3", "client4");

        assertMessageWithHeaderEquals(this.client1, new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2", "client3", "client4");

        System.out.println("Controllo Client1 PLACE");

        actionParser1.viewState.nextState(new StartPlayingGameMessage(this.client1.getNickname()));
        actionParser2.viewState.nextState(new StartPlayingGameMessage(this.client1.getNickname()));
        actionParser3.viewState.nextState(new StartPlayingGameMessage(this.client1.getNickname()));
        actionParser4.viewState.nextState(new StartPlayingGameMessage(this.client1.getNickname()));

        assertEquals(actionParser1.getState(), ViewState.PLACE);
        assertEquals(actionParser2.getState(), ViewState.OTHERTURN);
        assertEquals(actionParser3.getState(), ViewState.OTHERTURN);
        assertEquals(actionParser4.getState(), ViewState.OTHERTURN);

        actionParser1.parseAction("place_card(resource_23, initial_05, UP_RIGHT, DOWN)");
        assertEquals(actionParser1.getState(), ViewState.WAIT);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2", "client3", "client4");
        actionParser1.viewState.nextState(new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW));
        assertEquals(actionParser1.getState(), ViewState.PICK);

        actionParser1.parseAction("pick_card_table(GOLD, 1)");
        assertEquals(actionParser1.getState(), ViewState.WAIT);
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client2.getNickname(), TurnState.PLACE), "client1", "client2", "client3", "client4");
        actionParser1.viewState.nextState(new TurnStateMessage(this.client2.getNickname(), TurnState.PLACE));
        assertEquals(actionParser1.getState(), ViewState.OTHERTURN);
        actionParser2.viewState.nextState(new TurnStateMessage(this.client2.getNickname(), TurnState.PLACE));
        assertEquals(actionParser2.getState(), ViewState.PLACE);
        assertEquals(actionParser3.getState(), ViewState.OTHERTURN);
        assertEquals(actionParser4.getState(), ViewState.OTHERTURN);

        dummyFirstTurn(client2, actionParser2, PlayableCardType.RESOURCE);

        System.out.println("Dopo la client2 siamo in client1 PLACE?");
        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client3.getNickname(), TurnState.PLACE), "client1", "client2", "client3", "client4");

        actionParser1.viewState.nextState(new TurnStateMessage(this.client3.getNickname(), TurnState.PLACE));
        actionParser2.viewState.nextState(new TurnStateMessage(this.client3.getNickname(), TurnState.PLACE));
        actionParser3.viewState.nextState(new TurnStateMessage(this.client3.getNickname(), TurnState.PLACE));
        actionParser4.viewState.nextState(new TurnStateMessage(this.client3.getNickname(), TurnState.PLACE));
        assertEquals(actionParser1.getState(), ViewState.OTHERTURN);
        assertEquals(actionParser2.getState(), ViewState.OTHERTURN);
        assertEquals(actionParser3.getState(), ViewState.PLACE);
        assertEquals(actionParser4.getState(), ViewState.OTHERTURN);
    }

    @Test
    public void testDisconnect(){
        actionParser1.parseAction("create_player(client1)");
        assertEquals(actionParser1.getState(), ViewState.WAIT);
        MessageToClient message1 = assertMessageEquals(client1, new CreatedPlayerMessage(client1.getNickname()));
        client1.setToken(((CreatedPlayerMessage) message1).getToken());
        client1.setNickname(((CreatedPlayerMessage) message1).getNick());
        actionParser1.viewState.nextState(new CreatedPlayerMessage(client1.getNickname()));
        assertEquals(actionParser1.getState(), ViewState.NOTGAME);

        actionParser2.parseAction("create_player(client2)");
        assertEquals(actionParser2.getState(), ViewState.WAIT);
        MessageToClient message2 = assertMessageEquals(client2, new CreatedPlayerMessage(client2.getNickname()));
        String token2 = ((CreatedPlayerMessage) message2).getToken();
        System.out.println(token2);
        client2.setToken(token2);
        client2.setNickname(((CreatedPlayerMessage) message2).getNick());
        actionParser2.viewState.nextState(new CreatedPlayerMessage(client2.getNickname()));
        assertEquals(actionParser2.getState(), ViewState.NOTGAME);

        actionParser1.parseAction("create_game_seed(GAME1, 2, 1)");
        assertEquals(actionParser1.getState(), ViewState.WAIT);
        assertMessageEquals(client1, new CreatedGameMessage("GAME1"));
        actionParser1.viewState.nextState(new CreatedGameMessage("GAME1"));
        assertEquals(actionParser1.getState(), ViewState.SETUP);

        actionParser2.parseAction("join_first_game()");
        assertEquals(actionParser2.getState(), ViewState.WAIT);
        assertMessageEquals(client2, new JoinedGameMessage("GAME1"));
        actionParser2.viewState.nextState(new JoinedGameMessage("GAME1"));
        assertEquals(actionParser2.getState(), ViewState.SETUP);

        allPlayersPlacedInitialCard(actionParser1, actionParser2);
        allPlayersChooseColor(actionParser1, actionParser2);
        allPlayersChoosePrivateGoal(actionParser1, actionParser2);

        assertMessageWithHeaderEquals(this.client1,  new StartPlayingGameMessage(this.client1.getNickname()), "client1", "client2");

        assertMessageWithHeaderEquals(this.client1, new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");

        actionParser1.viewState.nextState(new StartPlayingGameMessage(this.client1.getNickname()));
        actionParser2.viewState.nextState(new StartPlayingGameMessage(this.client1.getNickname()));

        assertEquals(actionParser1.getState(), ViewState.PLACE);
        assertEquals(actionParser2.getState(), ViewState.OTHERTURN);

        client2.stopSendingHeartbeat();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Fine wait");

        assertMessageEquals(client1, new DisconnectedPlayerMessage(this.client2.getNickname()));

        actionParser1.viewState.nextState(new GamePausedMessage());
        assertEquals(actionParser1.getState(), ViewState.PAUSE);

        actionParser2.disconnect();
        assertEquals(actionParser2.getState(), ViewState.DISCONNECT);

        assertMessageEquals(client1, new GamePausedMessage());

        System.out.println("Fine wait");
        actionParser2.viewState.nextState(new JoinedGameMessage("GAME1"));
        assertEquals(actionParser2.getState(), ViewState.OTHERTURN);

        System.out.println("Fine wait");
        assertMessageWithHeaderEquals(client1, new GameResumedMessage(), "client1", "client2");
        //assertMessageEquals(client1, new GameResumedMessage());
        System.out.println("Fine wait");
        actionParser1.viewState.nextState(new GameResumedMessage());
        assertEquals(actionParser1.getState(), ViewState.PLACE);
    }

    @Test
    public void testEndGame(){
        actionParser1.parseAction("create_player(client1)");
        assertEquals(actionParser1.getState(), ViewState.WAIT);
        assertMessageEquals(client1, new CreatedPlayerMessage(client1.getNickname()));
        actionParser1.viewState.nextState(new CreatedPlayerMessage(client1.getNickname()));
        assertEquals(actionParser1.getState(), ViewState.NOTGAME);

        actionParser2.parseAction("create_player(client2)");
        assertEquals(actionParser2.getState(), ViewState.WAIT);
        assertMessageEquals(client2, new CreatedPlayerMessage(client2.getNickname()));
        actionParser2.viewState.nextState(new CreatedPlayerMessage(client2.getNickname()));
        assertEquals(actionParser2.getState(), ViewState.NOTGAME);

        actionParser3.parseAction("create_player(client3)");
        assertEquals(actionParser3.getState(), ViewState.WAIT);
        assertMessageEquals(client3, new CreatedPlayerMessage(client3.getNickname()));
        actionParser3.viewState.nextState(new CreatedPlayerMessage(client3.getNickname()));
        assertEquals(actionParser3.getState(), ViewState.NOTGAME);

        actionParser4.parseAction("create_player(client4)");
        assertEquals(actionParser4.getState(), ViewState.WAIT);
        assertMessageEquals(client4, new CreatedPlayerMessage(client4.getNickname()));
        actionParser4.viewState.nextState(new CreatedPlayerMessage(client4.getNickname()));
        assertEquals(actionParser4.getState(), ViewState.NOTGAME);


        actionParser1.parseAction("create_game_seed(GAME1, 4, 1)");
        assertEquals(actionParser1.getState(), ViewState.WAIT);
        assertMessageEquals(client1, new CreatedGameMessage("GAME1"));
        actionParser1.viewState.nextState(new CreatedGameMessage("GAME1"));
        assertEquals(actionParser1.getState(), ViewState.SETUP);

        actionParser2.parseAction("join_first_game()");
        assertEquals(actionParser2.getState(), ViewState.WAIT);
        assertMessageEquals(client2, new JoinedGameMessage("GAME1"));
        actionParser2.viewState.nextState(new JoinedGameMessage("GAME1"));
        assertEquals(actionParser2.getState(), ViewState.SETUP);

        actionParser3.parseAction("join_first_game()");
        assertEquals(actionParser3.getState(), ViewState.WAIT);
        assertMessageEquals(client3, new JoinedGameMessage("GAME1"));
        actionParser3.viewState.nextState(new JoinedGameMessage("GAME1"));
        assertEquals(actionParser3.getState(), ViewState.SETUP);

        actionParser4.parseAction("join_first_game()");
        assertEquals(actionParser4.getState(), ViewState.WAIT);
        assertMessageEquals(client4, new JoinedGameMessage("GAME1"));
        actionParser4.viewState.nextState(new JoinedGameMessage("GAME1"));
        assertEquals(actionParser4.getState(), ViewState.SETUP);

        allPlayersPlacedInitialCard(actionParser1, actionParser2, actionParser3, actionParser4);
        allPlayersChooseColor(actionParser1, actionParser2, actionParser3, actionParser4);
        allPlayersChoosePrivateGoal(actionParser1, actionParser2, actionParser3, actionParser4);

        assertMessageWithHeaderEquals(this.client1,  new StartPlayingGameMessage(this.client1.getNickname()), "client1", "client2", "client3", "client4");

        assertMessageWithHeaderEquals(this.client1, new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2", "client3", "client4");

        actionParser1.viewState.nextState(new StartPlayingGameMessage(this.client1.getNickname()));
        actionParser2.viewState.nextState(new StartPlayingGameMessage(this.client1.getNickname()));
        actionParser3.viewState.nextState(new StartPlayingGameMessage(this.client1.getNickname()));
        actionParser4.viewState.nextState(new StartPlayingGameMessage(this.client1.getNickname()));

        assertEquals(actionParser1.getState(), ViewState.PLACE);
        assertEquals(actionParser2.getState(), ViewState.OTHERTURN);

        client3.stopSendingHeartbeat();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Fine wait");

        assertMessageWithHeaderEquals(client1, new DisconnectedPlayerMessage(this.client3.getNickname()), "client1", "client2", "client4");
        assertEquals(actionParser1.getState(), ViewState.PLACE);
        assertEquals(actionParser2.getState(), ViewState.OTHERTURN);
        assertEquals(actionParser4.getState(), ViewState.OTHERTURN);

        client4.stopSendingHeartbeat();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Fine wait");

        assertMessageWithHeaderEquals(client1, new DisconnectedPlayerMessage(this.client4.getNickname()), "client1", "client2");
        assertEquals(actionParser1.getState(), ViewState.PLACE);
        assertEquals(actionParser2.getState(), ViewState.OTHERTURN);


        actionParser1.parseAction("place_card(resource_23, initial_05, UP_RIGHT, DOWN)");
        assertEquals(actionParser1.getState(), ViewState.WAIT);
        TurnStateMessage message = (TurnStateMessage) assertMessageWithHeaderEquals(client1, new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        actionParser1.viewState.nextState(message);
        assertEquals(actionParser1.getState(), ViewState.PICK);
        actionParser1.parseAction("pick_card_table(GOLD, 1)");
        assertEquals(actionParser1.getState(), ViewState.WAIT);
        message = (TurnStateMessage) assertMessageWithHeaderEquals(client1, new TurnStateMessage(this.client2.getNickname(), TurnState.PLACE), "client1", "client2");
        actionParser1.viewState.nextState(message);
        actionParser2.viewState.nextState(message);
        assertEquals(actionParser1.getState(), ViewState.OTHERTURN);

        firstTurn(client2, actionParser2, PlayableCardType.RESOURCE);

        message = (TurnStateMessage) assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        actionParser1.viewState.nextState(message);
        actionParser2.viewState.nextState(message);
        assertEquals(actionParser1.getState(), ViewState.PLACE);
        assertEquals(actionParser2.getState(), ViewState.OTHERTURN);

        actionParser1.parseAction("place_card(resource_01, initial_05, UP_LEFT, UP)");
        assertEquals(actionParser1.getState(), ViewState.WAIT);
        message = (TurnStateMessage) assertMessageWithHeaderEquals(client1, new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        actionParser1.viewState.nextState(message);
        assertEquals(actionParser1.getState(), ViewState.PICK);
        actionParser1.parseAction("pick_card_table(GOLD, 1)");
        assertEquals(actionParser1.getState(), ViewState.WAIT);
        message = (TurnStateMessage) assertMessageWithHeaderEquals(client1, new TurnStateMessage(this.client2.getNickname(), TurnState.PLACE), "client1", "client2");
        actionParser1.viewState.nextState(message);
        actionParser2.viewState.nextState(message);
        assertEquals(actionParser1.getState(), ViewState.OTHERTURN);

        dummyTurn(client2, actionParser2, PlayableCardType.RESOURCE);

        executeMainPlayerTurn("gold_39", "resource_01", Direction.UP_LEFT, CardOrientation.DOWN, PlayableCardType.GOLD, 1);
        dummyTurn(client2, actionParser2, PlayableCardType.RESOURCE);

        executeMainPlayerTurn("gold_23", "resource_23", Direction.UP_RIGHT, CardOrientation.UP, PlayableCardType.GOLD, 1);
        dummyTurn(client2, actionParser2, PlayableCardType.RESOURCE);
        // gold_23, resource_23, UP_RIGHT, UP

        executeMainPlayerTurn("gold_40", "gold_23", Direction.UP_LEFT, CardOrientation.DOWN, PlayableCardType.RESOURCE, 0);
        dummyTurn(client2, actionParser2, PlayableCardType.RESOURCE);
        // gold_40, gold_23, UP_LEFT, DOWN

        executeMainPlayerTurn("resource_05", "gold_39", Direction.UP_RIGHT, CardOrientation.DOWN, PlayableCardType.RESOURCE, 0);
        dummyTurn(client2, actionParser2, PlayableCardType.RESOURCE);
        // resource_05, gold_39, UP_RIGHT DOWN

        executeMainPlayerTurn("resource_03", "resource_05", Direction.UP_RIGHT, CardOrientation.DOWN, PlayableCardType.RESOURCE, 0);
        dummyTurn(client2, actionParser2, PlayableCardType.RESOURCE);

        executeMainPlayerTurn("gold_06", "resource_05", Direction.DOWN_RIGHT, CardOrientation.UP, PlayableCardType.GOLD, 1);
        dummyTurn(client2, actionParser2, PlayableCardType.RESOURCE);

        executeMainPlayerTurn("gold_20", "gold_23", Direction.DOWN_RIGHT, CardOrientation.DOWN, PlayableCardType.RESOURCE, 0);
        dummyTurn(client2, actionParser2, PlayableCardType.RESOURCE);

        executeMainPlayerTurn("resource_08", "gold_20", Direction.DOWN_RIGHT, CardOrientation.DOWN, PlayableCardType.RESOURCE, 1);
        dummyTurn(client2, actionParser2, PlayableCardType.RESOURCE);

        executeMainPlayerTurn("resource_21", "gold_20", Direction.UP_RIGHT, CardOrientation.DOWN, PlayableCardType.RESOURCE, 0);
        dummyTurn(client2, actionParser2, PlayableCardType.RESOURCE);

        executeMainPlayerTurn("gold_28", "resource_08", Direction.DOWN_RIGHT, CardOrientation.UP, PlayableCardType.RESOURCE, 0);
        dummyTurn(client2, actionParser2, PlayableCardType.RESOURCE);

        executeMainPlayerTurn("resource_30", "gold_28", Direction.UP_RIGHT, CardOrientation.UP, PlayableCardType.GOLD, 0);
        dummyTurn(client2, actionParser2, PlayableCardType.RESOURCE);

        executeMainPlayerTurn("resource_39", "resource_21", Direction.UP_RIGHT, CardOrientation.UP, PlayableCardType.GOLD, 1);
        dummyTurn(client2, actionParser2, PlayableCardType.RESOURCE);

        executeMainPlayerTurn("gold_24", "resource_21", Direction.DOWN_RIGHT, CardOrientation.UP, PlayableCardType.GOLD, 1);
        dummyTurn(client2, actionParser2, PlayableCardType.RESOURCE);

        executeMainPlayerTurn("resource_28", "resource_39", Direction.UP_RIGHT, CardOrientation.UP, PlayableCardType.RESOURCE, 1);
        dummyTurn(client2, actionParser2, PlayableCardType.RESOURCE);

        waitingThread(4000);

        HashMap<String, Integer> endingPoins = new HashMap<>();
        endingPoins.put("client1", 25);
        endingPoins.put("client2", 2);
        endingPoins.put("client3", 0);
        endingPoins.put("client4", 0);
        EndGameMessage endGameMessage = (EndGameMessage) assertMessageWithHeaderEquals(client1, new EndGameMessage(List.of(client1.getNickname()), endingPoins), "client1", "client2");

        actionParser1.viewState.nextState(endGameMessage);
        actionParser2.viewState.nextState(endGameMessage);
        assertEquals(actionParser1.getState(), ViewState.END);
        assertEquals(actionParser2.getState(), ViewState.END);

        assertMessageEquals(List.of(this.client2, this.client1), new DisconnectFromGameMessage("GAME1"));
    }

    @Test
    public void testErrorHandling(){
        actionParser1.parseAction("create_player(client1)");
        assertEquals(actionParser1.getState(), ViewState.WAIT);
        MessageToClient message1 = assertMessageEquals(client1, new CreatedPlayerMessage(client1.getNickname()));
        client1.setToken(((CreatedPlayerMessage) message1).getToken());
        client1.setNickname(((CreatedPlayerMessage) message1).getNick());
        actionParser1.viewState.nextState(new CreatedPlayerMessage(client1.getNickname()));
        assertEquals(actionParser1.getState(), ViewState.NOTGAME);

        actionParser2.parseAction("create_player(client2)");
        assertEquals(actionParser2.getState(), ViewState.WAIT);
        MessageToClient message2 = assertMessageEquals(client2, new CreatedPlayerMessage(client2.getNickname()));
        String token2 = ((CreatedPlayerMessage) message2).getToken();
        System.out.println(token2);
        client2.setToken(token2);
        client2.setNickname(((CreatedPlayerMessage) message2).getNick());
        actionParser2.viewState.nextState(new CreatedPlayerMessage(client2.getNickname()));
        assertEquals(actionParser2.getState(), ViewState.NOTGAME);

        actionParser1.parseAction("create_game_seed(GAME1, 2, 1)");
        assertEquals(actionParser1.getState(), ViewState.WAIT);
        assertMessageEquals(client1, new CreatedGameMessage("GAME1"));
        actionParser1.viewState.nextState(new CreatedGameMessage("GAME1"));
        assertEquals(actionParser1.getState(), ViewState.SETUP);

        actionParser2.parseAction("join_first_game()");
        assertEquals(actionParser2.getState(), ViewState.WAIT);
        assertMessageEquals(client2, new JoinedGameMessage("GAME1"));
        actionParser2.viewState.nextState(new JoinedGameMessage("GAME1"));
        assertEquals(actionParser2.getState(), ViewState.SETUP);

        allPlayersPlacedInitialCard(actionParser1, actionParser2);
        allPlayersChooseColor(actionParser1, actionParser2);
        allPlayersChoosePrivateGoal(actionParser1, actionParser2);

        assertMessageWithHeaderEquals(this.client1,  new StartPlayingGameMessage(this.client1.getNickname()), "client1", "client2");

        assertMessageWithHeaderEquals(this.client1, new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");

        actionParser1.viewState.nextState(new StartPlayingGameMessage(this.client1.getNickname()));
        actionParser2.viewState.nextState(new StartPlayingGameMessage(this.client1.getNickname()));

        assertEquals(actionParser1.getState(), ViewState.PLACE);
        assertEquals(actionParser2.getState(), ViewState.OTHERTURN);

        //actionParser1.viewState.nextState(new NetworkHandlingErrorMessage(NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER, ""));

        assertEquals(actionParser1.getState(), ViewState.PLACE);
        actionParser1.viewState.nextState(new NetworkHandlingErrorMessage(NetworkError.CLIENT_NOT_REGISTERED_TO_SERVER, ""));
        assertEquals(actionParser1.getState(), ViewState.NOTPLAYER);
    }

    @AfterEach
    public void resetTest(){
        this.client1.getMessageHandler().interrupt();
        this.client2.getMessageHandler().interrupt();
        this.client3.getMessageHandler().interrupt();
        this.client4.getMessageHandler().interrupt();
        this.client5.getMessageHandler().interrupt();

        this.client1.disconnect();
        this.client2.disconnect();
        this.client2.stopClient();
        this.client3.disconnect();
        this.client4.disconnect();
        this.client4.stopClient();
        mainServerRMI.resetServer();
    }

    @AfterAll
    public static void resetServer(){
        ServerApp.unexportRegistry();
        ServerApp.stopTCP();
    }

    public void executeMainPlayerTurn(String cardToPlace, String cardAnchor, Direction dir, CardOrientation orientation, PlayableCardType cardType, Integer tablePosition){
        TurnStateMessage message = (TurnStateMessage) assertMessageWithHeaderEquals(client1, new TurnStateMessage(this.client1.getNickname(), TurnState.PLACE), "client1", "client2");
        actionParser1.viewState.nextState(message);
        actionParser2.viewState.nextState(message);
        assertEquals(actionParser2.getState(), ViewState.OTHERTURN);
        assertEquals(actionParser1.getState(), ViewState.PLACE);


        actionParser1.parseAction("place_card("+cardToPlace+", "+cardAnchor+", "+dir.toString()+", "+orientation.toString()+")");
        assertEquals(actionParser1.getState(), ViewState.WAIT);
        message = (TurnStateMessage) assertMessageWithHeaderEquals(client1, new TurnStateMessage(this.client1.getNickname(), TurnState.DRAW), "client1", "client2");
        actionParser1.viewState.nextState(message);
        assertEquals(actionParser1.getState(), ViewState.PICK);
        actionParser1.parseAction("pick_card_table("+cardType.toString()+", "+tablePosition.toString()+")");
        assertEquals(actionParser1.getState(), ViewState.WAIT);
        message = (TurnStateMessage) assertMessageWithHeaderEquals(client1, new TurnStateMessage(this.client2.getNickname(), TurnState.PLACE), "client1", "client2");
        actionParser1.viewState.nextState(message);
        actionParser2.viewState.nextState(message);
        assertEquals(actionParser1.getState(), ViewState.OTHERTURN);

    }


    private MessageToClient assertMessageEquals(List<CommonClientMethodsForTests> receivers, MessageToClient message) {
        List<String> receiversName = receivers.stream().map(CommonClientMethodsForTests::getNickname).toList();
        message.setHeader(receiversName);
        MessageToClient retMessage = null;
        for (CommonClientMethodsForTests receiver : receivers) {
            receiver.waitForMessage(message.getClass());
            retMessage = receiver.getMessage(message.getClass());
            assertEquals(message, retMessage);
        }
        return retMessage;
    }

    private void dummyTurn(ClientInterface client, CommonClientMethodsForTests commonAction, PlayableCardType cardType){
        dummyPlace(client, commonAction);
        client.pickCardFromDeck(cardType);
    }



    private void dummyFirstTurn(ClientInterface client, CommonClientMethodsForTests commonAction, PlayableCardType cardType){
        dummyFirstPlace(client, commonAction);
        client.pickCardFromDeck(cardType);
    }

    private void dummyFirstTurn(TestClassClientTCP client, ActionParser actionParser, PlayableCardType cardType){
        client.waitForMessage(OwnStationConfigurationMessage.class);
        OwnStationConfigurationMessage latestMessage = (OwnStationConfigurationMessage) client.getMessage(OwnStationConfigurationMessage.class);

        clientsAnchors.put(client, latestMessage.getCardsInHand().getFirst());
        actionParser.parseAction("place_card("+latestMessage.getCardsInHand().getFirst().getCardCode()+
                                        ", "+ latestMessage.getInitialCard().getCardCode() + ", UP_RIGHT, DOWN)");
        assertEquals(actionParser.getState(), ViewState.WAIT);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(client.getNickname(), TurnState.DRAW), "client1", "client2", "client3", "client4");
        actionParser.viewState.nextState(new TurnStateMessage(client.getNickname(), TurnState.DRAW));
        assertEquals(actionParser.getState(), ViewState.PICK);

        actionParser.parseAction("pick_card_deck(" + cardType.toString() + ")");
        assertEquals(actionParser.getState(), ViewState.WAIT);

        client.waitForMessage(OwnAcceptedPickCardFromDeckMessage.class);
        OwnAcceptedPickCardFromDeckMessage pickedCardMessage = (OwnAcceptedPickCardFromDeckMessage) client.getMessage(OwnAcceptedPickCardFromDeckMessage.class);
        actionParser.viewState.nextState(pickedCardMessage);
        assertEquals(actionParser.getState(), ViewState.OTHERTURN);
    }

    private void firstTurn(TestClassClientTCP client, ActionParser actionParser, PlayableCardType cardType){
        assertEquals(actionParser.getState(), ViewState.PLACE);
        client.waitForMessage(OwnStationConfigurationMessage.class);
        OwnStationConfigurationMessage latestMessage = (OwnStationConfigurationMessage) client.getMessage(OwnStationConfigurationMessage.class);

        clientsAnchors.put(client, latestMessage.getCardsInHand().getFirst());
        /*System.out.println("Cosa Piazzo: " + latestMessage.getCardsInHand().getFirst().getCardCode());
        System.out.println("Cosa Initial: " + latestMessage.getInitialCard().getCardCode());
        System.out.println("place_card("+latestMessage.getCardsInHand().getFirst().getCardCode()+
                ", "+ latestMessage.getInitialCard().getCardCode() + ", UP_RIGHT, DOWN)");*/
        clientsAnchors.put(client, latestMessage.getCardsInHand().getFirst());
        actionParser.parseAction("place_card("+latestMessage.getCardsInHand().getFirst().getCardCode()+
                ", "+ latestMessage.getInitialCard().getCardCode() + ", UP_RIGHT, DOWN)");
        assertEquals(actionParser.getState(), ViewState.WAIT);

        assertMessageWithHeaderEquals(List.of(this.client1), new TurnStateMessage(client.getNickname(), TurnState.DRAW), "client1", "client2");
        actionParser.viewState.nextState(new TurnStateMessage(client.getNickname(), TurnState.DRAW));
        assertEquals(actionParser.getState(), ViewState.PICK);

        actionParser.parseAction("pick_card_deck(" + cardType.toString() + ")");
        assertEquals(actionParser.getState(), ViewState.WAIT);
        //System.out.println("Fine primo dummy turn");
    }


    private void dummyFirstPlace(ClientInterface client, CommonClientMethodsForTests commonAction){
        commonAction.waitForMessage(OwnStationConfigurationMessage.class);
        OwnStationConfigurationMessage latestMessage = (OwnStationConfigurationMessage) commonAction.getMessage(OwnStationConfigurationMessage.class);

        client.placeCard(latestMessage.getCardsInHand().getFirst().getCardCode(), latestMessage.getInitialCard().getCardCode(), Direction.UP_RIGHT, CardOrientation.DOWN);
        clientsAnchors.put(client, latestMessage.getCardsInHand().getFirst());
    }

    private void allPlayersChooseColor(ActionParser actionParser1, ActionParser actionParser2){
        actionParser1.parseAction("choose_color(RED)");
        actionParser2.parseAction("choose_color(GREEN)");
    }

    private void allPlayersChoosePrivateGoal(ActionParser actionParser1, ActionParser actionParser2){
        actionParser1.parseAction("choose_goal(0)");
        actionParser2.parseAction("choose_goal(1)");
    }

    private void allPlayersPlacedInitialCard(ActionParser actionParser1, ActionParser actionParser2){
        actionParser1.parseAction("place_initial_card(DOWN)");
        actionParser2.parseAction("place_initial_card(DOWN)");
    }

    private void dummyTurn(TestClassClientTCP client, CommonClientMethodsForTests commonAction, PlayableCardType cardType){
        dummyPlace(client, commonAction);
        assertMessageEquals(List.of(this.client1, client2), new TurnStateMessage(this.client2.getNickname(), TurnState.DRAW));
        client.pickCardFromDeck(cardType);
    }

    private void dummyFirstTurn(TestClassClientTCP client, CommonClientMethodsForTests commonAction, PlayableCardType cardType){
        dummyFirstPlace(client, commonAction);
        assertMessageEquals(List.of(this.client1, client2), new TurnStateMessage(this.client2.getNickname(), TurnState.DRAW));
        client.pickCardFromDeck(cardType);
    }

    private void dummyTurn(TestClassClientTCP client, ActionParser actionParser, PlayableCardType cardType){
        assertEquals(actionParser.getState(), ViewState.PLACE);
        String anchor = clientsAnchors.get(client).getCardCode();
        AcceptedPickCardMessage latestMessage;
        do {
            client.waitForMessage(AcceptedPickCardMessage.class);
            latestMessage = (AcceptedPickCardMessage) client.getMessage(AcceptedPickCardMessage.class);
        } while (!latestMessage.getNick().equals(client.getNickname()));

        assertMessageWithHeaderEquals(client, new TurnStateMessage("client1", TurnState.PLACE), "client1", "client2");
        assertMessageWithHeaderEquals(client, new TurnStateMessage("client1", TurnState.DRAW), "client1", "client2");

        assertMessageWithHeaderEquals(client, new TurnStateMessage(client.getNickname(), TurnState.PLACE), "client1", "client2");

        /*System.out.println("Cosa Piazzo: " + latestMessage.getPickedCard().getCardCode());
        System.out.println("Cosa Ancora: " + anchor);
        System.out.println("place_card(" + latestMessage.getPickedCard().getCardCode() +
                ", "+ anchor + ", UP_RIGHT, DOWN)");*/
        clientsAnchors.put(client, latestMessage.getPickedCard());
        actionParser.parseAction("place_card(" + latestMessage.getPickedCard().getCardCode() +
                ", "+ anchor + ", UP_RIGHT, DOWN)");
        assertEquals(actionParser.getState(), ViewState.WAIT);

        //System.out.println("Ecco la assert...");
        TurnStateMessage message = (TurnStateMessage) assertMessageWithHeaderEquals(List.of(client1, client2), new TurnStateMessage(client.getNickname(), TurnState.DRAW), "client1", "client2");
        actionParser.viewState.nextState(message);
        assertEquals(actionParser.getState(), ViewState.PICK);

        actionParser.parseAction("pick_card_deck(" + cardType.toString() + ")");
        assertEquals(actionParser.getState(), ViewState.WAIT);

        //System.out.println("Fine dummy turn");
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

    private void allPlayersChooseColor(ActionParser actionParser1, ActionParser actionParser2, ActionParser actionParser3, ActionParser actionParser4){
        actionParser1.parseAction("choose_color(RED)");
        actionParser2.parseAction("choose_color(GREEN)");
        actionParser3.parseAction("choose_color(BLUE)");
        actionParser4.parseAction("choose_color(YELLOW)");
    }

    private void allPlayersChoosePrivateGoal(ActionParser actionParser1, ActionParser actionParser2, ActionParser actionParser3, ActionParser actionParser4){
        actionParser1.parseAction("choose_goal(0)");
        actionParser2.parseAction("choose_goal(1)");
        actionParser3.parseAction("choose_goal(0)");
        actionParser4.parseAction("choose_goal(1)");
    }

    private void allPlayersPlacedInitialCard(ActionParser actionParser1, ActionParser actionParser2, ActionParser actionParser3, ActionParser actionParser4){
        actionParser1.parseAction("place_initial_card(DOWN)");
        actionParser2.parseAction("place_initial_card(DOWN)");
        actionParser3.parseAction("place_initial_card(DOWN)");
        actionParser4.parseAction("place_initial_card(DOWN)");
    }

    private MessageToClient assertMessageEquals(CommonClientMethodsForTests receiver, MessageToClient message) {
        return assertMessageEquals(List.of(receiver), message);
    }

    private void assertMessageEquals(MessageToClient message, CommonClientMethodsForTests... receivers) {
        ArrayList<CommonClientMethodsForTests> receiversName = Arrays.stream(receivers).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        assertMessageEquals(receiversName, message);
    }


    private MessageToClient assertMessageWithHeaderEquals(CommonClientMethodsForTests receiver, MessageToClient message, String ... header) {
        return assertMessageWithHeaderEquals(List.of(receiver), message, header);
    }

    private MessageToClient assertMessageWithHeaderEquals(List<CommonClientMethodsForTests> receivers, MessageToClient message, String ... header) {
        MessageToClient retMsg = null;
        message.setHeader(Arrays.stream(header).toList());
        for (CommonClientMethodsForTests receiver : receivers) {
            receiver.waitForMessage(message.getClass());
            retMsg = receiver.getMessage(message.getClass());
            assertEquals(message, retMsg);
        }

        return retMsg;
    }

    private void waitingThread(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
