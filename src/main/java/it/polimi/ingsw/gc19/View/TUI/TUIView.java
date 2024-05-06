package it.polimi.ingsw.gc19.View.TUI;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.*;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.Command.CommandType;
import it.polimi.ingsw.gc19.View.GameLocalView.LocalStationPlayer;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TUIView {

    private final CommandParser commandParser;

    public TUIView(CommandParser commandParser){
        this.commandParser = commandParser;
    }

    /**
     * This method prints to terminal a matrix of strings, each string represents two UTF-8 wide character(s),
     * Making all lines aligned. Make sure to use monospace supported fonts, line Noto Sans Mono + Noto Color Emoji
     *
     * @param TUIView the matrix of strings to print
     */
    public void printTUIView(String[][] TUIView) {
        for (String[] strings : TUIView) {
            for (String string : strings) {
                System.out.print(string);
            }
            System.out.println();
        }
    }

    public String[] textTUIView(String text) {
        List<String> res = new ArrayList<>();
        boolean startOfString = true;
        for (Character c : text.toCharArray()) {
            if (startOfString) {
                res.add("" + c);
                startOfString = false;
            } else {
                res.set(res.size() - 1, res.get(res.size() - 1) + c);
                startOfString = true;
            }
        }
        if (!startOfString) {
            res.set(res.size() - 1, res.get(res.size() - 1) + " ");
        }

        return res.toArray(String[]::new);
    }

    public String[][] availableColorsTUIView(List<Color> availableColors) {
        // create matrix of empty strings, each representing a single unicode character to display in console
        String[][] res = new String[availableColors.size() + 2][1];
        for (String[] strings : res) {
            Arrays.fill(strings, "");
        }

        res[0][0] = "Available colors:";
        for (int i = 1; i <= availableColors.size(); i++) {
            Color c = availableColors.get(i - 1);
            res[i][0] = i + ") " + c.stringColor() + c + c.colorReset();
        }

        return res;
    }

    public String[][] availableGamesTUIView(List<String> availableGames) {
        // create matrix of empty strings, each representing a single unicode character to display in console
        String[][] res = new String[availableGames.size() + 2][1];
        for (String[] strings : res) {
            Arrays.fill(strings, "");
        }

        res[0][0] = "Available games:";
        for (int i = 1; i <= availableGames.size(); i++) {
            String s = availableGames.get(i - 1);
            res[i][0] = i + ") " + s;
        }

        return res;
    }

    public String[][] cardTUIView(PlayableCard card) {
        // create matrix of empty strings, each representing a single unicode character to display in console
        String[][] res = new String[3][5];
        for (String[] strings : res) {
            Arrays.fill(strings, "");
        }

        //if the card is initial, set background color to white
        if (card.getCardType().equals(PlayableCardType.INITIAL)) {
            for (String[] strings : res) {
                for (int i = 0; i < strings.length; i++) {
                    strings[i] = strings[i].concat("\u001b[47;1m");
                }
            }
        }
        //else determine color of card by its seed
        else {
            for (String[] strings : res) {
                for (int i = 0; i < strings.length; i++) {
                    strings[i] = strings[i].concat(card.getSeed().stringColor());
                }
            }
        }

        //add to every corner the correct emoji
        for (CornerPosition cp : CornerPosition.values()) {
            Corner corner = card.getCorner(cp);
            res[cp.getX() * 2][cp.getY() * 4] = res[cp.getX() * 2][cp.getY() * 4].concat(corner.stringEmoji());
        }

        //add visible permanent resources
        {
            ArrayList<Symbol> permanentResources = new ArrayList<>(card.getPermanentResources());
            int row;
            row = permanentResources.size() == 1 ? 1 : 0;
            for (Symbol resource : permanentResources) {
                res[row][2] = res[row][2].concat(resource.stringEmoji());
                row++;
            }
            if (permanentResources.size() <= 2) {
                res[2][2] = res[2][2].concat("  ");
                if (permanentResources.size() <= 1) {
                    res[0][2] = res[0][2].concat("  ");
                    if (permanentResources.isEmpty()) {
                        res[1][2] = res[1][2].concat("  ");
                    }
                }
            }
        }

        // add space character where it is certain to not have any symbol
        for (Tuple<Integer, Integer> pos : List.of(new Tuple<>(1, 0), new Tuple<>(1, 4),
                new Tuple<>(0, 1), new Tuple<>(1, 1), new Tuple<>(2, 1),
                new Tuple<>(0, 3), new Tuple<>(1, 3), new Tuple<>(2, 3))) {
            res[pos.x()][pos.y()] = res[pos.x()][pos.y()].concat("  ");
        }
        //add ANSI code to reset background color
        for (String[] strings : res) {
            for (int i = 0; i < strings.length; i++) {
                strings[i] = strings[i].concat("\u001B[0m");
            }
        }
        return res;
    }

    public String[][] initialCardTUIView(PlayableCard card) {
        // create matrix of empty strings, each representing a single unicode character to display in console
        String[][] res = new String[5][17];
        for (String[] strings : res) {
            Arrays.fill(strings, "  ");
        }

        if (card != null) {
            card.setCardState(CardOrientation.UP);
            String[][] cardTUIView = cardTUIView(card);

            //print up above the printed card front
            String[] textTUIView = textTUIView("up:");
            for (int i = 0; i < textTUIView.length; i++) {
                res[0][4 + i] = textTUIView[i];
            }
            //print card front
            for (int i = 0; i < cardTUIView.length; i++) {
                for (int j = 0; j < cardTUIView[i].length; j++) {
                    res[1 + i][4 + j] = cardTUIView[i][j];
                }
            }
            card.setCardState(CardOrientation.DOWN);
            cardTUIView = cardTUIView(card);

            //print down above the printed card back
            textTUIView = textTUIView("down:");
            for (int i = 0; i < textTUIView.length; i++) {
                res[0][12 + i] = textTUIView[i];
            }
            //print card back
            for (int i = 0; i < cardTUIView.length; i++) {
                for (int j = 0; j < cardTUIView[i].length; j++) {
                    res[1 + i][12 + j] = cardTUIView[i][j];
                }
            }
        }

        return res;
    }

    public String[][] cardBackTUIView(Symbol deckSeed) {
        // create matrix of empty strings, each representing a single unicode character to display in console
        String[][] res = new String[3][5];
        for (String[] strings : res) {
            Arrays.fill(strings, "");
        }
        // set correct card color
        for (String[] strings : res) {
            for (int i = 0; i < strings.length; i++) {
                strings[i] = strings[i].concat(deckSeed.stringColor());
            }
        }
        //add to every corner the correct emoji (empty corner)
        for (CornerPosition cp : CornerPosition.values()) {
            res[cp.getX() * 2][cp.getY() * 4] = res[cp.getX() * 2][cp.getY() * 4].concat("\uD83D\uDFE8");
        }
        //add visible permanent resources
        res[1][2] = res[1][2].concat(deckSeed.stringEmoji());
        // add space character where it is certain to not have any symbol
        for (Tuple<Integer, Integer> pos : List.of(new Tuple<>(1, 0), new Tuple<>(1, 4),
                new Tuple<>(0, 1), new Tuple<>(1, 1), new Tuple<>(2, 1),
                new Tuple<>(0, 3), new Tuple<>(1, 3), new Tuple<>(2, 3),
                new Tuple<>(0, 2), new Tuple<>(2, 2))) {
            res[pos.x()][pos.y()] = res[pos.x()][pos.y()].concat("  ");
        }
        //add ANSI code to reset background color
        for (String[] strings : res) {
            for (int i = 0; i < strings.length; i++) {
                strings[i] = strings[i].concat("\u001B[0m");
            }
        }
        return res;
    }

    private PlayableCard dummyPlayableCard(Symbol symbol) {
        return new PlayableCard(
                "dummy",
                PlayableCardType.RESOURCE,
                new Corner[][]{
                        {NotAvailableCorner.NOT_AVAILABLE, NotAvailableCorner.NOT_AVAILABLE},
                        {NotAvailableCorner.NOT_AVAILABLE, NotAvailableCorner.NOT_AVAILABLE}
                },
                new HashMap<>(),
                new Corner[][]{
                        {NotAvailableCorner.NOT_AVAILABLE, NotAvailableCorner.NOT_AVAILABLE},
                        {NotAvailableCorner.NOT_AVAILABLE, NotAvailableCorner.NOT_AVAILABLE}
                },
                new ArrayList<Symbol>(List.of(symbol)),
                new NoConstraintEffect(0),
                null
        );
    }

    public String[][] playerAreaTUIView(List<Tuple<PlayableCard, Tuple<Integer, Integer>>> placedCardSequence) {
        //determine dimension of the matrix
        int h;
        int w;

        //find first and last row with a card placed
        int firstRow = placedCardSequence.stream().mapToInt(x -> x.y().x()).min().orElse(0);
        int lastRow = placedCardSequence.stream().mapToInt(x -> x.y().x()).max().orElse(0);
        //find first and last column with a card placed
        int firstCol = placedCardSequence.stream().mapToInt(x -> x.y().y()).min().orElse(0);
        int lastCol = placedCardSequence.stream().mapToInt(x -> x.y().y()).max().orElse(0);

        //compute matrix dimension (real dimension of the smallest rectangle containing all cards)
        h = lastRow - firstRow + 1;
        w = lastCol - firstCol + 1;

        //change width and height to a value relative to terminal cells:
        // width: for every card 5 cells, minus a cell for every card minus 1, simplifying
        //  w*5-(w-1)=5w-w+1=4w+1
        // height: for every card 3 cells, minus a cell for every card minus 1, simplifying
        //  h*3-(h-1)=3h-h+1=2h+1
        h = 2 * h + 1;
        w = 4 * w + 1;

        // create matrix of "  " strings, each representing two characters to display in console
        String[][] res = new String[h][w];
        for (String[] strings : res) {
            Arrays.fill(strings, "  ");
        }

        //iterate over all cards and add them to the matrix. Card order is important:
        //new cards overwrite angles of older cards, so they have to be passed in place order
        for (Tuple<PlayableCard, Tuple<Integer, Integer>> cardAndPosition
                : placedCardSequence) {
            //use firstRow and firstCol to determine position relative to the matrix
            int relX = cardAndPosition.y().x() - firstRow;
            int relY = cardAndPosition.y().y() - firstCol;

            //change relX and relY to coordinates to the characters coords
            relX = 2 * relX;
            relY = 4 * relY;

            String[][] cardTUIView = this.cardTUIView(cardAndPosition.x());

            //iterate on the matrix and place the card
            for (int i = 0; i < cardTUIView.length; i++) {
                for (int j = 0; j < cardTUIView[0].length; j++) {
                    res[relX + i][relY + j] = cardTUIView[i][j];
                }
            }

        }

        return res;
    }

    public String[][] tableTUIView(PlayableCard resource1, PlayableCard resource2, PlayableCard gold1, PlayableCard gold2, Symbol resourceDeckSeed, Symbol goldDeckSeed) {
        // create matrix of "  " strings, each representing a single unicode character to display in console
        String[][] res = new String[14][25];
        for (String[] strings : res) {
            Arrays.fill(strings, "  ");
        }

        List<String> labels = List.of("res_1", "res_2", "gold_1", "gold_2");
        List<PlayableCard> playableCards = Arrays.asList(resource1, resource2, gold1, gold2);
        List<Tuple<Integer, Integer>> initialCoordsInString = List.of(
                new Tuple<>(2, 4),
                new Tuple<>(2, 12),
                new Tuple<>(8, 4),
                new Tuple<>(8, 12)
        );

        Iterator<String> labelsIterator = labels.iterator();
        Iterator<PlayableCard> cardsIterator = playableCards.iterator();
        Iterator<Tuple<Integer, Integer>> coordsIterator = initialCoordsInString.iterator();

        while (labelsIterator.hasNext() && cardsIterator.hasNext() && coordsIterator.hasNext()) {

            String label = labelsIterator.next();
            PlayableCard card = cardsIterator.next();
            Tuple<Integer, Integer> coords = coordsIterator.next();

            //print label for command line commands over printed card
            String[] textTUIView = textTUIView(label + ":");
            for (int i = 0; i < textTUIView.length; i++) {
                res[coords.x() - 1][coords.y() + i] = textTUIView[i];
            }
            if (card != null) {
                card.setCardState(CardOrientation.UP);
                String[][] cardTUIView = cardTUIView(card);

                //print card
                for (int i = 0; i < cardTUIView.length; i++) {
                    for (int j = 0; j < cardTUIView[i].length; j++) {
                        res[coords.x() + i][coords.y() + j] = cardTUIView[i][j];
                    }
                }
                //print card code under the printed card
                textTUIView = textTUIView(card.getCardCode());
                for (int i = 0; i < textTUIView.length; i++) {
                    res[coords.x() + 3][coords.y() + i] = textTUIView[i];
                }
            } else {
                String[][] emptyCardTUIView = emptyCardTUIView();
                //print empty card placeholder
                for (int i = 0; i < emptyCardTUIView.length; i++) {
                    for (int j = 0; j < emptyCardTUIView[i].length; j++) {
                        res[coords.x() + i][coords.y() + j] = emptyCardTUIView[i][j];
                    }
                }
            }
        }

        //print label for command line commands over printed deck
        String[] textTUIView = textTUIView("res_deck:");
        for (int i = 0; i < textTUIView.length; i++) {
            res[2 - 1][20 + i] = textTUIView[i];
        }
        if (resourceDeckSeed != null) {
            // print deck
            String[][] deckSeedTUIView = cardBackTUIView(resourceDeckSeed);
            for (int i = 0; i < deckSeedTUIView.length; i++) {
                for (int j = 0; j < deckSeedTUIView[i].length; j++) {
                    res[2 + i][20 + j] = deckSeedTUIView[i][j];
                }
            }
        } else {
            String[][] emptyCardTUIView = emptyCardTUIView();
            //print empty card placeholder
            for (int i = 0; i < emptyCardTUIView.length; i++) {
                for (int j = 0; j < emptyCardTUIView[i].length; j++) {
                    res[2 + i][20 + j] = emptyCardTUIView[i][j];
                }
            }
        }


        //print label for command line commands over printed deck
        textTUIView = textTUIView("gold_deck:");
        for (int i = 0; i < textTUIView.length; i++) {
            res[8 - 1][20 + i] = textTUIView[i];
        }
        if (goldDeckSeed != null) {
            // print deck
            String[][] deckSeedTUIView = cardBackTUIView(goldDeckSeed);
            for (int i = 0; i < deckSeedTUIView.length; i++) {
                for (int j = 0; j < deckSeedTUIView[i].length; j++) {
                    res[8 + i][20 + j] = deckSeedTUIView[i][j];
                }
            }
        } else {
            String[][] emptyCardTUIView = emptyCardTUIView();
            //print empty card placeholder
            for (int i = 0; i < emptyCardTUIView.length; i++) {
                for (int j = 0; j < emptyCardTUIView[i].length; j++) {
                    res[8 + i][20 + j] = emptyCardTUIView[i][j];
                }
            }
        }

        return res;
    }

    private String[][] emptyCardTUIView() {
        // create matrix of empty strings, each representing a single unicode character to display in console
        String[][] res = new String[3][5];
        for (String[] strings : res) {
            Arrays.fill(strings, "");
        }
        // set background color to grey
        for (String[] strings : res) {
            for (int i = 0; i < strings.length; i++) {
                strings[i] = strings[i].concat("\u001b[48;5;245m");
            }
        }
        // add space characters
        for (String[] strings : res) {
            for (int i = 0; i < strings.length; i++) {
                strings[i] = strings[i].concat("  ");
            }
        }
        //add ANSI code to reset background color
        for (String[] strings : res) {
            for (int i = 0; i < strings.length; i++) {
                strings[i] = strings[i].concat("\u001B[0m");
            }
        }
        return res;
    }

    private String[][] visibleSymbolsTUIView(Map<Symbol, Integer> visibleSymbols) {
        // create matrix of empty strings, each representing a single unicode character to display in console
        String[][] res = new String[9][1];
        for (String[] strings : res) {
            Arrays.fill(strings, "");
        }
        res[0][0] = "\t┏━━━━━━━━━━┓\t";
        for (Symbol s : Symbol.values()) {
            res[1 + s.ordinal()][0] = "\t┃ " + s.stringEmoji() + "\t" + String.format("%2d", visibleSymbols.get(s)) + " ┃\t";
        }
        res[8][0] = "\t┣━━━━━━━━━━┫\t";

        return res;
    }

    public String[][] scoreboardTUIView(LocalStationPlayer localStationPlayer) {
        // create matrix of empty strings, each representing a single unicode character to display in console
        String[][] res = new String[12][1];
        for (String[] strings : res) {
            Arrays.fill(strings, "");
        }

        Optional<Color> color = Optional.ofNullable(localStationPlayer.getChosenColor());
        res[0][0] = "\t" + color.map(Color::stringColor).orElse("") +
                String.format("%-18.18s",
                        String.format("%.17s", localStationPlayer.getOwnerPlayer()) +
                                ":") +
                color.map(Color::colorReset).orElse("");

        String[][] visibleSymbolsTUIView = visibleSymbolsTUIView(localStationPlayer.getVisibleSymbols());
        for (int i = 0; i < visibleSymbolsTUIView.length; i++) {
            res[1 + i] = visibleSymbolsTUIView[i];
        }

        res[10][0] = "\t┃ " + "\uD83C\uDFC5" + "\t" + String.format("%2d", localStationPlayer.getNumPoints()) + " ┃\t";
        res[11][0] = "\t┗━━━━━━━━━━┛\t";

        return res;
    }

    public String[][] scoreboardTUIView(LocalStationPlayer... localStationPlayer) {
        // create matrix of empty strings, each representing a single unicode character to display in console
        String[][] res = new String[12][localStationPlayer.length];
        for (String[] strings : res) {
            Arrays.fill(strings, "");
        }

        int playerIndex = 0;
        for (LocalStationPlayer lsp : localStationPlayer) {
            String[][] scoreboardTUIView = scoreboardTUIView(lsp);
            for (int i = 0; i < scoreboardTUIView.length; i++) {
                for (int j = 0; j < scoreboardTUIView[i].length; j++) {
                    res[i][playerIndex] = scoreboardTUIView[i][j];
                }
            }
            playerIndex++;
        }

        return res;
    }

    public String[][] handTUIView(List<PlayableCard> cardsInHand) {
        // create matrix of "  " strings, each representing a single unicode character to display in console
        String[][] res = new String[5][25];
        for (String[] strings : res) {
            Arrays.fill(strings, "  ");
        }

        for (int idx = 0; idx < cardsInHand.size(); idx++) {

            PlayableCard card = cardsInHand.get(idx);

            if (card != null) {
                card.setCardState(CardOrientation.UP);
                String[][] cardTUIView = cardTUIView(card);

                //print card
                for (int i = 0; i < cardTUIView.length; i++) {
                    for (int j = 0; j < cardTUIView[i].length; j++) {
                        res[1 + i][4 + idx * 8 + j] = cardTUIView[i][j];
                    }
                }
                //print card code under the printed card
                String[] textTUIView = textTUIView(card.getCardCode());
                for (int i = 0; i < textTUIView.length; i++) {
                    res[1 + 3][4 + idx * 8 + i] = textTUIView[i];
                }
            }
        }

        return res;
    }

    public String[][] goalCardEffectTUIView(GoalCard card) {
        return card.getEffectView(this);
    }

    public String[][] playableCardEffectTUIView(PlayableCard card) {
        return card.getEffectView(this);
    }

    public String[][] goalEffectView(PatternEffect patternEffect) {


        List<Tuple<PlayableCard, Tuple<Integer, Integer>>> placedCardSequence = new ArrayList<>();

        Iterator<Tuple<Integer, Integer>> movesIterator = patternEffect.getMoves().iterator();
        Iterator<Symbol> symbolIterator = patternEffect.getRequiredSymbol().iterator();

        Symbol currentSymbol = symbolIterator.next();
        Tuple<Integer, Integer> currentPosition = new Tuple<>(0, 0);

        placedCardSequence.add(new Tuple<>(dummyPlayableCard(currentSymbol), currentPosition));

        while (movesIterator.hasNext()) {
            currentSymbol = symbolIterator.next();
            Tuple<Integer, Integer> relativePosition = movesIterator.next();
            currentPosition = new Tuple<>(currentPosition.x() + relativePosition.x(), currentPosition.y() + relativePosition.y());

            placedCardSequence.add(new Tuple<>(dummyPlayableCard(currentSymbol), currentPosition));
        }

        String[][] playerAreaTuiView = this.playerAreaTUIView(placedCardSequence);

        String[][] res = new String[playerAreaTuiView.length + 2][playerAreaTuiView[0].length];
        res[0] = this.textTUIView("Pattern:");
        res[1] = new String[]{};
        for (int i = 0; i < playerAreaTuiView.length; i++) {
            res[i + 2] = playerAreaTuiView[i];
        }

        return res;
    }

    public String[][] goalEffectView(SymbolEffect symbolEffect) {
        return this.requiredSymbolsTUIView(symbolEffect.getRequiredSymbol());
    }

    private String[][] requiredSymbolsTUIView(Map<Symbol, Integer> requiredSymbol) {
        // create matrix of empty strings, each representing a single unicode character to display in console
        String[][] res = new String[2 + requiredSymbol.size()][1];
        for (String[] strings : res) {
            Arrays.fill(strings, "");
        }
        res[0][0] = "\t┏━━━━━━━━━━┓\t";
        int i = 1;
        for (Symbol s : requiredSymbol.keySet()) {
            res[i][0] = "\t┃ " + s.stringEmoji() + "\t" + String.format("%2d", requiredSymbol.get(s)) + " ┃\t";
            i++;
        }
        res[i][0] = "\t┗━━━━━━━━━━┛\t";

        return res;
    }

    private void parseCommand(String command){
        Pattern pattern = Pattern.compile("^(.*?)\\(([^)]*)\\)$");
        Matcher matcher = pattern.matcher(command);

        if(matcher.matches()){
            CommandType commandType;
            try{
                commandType = CommandType.valueOf(matcher.group(1));
            }
            catch (IllegalArgumentException illegalArgumentException){
                //@TODO: view
                return;
            }

            String args = matcher.group(2);

            switch (commandType){
                case CommandType.CREATE_PLAYER -> commandParser.createPlayer(args);
                case CommandType.CREATE_GAME -> commandParser.createGame(args);
                case CommandType.AVAILABLE_GAMES -> commandParser.availableGames(args);
                case CommandType.JOIN_GAME -> commandParser.joinGame(args);
                case CommandType.JOIN_FIRST_GAME -> commandParser.joinFirstAvailableGame(args);
                case CommandType.CHOOSE_COLOR -> commandParser.chooseColor(args);
                case CommandType.CHOOSE_PRIVATE_GOAL_CARD -> commandParser.chooseGoal(args);
                case CommandType.PLACE_INITIAL_CARD -> commandParser.placeInitialCard(args);
                case CommandType.PICK_CARD_DECK -> commandParser.pickCardFromDeck(args);
                case CommandType.PICK_CARD_TABLE -> commandParser.pickCardFromTable(args);
                case CommandType.PLACE_CARD -> commandParser.placeCard(args);
                case CommandType.SEND_CHAT_MESSAGE -> commandParser.sendChatMessage(args);
                case CommandType.LOGOUT_FROM_GAME -> commandParser.logoutFromGame(args);
                case CommandType.DISCONNECT -> commandParser.disconnect(args);
            }
        }
        else{
            //@TODO: view: command format not correct!
        }
    }
}
