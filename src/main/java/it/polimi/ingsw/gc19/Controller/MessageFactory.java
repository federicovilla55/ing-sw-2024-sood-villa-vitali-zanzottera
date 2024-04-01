package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.ObserverPattern.Observable;
import it.polimi.ingsw.gc19.ObserverPattern.Observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageFactory implements Observable<MessageToClient>{

    private final ArrayList<Observer<MessageToClient>> mockedView;
    private final HashMap<String, Observer<MessageToClient>> connectedClients;

    public MessageFactory(){
        this.connectedClients = new HashMap<>();
        this.mockedView = new ArrayList<>();
    }

    public void sendMessageToPlayer(String nick, MessageToClient message){
        notifyObservers(message.setHeader(new ArrayList<>(List.of(nick))));
    }

    public void sendMessageToPlayer(ArrayList<String> nick, MessageToClient message){
        notifyObservers(message.setHeader(nick));
    }


    public void sendMessageToAllGamePlayers(MessageToClient message){
        notifyObservers(message.setHeader(connectedClients.keySet()
                                                         .stream()
                                                         .collect(ArrayList::new, ArrayList::add, ArrayList::addAll)));
    }

    public void sendMessageToAllGamePlayersExcept(MessageToClient message, String ... nickExcept){
        ArrayList<String> exceptedPlayers = new ArrayList<>(List.of(nickExcept));
        ArrayList<String> newHeader = connectedClients.keySet()
                                                      .stream()
                                                      .filter(p -> !exceptedPlayers.contains(p))
                                                      .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        notifyObservers(message.setHeader(newHeader));
    }

    public void attachObserver(Observer<MessageToClient> observer){
        this.mockedView.add(observer);
    }

    @Override
    public void attachObserver(String nickname, Observer<MessageToClient> observer){
        this.connectedClients.put(nickname, observer);
    }

    @Override
    public void removeObserver(Observer<MessageToClient> observer) {
        connectedClients.remove(connectedClients.entrySet()
                                                .stream()
                                                .filter(e -> e.getValue().equals(observer))
                                                .findAny()
                                                .orElseThrow(RuntimeException::new)
                                                .getKey());
    }

    @Override
    public void notifyObservers(MessageToClient message){
        notifyNamedObservers(message);
        notifyAnonymousObservers(message);
    }

    @Override
    public void notifyNamedObservers(MessageToClient message) {
        for(Observer<MessageToClient> obs : this.connectedClients.values()){
            obs.update(message);
        }

    }

    @Override
    public void notifyAnonymousObservers(MessageToClient message) {
        for(Observer<MessageToClient> obs : this.mockedView){
            obs.update(message);
        }
    }
}
