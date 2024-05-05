package it.polimi.ingsw.gc19.View.LocalStateManagement;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.ClientRMI;
import it.polimi.ingsw.gc19.Networking.Client.ClientSettings;
import it.polimi.ingsw.gc19.Networking.Client.ClientTCP.ClientTCP;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageHandler;
import it.polimi.ingsw.gc19.Networking.Server.ServerApp;
import it.polimi.ingsw.gc19.Networking.Server.ServerSettings;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class LocalStateManagementTest {

    private ClientInterface clientInterface1, clientInterface2, clientInterface3, clientInterface4;
    private ClientController clientController1, clientController2, clientController3, clientController4;
    private MessageHandler messageHandler1, messageHandler2, messageHandler3, messageHandler4;

    @BeforeEach
    public void setUp() throws IOException {
        ServerApp.startRMI(ServerSettings.DEFAULT_RMI_SERVER_PORT);
        ServerApp.startTCP(ServerSettings.DEFAULT_TCP_SERVER_PORT);

        clientController1 = new ClientController();
        clientController2 = new ClientController();
        clientController3 = new ClientController();
        clientController4 = new ClientController();

        messageHandler1 = new MessageHandler(clientController1);
        messageHandler2 = new MessageHandler(clientController2);
        messageHandler3 = new MessageHandler(clientController3);
        messageHandler4 = new MessageHandler(clientController4);

        clientInterface1 = new ClientTCP(messageHandler1, clientController1);
        clientController1.setClientInterface(clientInterface1);
        messageHandler1.setClient(clientInterface1);
        messageHandler1.start();

        clientInterface2 = new ClientRMI(messageHandler2, clientController2);
        clientController2.setClientInterface(clientInterface2);
        messageHandler2.setClient(clientInterface2);
        messageHandler2.start();

        clientInterface3 = new ClientRMI(messageHandler3, clientController3);
        clientController3.setClientInterface(clientInterface3);
        messageHandler3.setClient(clientInterface3);
        messageHandler3.start();

        clientInterface4 = new ClientTCP(messageHandler4, clientController4);
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
        ClientInterface clientInterface5 = new ClientTCP(messageHandler5, clientController5);
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

    private void waitingThread(long millis){
        try{
            TimeUnit.MILLISECONDS.sleep(millis);
        }
        catch (InterruptedException interruptedException){
            throw new RuntimeException(interruptedException);
        }
    }

}
