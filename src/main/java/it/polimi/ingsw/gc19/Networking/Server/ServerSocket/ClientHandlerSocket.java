package it.polimi.ingsw.gc19.Networking.Server.ServerSocket;

import it.polimi.ingsw.gc19.Networking.Client.Message.Action.*;
import it.polimi.ingsw.gc19.Networking.Client.Message.Chat.PlayerChatMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.Chat.PlayerChatMessageVisitor;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServerVisitor;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingErrorMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.ObserverPattern.ObserverMessageToServer;
import it.polimi.ingsw.gc19.Controller.GameController;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandlerSocket extends ClientHandler implements ObserverMessageToServer<MessageToServer> {

    private final Socket socket;
    private final Object socketLock;
    private final ObjectOutputStream outputStream;
    private final ClientToServerGameMessageVisitor messageVisitor;

    public ClientHandlerSocket(String nick, Socket socket) throws IOException{
        super(nick, null);

        ObjectOutputStream objectOutputStream = null;

        try{
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException ioException){
            System.err.println("[EXCEPTION] IOException occurred while trying to build object output stream from socket " + socket + ". Closing socket...");
            throw new IOException();
        }

        this.outputStream = objectOutputStream;
        this.messageVisitor = new ClientToServerGameMessageVisitor();
        this.socket = socket;
        this.socketLock = new Object();
    }

    public ClientHandlerSocket(Socket socket) throws IOException{
        this(null, socket);
    }

    public void pullClientHandlerSocketConfigIntoThis(ClientHandlerSocket clientHandler){
        this.username = clientHandler.username;
        this.gameController = clientHandler.getGameController();
        this.messageQueue.addAll(clientHandler.getQueueOfMessages());
    }

    /**
     * This method is used to send a message to client using
     * TCP socket.
     * @param message message to be sent
     */
    @Override
    public void sendMessageToClient(MessageToClient message) {
        try {
            synchronized (this.socketLock) {
                this.outputStream.writeObject(message);
                finalizeSending();
            }
        }
        catch (SocketException socketException){
            if(!socket.isClosed()) {
                System.err.println("[EXCEPTION] SocketException occurred while trying to shut down input from socket " + socket + " due to: " + socketException.getMessage());
            }
        }
        catch (IOException ioException) {
            System.err.println("[EXCEPTION] IOException occurred while trying to send message " + message.getClass() + " to client named " + this.username + " because of " + ioException.getMessage() + ". Skipping...");
        }
    }

    /**
     * This is method is used to be sure that message has been sent
     * @throws IOException if an IO error has occurred
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

    /**
     * This method is used to notify caller that {@link MessageToServer} can be accepted
     * by the class.
     * @param message {@link MessageToServer} that could be accepted
     * @return true if and only if message could be accepted
     */
    @Override
    public boolean accept(MessageToServer message) {
        return message instanceof ActionMessage || message instanceof PlayerChatMessage;
    }

    /**
     * This method is used to interrupt thread bounded to the object.
     * Consequently, it also tries to shut down output stream associated
     * to own socket.
     */
    @Override
    public void interruptClientHandler() {
        try{
            synchronized (this.socketLock) {
                this.socket.shutdownOutput();
            }
        }
        catch (SocketException socketException){
            if(!this.socket.isClosed()){
                System.err.println("[EXCEPTION] IOException occurred while trying to shut down output from socket " + socket + " due to: " + socketException.getMessage() + ". Skipping...");
            }
        }
        catch (IOException ioException){
            System.err.println("[EXCEPTION] IOException occurred while trying to shut down output from socket " + socket + " due to: " + ioException.getMessage() + ". Skipping...");
        }

        super.interruptClientHandler();
    }

    private class ClientToServerGameMessageVisitor implements MessageToServerVisitor, ActionMessageVisitor, PlayerChatMessageVisitor{

        /**
         * This method is used to visit a {@link ChosenGoalCardMessage}.
         * If {@link ClientHandlerSocket} is not registered to any game (e.g.its {@link GameController})
         * is null, it sends {@link GameHandlingErrorMessage} to the player.
         * @param message {@link ChosenColorMessage} to read.
         */
        @Override
        public void visit(ChosenGoalCardMessage message){
            if(gameController != null){
                ClientHandlerSocket.this.gameController.choosePrivateGoal(ClientHandlerSocket.this.username, message.getCardIdx());
            }
            else{
                sendMessageToClient(new GameHandlingErrorMessage(Error.GAME_NOT_FOUND,
                                                                 "You aren't connected to any game! It can be finished or you have lost connection!")
                                            .setHeader(username));
            }
        }

        /**
         * This method is used to visit a {@link DirectionOfInitialCardMessage}.
         * If {@link ClientHandlerSocket} is not registered to any game (e.g.its {@link GameController})
         * is null, it sends {@link GameHandlingErrorMessage} to the player.
         * @param message {@link DirectionOfInitialCardMessage} message to read.
         */
        @Override
        public void visit(DirectionOfInitialCardMessage message){
            if(gameController != null){
                ClientHandlerSocket.this.gameController.placeInitialCard(ClientHandlerSocket.this.username, message.getDirectionOfInitialCard());
            }
            else{
                sendMessageToClient(new GameHandlingErrorMessage(Error.GAME_NOT_FOUND,
                                                                 "You aren't connected to any game! It can be finished or you have lost connection!")
                                            .setHeader(username));
            }
        }

        /**
         * This method is used to visit a {@link PlaceCardMessage}.
         * If {@link ClientHandlerSocket} is not registered to any game (e.g.its {@link GameController})
         * is null, it sends {@link GameHandlingErrorMessage} to the player.
         * @param message {@link PlaceCardMessage} to read.
         */
        @Override
        public void visit(PlaceCardMessage message) {
            if(gameController != null){
                ClientHandlerSocket.this.gameController.placeCard(message.getNickname(), message.getCardToPlaceCode(), message.getAnchorCode(), message.getDirection(), message.getCardOrientation());
            }
            else{
                sendMessageToClient(new GameHandlingErrorMessage(Error.GAME_NOT_FOUND,
                                                                 "You aren't connected to any game! It can be finished or you have lost connection!")
                                            .setHeader(username));
            }
        }

        /**
         * This method is used to visit a {@link ChosenColorMessage}.
         * If {@link ClientHandlerSocket} is not registered to any game (e.g.its {@link GameController})
         * is null, it sends {@link GameHandlingErrorMessage} to the player.
         * @param message {@link ChosenColorMessage} to read.
         */
        @Override
        public void visit(ChosenColorMessage message) {
            if(gameController != null){
                ClientHandlerSocket.this.gameController.chooseColor(message.getNickname(), message.getChosenColor());
            }
            else{
                sendMessageToClient(new GameHandlingErrorMessage(Error.GAME_NOT_FOUND,
                                                                 "You aren't connected to any game! It can be finished or you have lost connection!")
                                            .setHeader(username));
            }
        }

        /**
         * This method is used to visit a {@link PickCardFromDeckMessage}.
         * If {@link ClientHandlerSocket} is not registered to any game (e.g.its {@link GameController})
         * is null, it sends {@link GameHandlingErrorMessage} to the player.
         * @param message {@link PickCardFromDeckMessage} to read.
         */
        @Override
        public void visit(PickCardFromDeckMessage message) {
            if(gameController != null){
                ClientHandlerSocket.this.gameController.drawCardFromDeck(message.getNickname(), message.getType());
            }
            else{
                sendMessageToClient(new GameHandlingErrorMessage(Error.GAME_NOT_FOUND,
                                                                 "You aren't connected to any game! It can be finished or you have lost connection!")
                                            .setHeader(username));
            }
        }

        /**
         * This method is used to visit a {@link PickCardFromTableMessage}.
         * If {@link ClientHandlerSocket} is not registered to any game (e.g.its {@link GameController})
         * is null, it sends {@link GameHandlingErrorMessage} to the player.
         * @param message {@link PickCardFromTableMessage} to read.
         */
        @Override
        public void visit(PickCardFromTableMessage message) {
            if(gameController != null) {
                ClientHandlerSocket.this.gameController.drawCardFromTable(message.getNickname(), message.getType(), message.getPosition());
            }
            else{
                sendMessageToClient(new GameHandlingErrorMessage(Error.GAME_NOT_FOUND,
                                                                 "You aren't connected to any game! It can be finished or you have lost connection!")
                                            .setHeader(username));
            }
        }

        /**
         * This method is used to visit a {@link PlayerChatMessage}.
         * If {@link ClientHandlerSocket} is not registered to any game (e.g.its {@link GameController})
         * is null, it sends {@link GameHandlingErrorMessage} to the player.
         * @param message {@link PlayerChatMessage} to read.
         */
        @Override
        public void visit(PlayerChatMessage message) {
            if(gameController != null) {
                ClientHandlerSocket.this.gameController.sendChatMessage(message.getReceivers(), message.getNickname(), message.getMessage());
            }
            else{
                sendMessageToClient(new GameHandlingErrorMessage(Error.GAME_NOT_FOUND,
                                                                 "You aren't connected to any game! It can be finished or you have lost connection!")
                                            .setHeader(username));
            }
        }

    }

}
