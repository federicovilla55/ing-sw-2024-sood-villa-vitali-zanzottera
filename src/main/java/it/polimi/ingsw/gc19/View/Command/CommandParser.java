package it.polimi.ingsw.gc19.View.Command;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.View.ClientController.ClientController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public record CommandParser(ClientController clientController) {

    private static String[] parseArguments(String args, boolean deleteSpaces) throws IllegalArgumentException {
        if (args == null) throw new IllegalArgumentException();

        if(deleteSpaces) {
            args = args.replaceAll("\\s", "");
        }

        String[] arguments = args.split(",\\s*");

        Arrays.stream(arguments)
              .filter(String::isEmpty)
              .findAny()
              .ifPresent(s -> {throw new IllegalArgumentException();});

        return arguments;
    }

    public void chooseColor(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs, true);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("requested color is not in " + List.of(Color.values()));
            return;
        }

        if (parsedArguments.length == CommandType.CHOOSE_COLOR.getNumArgs()) {
            try {
                clientController.chooseColor(Color.valueOf(parsedArguments[0].toUpperCase()));
            } catch (IllegalArgumentException illegalArgumentException) {
                this.clientController.getView().notifyGenericError("requested color is not in " + List.of(Color.values()));
            }
        }
        else {
            this.clientController.getView().notifyGenericError("required " + CommandType.CHOOSE_COLOR.getNumArgs() + "arguments, provided " + parsedArguments.length);
        }
    }

    public void chooseGoal(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs, true);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("integer expected");
            return;
        }

        if (parsedArguments.length == CommandType.CHOOSE_PRIVATE_GOAL_CARD.getNumArgs()) {
            try {
                clientController.chooseGoal(Math.abs(Integer.parseInt(parsedArguments[0])));
            } catch (IllegalArgumentException illegalArgumentException) {
                this.clientController.getView().notifyGenericError("integer expected");
            }
        }
        else {
            this.clientController.getView().notifyGenericError("required " + CommandType.CHOOSE_PRIVATE_GOAL_CARD.getNumArgs() + "arguments, provided " + parsedArguments.length);
        }
    }

    public void createGame(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs, true);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("non-empty arguments expected");
            return;
        }

        if (parsedArguments.length == CommandType.CREATE_GAME.getNumArgs()) {
            try {
                clientController.createGame(parsedArguments[0],
                                            Math.abs(Integer.parseInt(parsedArguments[1])));
            } catch (IllegalArgumentException illegalArgumentException) {
                this.clientController.getView().notifyGenericError("integer expected as second argument");
            }
        }
        else {
            this.clientController.getView().notifyGenericError("required " + CommandType.CREATE_GAME.getNumArgs() + "arguments, provided " + parsedArguments.length);
        }
    }

    public void joinGame(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs, true);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("non-empty arguments expected");
            return;
        }

        if (parsedArguments.length == CommandType.JOIN_GAME.getNumArgs()) {
            clientController.joinGame(parsedArguments[0]);
        }
        else {
            this.clientController.getView().notifyGenericError("required " + CommandType.JOIN_GAME.getNumArgs() + "arguments, provided " + parsedArguments.length);
        }
    }

    public void joinFirstAvailableGame(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs, true);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("non-empty arguments expected");
            return;
        }

        if (parsedArguments.length == CommandType.PLACE_INITIAL_CARD.getNumArgs()) {
            try {
                clientController.placeInitialCard(CardOrientation.valueOf(parsedArguments[0].toUpperCase()));
            } catch (IllegalArgumentException illegalArgumentException) {
                this.clientController.getView().notifyGenericError("card orientation argument must be in " + List.of(CardOrientation.values()));
            }
        }
        else {
            this.clientController.getView().notifyGenericError("required " + CommandType.JOIN_FIRST_GAME.getNumArgs() + "arguments, provided " + parsedArguments.length);
        }
    }

    public void placeCard(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs, true);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("expected non-empty arguments");
            return;
        }

        if (parsedArguments.length == CommandType.PLACE_CARD.getNumArgs()) {
            Direction direction;
            CardOrientation cardOrientation;

            try {
                direction = Direction.valueOf(parsedArguments[2].toUpperCase());
            } catch (IllegalArgumentException illegalArgumentException) {
                this.clientController.getView().notifyGenericError("direction argument must be in " + List.of(Direction.values()));
                return;
            }

            try {
                cardOrientation = CardOrientation.valueOf(parsedArguments[3].toUpperCase());
            } catch (IllegalArgumentException illegalArgumentException) {
                this.clientController.getView().notifyGenericError("card orientation argument must be in " + List.of(CardOrientation.values()));
                return;
            }

            clientController.placeCard(parsedArguments[0], parsedArguments[1], direction, cardOrientation);
        }
        else {
            this.clientController.getView().notifyGenericError("required " + CommandType.PLACE_CARD.getNumArgs() + "arguments, provided " + parsedArguments.length);
        }
    }

    public void pickCardFromTable(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs, true);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("non-empty arguments required");
            return;
        }

        if (parsedArguments.length == CommandType.PICK_CARD_TABLE.getNumArgs()) {
            PlayableCardType cardType;
            int position;

            try {
                cardType = PlayableCardType.valueOf(parsedArguments[0].toUpperCase());
            } catch (IllegalArgumentException illegalArgumentException) {
                this.clientController.getView().notifyGenericError("card type must be in " + List.of(PlayableCardType.values()));
                return;
            }

            try {
                position = Math.abs(Integer.parseInt(parsedArguments[1]));
            } catch (IllegalArgumentException illegalArgumentException) {
                this.clientController.getView().notifyGenericError("position argument must be integer");
                return;
            }

            clientController.pickCardFromTable(cardType, position);
        }
        else {
            this.clientController.getView().notifyGenericError("required " + CommandType.PICK_CARD_TABLE.getNumArgs() + "arguments, provided " + parsedArguments.length);
        }
    }

    public void pickCardFromDeck(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs, true);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("non-empty arguments required");
            return;
        }

        if (parsedArguments.length == CommandType.PICK_CARD_DECK.getNumArgs()) {
            try {
                clientController.pickCardFromDeck(PlayableCardType.valueOf(parsedArguments[0].toUpperCase()));
            } catch (IllegalArgumentException illegalArgumentException) {
                this.clientController.getView().notifyGenericError("card type must be in " + List.of(PlayableCardType.values()));
            }
        }
        else {
            this.clientController.getView().notifyGenericError("required " + CommandType.PICK_CARD_DECK.getNumArgs() + "arguments, provided " + parsedArguments.length);
        }
    }

    public void createPlayer(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs, true);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("non-empty arguments required");
            return;
        }

        if (parsedArguments.length == CommandType.CREATE_PLAYER.getNumArgs()) {
            clientController.createPlayer(parsedArguments[0]);
        }
        else{
            this.clientController.getView().notifyGenericError("required " + CommandType.CREATE_PLAYER.getNumArgs() + "arguments, provided " + parsedArguments.length);
        }
    }

    public void sendChatMessage(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs, false);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("provided arguments are not correct");
            return;
        }

        if (parsedArguments.length >= CommandType.SEND_CHAT_MESSAGE.getNumArgs()) {
            clientController.sendChatMessage(parsedArguments[0], new ArrayList<>(List.of(parsedArguments)).subList(1, parsedArguments.length));
        }
        else {
            this.clientController.getView().notifyGenericError("required " + CommandType.SEND_CHAT_MESSAGE.getNumArgs() + "arguments, provided " + parsedArguments.length);
        }
    }

    public void placeInitialCard(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs, true);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("non-empty arguments required");
            return;
        }

        if (parsedArguments.length >= CommandType.PLACE_INITIAL_CARD.getNumArgs()) {
            try {
                clientController.placeInitialCard(CardOrientation.valueOf(parsedArguments[0].toUpperCase()));
            } catch (IllegalArgumentException illegalArgumentException) {
                this.clientController.getView().notifyGenericError("card orientation parameter must be in " + List.of(CardOrientation.values()));
            }
        }
        else {
            this.clientController.getView().notifyGenericError("required " + CommandType.PLACE_INITIAL_CARD.getNumArgs() + "arguments, provided " + parsedArguments.length);
        }
    }
}