package it.polimi.ingsw.gc19.View.TUI;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.CornerPosition;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.Corner;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Utils.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class TUIView {

    /**
     * This method prints to terminal a matrix of strings, each string represents two UTF-8 wide character(s),
     * Making all lines aligned. Make sure to use monospace supported fonts, line Noto Sans Mono + Noto Color Emoji
     *
     * @param cardTUIView the matrix of strings to print
     */
    public void printTUIView(String[][] cardTUIView) {
        for (String[] strings : cardTUIView) {
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

    public String[][] deckSeedTUIView(Symbol deckSeed) {
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
            String[][] deckSeedTUIView = deckSeedTUIView(resourceDeckSeed);
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
            String[][] deckSeedTUIView = deckSeedTUIView(goldDeckSeed);
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
}
