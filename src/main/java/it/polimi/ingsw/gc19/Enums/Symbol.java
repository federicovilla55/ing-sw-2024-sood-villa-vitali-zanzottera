package it.polimi.ingsw.gc19.Enums;

import com.fasterxml.jackson.annotation.JsonTypeName;
import it.polimi.ingsw.gc19.Model.Card.Corner;

import java.util.EnumSet;
import java.util.Optional;

/**
 * This enums represents all possible symbols inside a corner
 */
@JsonTypeName("symbol")
public enum Symbol implements Corner {
    MUSHROOM("\u001b[41;1m", "\uD83C\uDF44"),
    VEGETABLE("\u001b[42;1m", "\uD83C\uDF31"),
    ANIMAL("\u001b[46;1m", "\uD83D\uDC3A"),
    INSECT("\u001b[45;1m", "\uD83E\uDD8B"),
    INK("", String.format("%-" + inkSpacing() + "." + inkSpacing() + "s", "✒\uFE0F     ")),
    FEATHER("", "\uD83E\uDEB6"),
    SCROLL("", "\uD83D\uDCDC");

    /**
     * UTF-8 code for the color
     */
    private final String stringColor;

    /**
     * UTF-8 code for emoji of {@link Symbol}
     */
    private final String stringEmoji;

    Symbol(String stringColor, String stringEmoji) {
        this.stringColor = stringColor;
        this.stringEmoji = stringEmoji;
    }

    /**
     * This method returns a boolean indicating whether {@link Corner} has a symbol
     * @return always <code>false</code> because the corner is not available
     */
    @Override
    public boolean hasSymbol() {
        return true;
    }

    /**
     * This method returns an optional containing the symbol in the corner if exists,
     * return an empty optional
     * @return an <code>Optional&lt;Symbol&gt;</code> empty or containing a {@link Symbol}
     */
    @Override
    public Optional<Symbol> getSymbol() {
        return Optional.of(this);
    }

    /**
     * Getter for {@link #stringEmoji}
     * @return the relative {@link #stringEmoji}
     */
    @Override
    public String stringEmoji() {
        return stringEmoji;
    }

    /**
     * Getter for {@link #stringColor}
     * @return the relative {@link #stringColor}
     */
    public String stringColor() {
        return stringColor;
    }

    /**
     * Getter for ink spacing property. Different
     * properties for different OS
     * @return the number of spacing for ink symbol for each OS
     */
    private static int inkSpacing() {
        String osName = System.getProperty("os.name");

        if (osName.toLowerCase().contains("windows")) {
            return 2;
        } else {
            return 3;
        }
    }
}