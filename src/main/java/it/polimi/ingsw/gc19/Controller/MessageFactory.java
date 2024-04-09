package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OwnStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessagePriorityComparator;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.ObserverPattern.Observable;
import it.polimi.ingsw.gc19.ObserverPattern.Observer;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class MessageFactory implements Observable<MessageToClient>{

    private final List<Observer<MessageToClient>> mockedView;
    private final Map<String, Observer<MessageToClient>> connectedClients;

    public MessageFactory(){
        this.connectedClients = new HashMap<>();
        this.mockedView = new ArrayList<>();
    }

    public void sendMessageToPlayer(String nick, MessageToClient message){
        notifyObservers(message.setHeader(new ArrayList<>(List.of(nick))));
    }

    public void sendMessageToPlayer(List<String> nick, MessageToClient message){
        notifyObservers(message.setHeader(nick));
    }

    public void sendMessageToAllGamePlayers(MessageToClient message){
        synchronized (this.connectedClients) {
            notifyObservers(message.setHeader(new ArrayList<>(connectedClients.keySet())));
        }
    }

    public void sendMessageToAllGamePlayersExcept(MessageToClient message, String ... nickExcept){
        ArrayList<String> newHeader;
        ArrayList<String> exceptedPlayers = new ArrayList<>(List.of(nickExcept));
        synchronized (this.connectedClients) {
            newHeader = connectedClients.keySet()
                                        .stream()
                                        .filter(p -> !exceptedPlayers.contains(p))
                                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
        notifyObservers(message.setHeader(newHeader));
    }

    public void attachObserver(Observer<MessageToClient> observer){
        synchronized (this.mockedView) {
            this.mockedView.add(observer);
        }
    }

    @Override
    public void attachObserver(String nickname, Observer<MessageToClient> observer){
        synchronized (this.connectedClients) {
            this.connectedClients.put(nickname, observer);
        }
    }

    @Override
    public void removeObserver(String nickName) {
        synchronized (this.connectedClients) {
            connectedClients.remove(connectedClients.entrySet()
                                                    .stream()
                                                    .filter(e -> e.getKey().equals(nickName))
                                                    .findAny()
                                                    .orElseThrow(RuntimeException::new)
                                                    .getKey());
        }
    }

    @Override
    public void removeObserver(Observer<MessageToClient> obs) {
        synchronized (this.connectedClients) {
            for (String key : connectedClients
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue().equals(obs))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet())) {
                connectedClients.remove(key);
            }
        }
        synchronized (this.mockedView) {
            mockedView.remove(obs);
        }
    }

    public void notifyObservers(MessageToClient message){
        notifyAnonymousObservers(message);
        notifyNamedObservers(message);
    }

    @Override
    public void notifyNamedObservers(MessageToClient message) {
        synchronized (this.connectedClients) {
            for (Map.Entry<String, Observer<MessageToClient>> obs : this.connectedClients.entrySet()) {
                if (message.getHeader().contains(obs.getKey()))
                    obs.getValue().update(message);
            }
        }
    }

    @Override
    public void notifyAnonymousObservers(MessageToClient message) {
        synchronized (this.mockedView) {
            for (Observer<MessageToClient> obs : this.mockedView) {
                obs.update(message);
            }
        }
    }

}
