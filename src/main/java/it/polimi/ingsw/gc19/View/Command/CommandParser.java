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

    private static String[] parseArguments(String args) throws IllegalArgumentException {
        if (args == null) throw new IllegalArgumentException();

        args = args.replaceAll("\\s", "");

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
            parsedArguments = parseArguments(commandArgs);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("Command format not known!");
            return;
        }

        if (parsedArguments.length == CommandType.CHOOSE_COLOR.getNumArgs()) {
            try {
                clientController.chooseColor(Color.valueOf(parsedArguments[0]));
            } catch (IllegalArgumentException illegalArgumentException) {
                this.clientController.getView().notifyGenericError("Command format not known!");
            }
        }
        else {
            this.clientController.getView().notifyGenericError("Command format not known!");
        }
    }

    public void chooseGoal(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("Command format not known!");
            return;
        }

        if (parsedArguments.length == CommandType.CHOOSE_PRIVATE_GOAL_CARD.getNumArgs()) {
            try {
                clientController.chooseGoal(Integer.parseInt(parsedArguments[0]));
            } catch (IllegalArgumentException illegalArgumentException) {
                this.clientController.getView().notifyGenericError("Command format not known!");
            }
        }
        else {
            this.clientController.getView().notifyGenericError("Command format not known!");
        }
    }

    public void createGame(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("Command format not known!");
            return;
        }

        if (parsedArguments.length == CommandType.CREATE_GAME.getNumArgs()) {
            try {
                clientController.createGame(parsedArguments[0],
                                            Integer.parseInt(parsedArguments[1]));
            } catch (IllegalArgumentException illegalArgumentException) {
                this.clientController.getView().notifyGenericError("Command format not known!");
            }
        }
        else {
            this.clientController.getView().notifyGenericError("Command format not known!");
        }
    }

    public void joinGame(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("Command format not known!");
            return;
        }

        if (parsedArguments.length == CommandType.JOIN_GAME.getNumArgs()) {
            clientController.joinGame(parsedArguments[0]);
        }
        else {
            this.clientController.getView().notifyGenericError("Command format not known!");
        }
    }

    public void joinFirstAvailableGame(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("Command format not known!");
            return;
        }

        if (parsedArguments.length == CommandType.PLACE_INITIAL_CARD.getNumArgs()) {
            try {
                clientController.placeInitialCard(CardOrientation.valueOf(parsedArguments[0]));
            } catch (IllegalArgumentException illegalArgumentException) {
                this.clientController.getView().notifyGenericError("Command format not known!");
            }
        }
        else {
            this.clientController.getView().notifyGenericError("Command format not known!");
        }
    }

    public void placeCard(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("Command format not known!");
            return;
        }

        if (parsedArguments.length == CommandType.PLACE_CARD.getNumArgs()) {
            Direction direction;
            CardOrientation cardOrientation;

            try {
                direction = Direction.valueOf(parsedArguments[2]);
            } catch (IllegalArgumentException illegalArgumentException) {
                this.clientController.getView().notifyGenericError("Command format not known!");
                return;
            }

            try {
                cardOrientation = CardOrientation.valueOf(parsedArguments[3]);
            } catch (IllegalArgumentException illegalArgumentException) {
                this.clientController.getView().notifyGenericError("Command format not known!");
                return;
            }

            clientController.placeCard(parsedArguments[0], parsedArguments[1], direction, cardOrientation);
        }
        else {
            this.clientController.getView().notifyGenericError("Command format not known!");
        }
    }

    public void pickCardFromTable(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("Command format not known!");
            return;
        }

        if (parsedArguments.length == CommandType.PICK_CARD_TABLE.getNumArgs()) {
            PlayableCardType cardType;
            int position;

            try {
                cardType = PlayableCardType.valueOf(parsedArguments[0]);
            } catch (IllegalArgumentException illegalArgumentException) {
                this.clientController.getView().notifyGenericError("Command format not known!");
                return;
            }

            try {
                position = Integer.parseInt(parsedArguments[1]);
            } catch (IllegalArgumentException illegalArgumentException) {
                this.clientController.getView().notifyGenericError("Command format not known!");
                return;
            }

            clientController.pickCardFromTable(cardType, position);
        }
        else {
            this.clientController.getView().notifyGenericError("Command format not known!");
        }
    }

    public void pickCardFromDeck(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("Command format not known!");
            return;
        }

        if (parsedArguments.length == CommandType.PICK_CARD_DECK.getNumArgs()) {
            try {
                clientController.pickCardFromDeck(PlayableCardType.valueOf(parsedArguments[0]));
            } catch (IllegalArgumentException illegalArgumentException) {
                this.clientController.getView().notifyGenericError("Command format not known!");
            }
        }
        else {
            this.clientController.getView().notifyGenericError("Command format not known!");
        }
    }

    public void createPlayer(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("Command format not known!");
            return;
        }

        if (parsedArguments.length == CommandType.CREATE_PLAYER.getNumArgs()) {
            clientController.createPlayer(parsedArguments[0]);
        }
    }

    public void sendChatMessage(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("Command format not known!");
            return;
        }

        if (parsedArguments.length >= CommandType.SEND_CHAT_MESSAGE.getNumArgs()) {
            clientController.sendChatMessage(parsedArguments[0], new ArrayList<>(List.of(parsedArguments)).subList(1, parsedArguments.length));
        }
        else {
            this.clientController.getView().notifyGenericError("Command format not known!");
        }
    }

    public void placeInitialCard(String commandArgs) {
        String[] parsedArguments;

        try {
            parsedArguments = parseArguments(commandArgs);
        } catch (IllegalArgumentException illegalArgumentException) {
            this.clientController.getView().notifyGenericError("Command format not known!");
            return;
        }

        if (parsedArguments.length >= CommandType.PLACE_INITIAL_CARD.getNumArgs()) {
            try {
                clientController.placeInitialCard(CardOrientation.valueOf(parsedArguments[0]));
            } catch (IllegalArgumentException illegalArgumentException) {
                this.clientController.getView().notifyGenericError("Command format not known!");
            }
        }
        else {
            this.clientController.getView().notifyGenericError("Command format not known!");
        }
    }

}