package it.polimi.ingsw.gc19.Networking.Client.ClientTCP;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.ClientSettings;
import it.polimi.ingsw.gc19.Networking.Client.Message.Action.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.Chat.PlayerChatMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.Heartbeat.HeartBeatMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Client.MessageHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.View.GameLocalView.ActionParser;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientTCP implements ClientInterface {
    private final Socket socket;
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;

    private String nickname;
    private final MessageHandler messageHandler;
    private final ActionParser actionParser;

    private ScheduledExecutorService heartbeatScheduler;
    private final Thread senderThread;
    private final Thread receiverThread;

    private final Deque<MessageToServer> messagesToSend;

    public ClientTCP(String nickname, MessageHandler messageHandler, ActionParser actionParser) throws IOException{
        this.nickname = nickname;
        this.messageHandler = messageHandler;
        this.actionParser = actionParser;

        try {
            this.socket = new Socket(ClientSettings.serverIP, ClientSettings.serverTCPPort);
            this.outputStream = new ObjectOutputStream(socket.getOutputStream());
            this.inputStream = new ObjectInputStream(socket.getInputStream());
        }
        catch (IOException e) {
            //Method throws IOException to notify Action Parsers that connection establishing have gone wrong
            throw new IOException();
        }

        this.messagesToSend = new ArrayDeque<>();

        this.heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
        this.startSendingHeartbeat();
        this.receiverThread = new Thread(this::receiveMessages);
        this.senderThread = new Thread(this::sendMessageToServer);

        this.receiverThread.start();
        this.senderThread.start();
    }

    public ClientTCP(ClientTCP clientTCP) throws IOException{
        this(clientTCP.nickname, clientTCP.messageHandler, clientTCP.actionParser);
        this.messagesToSend.addAll(clientTCP.messagesToSend);
    }

    private boolean networkDisconnectionRoutine(){
        boolean sent = false;
        long startingTime = new Date().getTime();

        while(!Thread.interrupted() && !sent && new Date().getTime() - startingTime < 1000 * ClientSettings.MAX_TRY_TIME_BEFORE_SIGNAL_DISCONNECTION){
            try{
                this.outputStream.writeObject(new HeartBeatMessage(this.nickname));
                finalizeSending();
                sent = true;
            }
            catch (IOException ioException){
                try{
                    Thread.sleep(20);
                }
                catch (InterruptedException interruptedException){
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        return sent;
    }

    private boolean send(MessageToServer message){
        synchronized (this.outputStream) {
            try {
                this.outputStream.writeObject(message);
                this.finalizeSending();
            } catch (IOException ioException) {
                return false;
            }
        }
        return true;
    }

    private void sendMessageToServer(){
        MessageToServer message;

        while(!Thread.interrupted()) {
            if(!this.actionParser.isDisconnected()) {
                synchronized (this.messagesToSend) {
                    while (this.messagesToSend.isEmpty()) {
                        try {
                            this.messagesToSend.wait();
                        } catch (InterruptedException interruptedException) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    message = this.messagesToSend.removeFirst();
                }

                if (message != null && !send(message)) {
                    if (!this.networkDisconnectionRoutine()) {
                        this.actionParser.disconnect();
                    }
                    else {
                        synchronized (this.messagesToSend) {
                            this.messagesToSend.addFirst(message);
                        }
                    }
                }
            }
        }
    }

    public void sendMessage(MessageToServer message){
        synchronized (this.messagesToSend){
            this.messagesToSend.addLast(message);
            this.messagesToSend.notifyAll();
        }
    }

    private void finalizeSending() throws IOException{
        this.outputStream.flush();
        this.outputStream.reset();
    }

    public void receiveMessages(){
        MessageToClient incomingMessage = null;
        while(!Thread.interrupted()) {
            try {
                incomingMessage = (MessageToClient) this.inputStream.readObject();
            }
            catch (ClassNotFoundException ignored){ }
            catch (IOException ioException){
                //@TODO: what to do in this case?
                //this.actionParser.disconnect();
            }

            if(incomingMessage != null) {
                this.messageHandler.update(incomingMessage);
            }
        }
    }

    public void stopClient(){
        stopSendingHeartbeat();

        this.senderThread.interrupt();
        this.receiverThread.interrupt();

        try {
            if (socket != null) {
                this.socket.shutdownOutput();
                this.socket.shutdownInput();
                this.socket.close();
            }
        }
        catch (IOException ignored){ }
    }

    public void startSendingHeartbeat(){
        if(this.heartbeatScheduler.isShutdown()) {
            this.heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
        }

        this.heartbeatScheduler.scheduleAtFixedRate(() -> {
            if(!this.actionParser.isDisconnected()) {
                sendMessage(new HeartBeatMessage(this.nickname));
            }
        }
        , 0, 400, TimeUnit.MILLISECONDS);

    }

    public void stopSendingHeartbeat() {
        if (heartbeatScheduler != null && !heartbeatScheduler.isShutdown()) {
            heartbeatScheduler.shutdownNow();
        }
    }

    public MessageHandler getMessageHandler() {
        return this.messageHandler;
    }

    @Override
    public void connect() {
        this.sendMessage(new NewUserMessage(this.nickname));
    }

    @Override
    public void createGame(String gameName, int numPlayers) {
        this.sendMessage(new CreateNewGameMessage(this.nickname, gameName, numPlayers, null));
    }

    @Override
    public void createGame(String gameName, int numPlayers, int seed) {
        this.sendMessage(new CreateNewGameMessage(this.nickname, gameName, numPlayers, Integer.toUnsignedLong(seed)));
    }

    @Override
    public void joinGame(String gameName) {
        this.sendMessage(new JoinGameMessage(gameName, this.nickname));
    }

    @Override
    public void joinFirstAvailableGame() {
        this.sendMessage(new JoinFirstAvailableGameMessage(this.nickname));
    }

    @Override
    public void logoutFromGame() throws RuntimeException{
        if(!this.send(new RequestGameExitMessage(this.nickname))){
            throw new RuntimeException("Message could not be sent to server!");
        }
        synchronized (this.messagesToSend){
            this.messagesToSend.clear();
        }
    }

    @Override
    public void reconnect() throws RuntimeException{
        String token;
        File tokenFile = new File("src/main/java/it/polimi/ingsw/gc19/Networking/Client/ClientRMI/TokenFile" + "_" + this.nickname);

        if(tokenFile.isFile() && tokenFile.exists()) {
            try {
                Scanner tokenScanner = new Scanner(tokenFile);
                token = tokenScanner.nextLine();
                tokenScanner.close();
                if(!this.send(new ReconnectToServerMessage(this.nickname, token))){
                    throw new RuntimeException("Could not send message to server!");
                }
            }
            catch (IOException ioException) {
                throw new IllegalStateException("Reconnection is not possible because token file has not been found!");
            }
        }
        else{
            throw new IllegalStateException("Reconnection is not possible because token file has not been found!");
        }
    }

    @Override
    public void disconnect() throws RuntimeException{
        if(!this.send(new DisconnectMessage(this.nickname))){
            throw new RuntimeException("Message could not be sent to server!");
        }

        File tokenFile = new File("src/main/java/it/polimi/ingsw/gc19/Networking/Client/ClientRMI/TokenFile" + "_" + this.nickname);
        if(tokenFile.exists() && tokenFile.exists() && tokenFile.delete()){
            System.err.println("[TOKEN]: token file deleted.");
        }

        this.stopClient();
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
    public void availableGames() {
        this.sendMessage(new RequestAvailableGamesMessage(this.nickname));
    }

    @Override
    public void setToken(String token) {
        File tokenFile = new File("src/main/java/it/polimi/ingsw/gc19/Networking/Client/ClientRMI/TokenFile" + "_" + this.nickname);
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tokenFile));
            bufferedWriter.write(token);
            bufferedWriter.close();
            if(tokenFile.setReadOnly()){
                System.err.println("[TOKEN]: token file written and set read only.");
            }
        }
        catch (IOException ignored){ };
    }

    @Override
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String getNickname() {
        return this.nickname;
    }

}
