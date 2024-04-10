package it.polimi.ingsw.gc19.Model.Station;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

class StationTest{

    private static ArrayList<SingleStationTest> stationTests;

    @BeforeAll
    public static void setUpTest() throws IOException{
        stationTests = new ArrayList<>();
        stationTests.add(new TestBuilder("src/test/java/it/polimi/ingsw/gc19/Model/Station/Test Station - 1").buildTest());
        stationTests.add(new TestBuilder("src/test/java/it/polimi/ingsw/gc19/Model/Station/Test Station - 2").buildTest());
        stationTests.add(new TestBuilder("src/test/java/it/polimi/ingsw/gc19/Model/Station/Test Station - 3").buildTest());
    }

    @AfterAll
    public static void tearDownTest() {
        stationTests = null;
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