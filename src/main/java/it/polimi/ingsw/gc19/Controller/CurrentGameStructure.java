package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Enums.State;
import it.polimi.ingsw.gc19.Model.Game.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CurrentGameStructure{
    private final HashMap<String, Game> games;
    private final HashMap<String, State> playerState;
    private final HashMap<String, GameController> playerToGameController;

    public CurrentGameStructure(){
        this.playerToGameController = new HashMap<>();
        this.games = new HashMap<>();
        this.playerState = new HashMap<>();
    }

    public void setInactivePlayer(String player){
        playerState.put(player, State.INACTIVE);
    }

    public ArrayList<String> getInactivePlayers(){
        return playerState.entrySet()
                          .stream()
                          .filter(e -> e.getValue() == State.INACTIVE)
                          .map(Map.Entry::getKey)
                          .collect(Collectors.toCollection(ArrayList::new));
    }

    public void setActivePlayer(String player){
        playerState.put(player, State.ACTIVE);
    }

    public ArrayList<String> getActivePlayers(){
        return playerState.entrySet()
                          .stream()
                          .filter(e -> e.getValue() == State.ACTIVE)
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

    public boolean isActive(String player){
        return this.playerState.get(player) == State.ACTIVE;
    }

}
