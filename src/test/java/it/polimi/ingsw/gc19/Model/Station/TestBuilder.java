package it.polimi.ingsw.gc19.Model.Station;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Tuple;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.Scanner;

class TestBuilder {

    private final File testFile;

    private final SingleStationTest singleStationTest;

    public TestBuilder(String filename) throws IOException {
        testFile = new File(filename);
        singleStationTest = new SingleStationTest();
    }

    private void parseFile() throws FileNotFoundException {
        Scanner scanner = new Scanner(testFile);
        String read;
        String arguments[];
        int lineNumber = 1;
        while (scanner.hasNextLine()) {
            read = scanner.nextLine();
            if (read != null) {
                arguments = read.replaceAll("\\s", "").split("[(),\\->:]+");
                switch (arguments[0]) {
                    case "placeInitialCard" ->
                            singleStationTest.getStation().placeInitialCard(singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get(), CardOrientation.valueOf(arguments[2]));
                    case "updateCardsInHand" ->
                            singleStationTest.getStation().updateCardsInHand(singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get());
                    case "swapCard" ->
                            singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get().swapCard();
                    case "getCardOverAnchor" -> buildCardOverAnchor(testFile.getName(), lineNumber, arguments);
                    case "getNumPoints" -> buildGetNumPoints(testFile.getName(), lineNumber, arguments);
                    case "cardIsPlaceable" -> buildCardIsPlaceable(testFile.getName(), lineNumber, arguments);
                    case "placeCard" -> buildPlaceCard(testFile.getName(), lineNumber, arguments);
                    case "getVisibleSymbolsInStation" -> buildGetVisibleSymbolsInStation(testFile.getName(), lineNumber, arguments);
                    case "updateGoalCardPoints" -> buildGoalCardUpdatePoints(testFile.getName(), lineNumber, arguments);
                    case "getCardWithAnchor" -> buildGetCardWithAnchor(testFile.getName(), lineNumber, arguments);
                }
                lineNumber++;
            }
        }
    }

    public SingleStationTest buildTest() throws FileNotFoundException {
        parseFile();
        return this.singleStationTest;
    }

    private void buildCardIsPlaceable(String fileName, int lineNumber, String[] arguments) {
        boolean realOutput;
        try {
            realOutput = singleStationTest.getStation().cardIsPlaceable(singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get(),
                                                                        singleStationTest.getGame().getPlayableCardFromCode(arguments[2]).get(),
                                                                        Direction.valueOf(arguments[3]));
            singleStationTest.getRealOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                                  new Tuple<>(null, realOutput));
        } catch (InvalidCardException | InvalidAnchorException e) {
            singleStationTest.getRealOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                                  new Tuple<>(e, null));
        }

        switch (arguments[4]) {
            case "InvalidCardException" ->
                    singleStationTest.getExpectedOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                                          new Tuple<>(new InvalidCardException(), null));
            case "InvalidAnchorException" ->
                    singleStationTest.getExpectedOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                                          new Tuple<>(new InvalidAnchorException(), null));
            default ->
                    singleStationTest.getExpectedOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                                          new Tuple<>(null, Boolean.parseBoolean(arguments[4])));
        }
    }

    private void buildGetNumPoints(String fileName, int lineNumber, String[] arguments) {
        singleStationTest.getExpectedOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]), new Tuple<>(null, Integer.valueOf(arguments[1])));
        singleStationTest.getRealOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                              new Tuple<>(null, singleStationTest.getStation().getNumPoints()));
    }

    private void buildPlaceCard(String fileName, int lineNumber, String[] arguments) {
        boolean realOutput;
        try {
            realOutput = singleStationTest.getStation().placeCard(singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get(),
                                                                  singleStationTest.getGame().getPlayableCardFromCode(arguments[2]).get(),
                                                                  Direction.valueOf(arguments[3]),
                                                                  singleStationTest.getGame().getPlayableCardFromCode(arguments[2]).get().getCardOrientation());
            singleStationTest.getRealOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                                  new Tuple<>(null, realOutput));
        } catch (InvalidCardException | InvalidAnchorException e) {
            singleStationTest.getRealOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                                  new Tuple<>(e, null));
        }

        switch (arguments[4]) {
            case "InvalidCardException" ->
                    singleStationTest.getExpectedOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                                          new Tuple<>(new InvalidCardException(), null));
            case "InvalidAnchorException" ->
                    singleStationTest.getExpectedOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                                          new Tuple<>(new InvalidAnchorException(), null));
            default ->
                    singleStationTest.getExpectedOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                                          new Tuple<>(null, Boolean.parseBoolean(arguments[4])));
        }
    }

    private void buildGetVisibleSymbolsInStation(String fileName, int lineNumber, String[] arguments) {
        HashMap<Symbol, Integer> expectedHashMap = new HashMap<>();
        for (int i = 1; i < arguments.length - 1; i = i + 2) {
            expectedHashMap.put(Symbol.valueOf(arguments[i]), Integer.valueOf(arguments[i + 1]));
        }
        for (Symbol s : Symbol.values()) {
            if (!expectedHashMap.containsKey(s)) {
                expectedHashMap.put(s, 0);
            }
        }
        singleStationTest.getRealOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                              new Tuple<>(null, singleStationTest.getStation().getVisibleSymbolsInStation().clone()));
        singleStationTest.getExpectedOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                              new Tuple<>(null, expectedHashMap));
    }

    private void buildGoalCardUpdatePoints(String fileName, int lineNumber, String[] arguments) {
        singleStationTest.getStation().updatePoints(singleStationTest.getGame().getGoalCardFromCode(arguments[1]).get());
        singleStationTest.getRealOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                               new Tuple<>(null, singleStationTest.getStation().getNumPoints()));
        singleStationTest.getExpectedOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                              new Tuple<>(null, Integer.valueOf(arguments[2])));
    }

    private void buildCardOverAnchor(String fileName, int lineNumber, String[] arguments) {
        boolean cardOverAnchor;
        try {
            cardOverAnchor = singleStationTest.getStation().cardOverAnchor(singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get(),
                                                                           Direction.valueOf(arguments[2]));
            singleStationTest.getRealOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                                  new Tuple<>(null, cardOverAnchor));
        } catch (InvalidCardException e) {
            singleStationTest.getRealOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                                  new Tuple<>(e, null));
        }
        switch (arguments[3]) {
            case "InvalidCardException" ->
                    singleStationTest.getExpectedOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                                          new Tuple<>(new InvalidCardException(), null));
            default ->
                    singleStationTest.getExpectedOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                                          new Tuple<>(null, Boolean.parseBoolean(arguments[3])));
        }
    }

    private void buildGetCardWithAnchor(String fileName, int lineNumber, String[] arguments) {
        Optional<PlayableCard> playableCard;
        try {
            playableCard = singleStationTest.getStation().getCardWithAnchor(singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get(),
                                                                            Direction.valueOf(arguments[2]));
            singleStationTest.getRealOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                                  new Tuple<>(null, playableCard.map(Card::getCardCode).orElse("empty")));
        } catch (InvalidCardException e) {
            singleStationTest.getRealOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                                  new Tuple<>(e, null));
        }
        switch (arguments[3]) {
            case "InvalidCardException" ->
                    singleStationTest.getExpectedOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                                          new Tuple<>(new InvalidCardException(), null));
            case "empty" ->
                    singleStationTest.getExpectedOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                                          new Tuple<>(null, "empty"));
            default ->
                    singleStationTest.getExpectedOutput().put(new Triplet<>(fileName, lineNumber, arguments[0]),
                                                          new Tuple<>(null, arguments[3]));
        }
    }

}
