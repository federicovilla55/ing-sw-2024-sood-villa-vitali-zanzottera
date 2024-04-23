package it.polimi.ingsw.gc19.Networking.Server.ServerSocket;

import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.DisconnectMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.GameHandling.NewUserMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.MessageToServer;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.Error;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.Errors.GameHandlingError;
import it.polimi.ingsw.gc19.ObserverPattern.ObservableMessageToClient;
import it.polimi.ingsw.gc19.ObserverPattern.ObservableMessageToServer;
import it.polimi.ingsw.gc19.ObserverPattern.ObserverMessageToServer;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

/**
 * This class acts as a router for TCP messages from client to server.
 * It extends {@link Thread} and implements {@link ObservableMessageToServer}.
 * Every TCP client has his own {@link MessageToServerDispatcher}. It reads incoming
 * messages and dispatches it to {@link MainServerTCP} or {@link ClientHandlerSocket}
 * based on message dynamic type.
 * It is built by {@link TCPConnectionAcceptor} when a new connection is accepted.
 */
public class MessageToServerDispatcher extends Thread implements ObservableMessageToServer<MessageToServer>{
    private final Socket socket;
    private final Object socketLock;
    private final ObjectInputStream objectInputStream;
    private final Set<ObserverMessageToServer<MessageToServer>> attachedObserver;

    public MessageToServerDispatcher(Socket socket){
        super();

        this.socket = socket;
        this.socketLock = new Object();
        ObjectInputStream objectInputStream = null;
        try{
            objectInputStream = new ObjectInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            System.err.println("[Exception] IOException occurred while trying to open Object Input Stream of socket " + socket + ". Closing socket...");
            try{
                socket.close();
            }
            catch (IOException ignored){ };
        }
        this.objectInputStream = objectInputStream;

        this.attachedObserver = new HashSet<>();
    }

    /**
     * This method is used to insert an {@link ObserverMessageToServer}
     * to <code>Set<ObserverMessageToServer></code> of the class.
     * @param observer the {@link ObserverMessageToServer} to insert.
     */
    @Override
    public void attachObserver(ObserverMessageToServer<MessageToServer> observer) {
        synchronized (this.attachedObserver) {
            this.attachedObserver.add(observer);
        }
    }

    /**
     * This method is used to remove an {@link ObserverMessageToServer}
     * from <code>Set<ObserverMessageToServer></code> of the class.
     * @param observer the {@link ObserverMessageToServer} to remove.
     */
    @Override
    public void removeObserver(ObserverMessageToServer<MessageToServer> observer) {
        synchronized (this.attachedObserver) {
            this.attachedObserver.remove(observer);
        }
    }

    /**
     * This method is inherited from {@link Thread} class. It waits
     * for new incoming messages, and it delivers them to the correct {@link ObserverMessageToServer}:
     * for every {@link ObserverMessageToServer} in <code>Set<ObserverMessageToClient></code> it
     * asks if they can accept it and, if yes, calls their {@link ObserverMessageToServer#update(Socket, MessageToServer)}
     */
    @Override
    public void run() {
        MessageToServer incomingMessage;

        while(!Thread.interrupted()) {
            incomingMessage = null;
            try {
                incomingMessage = (MessageToServer) MessageToServerDispatcher.this.objectInputStream.readObject();
            }
            catch (EOFException | SocketException exception){
                break;
            }
            catch (IOException ioException) {
                System.err.println("[EXCEPTION] IOException occurred while trying to read message from socket " + socket + ". " + "Description: " + ioException.getClass());
            }
            catch (ClassNotFoundException classNotFoundException) {
                System.err.println("[EXCEPTION] ClassNotFoundException occurred while trying to deserialize object received from " + socket);
            }

            if(incomingMessage != null) {
                Set<ObserverMessageToServer<MessageToServer>> observersToNotify;
                synchronized (this.attachedObserver) {
                    observersToNotify = new HashSet<>(this.attachedObserver);
                }
                for (ObserverMessageToServer<MessageToServer> o : observersToNotify) {
                    if (o.accept(incomingMessage)) o.update(socket, incomingMessage);
                }
            }
        }
    }

    /**
     * This method is used to interrupt a {@link MessageToServerDispatcher}.
     * It tries to shut down input of socket and interrupts thread.
     */
    public void interruptMessageDispatcher(){
        synchronized (this.socketLock) {
            try {
                socket.shutdownInput();
            }
            catch (SocketException socketException){
                if(!this.socket.isClosed()){
                    System.err.println("[EXCEPTION] SocketException occurred while trying to shut down input from socket " + socket + " due to: " + socketException.getMessage());
                }
            }
            catch (IOException ioException) {
                System.err.println("[EXCEPTION] IOException occurred while trying to shut down input from socket " + socket + " due to: " + ioException.getMessage() + ". Skipping...");
            }
        }

        this.interrupt();
    }

}
