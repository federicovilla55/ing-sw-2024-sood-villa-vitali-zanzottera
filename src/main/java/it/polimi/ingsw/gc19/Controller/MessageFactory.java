package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessagePriorityComparator;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.ObserverPattern.Observable;
import it.polimi.ingsw.gc19.ObserverPattern.Observer;

import java.util.*;
import java.util.stream.Collectors;

public class MessageFactory implements Observable<MessageToClient>{

    private final PriorityQueue<MessageToClient> messagesToBeProcessed;
    private final List<Observer<MessageToClient>> mockedView;
    private final Map<String, Observer<MessageToClient>> connectedClients;

    public MessageFactory(){
        this.connectedClients = new HashMap<>();
        this.mockedView = new ArrayList<>();
        this.messagesToBeProcessed = new PriorityQueue<>(new MessagePriorityComparator());
        new Thread(){
            @Override
            public void run(){
                MessageFactory.this.notifyObservers();
            }
        }.start();
    }

    public void sendMessageToPlayer(String nick, MessageToClient message){
        synchronized(this.messagesToBeProcessed){
            this.messagesToBeProcessed.add(message.setHeader(new ArrayList<>(List.of(nick))));
            this.messagesToBeProcessed.notifyAll();
        }
        //notifyObservers(message.setHeader(new ArrayList<>(List.of(nick))));
    }

    public void sendMessageToPlayer(List<String> nick, MessageToClient message){
        synchronized(this.messagesToBeProcessed){
            this.messagesToBeProcessed.add(message.setHeader(nick));
            this.messagesToBeProcessed.notifyAll();
        }
        //notifyObservers(message.setHeader(nick));
    }

    public void sendMessageToAllGamePlayers(MessageToClient message){
        synchronized(this.messagesToBeProcessed){
            this.messagesToBeProcessed.add(message.setHeader(new ArrayList<>(connectedClients.keySet())));
            this.messagesToBeProcessed.notifyAll();
        }
        //notifyObservers(message.setHeader(new ArrayList<>(connectedClients.keySet())));
    }

    public void sendMessageToAllGamePlayersExcept(MessageToClient message, String ... nickExcept){
        ArrayList<String> exceptedPlayers = new ArrayList<>(List.of(nickExcept));
        ArrayList<String> newHeader = connectedClients.keySet()
                                                      .stream()
                                                      .filter(p -> !exceptedPlayers.contains(p))
                                                      .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        synchronized(this.messagesToBeProcessed){
            this.messagesToBeProcessed.add(message.setHeader(newHeader));
            this.messagesToBeProcessed.notifyAll();
        }
        //notifyObservers(message.setHeader(newHeader));
    }

    public void attachObserver(Observer<MessageToClient> observer){
        this.mockedView.add(observer);
    }

    @Override
    public void attachObserver(String nickname, Observer<MessageToClient> observer){
        this.connectedClients.put(nickname, observer);
    }

    @Override
    public void removeObserver(String nickName) {
        connectedClients.remove(connectedClients.entrySet()
                                                .stream()
                                                .filter(e -> e.getKey().equals(nickName))
                                                .findAny()
                                                .orElseThrow(RuntimeException::new)
                                                .getKey());
    }

    @Override
    public void removeObserver(Observer<MessageToClient> obs) {
        for (String key : connectedClients
                .entrySet()
                .stream()
                .filter(e -> e.getValue().equals(obs))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet())) {
            connectedClients.remove(key);
        }
        mockedView.remove(obs);
    }

    public void notifyObservers(){
        synchronized(this.messagesToBeProcessed){
            while(this.messagesToBeProcessed.isEmpty()){
                try{
                    this.messagesToBeProcessed.wait();
                }catch(InterruptedException ignored){};
            }
            notifyNamedObservers(this.messagesToBeProcessed.remove());
            notifyAnonymousObservers(this.messagesToBeProcessed.remove());
        }
    }

    @Override
    public void notifyNamedObservers(MessageToClient message) {
        for(Map.Entry<String,Observer<MessageToClient>> obs : this.connectedClients.entrySet()){
            if (message.getHeader().contains(obs.getKey()))
                obs.getValue().update(message);
        }

    }

    @Override
    public void notifyAnonymousObservers(MessageToClient message) {
        for(Observer<MessageToClient> obs : this.mockedView){
            obs.update(message);
        }
    }

}
