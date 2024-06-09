package it.polimi.ingsw.gc19.Enums;

/**
 * This enum represents all possible colors
 */
public enum Color {

    BLUE("\u001b[38;5;4m"),
    GREEN("\u001b[38;5;2m"),
    YELLOW("\u001b[38;5;3m"),
    RED("\u001b[38;5;1m");

    /**
     * UTF-8 name of the color
     */
    private final String stringColor;

    Color(String stringColor) {
        this.stringColor = stringColor;
    }

    /**
     * Getter for UTF-8 color code
     * @return the UTF-8 color code
     */
    public String stringColor() {
        return stringColor;
    }

    /**
     * Getter for UTF-8 reset color code
     * @return the UTF-8 reset color code
     */
    public String colorReset() {
        return "\u001b[0m";
    }
}
