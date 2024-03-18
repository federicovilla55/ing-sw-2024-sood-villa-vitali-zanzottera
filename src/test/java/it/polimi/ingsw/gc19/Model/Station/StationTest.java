package it.polimi.ingsw.gc19.Model.Station;

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

class TestBuilder{

    private final File testFile;

    private final SingleStationTest singleStationTest;

    public TestBuilder(String filename) throws IOException {
        testFile = new File(filename);
        singleStationTest = new SingleStationTest();
    }

    private void parseFile() throws FileNotFoundException{
        Scanner scanner = new Scanner(testFile);
        String read;
        String arguments[];
        while(scanner.hasNextLine()){
            read = scanner.nextLine();
            if(read != null){
                arguments = read.replaceAll("\\s","").split("[(),\\->:]+");
                switch(arguments[0]){
                    case "placeInitialCard" -> singleStationTest.getStation().placeInitialCard(singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get());
                    case "updateCardsInHand" -> singleStationTest.getStation().updateCardsInHand(singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get());
                    case "swapCard" -> singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get().swapCard();
                    case "getNumPoints" -> buildGetNumPoints(arguments);
                    case "cardIsPlaceable" -> buildCardIsPlaceable(arguments);
                    case "placeCard" -> buildPlaceCard(arguments);
                    case "getVisibleSymbolsInStation" -> buildGetVisibleSymbolsInStation(arguments);
                    case "updateGoalCardPoints" -> buildGoalCardUpdatePoints(arguments);
                }
            }
        }
    }

    public SingleStationTest buildTest() throws FileNotFoundException{
        parseFile();
        return this.singleStationTest;
    }

    private void buildCardIsPlaceable(String[] arguments){
        boolean realOutput;
        try{
            realOutput = singleStationTest.getStation().cardIsPlaceable(singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get(),
                                                                        singleStationTest.getGame().getPlayableCardFromCode(arguments[2]).get(),
                                                                        Direction.valueOf(arguments[3]));
            singleStationTest.getRealOutput().addLast(new Triplet<>(arguments[0], null, realOutput));
        }
        catch(InvalidCardException | InvalidAnchorException e){
            singleStationTest.getRealOutput().addLast(new Triplet<>(arguments[0], e, null));
        }

        switch(arguments[4]){
            case "InvalidCardException" -> singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], new InvalidCardException(), null));
            case "InvalidAnchorException" -> singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], new InvalidAnchorException(), null));
            default -> singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], null, Boolean.parseBoolean(arguments[4])));
        }
    }

    private void buildGetNumPoints(String[] arguments){
        singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], null, Integer.valueOf(arguments[1])));
        singleStationTest.getRealOutput().addLast(new Triplet<>(arguments[0], null, singleStationTest.getStation().getNumPoints()));
    }

    private void buildPlaceCard(String[] arguments){
        boolean realOutput;
        try{
            realOutput = singleStationTest.getStation().placeCard(singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get(),
                                                                  singleStationTest.getGame().getPlayableCardFromCode(arguments[2]).get(),
                                                                  Direction.valueOf(arguments[3]));
            singleStationTest.getRealOutput().addLast(new Triplet<>(arguments[0], null, realOutput));
        }
        catch(InvalidCardException | InvalidAnchorException e){
            singleStationTest.getRealOutput().addLast(new Triplet<>(arguments[0], e, null));
        }

        switch(arguments[4]){
            case "InvalidCardException" -> singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], new InvalidCardException(), null));
            case "InvalidAnchorException" -> singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], new InvalidAnchorException(), null));
            default -> singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], null, Boolean.parseBoolean(arguments[4])));
        }
    }

    private void buildGetVisibleSymbolsInStation(String[] arguments){
        HashMap<Symbol, Integer> expectedHashMap = new HashMap<>();
        for(int i = 1; i < arguments.length - 1; i = i + 2){
            expectedHashMap.put(Symbol.valueOf(arguments[i]), Integer.valueOf(arguments[i + 1]));
        }
        for(Symbol s : Symbol.values()){
            if(!expectedHashMap.containsKey(s)){
                expectedHashMap.put(s, 0);
            }
        }
        singleStationTest.getRealOutput().addLast(new Triplet<>("getVisibleSymbolsInStation", null, singleStationTest.getStation().getVisibleSymbolsInStation().clone()));
        singleStationTest.getExpectedOutput().addLast(new Triplet<>("getVisibleSymbolsInStation", null, expectedHashMap));
    }

    private void buildGoalCardUpdatePoints(String[] arguments){
        singleStationTest.getStation().updatePoints(singleStationTest.getGame().getGoalCardFromCode(arguments[1]).get());
        singleStationTest.getRealOutput().addLast(new Triplet<>("getVisibleSymbolsInStation", null, singleStationTest.getStation().getNumPoints()));
        singleStationTest.getExpectedOutput().addLast(new Triplet<>("getVisibleSymbolsInStation", null, Integer.valueOf(arguments[2])));
    }

}

class SingleStationTest{
    private final Station station;
    private final Game game;
    private final ArrayList<Triplet<String, Exception, Object>> expectedOutput;
    private final ArrayList<Triplet<String, Exception, Object>> realOutput;

    public SingleStationTest() throws IOException {
        this.station = new Station();
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
}

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

}

/*
case "place_initial" -> singleStationTest.getStation().placeInitialCard(singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get());
                    case "insert_card" -> singleStationTest.getStation().placeInitialCard(singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get());
                    case "place" -> {
                                        singleStationTest.getStation().updateCardsInHand(singleStationTest.getGame().getPlayableCardFromCode(arguments[2]).get());
                                        singleStationTest.getStation().placeCard(singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get(),
                                                                                 singleStationTest.getGame().getPlayableCardFromCode(arguments[2]).get(),
                                                                                 Direction.valueOf(arguments[3]));
                                    }
                    case "set_expected_points" -> singleStationTest.setExpectedPoints(Integer.parseInt(arguments[1]));
                    case "set_private_goal" -> singleStationTest.getStation().setPrivateGoalCard(singleStationTest.getGame().getGoalCardFromCode(arguments[1]).get());
                    case "update_goal_points" -> singleStationTest.getStation().updatePoints(singleStationTest.getGame().getGoalCardFromCode(arguments[1]).get());
                    case "swap" -> singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get().swapCard();
 */