package it.polimi.ingsw.gc19.Enums;

/**
 * This enum represents all possible colors
 */
public enum Color {

    BLUE("\u001b[38;5;4m"),
    GREEN("\u001b[38;5;2m"),
    YELLOW("\u001b[38;5;3m"),
    RED("\u001b[38;5;1m");

    private final String stringColor;

    Color(String stringColor) {
        this.stringColor = stringColor;
    }

    public String stringColor() {
        return stringColor;
    }

    public String colorReset() {
        return "\u001b[0m";
    }
}
