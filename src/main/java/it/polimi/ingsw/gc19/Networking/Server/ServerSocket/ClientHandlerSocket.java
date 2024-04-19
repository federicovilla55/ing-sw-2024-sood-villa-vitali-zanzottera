package it.polimi.ingsw.gc19.Networking.Server.ServerSocket;

import it.polimi.ingsw.gc19.Controller.MainController;
import it.polimi.ingsw.gc19.Networking.Client.Message.Action.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.Chat.PlayerChatMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.Chat.PlayerChatMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.GameHandlingMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Client.Message.Heartbeat.HeartBeatMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.Heartbeat.HeartBeatMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.Chat.NotifyChatMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.DisconnectedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.NotifyEventOnGame;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.ObserverPattern.ObserverMessageToServer;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.*;

public class ClientHandlerSocket extends ClientHandler implements ObserverMessageToServer<MessageToServer> {

    private final Socket socket;
    private final ObjectOutputStream outputStream;
    private boolean readIncomingMessages, writeOutputMessages;
    private final Object lockOnRead, lockOnWrite;
    private final ClientToServerGameMessageVisitor messageVisitor;

    public ClientHandlerSocket(Socket socketToClient, String nick){
        super(nick, null);

        this.socket = socketToClient;

        ObjectOutputStream objectOutputStreamToBuild = null;
        try {
            objectOutputStreamToBuild = new ObjectOutputStream(socketToClient.getOutputStream());
        }
        catch (IOException ioException){
            //@TODO: handle this exception
        }
        this.outputStream = objectOutputStreamToBuild;

        this.readIncomingMessages = true;
        this.writeOutputMessages = true;
        this.lockOnRead = new Object();
        this.lockOnWrite = new Object();

        this.messageVisitor = new ClientToServerGameMessageVisitor();
    }

    public ClientHandlerSocket(Socket socket){
        this(socket, null);
    }

    public void pullClientHandlerSocketConfigIntoThis(ClientHandlerSocket clientHandler){
        this.username = clientHandler.username;
        this.gameController = clientHandler.getGameController();
        this.readIncomingMessages = clientHandler.readIncomingMessages;
        this.writeOutputMessages = clientHandler.writeOutputMessages;
        this.messageQueue.addAll(clientHandler.getQueueOfMessages());
    }

    public void setUsername(String name){
        this.username = name;
    }

    public void startReadingMessages(){
        synchronized (this.lockOnRead){
            this.readIncomingMessages = true;
        }
    }

    public void stopReadingMessages(){
        synchronized (this.lockOnRead){
            this.readIncomingMessages = false;
        }
    }

    public void startWritingMessages(){
        synchronized (this.lockOnWrite){
            this.writeOutputMessages = true;
            this.lockOnWrite.notifyAll();
        }
    }

    public void stopWritingMessages(){
        synchronized (this.lockOnWrite){
            this.writeOutputMessages = false;
            this.lockOnWrite.notifyAll(); //necessaria?
        }
    }

    @Override
    public void sendMessageToClient(MessageToClient message) {
        if(message instanceof JoinedGameMessage) System.out.println(username +  "   " + this.writeOutputMessages + "   " + this.socket);
        synchronized (this.lockOnWrite) {
            while(!this.writeOutputMessages){
                try{
                    this.lockOnWrite.wait();
                }
                catch (InterruptedException interruptedException){
                    //System.out.println("error");
                    //@TODO: handle this exception
                }
            }
            synchronized (this.outputStream){
                try {
                    this.outputStream.reset();
                    this.outputStream.writeObject(message);
                    this.outputStream.flush();
                }
                catch(IOException ioException){
                    System.out.println(username + " -> " + ioException.getMessage() + "   " +  ioException.getClass() + "   " + ioException.getCause() + " " + message.getClass());
                }
            }
        }
    }

    public Socket getSocket(){
        return this.socket;
    }

    /**
     * This is method is used to be sure that message has been sent
     */
    private void finalizeSending(){
        try {
            this.outputStream.flush();
        }
        catch (IOException ioException){
            //@TODO: handle this exception
        }
    }

