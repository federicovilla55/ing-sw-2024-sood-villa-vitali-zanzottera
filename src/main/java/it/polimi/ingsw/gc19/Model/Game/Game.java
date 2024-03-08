package it.polimi.ingsw.gc19.Model.Game;

import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Deck.Deck;
import it.polimi.ingsw.gc19.Model.Deck.EmptyDeckException;
import it.polimi.ingsw.gc19.Model.Enums.Color;
import it.polimi.ingsw.gc19.Model.Player.Player;
import it.polimi.ingsw.gc19.Model.Player.PlayerNotFoundException;
import it.polimi.ingsw.gc19.Model.Station.Station;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class Game {
    private ArrayList<Player> players;
    private int numPlayers;
    private Player activePlayer;
    private Player firstPlayer;
    private ArrayList<Color> availableColors;
    private HashMap<Player, Station> stations;
    private Deck<GoalCard> goalDeck;
    private Deck<PlayableCard> initialDeck;
    private Deck<PlayableCard> resourceDeck;
    private Deck<PlayableCard> goldDeck;
    private PlayableCard[] goldcardsOnTable;
    private PlayableCard[] resourceCardsOnTable;
    private GoalCard[] pulicGoalCardsOnTable;

    public Game(int numPlayers){
        availableColors = new ArrayList<>(Arrays.asList(Color.BLUE, Color.GREEN, Color.YELLOW, Color.RED));
        players = new ArrayList<Player>();
        stations = new HashMap<>();
        firstPlayer = null;
        this.numPlayers = numPlayers;
        // @todo: insert json filenames
        goalDeck = new Deck<GoalCard>("goal_cards_file_path");
        initialDeck = new Deck<PlayableCard>("initial_cards_file_path");
        resourceDeck = new Deck<PlayableCard>("resource_cards_file_path");
        goldDeck = new Deck<PlayableCard>("gold_cards_file_path");

        fillCardsOnTable();
    }
    public void startGame(){
        // @todo: keep track of turns and game states.
    }

    public void createNewPlayer(String name){
        // @todo: understand whether the case in which two
        //  players have chosen the same name should be handled.
        Player player = new Player(name);
        players.add(player);
        stations.put(player, new Station());
    }
    public void removePlayer(Player p){
        players.remove(p);
        stations.remove(p);
    }
    public Player getPlayerByName(String name) throws PlayerNotFoundException{
        for(Player p : players){
            if(p.getName().equals(name)){
                return p;
            }
        }
        throw new PlayerNotFoundException("A player with the given name was not found.");
    }
    public Player getActivePlayer(){
        return this.activePlayer;
    }
    public Station getStationByPlayer(Player p){
        return stations.get(p);
    }
    public ArrayList<Station> getStations(){
        ArrayList<Station> allStations = new ArrayList<Station>();
        for(Player p : players){
            allStations.add(stations.get(p));
        }
        //stations.values();
        return allStations;
    }
    public void setActivePlayer(Player p){
        this.activePlayer = p;
    }

    public Player getFirstPlayer(){
        return this.firstPlayer;
    }

    public void setFirstPlayer(){
        if(firstPlayer == null && players.size() == numPlayers){
            // Assign the first player to the one at the random index
            Random random = new Random();
            this.firstPlayer = players.get(random.nextInt(players.size()));
        }
    }

    public void fillCardsOnTable() {
        try{
            goldcardsOnTable = new PlayableCard[]{goldDeck.pickACard(), goldDeck.pickACard()};
            resourceCardsOnTable = new PlayableCard[]{resourceDeck.pickACard(), resourceDeck.pickACard()};
            pulicGoalCardsOnTable = new GoalCard[]{goalDeck.pickACard(), goalDeck.pickACard()};
        }catch (EmptyDeckException e){
            // @todo: how to handle exceptions?
            e.printStackTrace();
        }
    }
}
