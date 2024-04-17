package it.polimi.ingsw.gc19.Networking.Socket;

import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.ClientRMI;
import it.polimi.ingsw.gc19.Networking.Client.ClientTCP.ClientTCP;
import it.polimi.ingsw.gc19.Networking.Client.Message.Chat.PlayerChatMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.CreateNewGameMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.JoinGameMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.NewUserMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.ServerApp;
import it.polimi.ingsw.gc19.Networking.Server.Settings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
public class ServerTCPTest{
    @BeforeAll
    public static void startServer() throws IOException {
        ServerApp.startTCP();
        ServerApp.startRMI();
    }

    @Test
    public void firstTCPConnection() throws IOException {
        Socket socket = null;
        try {
            socket = new Socket(Settings.DEFAULT_SERVER_IP, Settings.DEFAULT_SERVER_PORT);
        }
        catch (IOException ignored){ };

        assert socket != null;
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());

        new Thread(){
            @Override
            public void run() {
                while(true){
                    try{
                        MessageToClient message = (MessageToClient) inputStream.readObject();
                        System.out.println(message);
                    }
                    catch (IOException  | ClassNotFoundException ignored){ };
                }
            }
        }.start();

        outputStream.writeObject(new NewUserMessage("Matteo"));
        outputStream.flush();
        outputStream.reset();

        try{
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void TCPAndRMIConnection() throws IOException{
        /*ClientRMI clientRMI1 = new ClientRMI(ServerApp.instance);

        Socket socket = null;
        try {
            socket = new Socket(Settings.DEFAULT_SERVER_IP, Settings.DEFAULT_SERVER_PORT);
        }
        catch (IOException ignored){ };

        assert socket != null;
        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

        new Thread(){
            @Override
            public void run() {
                while(true){
                    try{
                        MessageToClient message = (MessageToClient) inputStream.readObject();
                        //System.out.println(message);
                        //System.out.println("Messaggio ricevuto");
                    }
                    catch (IOException  | ClassNotFoundException ignored){ };
                }
            }
        }.start();

        outputStream.writeObject(new NewUserMessage("Client1"));
        outputStream.flush();
        outputStream.reset();

        try{
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //System.out.println("Ora rifacciamo con RMI...");

        clientRMI1.connect("Client1");

        assertMessageEquals(clientRMI1, new GameHandlingError(Error.PLAYER_NAME_ALREADY_IN_USE, ""));

        //outputStream.writeObject(new CreateNewGameMessage("game", 2));

        clientRMI1.clearMessages();

        ClientRMI clientRMI2 = new ClientRMI(ServerApp.instance);
        clientRMI2.connect("Client2");

        assertMessageEquals(clientRMI2, new CreatedPlayerMessage("Client2"));

        clientRMI2.clearMessages();
        clientRMI2.createGame("Game1", 2);

        assertMessageEquals(clientRMI2, new JoinedGameMessage("Game1"));

        outputStream.writeObject(new JoinGameMessage("Game1", "Client1"));
        outputStream.flush();
        outputStream.reset();

        //System.out.println("---------------------------");

        try{
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        clientRMI2.clearMessages();
        clientRMI1.clearMessages();

        ArrayList<String> receivers = new ArrayList<>(Arrays.asList("Client1", "Client2"));
        outputStream.writeObject(new PlayerChatMessage(receivers, "Client1", "Ciao, come va?"));
        outputStream.flush();
        outputStream.reset();

        try{
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //System.out.println("||||||||||||||||||");

        //assertMessageEquals(List.of(clientRMI2), new NotifyChatMessage("Client1", "Ciao, come va?"));

        //System.out.println("Messaggi finiti...");*/
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
