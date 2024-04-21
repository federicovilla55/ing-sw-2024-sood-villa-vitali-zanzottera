package it.polimi.ingsw.gc19.Networking.Client.ClientTCP;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.ClientRMI.ClientRMI;
import it.polimi.ingsw.gc19.Networking.Client.Message.Action.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.Chat.PlayerChatMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.Heartbeat.HeartBeatMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Client.MessageHandler;
import it.polimi.ingsw.gc19.Networking.Client.VirtualClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Settings;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientTCP implements VirtualClient, ClientInterface {
    private Socket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private final Deque<MessageToClient> incomingMessages;
    private String nickname;
    private String token;
    private String gameName;
    private final MessageHandler messageHandler;
    private final ExecutorService incomingMessageHandler = Executors.newSingleThreadExecutor();
    private ScheduledExecutorService heartbeatScheduler;

    public ClientTCP(String nickname){
        try {
            this.socket = new Socket(Settings.DEFAULT_SERVER_IP, Settings.DEFAULT_SERVER_PORT);
            this.outputStream = new ObjectOutputStream(this.socket.getOutputStream());
            this.inputStream = new ObjectInputStream(this.socket.getInputStream());
            this.startSendingHeartbeat();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.nickname = nickname;
        this.incomingMessages = new ArrayDeque<>();
        this.messageHandler = new MessageHandler(this);
        this.incomingMessageHandler.submit(this::receiveMessages);
    }

    public void sendMessage(MessageToServer message){
        boolean sent;
        int numOfTry = 0;
        synchronized (this.outputStream){
            sent = false;
            while(!sent && !socket.isClosed() && numOfTry < 25) {
                try {
                    this.outputStream.writeObject(message);
                    this.outputStream.flush();
                    this.outputStream.reset();
                    sent = true;
                } catch (Exception e) {
                    numOfTry++;
                }
            }
        }
    }

    public void receiveMessages(){
        MessageToClient incomingMessage;
        while (true){
            try{
                incomingMessage = (MessageToClient) this.inputStream.readObject();
            } catch (IOException  | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            pushUpdate(incomingMessage);
        }
    }

    public void stopClient(){
        try {
            //if (inputStream != null) inputStream.close();
            //if (outputStream != null) outputStream.close();
            if (socket != null) this.socket.close();

            this.incomingMessageHandler.shutdownNow();
            this.heartbeatScheduler.shutdownNow();
        }
        catch (IOException ioException){
            throw new RuntimeException(ioException);
        }


    }

    public void startSendingHeartbeat(){
        this.heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
        this.heartbeatScheduler.scheduleAtFixedRate(() -> {
            sendMessage(new HeartBeatMessage(this.nickname));
        }, 0, 400, TimeUnit.MILLISECONDS);

    }

    public void stopSendingHeartbeat() {
        if (heartbeatScheduler != null && !heartbeatScheduler.isShutdown()) {
            heartbeatScheduler.shutdown();
        }
    }

    @Override
    public void connect() {

        //this.incomingMessageHandler.submit(this::receiveMessages);
        this.sendMessage(new NewUserMessage(this.nickname));
    }

    @Override
    public void createGame(String gameName, int numPlayers) {
        this.sendMessage(new CreateNewGameMessage(this.nickname, gameName, numPlayers, 0));
    }

    @Override
    public void createGame(String gameName, int numPlayers, int seed) {
        this.sendMessage(new CreateNewGameMessage(this.nickname, gameName, numPlayers, seed));
    }

    @Override
    public void joinGame(String gameName) {
        this.sendMessage(new JoinGameMessage(gameName, this.nickname));
    }

    public void joinGame(String gameName, boolean wait){
        if(wait){
            boolean found = false;
            while (!found) {
                this.sendMessage(new JoinGameMessage(gameName, this.nickname));
                if (waitAndNotifyTypeOfMessage(GameHandlingError.class, JoinedGameMessage.class) == 1) {
                    found = true;
                } else {
                    getMessage(GameHandlingError.class);
                }
            }
        }else {
            joinGame(gameName);
        }
    }

    @Override
    public void joinFirstAvailableGame() {
        this.sendMessage(new JoinFirstAvailableGameMessage(this.nickname));
    }

    @Override
    public void reconnect() {
        this.sendMessage(new ReconnectToServerMessage(this.nickname, this.token));
    }

    @Override
    public void disconnect() {
        this.sendMessage(new DisconnectMessage(this.nickname));
        this.stopSendingHeartbeat();
    }

    @Override
    public void placeCard(String cardToInsert, String anchorCard, Direction directionToInsert, CardOrientation orientation) {
        this.sendMessage(new PlaceCardMessage(this.nickname, cardToInsert, anchorCard, directionToInsert, orientation));
    }

    @Override
    public void sendChatMessage(ArrayList<String> usersToSend, String messageToSend) {
        this.sendMessage(new PlayerChatMessage(new ArrayList<>(usersToSend), this.nickname, messageToSend));
    }

    @Override
    public void placeInitialCard(CardOrientation cardOrientation) {
        this.sendMessage(new DirectionOfInitialCardMessage(this.nickname, cardOrientation));
    }

    @Override
    public void pickCardFromTable(PlayableCardType type, int position) {
        this.sendMessage(new PickCardFromTableMessage(this.nickname, type, position));
    }

    @Override
    public void pickCardFromDeck(PlayableCardType type) {
        this.sendMessage(new PickCardFromDeckMessage(this.nickname, type));
    }

    @Override
    public void chooseColor(Color color) {
        this.sendMessage(new ChosenColorMessage(this.nickname, color));
    }

    @Override
    public void choosePrivateGoalCard(int cardIdx) {
        this.sendMessage(new ChosenGoalCardMessage(this.nickname, cardIdx));
    }

    @Override
    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String getToken() {
        return this.token;
    }

    @Override
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String getNickname() {
        return this.nickname;
    }

    @Override
    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    @Override
    public String getGameName() {
        return this.gameName;
    }

    @Override
    public void pushUpdate(MessageToClient message) {
        synchronized (this.incomingMessages){
            this.incomingMessages.add(message);
            this.incomingMessages.notifyAll();
        }

        message.accept(this.messageHandler);
    }

    public void clearQueue(){
        synchronized (this.incomingMessages) {
            this.incomingMessages.clear();
        }
    }

    @Override
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

    @Override
    public MessageToClient getMessage() {
        return getMessage(MessageToClient.class);
    }

    @Override
    public MessageToClient getMessage(Class<? extends MessageToClient> messageToClientClass) {
        synchronized (this.incomingMessages) {
            while (!this.incomingMessages.isEmpty()) {
                MessageToClient res = this.incomingMessages.remove();
                if (messageToClientClass.isInstance(res)) return res;
            }
        }
        return null;
    }

}
