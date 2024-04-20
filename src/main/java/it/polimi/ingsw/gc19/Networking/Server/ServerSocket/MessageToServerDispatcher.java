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
import java.util.HashSet;
import java.util.Set;

public class MessageToServerDispatcher extends Thread implements ObservableMessageToServer<MessageToServer>{
    private final Socket socket;
    private final ObjectInputStream objectInputStream;
    private final Set<ObserverMessageToServer<MessageToServer>> attachedObserver;

    public MessageToServerDispatcher(Socket socket){
        super();
        this.socket = socket;

        try{
            this.objectInputStream = new ObjectInputStream(this.socket.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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
            try{
                incomingMessage = (MessageToServer) MessageToServerDispatcher.this.objectInputStream.readObject();
            }
            catch (IOException | ClassNotFoundException ioException){
                //System.out.println(ioException.getMessage());
            }

            if(incomingMessage != null) {
                synchronized (this.attachedObserver) {
                    System.out.println("processing " + incomingMessage.getClass());
                    for (ObserverMessageToServer<MessageToServer> o : this.attachedObserver) {
                        if (o.accept(incomingMessage)) o.update(socket, incomingMessage);
                    }
                }
            }
        }
        System.err.println("Interrupted " + this);
    }

    public void interruptMessageDispatcher(){
        this.interrupt();
    }

    //@TODO: implement logic for destruction of thread and object. at the end of game? alla fine del gioco non devo fare nulla si occupa di tutto il main controller

}
