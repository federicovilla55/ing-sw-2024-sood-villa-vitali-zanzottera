package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Enums.State;
import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Model.Tuple;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.ObserverPattern.Observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CurrentGameStructure{
    private final HashMap<String, Game> games;
    private final HashMap<String, Tuple<State, Observer<MessageToClient>>> playerInfo;
    private final HashMap<String, GameController> playerToGameController;

    public CurrentGameStructure(){
        this.playerToGameController = new HashMap<>();
        this.games = new HashMap<>();
        this.playerInfo = new HashMap<>();
    }

    public void setInactivePlayer(String player){
        playerInfo.put(player, new Tuple<>(State.INACTIVE, this.playerInfo.get(player).y()));
    }

    public void setActivePlayer(String player){
        playerInfo.put(player, new Tuple<>(State.ACTIVE, this.playerInfo.get(player).y()));
    }

    public ArrayList<String> getInactivePlayers(){
        return playerInfo.entrySet()
                         .stream()
                         .filter(e -> e.getValue().x() == State.INACTIVE)
                         .map(Map.Entry::getKey)
                         .collect(Collectors.toCollection(ArrayList::new));
    }

    public void setActivePlayer(String player,Observer<MessageToClient> client){
        playerInfo.put(player, new Tuple<>(State.ACTIVE, client));
    }

    public ArrayList<String> getActivePlayers(){
        return playerInfo.entrySet()
                         .stream()
                         .filter(e -> e.getValue().x() == State.ACTIVE)
                         .map(Map.Entry::getKey)
                         .collect(Collectors.toCollection(ArrayList::new));
    }

    public Game getGameFromPlayer(String player){
        return playerToGameController.get(player).getGameAssociated();
    }

    public void insertGameControllerForPlayer(String player, GameController gameController){
        this.playerToGameController.put(player, gameController);
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

}