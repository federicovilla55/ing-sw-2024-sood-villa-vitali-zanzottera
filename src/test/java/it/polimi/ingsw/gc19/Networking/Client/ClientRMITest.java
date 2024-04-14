package it.polimi.ingsw.gc19.Networking.Client;

import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.ClientRMI;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.ServerApp;
import it.polimi.ingsw.gc19.Networking.Server.Settings;
import it.polimi.ingsw.gc19.Networking.Server.VirtualMainServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ClientRMITest {
    private static VirtualMainServer virtualMainServer;
    private static Registry registry;

    private ClientRMI client1, client2, client3, client4, client5;

    @BeforeAll
    public static void setUpServer() throws IOException, NotBoundException {
        ServerApp.startRMI();
        registry = LocateRegistry.getRegistry("localhost");
        virtualMainServer = (VirtualMainServer) registry.lookup(Settings.mainRMIServerName);
    }

    @BeforeEach
    public void setUpTest() throws RemoteException {
        this.client1 = new ClientRMI(virtualMainServer);
        this.client2 = new ClientRMI(virtualMainServer);
        this.client3 = new ClientRMI(virtualMainServer);
        this.client4 = new ClientRMI(virtualMainServer);
        this.client5 = new ClientRMI(virtualMainServer);
    }

    @Test
    public void testClientCreation() throws InterruptedException {
        this.client1.connect("Client1");
        assertMessageEquals(this.client1, new CreatedPlayerMessage("Client1"));
    }

    @Test
    public void testCreateClient() throws RemoteException, InterruptedException {
        this.client1.connect("client1");
        assertMessageEquals(this.client1, new CreatedPlayerMessage("client1"));
        this.client2.connect("client2");

        assertMessageEquals(this.client2, new CreatedPlayerMessage("client2"));
        assertNull(this.client1.getMessage());
        this.client3.connect("client3");

        assertMessageEquals(this.client3, new CreatedPlayerMessage("client3"));
        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());
        this.client4.connect("client4");

        assertMessageEquals(this.client4, new CreatedPlayerMessage("client4"));
        assertNull(this.client1.getMessage());
        assertNull(this.client2.getMessage());
        assertNull(this.client3.getMessage());

        this.client1.connect("client1");

        assertMessageEquals(this.client1, new GameHandlingError(Error.CLIENT_ALREADY_CONNECTED_TO_SERVER, null));
        assertNull(this.client2.getMessage());
        assertNull(this.client3.getMessage());
        assertNull(this.client4.getMessage());

        //Create new client with other name
        this.client5.setNickname("client1");
        this.client5.connect("client1");

        assertMessageEquals(this.client5, new GameHandlingError(Error.PLAYER_NAME_ALREADY_IN_USE, null));
    }

    @AfterEach
    public void resetClients() throws RemoteException {
        this.client1.disconnect();
        this.client2.disconnect();

        virtualMainServer.resetMainServer();
    }

    @AfterAll
    public static void tearDownServer() {
        ServerApp.unexportRegistry();
    }


    private void assertMessageEquals(ClientRMI receiver, MessageToClient message) {
        assertMessageEquals(List.of(receiver), message);
    }

    private void assertMessageEquals(MessageToClient message, ClientRMI... receivers) {
        ArrayList<ClientRMI> receiversName = Arrays.stream(receivers).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        assertMessageEquals(receiversName, message);
    }

    private void assertMessageEquals(List<ClientRMI> receivers, MessageToClient message) {
        List<String> receiversName;
        receiversName = receivers.stream().map(ClientRMI::getNickname).toList();
        message.setHeader(receiversName);
        for (ClientRMI receiver : receivers) {
            receiver.waitForMessage(message.getClass());
            assertEquals(message, receiver.getMessage(message.getClass()));
        }
    }

}
