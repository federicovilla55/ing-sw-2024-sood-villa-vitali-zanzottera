package it.polimi.ingsw.gc19.View.LocalStateManagement;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.ClientRMI;
import it.polimi.ingsw.gc19.Networking.Client.ClientTCP.ClientTCP;
import it.polimi.ingsw.gc19.Networking.Client.CommonClientMethodsForTests;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.AcceptedPickCardMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OwnStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;
import it.polimi.ingsw.gc19.Networking.Server.ServerApp;
import it.polimi.ingsw.gc19.Networking.Server.ServerSettings;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;
import it.polimi.ingsw.gc19.View.ClientController.ViewState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocalStateManagementTests {

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

    @Test
    public void testCreateClient(){
        assertEquals(clientController1.getState(), ViewState.NOT_PLAYER);
        clientInterface1.connect("client1");
        waitingThread(500);
        assertEquals(ViewState.NOT_GAME, clientController1.getState());
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
