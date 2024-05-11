package it.polimi.ingsw.gc19.View.TUI;

import it.polimi.ingsw.gc19.Enums.*;
import it.polimi.ingsw.gc19.Model.Card.*;
import it.polimi.ingsw.gc19.Model.Chat.Message;
import it.polimi.ingsw.gc19.Networking.Client.ClientInterface;
import it.polimi.ingsw.gc19.Networking.Client.ClientSettings;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.Configuration;
import it.polimi.ingsw.gc19.Networking.Client.Configuration.ConfigurationManager;
import it.polimi.ingsw.gc19.Utils.Tuple;
import it.polimi.ingsw.gc19.View.ClientController.*;
import it.polimi.ingsw.gc19.View.Command.CommandParser;
import it.polimi.ingsw.gc19.View.Command.CommandType;
import it.polimi.ingsw.gc19.View.GameLocalView.*;
import it.polimi.ingsw.gc19.View.Listeners.GameEventsListeners.*;
import it.polimi.ingsw.gc19.View.Listeners.GameHandlingListeners.GameHandlingEvents;
import it.polimi.ingsw.gc19.View.Listeners.SetupListeners.SetupEvent;
import it.polimi.ingsw.gc19.View.UI;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TUIView implements UI, GeneralListener {

    private LocalModel localModel;
    private final CommandParser commandParser;
    private final ClientController clientController;

    enum ShowState{
        NOT_PLAYING, PERSONAL_STATION, OTHER_STATION, CHAT;
    }

    private ShowState showState;
    private String currentViewPlayer;

    private record StationInfos(PersonalStation personalStation, List<LocalStationPlayer> allStations) { }

    private static Configuration.ConnectionType chooseClientType() {
        String connectionType;
        do {
            System.out.println("Please enter what type of connection you want to use to connect to game server: ");
            System.out.println("- RMI");
            System.out.println("- TCP");
            Scanner scanner = new Scanner(System.in);
            connectionType = scanner.nextLine();
        } while (!connectionType.equalsIgnoreCase("rmi") && !connectionType.equalsIgnoreCase("tcp"));

        return Configuration.ConnectionType.valueOf(connectionType.toUpperCase());
    }

    private void runTUI() {
        Configuration config;
        Configuration.ConnectionType connectionType;
        String reconnectChoice;

        ClientInterface client;
        try {
            config = ConfigurationManager.retriveConfiguration();
            connectionType = config.getConnectionType();

            do {
                System.out.println("Configuration found. Printing infos about last interaction with server:");

                System.out.println("-> nickname: " + config.getNick());
                System.out.println("-> timestamp: " + config.getTimestamp());
                System.out.println("-> connection type: " + config.getConnectionType());

                System.out.println("do you want to try to reconnect? (s/n) ");

                Scanner scanner = new Scanner(System.in);
                reconnectChoice = scanner.nextLine();
            } while(!reconnectChoice.equalsIgnoreCase("s") && !reconnectChoice.equalsIgnoreCase("n"));

            if(reconnectChoice.equalsIgnoreCase("s")) {
                client = connectionType.getClientFactory().createClient(clientController);
                client.configure(config.getNick(), config.getToken());
                clientController.setNickname(config.getNick());
                clientController.setClientInterface(client);
                clientController.setNextState(new Disconnect(clientController));
            }
            else{
                ConfigurationManager.deleteConfiguration();
            }

        } catch (RuntimeException e) {
            System.out.println("No valid configuration found... creating new client");
            reconnectChoice = "n";
        } catch (IOException e) {
            System.out.println("Error while creating client... aborting");
            System.exit(1);
            return;
        }

        if(reconnectChoice.equalsIgnoreCase("n")) {
            connectionType = chooseClientType();
            try {
                client = connectionType.getClientFactory().createClient(clientController);
            } catch (IOException e) {
                System.out.println("Error while creating client... aborting");
                System.exit(1);
                return;
            }
            clientController.setClientInterface(client);
            System.out.println("Successfully connected to the server!");
            System.out.print("> ");
            clientController.setNextState(new NotPlayer(clientController));
        }

        Scanner scanner = new Scanner(System.in);

        while (!Thread.currentThread().isInterrupted()){
            String command = scanner.nextLine();

            this.parseCommand(command);
        }
    }

    public TUIView(CommandParser commandParser) {
        this.commandParser = commandParser;
        this.clientController = commandParser.clientController();
        this.clientController.getListenersManager().attachListener(this);
        this.clientController.setView(this);

        this.localModel = null;
        this.showState = ShowState.NOT_PLAYING;
        this.currentViewPlayer = "";
        Thread readerThread = new Thread(this::runTUI);
        //readerThread.setDaemon(true);
        readerThread.start();
    }

    public void setLocalModel(LocalModel localModel){
        this.localModel = localModel;
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

    String[] textTUIView(String text) {
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

    String[][] availableColorsTUIView(List<Color> availableColors) {
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

    String[][] availableGamesTUIView(List<String> availableGames) {
        // create matrix of empty strings, each representing a single unicode character to display in console
        String[][] res = new String[availableGames.size() + 2][1];
        for (String[] strings : res) {
            Arrays.fill(strings, "");
        }

        if(!availableGames.isEmpty()) {
            res[0][0] = "Available games:";
            for (int i = 1; i <= availableGames.size(); i++) {
                String s = availableGames.get(i - 1);
                res[i][0] = i + ") " + s;
            }
        }
        else{
            res[0][0] = "At the moment there are not available games to join...";
        }

        return res;
    }

    String[][] cardTUIView(PlayableCard card) {
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

    String[][] initialCardTUIView(PlayableCard card) {
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

    private String[][] cardBackTUIView(Symbol deckSeed) {
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

    String[][] playerAreaTUIView(List<Tuple<PlayableCard, Tuple<Integer, Integer>>> placedCardSequence) {
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

    String[][] tableTUIView(PlayableCard resource1, PlayableCard resource2, PlayableCard gold1, PlayableCard gold2, Symbol resourceDeckSeed, Symbol goldDeckSeed) {
        // create matrix of "  " strings, each representing a single unicode character to display in console
        String[][] res = new String[14][40];
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

    String[][] scoreboardTUIView(LocalStationPlayer localStationPlayer) {
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

    String[][] scoreboardTUIView(LocalStationPlayer... localStationPlayer) {
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

    String[][] handTUIView(List<PlayableCard> cardsInHand) {
        // create matrix of "  " strings, each representing a single unicode character to display in console
        String[][] res = new String[5][40];
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

    String[][] backHandTUIView(List<Tuple<Symbol, PlayableCardType>> cardSymbolsInHand) {
        // create matrix of "  " strings, each representing a single unicode character to display in console
        String[][] res = new String[5][40];
        for (String[] strings : res) {
            Arrays.fill(strings, "  ");
        }

        for (int idx = 0; idx < cardSymbolsInHand.size(); idx++) {

            Tuple<Symbol,PlayableCardType> cardBack = cardSymbolsInHand.get(idx);

            if (cardBack != null) {
                String[][] cardTUIView = cardBackTUIView(cardBack.x());

                //print card
                for (int i = 0; i < cardTUIView.length; i++) {
                    for (int j = 0; j < cardTUIView[i].length; j++) {
                        res[1 + i][4 + idx * 8 + j] = cardTUIView[i][j];
                    }
                }
            }
        }

        return res;
    }

    private String[][] chatTUIView(ArrayList<Message> chat){
        ArrayList<String> printedChat = new ArrayList<>();

        printedChat.add("\n");

        for(Message message : chat){
            System.out.println(localModel.getStations().values().stream().map(LocalStationPlayer::getOwnerPlayer).toList() + "   " + message.getSenderPlayer());
            Optional<Color> color = Optional.ofNullable(localModel.getStations().get(message.getSenderPlayer()).getChosenColor());
            printedChat.add(color.map(Color::stringColor).orElse("") +
                    String.format("%-18.18s",
                                  String.format("%.17s", localModel.getStations().get(message.getSenderPlayer()).getOwnerPlayer()) +
                                          ":") +
                    color.map(Color::colorReset).orElse("") + "\t" + message.getSendTime());
            printedChat.add(message.getMessage());
            printedChat.add("\n");
        }

        return printedChat.stream()
                        .map(s -> new String[]{s})
                        .toArray(String[][]::new);
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

    private void printPlacingSequence(){
        if(this.localModel == null) return;
        
        System.out.println("This is your placing history: ");
        for(int i = 0; i < this.localModel.getPersonalStation().getPlacedCardSequence().size(); i++){
            System.out.println((i + 1) + ") " + this.localModel.getPersonalStation().getPlacedCardSequence().get(i).x().getCardCode());
            printTUIView(cardTUIView(this.localModel.getPersonalStation().getPlacedCardSequence().get(i).x()));
            System.out.println();
        }
}

    private void TUIViewCommands(Matcher matcher){
        switch (matcher.group(1)) {
            case "show_private_goal_card" -> choosePrivateGoalCardScene();
            case "help" -> printHelper();
            case "show_initial_card" -> showInitialCard();
            case "show_chat" -> {
                if(this.localModel == null) return;
                this.showState = ShowState.CHAT;
                this.currentViewPlayer = localModel.getNickname();
                printChat();
            }
            case "show_station" -> {
                if(this.localModel == null) return;
                this.showState = ShowState.OTHER_STATION;
                this.currentViewPlayer = matcher.group(2);
                printOtherStation();
            }
            case "show_personal_station" -> {
                if(this.localModel == null) return;
                this.showState = ShowState.PERSONAL_STATION;
                this.currentViewPlayer = localModel.getNickname();
                printPersonalStation();
            }
            case "show_placing_sequence" -> printPlacingSequence();
            case "info_card" -> printInfoCard(matcher.group(2));
            default -> System.out.println("Command is not recognized! Try again...");
        }
    }

    private void commandParserCommands(Matcher matcher, CommandType commandType){
        String args = matcher.group(2);

        switch (commandType) {
            case CommandType.CREATE_PLAYER            -> commandParser.createPlayer(args);
            case CommandType.CREATE_GAME              -> commandParser.createGame(args);
            case CommandType.JOIN_GAME                -> commandParser.joinGame(args);
            case CommandType.AVAILABLE_COLORS         -> clientController.availableColors();
            case CommandType.CHOOSE_COLOR             -> commandParser.chooseColor(args);
            case CommandType.CHOOSE_GOAL -> commandParser.chooseGoal(args);
            case CommandType.PLACE_INITIAL_CARD       -> commandParser.placeInitialCard(args);
            case CommandType.PICK_CARD_DECK           -> commandParser.pickCardFromDeck(args);
            case CommandType.PICK_CARD_TABLE          -> commandParser.pickCardFromTable(args);
            case CommandType.PLACE_CARD               -> commandParser.placeCard(args);
            case CommandType.SEND_CHAT_MESSAGE        -> commandParser.sendChatMessage(args);
            case CommandType.JOIN_FIRST_GAME          -> clientController.joinFirstAvailableGame();
            case CommandType.AVAILABLE_GAMES          -> clientController.availableGames();
            case CommandType.LOGOUT_FROM_GAME         -> clientController.logoutFromGame();
            case CommandType.DISCONNECT               -> clientController.disconnect();
            default                                   -> System.out.println("Command is not recognized! Try again...");
        }
    }

    private void parseCommand(String command) {
        Pattern pattern = Pattern.compile("^([^(]*)\\(([^)]*)\\)$");
        Matcher matcher = pattern.matcher(command);

        if (matcher.matches()) {
            CommandType commandType;
            try {
                commandType = CommandType.valueOf(matcher.group(1).toUpperCase());

                commandParserCommands(matcher, commandType);
            } catch (IllegalArgumentException illegalArgumentException) {
                TUIViewCommands(matcher);
            }

            System.out.print(">");
        }
        else {
            System.out.println("Command " + command + " is not recognized! Try again...");
            System.out.print(">");
        }
    }

    private void showInitialCard() {
        if(this.localModel == null) return;
        
        if(this.localModel.getPersonalStation().getInitialCard() != null) {
            System.out.println("This is your initial card: ");
            System.out.println(this.localModel.getPersonalStation().getInitialCard().getCardCode());
            printTUIView(initialCardTUIView(this.localModel.getPersonalStation().getInitialCard()));
        }
        else{
            System.out.println("No infos about your initial card, try later...");
        }
    }

    private void choosePrivateGoalCardScene() {
        if(this.localModel == null) return;
        
        if(this.localModel.getPersonalStation().getPrivateGoalCardInStation() != null){
            System.out.println("This is your private goal card: ");
            GoalCard goalCard = this.localModel.getPersonalStation().getPrivateGoalCardInStation();
            System.out.println(goalCard.getCardDescription());
            printTUIView(goalCardEffectTUIView(goalCard));
        }
        else{
            if(this.localModel.getPersonalStation().getPrivateGoalCardsInStation() != null) {
                System.out.println("Those are your private goal card you can choose: ");
                GoalCard goalCard = this.localModel.getPersonalStation().getPrivateGoalCardsInStation()[0];
                System.out.println(goalCard.getCardDescription());
                printTUIView(goalCardEffectTUIView(goalCard));
                System.out.println();
                goalCard = this.localModel.getPersonalStation().getPrivateGoalCardsInStation()[1];
                System.out.println(goalCard.getCardDescription());
                printTUIView(goalCardEffectTUIView(goalCard));
            }
            else{
                System.out.println("No infos about your private goal. Try later...");
            }
        }
    }

    @Override
    public void notify(String message) {
        System.out.println(message);
        System.out.print(">");
    }

    @Override
    public void notifyPlayerCreation(String name) {
        System.out.println("Your player has been correctly created. Your username is: " + name);
        System.out.println();
        System.out.print(">");
    }

    @Override
    public void notifyPlayerCreationError(String error) {
        System.out.println("[ERROR]: " + error);
        System.out.print(">");
    }

    @Override
    public void notifyGenericError(String errorDescription){
        System.err.println("[ERROR]: " + errorDescription);
        System.out.print(">");
    }

    @Override
    public void notify(GameHandlingEvents type, List<String> varArgs) {
        switch (type){
            case GameHandlingEvents.CREATED_GAME -> System.out.println("The requested game '" + varArgs.getFirst() + "' has been created!");
            case GameHandlingEvents.JOINED_GAMES -> System.out.println("You have been registered to game named '" + varArgs.getFirst() + "'.");
            case AVAILABLE_GAMES -> {
                printTUIView(availableGamesTUIView(varArgs));
                System.out.print(">");
            }
        }
    }

    @Override
    public void notify(SetupEvent type){
        switch (type){
            case SetupEvent.AVAILABLE_COLOR -> {
                if(localModel.getAvailableColors() != null && localModel.getPersonalStation() != null && localModel.getPersonalStation().getChosenColor() == null) {
                    printTUIView(availableColorsTUIView(localModel.getAvailableColors()));
                }
                else {
                    System.out.println("No infos about available colors, try later...");
                }
            }
            case SetupEvent.ACCEPTED_COLOR -> {
                System.out.println("Your color is now: " + localModel.getPersonalStation().getChosenColor());
            }
            case SetupEvent.ACCEPTED_INITIAL_CARD -> {
                System.out.println("Initial card placed successfully");
            }
            case SetupEvent.ACCEPTED_PRIVATE_GOAL_CARD -> {
                System.out.println("Private card accepted successfully");
            }
            case SetupEvent.COMPLETED -> {
                this.showState = ShowState.PERSONAL_STATION;
                printPersonalStation();
            }
        }
        System.out.print(">");
    }

    @Override
    public void notify(ArrayList<Message> msg){
        if(this.showState == ShowState.CHAT) {
            printChat();
            System.out.print(">");
        }
    }

    @Override
    public void notify(LocalModelEvents type, LocalModel localModel, String ... varArgs){
        switch (type) {
            case NEW_PLAYER_CONNECTED -> System.out.println(varArgs[0]+ " has joined the game!");
            case RECONNECTED_PLAYER -> System.out.println(varArgs[0]+ " has reconnected to the game!");
            case DISCONNECTED_PLAYER -> System.out.println(varArgs[0]+ " disconnected...");
        }
        System.out.println(">");
    }

    @Override
    public void notify(PersonalStation localStationPlayer){
        if(this.showState == ShowState.PERSONAL_STATION) {
            printPersonalStation();
        }
        System.out.print(">");
    }

    @Override
    public void notify(OtherStation otherStation){
        if(this.showState == ShowState.OTHER_STATION
            && Objects.equals(otherStation.getOwnerPlayer(), this.currentViewPlayer)) {
            printOtherStation();
        }
        System.out.print(">");

    }

    @Override
    public void notifyErrorStation(String... varArgs) {
        System.err.println("[ERROR]: card " + varArgs[0] + " is not placeable starting from " + varArgs[1] + " in direction " + varArgs[2] + "! Try again...");
        System.out.print(">");
    }

    @Override
    public void notify(LocalTable localTable){
        if(this.showState == ShowState.PERSONAL_STATION) {
            printPersonalStation();
            System.out.print(">");
        }
        else if(this.showState == ShowState.OTHER_STATION) {
            printOtherStation();
            System.out.print(">");
        }
    }

    @Override
    public void notify(String nick, TurnState turnState){
        if(this.localModel.getPersonalStation().getOwnerPlayer().equals(nick)){
            System.out.println("It is your turn, you can " + turnState.toString().toLowerCase());
        }
        else{
            System.out.println("It is the turn of player '" + nick + "'. He / she can " + turnState.toString().toLowerCase());
        }
        System.out.print(">");
    }

    @Override
    public void notify(ViewState viewState) {
        switch (viewState){
            case ViewState.NOT_PLAYER -> printCreationPlayerScene();
            case ViewState.NOT_GAME -> printEnteringGameScene();
            case ViewState.SETUP -> System.out.println("This the setup phase. Choose your setup...");
            case ViewState.PAUSE -> System.out.println("Game is in pause! Sorry, you have to wait...");
            case ViewState.DISCONNECT -> System.err.println("[NETWORK PROBLEMS]: there are network problems. In background, we are trying to fix them...");
            case ViewState.END -> printWinners();
        }

        System.out.print(">");

    }

    private void clearTerminal(){
        try{
            if(System.getProperty("os.name").contains("Windows")){
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            }
            else{
                System.out.println("\033\143");
            }
        }
        catch (IOException | InterruptedException ignored){ }
    }

    private void printInfoCard(String cardCode){
        String[] code = cardCode.split("_");

        if(this.localModel == null) return;

        if(code.length == 2) {
            if (code[0].equals("goal") && this.localModel.getGoalCard(cardCode) != null) {
                printTUIView(goalCardEffectTUIView(this.localModel.getGoalCard(cardCode)));
                return;
            }

            if((code[0].equals("resource") || code[0].equals("initial") || code[0].equals("gold")) && this.localModel.getPlayableCard(cardCode) != null) {
                printTUIView(playableCardEffectTUIView(this.localModel.getPlayableCard(cardCode)));
                return;
            }

        }

        System.out.println("Card code is not recognized! ");
        System.out.print(">");
    }

    private void printChat(){
        if(localModel == null) return;
        this.clearTerminal();
        printTUIView(chatTUIView(localModel.getMessages()));
    }

    private void printPersonalStation(){
        if(this.localModel == null) return;

        StationInfos stationInfos = getStations();

        this.clearTerminal();
        printScoreBoard(stationInfos.allStations());
        System.out.println("Your station:");
        System.out.println("\n");
        printTUIView(playerAreaTUIView(stationInfos.personalStation().getPlacedCardSequence()));
        System.out.println("\n");
        System.out.println("Your hand:");
        printTUIView(handTUIView(stationInfos.personalStation().getCardsInHand()));
        System.out.println("\n");
    }

    @NotNull
    private TUIView.StationInfos getStations() {
        PersonalStation personalStation = this.localModel.getPersonalStation();
        List<OtherStation> otherStations = localModel.getOtherStations().values().stream().toList();
        List<LocalStationPlayer> allStations = new ArrayList<>();
        allStations.add(personalStation);
        allStations.addAll(otherStations);
        return new StationInfos(personalStation, allStations);
    }

    private void printOtherStation(){
        if(this.localModel == null) return;
        
        if(this.currentViewPlayer.isEmpty() || this.currentViewPlayer.equals(localModel.getNickname())) return;
        if(localModel.getOtherStations().get(this.currentViewPlayer) == null) {
            this.notify("There is no other player with that name!");
            return;
        }
        
        this.clearTerminal();
        
        Optional<Color> color = Optional.ofNullable(localModel.getOtherStations().get(this.currentViewPlayer).getChosenColor());
        System.out.println("You are currently visualizing the station of: " + color.map(Color::stringColor).orElse("") +
                                   localModel.getOtherStations().get(this.currentViewPlayer).getOwnerPlayer() +
                                   color.map(Color::colorReset).orElse("") + "\n");
        StationInfos stationInfos = getStations();
        printScoreBoard(stationInfos.allStations);
        System.out.println(color.map(Color::stringColor).orElse("") +
                        localModel.getOtherStations().get(this.currentViewPlayer).getOwnerPlayer() +
                color.map(Color::colorReset).orElse("") + " station:");
        System.out.println("\n");
        printTUIView(playerAreaTUIView(localModel.getOtherStations().get(this.currentViewPlayer).getPlacedCardSequence()));
        System.out.println("\n");
        System.out.println(color.map(Color::stringColor).orElse("") +
                        localModel.getOtherStations().get(this.currentViewPlayer).getOwnerPlayer() +
                color.map(Color::colorReset).orElse("") + " hand:");
        printTUIView(backHandTUIView(this.localModel.getOtherStations().get(this.currentViewPlayer).getBackCardHand()));
        System.out.println("\n");
    }

    private void printScoreBoard(List<LocalStationPlayer> allStations) {
        System.out.println("Scoreboard:");
        printTUIView(scoreboardTUIView(allStations.toArray(new LocalStationPlayer[]{})));
        System.out.println("\n");
        printTable();
    }

    private void printTable() {
        System.out.println("Table:");
        printTUIView(tableTUIView(localModel.getTable().getResource1(), localModel.getTable().getResource2(),
                localModel.getTable().getGold1(), localModel.getTable().getGold2(),
                localModel.getTable().getNextSeedOfResourceDeck(), localModel.getTable().getNextSeedOfGoldDeck()));
        System.out.println("\n");
    }

    private void printCreationPlayerScene(){
        this.clearTerminal();
        System.out.println(ClientSettings.CODEX_NATURALIS_LOGO);
        printHelper();
        System.out.println();
        System.out.println("All is ready to start!");
        System.out.println();
    }

    private void printEnteringGameScene(){
        this.clearTerminal();
        System.out.println(ClientSettings.CODEX_NATURALIS_LOGO);
        System.out.println("Now you can create or join a game.\n");
        System.out.println();
        System.out.print(">");
    }

    private void printWinners(){
        this.clearTerminal();
        System.out.println(ClientSettings.CODEX_NATURALIS_LOGO);
        System.out.print("Game " + localModel.getGameName() + "ended! \nCongratulation to ");
        for(String name : localModel.getWinners()) System.out.print(name + " ");
        System.out.print("for winning the game!");
    }

    private void printHelper(){
        System.out.println("This is the helper of TUI view. Here you can find the infos about commands: ");

        System.out.println();

        System.out.println("-> " + CommandType.CREATE_PLAYER.getCommandName() + "(nick): to register your player;");

        System.out.println();

        System.out.println("-> " + CommandType.CREATE_GAME.getCommandName() + "(game_name, number_of_player): to create your game with the specified number of players;");
        System.out.println("-> " + CommandType.JOIN_GAME.getCommandName() + "(game name): to join the specified game;");
        System.out.println("-> " + CommandType.JOIN_FIRST_GAME.getCommandName() + "(): to join first available game;");
        System.out.println("-> " + CommandType.AVAILABLE_GAMES.getCommandName() + "(): to display the available games;");

        System.out.println();

        System.out.println("-> " + CommandType.AVAILABLE_COLORS.getCommandName() + "(): to display available colors;");
        System.out.println("-> " + CommandType.CHOOSE_COLOR.getCommandName() + "(color): to pick choose your color;");
        System.out.println("-> " + CommandType.CHOOSE_GOAL.getCommandName() + "(idx): to choose your private goal card: idx is the index of the card;");
        System.out.println("-> " + CommandType.PLACE_INITIAL_CARD.getCommandName() + "(card_orientation): to place initial card with the specified orientation (UP, DOWN);");
        System.out.println("-> show_private_goal_card(): to show your private goal card/s;");
        System.out.println("-> show_personal_station(): to see your personal station;");
        System.out.println("-> show_station(nick): to see the station of the player 'nick';");
        System.out.println("-> show_chat(): to see chat;");
        System.out.println("-> info_card(card_code): gives infos about a card;");

        System.out.println();

        System.out.println("-> " + CommandType.PLACE_CARD.getCommandName() + "(anchor_code, to_place_code, direction, card_orientation): " +
                                   "to place card with code 'to_place_code' from card 'anchor_code' with the direction (UP_RIGHT, UP_LEFT, DOWN_LEFT, DOWN_RIGHT) and orientation (UP, DOWN) specified;");
        System.out.println("-> " + CommandType.PICK_CARD_TABLE.getCommandName() + "(type, position): to pick a card of type 'type' (RESOURCE, GOLD) from table in position 'position';");
        System.out.println("-> " + CommandType.PICK_CARD_DECK.getCommandName() + "(type): to pick a card of type 'type' (RESOURCE, GOLD) from deck;");

        System.out.println();

        System.out.println("-> " + CommandType.LOGOUT_FROM_GAME.getCommandName() + "(): to logout from game;");
        System.out.println("-> " + CommandType.DISCONNECT.getCommandName() + "(): to disconnect from server and close app;");

        System.out.println();
        System.out.println("-> help(): to see helper;");
    }

}