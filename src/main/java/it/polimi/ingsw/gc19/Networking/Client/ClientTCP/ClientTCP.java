package it.polimi.ingsw.gc19.Networking.Client.ClientTCP;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.Message.Action.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.Chat.PlayerChatMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.Heartbeat.HeartBeatMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Client.MessageHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientTCP extends Thread implements ClientInterface {
    private final Socket socket;
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;
    private String nickname;
    private String token;
    private final MessageHandler messageHandler;
    private final ScheduledExecutorService heartbeatScheduler;


    public ClientTCP(MessageHandler messageHandler, String serverIP, int serverPort){

        this.messageHandler = messageHandler;

        Socket socket = null;
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            socket = new Socket(serverIP, serverPort);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            //@TODO: handle this exception. How to notify view? Returning?
            //At the beginning we can write on console I think, so print on err could be fine
            System.exit(-1);
        }
        this.socket = socket;
        this.inputStream = objectInputStream;
        this.outputStream = objectOutputStream;

        this.heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void run(){
        while(!Thread.interrupted()){
            this.receiveMessages();
        }
    }

    public void sendMessage(MessageToServer message){
        synchronized (this.outputStream){
            try {
                this.outputStream.writeObject(message);
                finalizeSending();
            } catch (IOException e) {
                //@TODO: invoke correct method of message handler
            }
        }
    }

    private void finalizeSending() throws IOException{
        this.outputStream.flush();
        this.outputStream.reset();
    }

    public void receiveMessages(){
        MessageToClient incomingMessage;
        try{
            incomingMessage = (MessageToClient) this.inputStream.readObject();
        } catch (IOException  | ClassNotFoundException e) {
            //@TODO: handle this exception
            throw new RuntimeException(e);
        }
        this.messageHandler.update(incomingMessage);
    }

    public void stopClient(){
        stopSendingHeartbeat();

        try {
            if (socket != null) {
                this.socket.shutdownOutput();
                this.socket.shutdownInput();
                this.socket.close();
            }
        }
        catch (IOException ioException){
            //@TODO: handle this exception
            throw new RuntimeException(ioException);
        }
    }

    public void startSendingHeartbeat(){
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

    @Override
    public void joinFirstAvailableGame() {
        this.sendMessage(new JoinFirstAvailableGameMessage(this.nickname));
    }

    @Override
    public void reconnect() {
        File tokenFile = new File("src/main/java/it/polimi/ingsw/gc19/Networking/Client/ClientRMI/TokenFile" + "_" + this.nickname);
        try {
            Scanner tokenScanner = new Scanner(tokenFile);
            this.sendMessage(new ReconnectToServerMessage(this.nickname, tokenScanner.nextLine()));
        }
        catch (IOException ignored){ };
    }

    @Override
    public void disconnect() {
        this.sendMessage(new DisconnectMessage(this.nickname));
        this.interrupt();
        this.stopSendingHeartbeat();

        File tokenFile = new File("src/main/java/it/polimi/ingsw/gc19/Networking/Client/ClientRMI/TokenFile" + "_" + this.nickname);
        if(tokenFile.delete()){
            System.err.println("Token file deleted...");
        }
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
        File tokenFile = new File("src/main/java/it/polimi/ingsw/gc19/Networking/Client/ClientRMI/TokenFile" + "_" + this.nickname);
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tokenFile));
            bufferedWriter.write(token);
            tokenFile.setWritable(false);
        }
        catch (IOException ignored){ };

        this.token = token;
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
