package it.polimi.ingsw.gc19.Model.Station;

import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Model.Enums.Direction;
import it.polimi.ingsw.gc19.Model.Enums.Symbol;

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
        while (scanner.hasNextLine()) {
            read = scanner.nextLine();
            if (read != null) {
                arguments = read.replaceAll("\\s", "").split("[(),\\->:]+");
                switch (arguments[0]) {
                    case "placeInitialCard" ->
                            singleStationTest.getStation().placeInitialCard(singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get());
                    case "updateCardsInHand" ->
                            singleStationTest.getStation().updateCardsInHand(singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get());
                    case "swapCard" ->
                            singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get().swapCard();
                    case "getCardOverAnchor" -> buildCardOverAnchor(arguments);
                    case "getNumPoints" -> buildGetNumPoints(arguments);
                    case "cardIsPlaceable" -> buildCardIsPlaceable(arguments);
                    case "placeCard" -> buildPlaceCard(arguments);
                    case "getVisibleSymbolsInStation" -> buildGetVisibleSymbolsInStation(arguments);
                    case "updateGoalCardPoints" -> buildGoalCardUpdatePoints(arguments);
                    case "getCardWithAnchor" -> buildGetCardWithAnchor(arguments);
                }
            }
        }
    }

    public SingleStationTest buildTest() throws FileNotFoundException {
        parseFile();
        return this.singleStationTest;
    }

    private void buildCardIsPlaceable(String[] arguments) {
        boolean realOutput;
        try {
            realOutput = singleStationTest.getStation().cardIsPlaceable(singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get(),
                                                                        singleStationTest.getGame().getPlayableCardFromCode(arguments[2]).get(),
                                                                        Direction.valueOf(arguments[3]));
            singleStationTest.getRealOutput().addLast(new Triplet<>(arguments[0], null, realOutput));
        } catch (InvalidCardException | InvalidAnchorException e) {
            singleStationTest.getRealOutput().addLast(new Triplet<>(arguments[0], e, null));
        }

        switch (arguments[4]) {
            case "InvalidCardException" ->
                    singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], new InvalidCardException(), null));
            case "InvalidAnchorException" ->
                    singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], new InvalidAnchorException(), null));
            default ->
                    singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], null, Boolean.parseBoolean(arguments[4])));
        }
    }

    private void buildGetNumPoints(String[] arguments) {
        singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], null, Integer.valueOf(arguments[1])));
        singleStationTest.getRealOutput().addLast(new Triplet<>(arguments[0], null, singleStationTest.getStation().getNumPoints()));
    }

    private void buildPlaceCard(String[] arguments) {
        boolean realOutput;
        try {
            realOutput = singleStationTest.getStation().placeCard(singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get(),
                                                                  singleStationTest.getGame().getPlayableCardFromCode(arguments[2]).get(),
                                                                  Direction.valueOf(arguments[3]));
            singleStationTest.getRealOutput().addLast(new Triplet<>(arguments[0], null, realOutput));
        } catch (InvalidCardException | InvalidAnchorException e) {
            singleStationTest.getRealOutput().addLast(new Triplet<>(arguments[0], e, null));
        }

        switch (arguments[4]) {
            case "InvalidCardException" ->
                    singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], new InvalidCardException(), null));
            case "InvalidAnchorException" ->
                    singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], new InvalidAnchorException(), null));
            default ->
                    singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], null, Boolean.parseBoolean(arguments[4])));
        }
    }

    private void buildGetVisibleSymbolsInStation(String[] arguments) {
        HashMap<Symbol, Integer> expectedHashMap = new HashMap<>();
        for (int i = 1; i < arguments.length - 1; i = i + 2) {
            expectedHashMap.put(Symbol.valueOf(arguments[i]), Integer.valueOf(arguments[i + 1]));
        }
        for (Symbol s : Symbol.values()) {
            if (!expectedHashMap.containsKey(s)) {
                expectedHashMap.put(s, 0);
            }
        }
        singleStationTest.getRealOutput().addLast(new Triplet<>(arguments[0], null, singleStationTest.getStation().getVisibleSymbolsInStation().clone()));
        singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], null, expectedHashMap));
    }

    private void buildGoalCardUpdatePoints(String[] arguments) {
        singleStationTest.getStation().updatePoints(singleStationTest.getGame().getGoalCardFromCode(arguments[1]).get());
        singleStationTest.getRealOutput().addLast(new Triplet<>(arguments[0], null, singleStationTest.getStation().getNumPoints()));
        singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], null, Integer.valueOf(arguments[2])));
    }

    private void buildCardOverAnchor(String[] arguments) {
        boolean cardOverAnchor;
        try {
            cardOverAnchor = singleStationTest.getStation().cardOverAnchor(singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get(),
                                                                           Direction.valueOf(arguments[2]));
            singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], null, cardOverAnchor));
        } catch (InvalidCardException e) {
            singleStationTest.getRealOutput().addLast(new Triplet<>(arguments[0], e, null));
        }
        switch (arguments[3]) {
            case "InvalidCardException" ->
                    singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], new InvalidCardException(), null));
            default ->
                    singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], null, Boolean.valueOf(arguments[3])));
        }
    }

    private void buildGetCardWithAnchor(String[] arguments) {
        Optional<PlayableCard> playableCard;
        try {
            playableCard = singleStationTest.getStation().getCardWithAnchor(singleStationTest.getGame().getPlayableCardFromCode(arguments[1]).get(),
                                                                            Direction.valueOf(arguments[2]));
            singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], null, playableCard));
        } catch (InvalidCardException e) {
            singleStationTest.getRealOutput().addLast(new Triplet<>(arguments[0], e, null));
        }
        switch (arguments[3]) {
            case "InvalidCardException" ->
                    singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], new InvalidCardException(), null));
            case " " ->
                    singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], null, Optional.empty()));
            default ->
                    singleStationTest.getExpectedOutput().addLast(new Triplet<>(arguments[0], null, Optional.of(arguments[3])));
        }
    }

}