    @Override
    public void update(Socket senderSocket, MessageToServer message) {
        synchronized (this.lockOnRead){
            if(this.readIncomingMessages && socket.equals(senderSocket)){
                synchronized (this.messageVisitor) {
                    message.accept(messageVisitor);
                }
            }
        }
    }

    @Override
    public boolean accept(MessageToServer message) {
        return message instanceof ActionMessage || message instanceof PlayerChatMessage;
    }

    public boolean canRead(){
        return this.readIncomingMessages;
    }

    public boolean canWrite(){
        return this.writeOutputMessages;
    }

    private class ClientToServerGameMessageVisitor implements MessageToServerVisitor, ActionMessageVisitor, PlayerChatMessageVisitor{
        @Override
        public void visit(ChosenGoalCardMessage message){
            if(gameController != null){
                ClientHandlerSocket.this.gameController.choosePrivateGoal(ClientHandlerSocket.this.username, message.getCardIdx());
            }
            else{
                sendMessageToClient(new GameHandlingError(Error.GAME_NOT_FOUND,
                                                          "You aren't connected to any game! It can be finished or you have lost connection!")
                                            .setHeader(username));
            }
        }

        @Override
        public void visit(DirectionOfInitialCardMessage message){
            if(gameController != null){
                ClientHandlerSocket.this.gameController.placeInitialCard(ClientHandlerSocket.this.username, message.getDirectionOfInitialCard());
            }
            else{
                sendMessageToClient(new GameHandlingError(Error.GAME_NOT_FOUND,
                                                          "You aren't connected to any game! It can be finished or you have lost connection!")
                                            .setHeader(username));
            }
        }

        @Override
        public void visit(PlaceCardMessage message) {
            if(gameController != null){
                ClientHandlerSocket.this.gameController.placeCard(message.getNickname(), message.getCardToPlaceCode(), message.getAnchorCode(), message.getDirection(), message.getCardOrientation());
            }
            else{
                sendMessageToClient(new GameHandlingError(Error.GAME_NOT_FOUND,
                                                          "You aren't connected to any game! It can be finished or you have lost connection!")
                                            .setHeader(username));
            }
        }

        @Override
        public void visit(ChosenColorMessage message) {
            if(gameController != null){
                ClientHandlerSocket.this.gameController.chooseColor(message.getNickname(), message.getChosenColor());
            }
            else{
                sendMessageToClient(new GameHandlingError(Error.GAME_NOT_FOUND,
                                                          "You aren't connected to any game! It can be finished or you have lost connection!")
                                            .setHeader(username));
            }
        }

        @Override
        public void visit(PickCardFromDeckMessage message) {
            if(gameController != null){
                ClientHandlerSocket.this.gameController.drawCardFromDeck(message.getNickname(), message.getType());
            }
            else{
                sendMessageToClient(new GameHandlingError(Error.GAME_NOT_FOUND,
                                                          "You aren't connected to any game! It can be finished or you have lost connection!")
                                            .setHeader(username));
            }
        }

        @Override
        public void visit(PickCardFromTableMessage message) {
            if(gameController != null) {
                ClientHandlerSocket.this.gameController.drawCardFromTable(message.getNickname(), message.getType(), message.getPosition());
            }
            else{
                sendMessageToClient(new GameHandlingError(Error.GAME_NOT_FOUND,
                                                          "You aren't connected to any game! It can be finished or you have lost connection!")
                                            .setHeader(username));
            }
        }

        @Override
        public void visit(PlayerChatMessage message) {
            if(gameController != null) {
                ClientHandlerSocket.this.gameController.sendChatMessage(message.getReceivers(), message.getNickname(), message.getMessage());
            }
            else{
                System.out.println("kkkkkkkkkkkkkkkkkkk");
                sendMessageToClient(new GameHandlingError(Error.GAME_NOT_FOUND,
                                                          "You aren't connected to any game! It can be finished or you have lost connection!")
                                            .setHeader(username));
            }
        }

    }

}
