package it.polimi.ingsw.gc19.Model.Station;

import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Enums.Direction;
import it.polimi.ingsw.gc19.Model.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Game.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class StationTest{

    private ArrayList<SingleStationTest> stationTests;

    @BeforeEach
    public void setUPTest() throws IOException{
        stationTests = new ArrayList<>();
        stationTests.add(new TestBuilder("src/test/java/it/polimi/ingsw/gc19/Model/Station/Test.txt").buildTest());
    }

    @Test
    void testCardIsPlaceable(){
        for(SingleStationTest s : stationTests){
            s.testCardIsPlaceable();
        }
    }

    @Test
    void testPlaceCard(){
        for(SingleStationTest s : stationTests){
            s.testPlaceCard();
        }
    }

    @Test
    void testGetNumPoints(){
        for(SingleStationTest s : stationTests){
            s.testGetNumPoints();
        }
    }

    @Test
    void testGetVisibleSymbolsInStation(){
        for(SingleStationTest s : stationTests){
            s.testGetVisibleSymbolsInStation();
        }
    }

    @Test
    void testUpdatePointsGoalCard(){
        for(SingleStationTest s : stationTests){
            s.testUpdatePointsGoalCard();
        }
    }

    @Test
    void testCardOverAnchor(){
        for(SingleStationTest s : stationTests){
            s.testCardOverAnchor();
        }
    }

    @Test
    void testGetCardWithAnchor(){
        for(SingleStationTest s : stationTests){
            s.testGetCardWithAnchor();
        }
    }

}