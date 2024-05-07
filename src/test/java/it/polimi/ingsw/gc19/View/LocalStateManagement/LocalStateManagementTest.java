package it.polimi.ingsw.gc19.View.LocalStateManagement;

import it.polimi.ingsw.gc19.Controller.JSONParser;
import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.ClientRMI;
import it.polimi.ingsw.gc19.Networking.Client.ClientSettings;
import it.polimi.ingsw.gc19.Networking.Client.ClientTCP.ClientTCP;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OtherStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OwnStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.TableConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.NewPlayerConnectedToGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.ServerApp;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import it.polimi.ingsw.gc19.View.ClientController.Wait;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class LocalStateManagementTest {

    private Map<String, PlayableCard> playableCards;
    private Map<String, GoalCard> goalCards;
    private ClientInterface clientInterface1, clientInterface2, clientInterface3, clientInterface4;
    private ClientController clientController1, clientController2, clientController3, clientController4;
    private MessageHandler messageHandler1, messageHandler2, messageHandler3, messageHandler4;

    @BeforeEach
    public void setUp() throws IOException {
        try {
            this.playableCards = JSONParser.readPlayableCardFromFile().collect(Collectors.toMap(Card::getCardCode, p -> p));
            this.goalCards = JSONParser.readGoalCardFromFile().collect(Collectors.toMap(Card::getCardCode, p -> p));
        }
        catch(IOException e){
            e.printStackTrace();
        }

        ServerApp.startRMI();
        ServerApp.startTCP();

        clientController1 = new ClientController();
        clientController2 = new ClientController();
        clientController3 = new ClientController();
        clientController4 = new ClientController();

        messageHandler1 = new MessageHandler(clientController1);
        messageHandler2 = new MessageHandler(clientController2);
        messageHandler3 = new MessageHandler(clientController3);
        messageHandler4 = new MessageHandler(clientController4);

        clientInterface1 = new ClientTCP(messageHandler1);
        clientController1.setClientInterface(clientInterface1);
        messageHandler1.setClient(clientInterface1);
        messageHandler1.start();

        clientInterface2 = new ClientRMI(messageHandler2);
        clientController2.setClientInterface(clientInterface2);
        messageHandler2.setClient(clientInterface2);
        messageHandler2.start();

        clientInterface3 = new ClientRMI(messageHandler3);
        clientController3.setClientInterface(clientInterface3);
        messageHandler3.setClient(clientInterface3);
        messageHandler3.start();

        clientInterface4 = new ClientTCP(messageHandler4);
        clientController4.setClientInterface(clientInterface4);
        messageHandler4.setClient(clientInterface4);
        messageHandler4.start();
    }

    @AfterEach
    public void tearDown(){
        File configFile = new File(ClientSettings.CONFIG_FILE_PATH);
        for(File f : Objects.requireNonNull(configFile.listFiles())){
            f.delete();
        }

        ServerApp.stopTCP();
        ServerApp.stopRMI();

        clientInterface1.stopClient();
        messageHandler1.interruptMessageHandler();
        clientInterface2.stopClient();
        messageHandler2.interruptMessageHandler();
        clientInterface3.stopClient();
        messageHandler3.interruptMessageHandler();
        clientInterface4.stopClient();
        messageHandler4.interruptMessageHandler();
    }

    @Test
    public void testCreateClient() throws IOException {
        assertEquals(clientController1.getState(), ViewState.NOT_PLAYER);
        clientController1.createPlayer("client1");
        waitingThread(500);
        assertEquals(ViewState.NOT_GAME, clientController1.getState());

        assertEquals(clientController2.getState(), ViewState.NOT_PLAYER);
        clientController2.createPlayer("client2");
        waitingThread(500);
        assertEquals(ViewState.NOT_GAME, clientController2.getState());

        ClientController clientController5 = new ClientController();
        MessageHandler messageHandler5 = new MessageHandler(clientController5);
        ClientInterface clientInterface5 = new ClientTCP(messageHandler5);
        clientController5.setClientInterface(clientInterface5);
        messageHandler5.setClient(clientInterface5);
        messageHandler5.start();

        clientController5.createPlayer("client1");
        waitingThread(500);
        assertEquals(ViewState.NOT_PLAYER, clientController5.getState());

        clientInterface5.stopClient();
        messageHandler5.interruptMessageHandler();
    }

    @Test
    public void testCreateGame(){
        assertEquals(clientController1.getState(), ViewState.NOT_PLAYER);
        clientController1.createGame("game1", 3);
        assertEquals(clientController1.getState(), ViewState.NOT_PLAYER);

        assertEquals(clientController1.getState(), ViewState.NOT_PLAYER);
        clientController1.createPlayer("client1");
        waitingThread(500);
        assertEquals(ViewState.NOT_GAME, clientController1.getState());

        clientController1.createGame("game1", 3);
        waitingThread(500);
        assertEquals(ViewState.SETUP, clientController1.getState());

        clientController1.createGame("game3", 2);
        waitingThread(500);
        assertEquals(ViewState.SETUP, clientController1.getState());
    }

    @Test
    public void testJoinGame(){
        assertEquals(clientController1.getState(), ViewState.NOT_PLAYER);
        clientController1.createPlayer("client1");
        waitingThread(500);
        assertEquals(ViewState.NOT_GAME, clientController1.getState());

        clientController1.createGame("game1", 3);
        waitingThread(500);
        assertEquals(ViewState.SETUP, clientController1.getState());

        assertEquals(clientController2.getState(), ViewState.NOT_PLAYER);
        clientController2.createPlayer("client2");
        waitingThread(500);
        assertEquals(ViewState.NOT_GAME, clientController2.getState());

        clientController2.joinGame("game1");
        waitingThread(500);
        assertEquals(ViewState.SETUP, clientController2.getState());

        assertEquals(clientController3.getState(), ViewState.NOT_PLAYER);
        clientController3.createPlayer("client3");
        waitingThread(500);
        assertEquals(ViewState.NOT_GAME, clientController3.getState());

        clientController3.joinGame("game1");
        waitingThread(500);
        assertEquals(ViewState.SETUP, clientController3.getState());

        assertEquals(clientController4.getState(), ViewState.NOT_PLAYER);
        clientController4.createPlayer("client4");
        waitingThread(500);
        assertEquals(ViewState.NOT_GAME, clientController4.getState());

        clientController4.joinGame("game1");
        waitingThread(500);
        assertEquals(ViewState.NOT_GAME, clientController4.getState());
    }

    @Test
    public void testJoinFirstAvailableGame(){
        assertEquals(clientController1.getState(), ViewState.NOT_PLAYER);
        clientController1.createPlayer("client1");
        waitingThread(500);
        assertEquals(ViewState.NOT_GAME, clientController1.getState());

        clientController1.createGame("game1", 3);
        waitingThread(500);
        assertEquals(ViewState.SETUP, clientController1.getState());

        assertEquals(clientController2.getState(), ViewState.NOT_PLAYER);
        clientController2.createPlayer("client2");
        waitingThread(500);
        assertEquals(ViewState.NOT_GAME, clientController2.getState());

        clientController2.joinFirstAvailableGame();
        waitingThread(500);
        assertEquals(ViewState.SETUP, clientController2.getState());

        assertEquals(clientController3.getState(), ViewState.NOT_PLAYER);
        clientController3.createPlayer("client3");
        waitingThread(500);
        assertEquals(ViewState.NOT_GAME, clientController3.getState());

        clientController3.joinFirstAvailableGame();
        waitingThread(500);
        assertEquals(ViewState.SETUP, clientController3.getState());

        assertEquals(clientController4.getState(), ViewState.NOT_PLAYER);
        clientController4.createPlayer("client4");
        waitingThread(500);
        assertEquals(ViewState.NOT_GAME, clientController4.getState());

        clientController4.joinFirstAvailableGame();
        waitingThread(500);
        assertEquals(ViewState.NOT_GAME, clientController4.getState());
    }

    @Test
    public void testGameSetup(){
        clientController1.createPlayer("client1");
        waitingThread(500);
        assertEquals(ViewState.NOT_GAME, clientController1.getState());

        clientController1.createGame("game1", 2);
        waitingThread(500);
        assertEquals(ViewState.SETUP, clientController1.getState());

        clientController2.createPlayer("client2");
        waitingThread(500);
        assertEquals(ViewState.NOT_GAME, clientController2.getState());

        clientController2.joinFirstAvailableGame();
        waitingThread(500);
        assertEquals(ViewState.SETUP, clientController2.getState());

        clientController1.chooseColor(Color.BLUE);
        waitingThread(500);
        assertEquals(ViewState.SETUP, clientController1.getState());

        clientController2.chooseColor(Color.RED);
        waitingThread(500);
        assertEquals(ViewState.SETUP, clientController2.getState());

        clientController1.chooseGoal(1);
        clientController2.chooseGoal(0);

        clientController1.placeInitialCard(CardOrientation.UP);
        clientController2.placeInitialCard(CardOrientation.UP);

        waitingThread(500);

        assertNotEquals(ViewState.SETUP, clientController2.getState());
        assertNotEquals(ViewState.SETUP, clientController1.getState());
    }

    @Test
    public void testGamePause(){
        clientController1.createPlayer("client1");
        waitingThread(500);
        assertEquals(ViewState.NOT_GAME, clientController1.getState());

        clientController1.createGame("game1", 2);
        waitingThread(500);
        assertEquals(ViewState.SETUP, clientController1.getState());

        clientController2.createPlayer("client2");
        waitingThread(500);
        assertEquals(ViewState.NOT_GAME, clientController2.getState());

        clientController2.joinFirstAvailableGame();
        waitingThread(500);
        assertEquals(ViewState.SETUP, clientController2.getState());

        clientController1.chooseColor(Color.BLUE);
        waitingThread(500);
        assertEquals(ViewState.SETUP, clientController1.getState());

        clientController2.chooseColor(Color.RED);
        waitingThread(500);
        assertEquals(ViewState.SETUP, clientController2.getState());

        clientController1.chooseGoal(1);
        clientController2.chooseGoal(0);

        clientController1.placeInitialCard(CardOrientation.UP);
        clientController2.placeInitialCard(CardOrientation.UP);

        waitingThread(500);

        clientController1.logoutFromGame();

        waitingThread(500);
        assertEquals(ViewState.PAUSE, clientController2.getState());
    }

    @Test
    public void testReconnection(){
        clientController1.createPlayer("client1");
        waitingThread(500);
        assertEquals(ViewState.NOT_GAME, clientController1.getState());

        ServerApp.stopTCP();

        waitingThread(32500);

        assertEquals(ViewState.DISCONNECT, clientController1.getState());
    }

    @Test
    public void testSetup(){
        clientController1.setNextState(new Wait(clientController1, clientInterface1));
        this.messageHandler1.update(new CreatedPlayerMessage("player1"));
        clientController2.setNextState(new Wait(clientController2, clientInterface2));
        this.messageHandler2.update(new CreatedPlayerMessage("player2"));

        waitingThread(500);

        clientController1.setNextState(new Wait(clientController1, clientInterface1));
        this.messageHandler1.update(new CreatedGameMessage("game1"));

        waitingThread(500);

        assertNotNull(clientController1.getLocalModel());

        assertEquals("player1", this.clientController1.getLocalModel().getNickname());

        messageHandler1.update(new TableConfigurationMessage(
                playableCards.get("resource_05").setCardState(CardOrientation.UP),
                playableCards.get("resource_21").setCardState(CardOrientation.UP),
                playableCards.get("gold_19").setCardState(CardOrientation.UP),
                playableCards.get("gold_23").setCardState(CardOrientation.UP),
                goalCards.get("goal_11"),
                goalCards.get("goal_15"),
                Symbol.VEGETABLE,
                Symbol.ANIMAL
        ));

        waitingThread(500);

        assertEquals(goalCards.get("goal_11"), clientController1.getLocalModel().getTable().getPublicGoal1());

        messageHandler1.update(new OwnStationConfigurationMessage(
                "player1",
                null,
                List.of(
                        playableCards.get("resource_23"),
                        playableCards.get("resource_01"),
                        playableCards.get("gold_28")
                ),
                Map.of(
                        Symbol.ANIMAL, 0,
                        Symbol.MUSHROOM, 0,
                        Symbol.VEGETABLE, 0,
                        Symbol.INSECT, 0,
                        Symbol.INK, 0,
                        Symbol.FEATHER, 0,
                        Symbol.SCROLL, 0
                ),
                null,
                0,
                playableCards.get("initial_05"),
                goalCards.get("goal_09"),
                goalCards.get("goal_14"),
                List.of()
        ));

        waitingThread(500);
        assertNotNull(clientController1.getLocalModel().getPersonalStation());
        assertEquals(List.of(goalCards.get("goal_09"), goalCards.get("goal_14")), List.of(clientController1.getLocalModel().getPersonalStation().getPrivateGoalCardsInStation()));

        clientController2.setNextState(new Wait(clientController2, clientInterface2));
        this.messageHandler2.update(new JoinedGameMessage("game1"));

        messageHandler1.update(new NewPlayerConnectedToGameMessage("player2"));

        //assertEquals(2, clientController1.getLocalModel().getNumPlayers());

        messageHandler1.update(new OtherStationConfigurationMessage(
                "player2",
                null,
                List.of(
                        Symbol.VEGETABLE,
                        Symbol.INSECT,
                        Symbol.ANIMAL
                ),
                Map.of(
                        Symbol.ANIMAL, 0,
                        Symbol.MUSHROOM, 0,
                        Symbol.VEGETABLE, 0,
                        Symbol.INSECT, 0,
                        Symbol.INK, 0,
                        Symbol.FEATHER, 0,
                        Symbol.SCROLL, 0
                ),
                0,
                List.of()
        ));

        messageHandler1.update(new TableConfigurationMessage(
                playableCards.get("resource_05").setCardState(CardOrientation.UP),
                playableCards.get("resource_21").setCardState(CardOrientation.UP),
                playableCards.get("gold_19").setCardState(CardOrientation.UP),
                playableCards.get("gold_23").setCardState(CardOrientation.UP),
                goalCards.get("goal_11"),
                goalCards.get("goal_15"),
                Symbol.ANIMAL,
                Symbol.VEGETABLE
        ));

        messageHandler2.update(new TableConfigurationMessage(
                playableCards.get("resource_05").setCardState(CardOrientation.UP),
                playableCards.get("resource_21").setCardState(CardOrientation.UP),
                playableCards.get("gold_19").setCardState(CardOrientation.UP),
                playableCards.get("gold_23").setCardState(CardOrientation.UP),
                goalCards.get("goal_11"),
                goalCards.get("goal_15"),
                Symbol.ANIMAL,
                Symbol.VEGETABLE
        ));

        messageHandler2.update(new OwnStationConfigurationMessage(
                "player2",
                null,
                List.of(
                        playableCards.get("resource_15"),
                        playableCards.get("resource_37"),
                        playableCards.get("gold_21")
                ),
                Map.of(
                        Symbol.ANIMAL, 0,
                        Symbol.MUSHROOM, 0,
                        Symbol.VEGETABLE, 0,
                        Symbol.INSECT, 0,
                        Symbol.INK, 0,
                        Symbol.FEATHER, 0,
                        Symbol.SCROLL, 0
                ),
                null,
                0,
                playableCards.get("initial_01"),
                goalCards.get("goal_16"),
                goalCards.get("goal_01"),
                List.of()
        ));

        messageHandler2.update(new OtherStationConfigurationMessage(
                "player1",
                null,
                List.of(
                        Symbol.ANIMAL,
                        Symbol.MUSHROOM,
                        Symbol.ANIMAL
                ),
                Map.of(
                        Symbol.ANIMAL, 0,
                        Symbol.MUSHROOM, 0,
                        Symbol.VEGETABLE, 0,
                        Symbol.INSECT, 0,
                        Symbol.INK, 0,
                        Symbol.FEATHER, 0,
                        Symbol.SCROLL, 0
                ),
                0,
                List.of()
        ));

/*

        assertMessageEquals(player2,
                            new AvailableColorsMessage(List.of(Color.values())));

        assertMessageEquals(player2,
                            new GameConfigurationMessage(
                                    GameState.SETUP,
                                    null,
                                    null,
                                    null,
                                    false,
                                    4
                            ));

        this.mainController.registerToGame(player3, "game1");
        tableConfigurationMessage = new TableConfigurationMessage(
                playableCards.get("resource_05").setCardState(CardOrientation.UP),
                playableCards.get("resource_21").setCardState(CardOrientation.UP),
                playableCards.get("gold_19").setCardState(CardOrientation.UP),
                playableCards.get("gold_23").setCardState(CardOrientation.UP),
                goalCards.get("goal_11"),
                goalCards.get("goal_15"),
                Symbol.VEGETABLE,
                Symbol.MUSHROOM
        );

        assertMessageEquals(List.of(player1,player2), new NewPlayerConnectedToGameMessage("player3"));
        assertMessageEquals(List.of(player1,player2),
                            new OtherStationConfigurationMessage(
                                    "player3",
                                    null,
                                    Map.of(
                                            Symbol.ANIMAL, 0,
                                            Symbol.MUSHROOM, 0,
                                            Symbol.VEGETABLE, 0,
                                            Symbol.INSECT, 0,
                                            Symbol.INK, 0,
                                            Symbol.FEATHER, 0,
                                            Symbol.SCROLL, 0
                                    ),
                                    0,
                                    List.of()
                            ));
        assertMessageEquals(List.of(player1,player2),tableConfigurationMessage);

        assertEquals(new JoinedGameMessage("game1").setHeader(player3.getUsername()), player3.getMessage());

        assertMessageEquals(player3, tableConfigurationMessage);

        assertMessageEquals(player3,
                            new OwnStationConfigurationMessage(
                                    "player3",
                                    null,
                                    List.of(
                                            playableCards.get("resource_24"),
                                            playableCards.get("resource_09"),
                                            playableCards.get("gold_14")
                                    ),
                                    Map.of(
                                            Symbol.ANIMAL, 0,
                                            Symbol.MUSHROOM, 0,
                                            Symbol.VEGETABLE, 0,
                                            Symbol.INSECT, 0,
                                            Symbol.INK, 0,
                                            Symbol.FEATHER, 0,
                                            Symbol.SCROLL, 0
                                    ),
                                    null,
                                    0,
                                    playableCards.get("initial_06"),
                                    goalCards.get("goal_07"),
                                    goalCards.get("goal_06"),
                                    List.of()
                            ));
        for(String nickname : List.of("player1","player2")) {
            assertMessageEquals(player3,
                                new OtherStationConfigurationMessage(
                                        nickname,
                                        null,
                                        Map.of(
                                                Symbol.ANIMAL, 0,
                                                Symbol.MUSHROOM, 0,
                                                Symbol.VEGETABLE, 0,
                                                Symbol.INSECT, 0,
                                                Symbol.INK, 0,
                                                Symbol.FEATHER, 0,
                                                Symbol.SCROLL, 0
                                        ),
                                        0,
                                        List.of()
                                ));
        }
        assertMessageEquals(player3,
                            new AvailableColorsMessage(List.of(Color.values())));
        assertMessageEquals(player3,
                            new GameConfigurationMessage(
                                    GameState.SETUP,
                                    null,
                                    null,
                                    null,
                                    false,
                                    4
                            ));

        this.mainController.registerToGame(player4, "game1");
        this.clearQueue(List.of(player1,player2,player3,player4));*/
    }



    private void waitingThread(long millis){
        try{
            TimeUnit.MILLISECONDS.sleep(millis);
        }
        catch (InterruptedException interruptedException){
            throw new RuntimeException(interruptedException);
        }
    }

}
