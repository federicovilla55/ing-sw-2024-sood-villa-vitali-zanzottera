package it.polimi.ingsw.gc19.Model.Station;

import it.polimi.ingsw.gc19.Controller.MessageFactory;
import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Model.Tuple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SingleStationTest{
    private final Station station;
    private final Game game;
    private final HashMap<Triplet<String, Integer, String>,Tuple<Exception, Object>> expectedOutput;
    private final HashMap<Triplet<String, Integer, String>,Tuple<Exception, Object>> realOutput;

    public SingleStationTest() throws IOException {
        this.station = new Station(null, null, null);
        this.game = new Game(1);
        this.expectedOutput = new HashMap<>();
        this.realOutput = new HashMap<>();
    }

    public HashMap<Triplet<String, Integer, String>, Tuple<Exception, Object>> getExpectedOutput(){
        return expectedOutput;
    }

    public HashMap<Triplet<String, Integer, String>, Tuple<Exception, Object>> getRealOutput(){
        return realOutput;
    }

    public Station getStation() {
        return station;
    }

    public Game getGame(){
        return this.game;
    }

    public void testCardIsPlaceable(){
        for(Triplet<String, Integer, String> key : expectedOutput.keySet()){
            if(key.s().equals("cardIsPlaceable")){
                try {
                    assertEquals(expectedOutput.get(key).toString(), realOutput.get(key).toString());
                }catch(AssertionError e){
                    System.out.println("Error at -> " + key + "\n" +
                                       "Expected -> " + expectedOutput.get(key) + "\n" +
                                       "Found -> " + realOutput.get(key));
                    throw new AssertionError();
                }
            }
        }
    }

    public void testPlaceCard(){
        for(Triplet<String, Integer, String> key : expectedOutput.keySet()){
            if(key.s().equals("placeCard")){
                try {
                    assertEquals(expectedOutput.get(key).toString(), realOutput.get(key).toString());
                }catch(AssertionError e){
                    System.out.println("Error at -> " + key + "\n" +
                                               "Expected -> " + expectedOutput.get(key) + "\n" +
                                               "Found -> " + realOutput.get(key));
                    throw new AssertionError();
                }
            }
        }
    }

    public void testGetNumPoints(){
        for(Triplet<String, Integer, String> key : expectedOutput.keySet()){
            if(key.s().equals("getNumPoints")){
                try {
                    assertEquals(expectedOutput.get(key), realOutput.get(key));
                }catch(AssertionError e){
                    System.out.println("Error at -> " + key + "\n" +
                                               "Expected -> " + expectedOutput.get(key) + "\n" +
                                               "Found -> " + realOutput.get(key));
                    throw new AssertionError();
                }
            }
        }
    }

    public void testGetVisibleSymbolsInStation(){
        for(Triplet<String, Integer, String> key : expectedOutput.keySet()){
            if(key.s().equals("getVisibleSymbolsInStation")){
                try {
                    assertEquals(expectedOutput.get(key), realOutput.get(key));
                }catch(AssertionError e){
                    System.out.println("Error at -> " + key + "\n" +
                                               "Expected -> " + expectedOutput.get(key) + "\n" +
                                               "Found -> " + realOutput.get(key));
                    throw new AssertionError();
                }
            }
        }
    }

    public void testUpdatePointsGoalCard(){
        for(Triplet<String, Integer, String> key : expectedOutput.keySet()){
            if(key.s().equals("updateGoalCardPoints")){
                try {
                    assertEquals(expectedOutput.get(key), realOutput.get(key));
                }catch(AssertionError e){
                    System.out.println("Error at -> " + key + "\n" +
                                               "Expected -> " + expectedOutput.get(key) + "\n" +
                                               "Found -> " + realOutput.get(key));
                    throw new AssertionError();
                }
            }
        }
    }

    public void testCardOverAnchor(){
        for(Triplet<String, Integer, String> key : expectedOutput.keySet()){
            if(key.s().equals("cardOverAnchor")){
                try {
                    //System.out.println(expectedOutput.get(key) + " " + realOutput.get(key));
                    assertEquals(expectedOutput.get(key).toString(), realOutput.get(key).toString());
                }catch(AssertionError e){
                    System.out.println("Error at -> " + key + "\n" +
                                               "Expected -> " + expectedOutput.get(key) + "\n" +
                                               "Found -> " + realOutput.get(key));
                    throw new AssertionError();
                }
            }
        }
    }

    public void testGetCardWithAnchor(){
        for(Triplet<String, Integer, String> key : expectedOutput.keySet()){
            if(key.s().equals("getCardWithAnchor")){
                try {
                    assertEquals(expectedOutput.get(key), realOutput.get(key));
                }catch(AssertionError e){
                    System.out.println("Error at -> " + key + "\n" +
                                               "Expected -> " + expectedOutput.get(key) + "\n" +
                                               "Found -> " + realOutput.get(key));
                    throw new AssertionError();
                }
            }
        }
    }
}
