package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Enums.State;
import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Model.Tuple;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.ObserverPattern.Observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CurrentGameStructure{

    private final HashMap<String, Game> games;
    private final HashMap<String, Tuple<State, Observer<MessageToClient>>> playerState;
    private final HashMap<String, GameController> playerToGameController;
    private final HashMap<String, GameController> gamesNamesToGameController;


    public CurrentGameStructure(){
        this.playerToGameController = new HashMap<>();
        this.games = new HashMap<>();
        this.playerState = new HashMap<>();
        this.gamesNamesToGameController = new HashMap<>();
    }


    public void setInactivePlayer(String player){
        playerState.put(player, new Tuple<>(State.INACTIVE, this.playerState.get(player).y()));
    }

    public ArrayList<String> getInactivePlayers(){
        return playerState.entrySet()
                          .stream()
                          .filter(e -> e.getValue().x() == State.INACTIVE)
                          .map(Map.Entry::getKey)
                          .collect(Collectors.toCollection(ArrayList::new));
    }

    public void setActivePlayer(String player){
        playerState.put(player, new Tuple<>(State.ACTIVE, this.playerState.get(player).y()));
    }

    public ArrayList<String> getActivePlayers(){
        return playerState.entrySet()
                          .stream()
                          .filter(e -> e.getValue().x() == State.ACTIVE)
                          .map(Map.Entry::getKey)
                          .collect(Collectors.toCollection(ArrayList::new));
    }

    public void insertGameControllerForPlayer(String player, GameController gameController){
        this.playerToGameController.put(player, gameController);
    }

    public void registerPlayer(String nick, Observer<MessageToClient> observer){
        this.playerState.put(nick, new Tuple<>(State.ACTIVE, observer));
    }

    public Observer<MessageToClient> getObserverOfPlayer(String nick){
        return this.playerState.get(nick).y();
    }

    public GameController getGameControllerFromPlayer(String player){
        return this.playerToGameController.get(player);
    }

    public void insertGame(String gameName, Game game){
        games.put(gameName, game);
    }

    public Game getGameFromName(String gameName){
        return games.get(gameName);
    }

    public boolean checkGameAlreadyExist(String gameToCheck){
        return games.containsKey(gameToCheck);
    }

    public boolean isActive(String player){
        return this.playerState.get(player).x() == State.ACTIVE;
    }

    public GameController getGameControllerForGame(String gameName){
        return this.gamesNamesToGameController.get(gameName);
    }

    public void registerGameController(String gameName, GameController gameController){
        this.gamesNamesToGameController.put(gameName, gameController);
    }

    public boolean userNameAlreadyTaken(String userName){
        return this.playerState.containsKey(userName);
    }
}
