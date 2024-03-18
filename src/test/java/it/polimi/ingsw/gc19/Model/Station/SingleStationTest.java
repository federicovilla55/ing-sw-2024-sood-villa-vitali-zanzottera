package it.polimi.ingsw.gc19.Model.Station;

import it.polimi.ingsw.gc19.Model.Game.Game;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SingleStationTest{
    private final Station station;
    private final Game game;
    private final ArrayList<Triplet<String, Exception, Object>> expectedOutput;
    private final ArrayList<Triplet<String, Exception, Object>> realOutput;

    public SingleStationTest() throws IOException {
        this.station = new Station(null, null, null);
        this.game = new Game(1);
        this.expectedOutput = new ArrayList<>();
        this.realOutput = new ArrayList<>();
    }

    public ArrayList<Triplet<String, Exception, Object>> getExpectedOutput(){
        return expectedOutput;
    }

    public ArrayList<Triplet<String, Exception, Object>> getRealOutput(){
        return realOutput;
    }

    public Station getStation() {
        return station;
    }

    public Game getGame(){
        return this.game;
    }

    public void testCardIsPlaceable(){
        for(int i = 0; i < expectedOutput.size(); i++){
            if(expectedOutput.get(i).t().equals("cardIsPlaceable")){
                assertEquals(expectedOutput.get(i).toString(), realOutput.get(i).toString());
            }
        }
    }

    public void testPlaceCard(){
        for(int i = 0; i < expectedOutput.size(); i++){
            if(expectedOutput.get(i).t().equals("placeCard")){
                assertEquals(expectedOutput.get(i).toString(), realOutput.get(i).toString());
            }
        }
    }

    public void testGetNumPoints(){
        for(int i = 0; i < expectedOutput.size(); i++){
            if(expectedOutput.get(i).t().equals("getNumPoints")){
                assertEquals(expectedOutput.get(i), realOutput.get(i));
            }
        }
    }

    public void testGetVisibleSymbolsInStation(){
        for(int i = 0; i < expectedOutput.size(); i++){
            if(expectedOutput.get(i).t().equals("getVisibleSymbolsInStation")){
                assertEquals(expectedOutput.get(i), realOutput.get(i));
            }
        }
    }

    public void testUpdatePointsGoalCard(){
        for(int i = 0; i < expectedOutput.size(); i++){
            if(expectedOutput.get(i).t().equals("updateGoalCardPoints")){
                assertEquals(expectedOutput.get(i), realOutput.get(i));
            }
        }
    }

    public void testCardOverAnchor(){
        for(int i = 0; i < expectedOutput.size(); i++){
            if(expectedOutput.get(i).t().equals("cardOverAnchor")){
                assertEquals(expectedOutput.get(i), realOutput.get(i));
            }
        }
    }

    public void testGetCardWithAnchor(){
        for(int i = 0; i < expectedOutput.size(); i++){
            if(expectedOutput.get(i).t().equals("getCardOverAnchor")){
                assertEquals(expectedOutput.get(i), realOutput.get(i));
            }
        }
    }
}
