package it.polimi.ingsw.gc19.Networking.Server.ServerSocket;

import it.polimi.ingsw.gc19.Networking.Client.Message.Action.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.Chat.PlayerChatMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.Chat.PlayerChatMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.ObserverPattern.ObserverMessageToServer;

import java.io.*;
import java.net.Socket;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandlerSocket extends ClientHandler implements ObserverMessageToServer<MessageToServer> {

    private Socket socket;
    private final ObjectOutputStream outputStream;
    private final ClientToServerGameMessageVisitor messageVisitor;

    public ClientHandlerSocket(ObjectOutputStream objectOutputStream, String nick){
        super(nick, null);
        this.outputStream = objectOutputStream;
        this.messageVisitor = new ClientToServerGameMessageVisitor();
    }

    public ClientHandlerSocket(ObjectOutputStream objectOutputStream, Socket socket){
        this(objectOutputStream, (String) null);
        this.socket = socket;
    }

    public void pullClientHandlerSocketConfigIntoThis(ClientHandlerSocket clientHandler){
        this.username = clientHandler.username;
        this.gameController = clientHandler.getGameController();
        this.messageQueue.addAll(clientHandler.getQueueOfMessages());
    }

    public void setUsername(String name){
        this.username = name;
    }

    @Override
    public void sendMessageToClient(MessageToClient message) {
        synchronized (this.outputStream) {
            try {
                this.outputStream.writeObject(message);
                finalizeSending();
            } catch (IOException ioException) {
                System.err.println("[EXCEPTION] IOException occurred while trying to send message " + message.getClass() + " to client named " + this.username + ". Skipping...");
            }
        }
    }

    /**
     * This is method is used to be sure that message has been sent
     */
    private void finalizeSending() throws IOException{
        this.outputStream.flush();
        this.outputStream.reset();
    }

    @Override
    public void update(Socket senderSocket, MessageToServer message) {
        synchronized (this.messageVisitor) {
            message.accept(messageVisitor);
        }
    }

    @Override
    public boolean accept(MessageToServer message) {
        return message instanceof ActionMessage || message instanceof PlayerChatMessage;
    }

    @Override
    public void interruptClientHandler() {
        try{
            this.socket.shutdownOutput();
        }
        catch (IOException ioException){
            System.err.println("[EXCEPTION] IOException occurred while trying to shut down output from socket " + socket + " due to: " + ioException.getMessage() + ". Skipping...");
        }

        this.socket = null; //Maybe errors here?

        super.interruptClientHandler();
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
                sendMessageToClient(new GameHandlingError(Error.GAME_NOT_FOUND,
                                                          "You aren't connected to any game! It can be finished or you have lost connection!")
                                            .setHeader(username));
            }
        }

    }

}
