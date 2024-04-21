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

    @Override
    public void attachObserver(ObserverMessageToServer<MessageToServer> observer) {
        synchronized (this.attachedObserver) {
            this.attachedObserver.add(observer);
        }
    }

    @Override
    public void removeObserver(ObserverMessageToServer<MessageToServer> observer) {
        synchronized (this.attachedObserver) {
            this.attachedObserver.remove(observer);
        }
    }

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
