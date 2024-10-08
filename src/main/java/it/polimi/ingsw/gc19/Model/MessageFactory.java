package it.polimi.ingsw.gc19.Model;

import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.ObserverPattern.ObservableMessageToClient;
import it.polimi.ingsw.gc19.ObserverPattern.ObserverMessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.ClientHandler;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is responsible for distributing messages
 * generated by model and controller to all listeners.
 * Listeners can be named (corresponds to a {@link ClientHandler})
 * or unnamed (corresponds to loggers).
 */
public class MessageFactory implements ObservableMessageToClient<MessageToClient> {

    /**
     * List of connected unnamed observers
     */
    private final List<ObserverMessageToClient<MessageToClient>> mockedView;

    /**
     * List of connected named observers
     */
    private final Map<String, ObserverMessageToClient<MessageToClient>> connectedClients;

    public MessageFactory(){
        this.connectedClients = new HashMap<>();
        this.mockedView = new ArrayList<>();
    }

    /**
     * This method is used to send a message to a single player
     * @param nick nickname of the player to send the message to
     * @param message message to send
     */
    public void sendMessageToPlayer(String nick, MessageToClient message){
        notifyObservers(message.setHeader(new ArrayList<>(List.of(nick))));
    }

    /**
     * This method is used to a message to a group of player in the game
     * @param nick list of nickname to send message to
     * @param message message to send
     */
    public void sendMessageToPlayer(List<String> nick, MessageToClient message){
        notifyObservers(message.setHeader(nick));
    }

    /**
     * This method is used to send a message to all game players
     * @param message message to send
     */
    public void sendMessageToAllGamePlayers(MessageToClient message){
        synchronized (this.connectedClients) {
            notifyObservers(message.setHeader(new ArrayList<>(connectedClients.keySet())));
        }
    }

    /**
     * This method is used to send a message to all game players except <code>nickExcept</code>
     * @param message message to send
     * @param nickExcept player that hasn't to receive the message
     */
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

    /**
     * This method is used to attach an anonymous observer to {@link MessageFactory}
     * @param observerMessageToClient observer to attach
     */
    @Override
    public void attachObserver(ObserverMessageToClient<MessageToClient> observerMessageToClient){
        synchronized (this.mockedView) {
            this.mockedView.add(observerMessageToClient);
        }
    }

    /**
     * This method is used to attach a named observer to {@link MessageFactory}
     * @param nickname nickname of player owning {@param observer}
     * @param observerMessageToClient the observer of the player
     */
    @Override
    public void attachObserver(String nickname, ObserverMessageToClient<MessageToClient> observerMessageToClient){
        synchronized (this.connectedClients) {
            this.connectedClients.put(nickname, observerMessageToClient);
        }
    }

    /**
     * This method is used to remove a named observer from the {@link MessageFactory}
     * @param nickName name of the player owning the observer to remove
     */
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

    /**
     * This method is used to remove an anonymous observer from the {@link MessageFactory}
     * @param obs anonymous observer to remove
     */
    @Override
    public void removeObserver(ObserverMessageToClient<MessageToClient> obs) {
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

    /**
     * Notifies all observers (both named and anonymous) attached
     * @param message the {@link MessageToClient} to be dispatched
     */
    private void notifyObservers(MessageToClient message){
        notifyAnonymousObservers(message);
        notifyNamedObservers(message);
    }

    /**
     * This method is used to notify named observers.
     * @param message message to send to named observers
     */
    @Override
    public void notifyNamedObservers(MessageToClient message) {
        synchronized (this.connectedClients) {
            for (Map.Entry<String, ObserverMessageToClient<MessageToClient>> obs : this.connectedClients.entrySet()) {
                if (message.getHeader().contains(obs.getKey()))
                    obs.getValue().update(message);
            }
        }
    }

    /**
     * This method is used to notify anonymous observers
     * @param message message to send to anonymous observer
     */
    @Override
    public void notifyAnonymousObservers(MessageToClient message) {
        synchronized (this.mockedView) {
            for (ObserverMessageToClient<MessageToClient> obs : this.mockedView) {
                obs.update(message);
            }
        }
    }

}
