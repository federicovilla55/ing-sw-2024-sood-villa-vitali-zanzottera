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
    MUSHROOM ("\u001b[41;1m", "\uD83C\uDF44"),
    VEGETABLE ("\u001b[42;1m", "\uD83C\uDF31"),
    ANIMAL ("\u001b[46;1m", "\uD83D\uDC3A"),
    INSECT ("\u001b[45;1m", "\uD83E\uDD8B"),
    INK ("", "✒\uFE0F "),
    FEATHER ("", "\uD83E\uDEB6"),
    SCROLL ("", "\uD83D\uDCDC");

    private final String stringColor;
    private final String stringEmoji;

    Symbol(String stringColor, String stringEmoji) {
        this.stringColor = stringColor;
        this.stringEmoji = stringEmoji;
    }

    @Override
    public boolean hasSymbol() {
        return true;
    }

    @Override
    public Optional<Symbol> getSymbol() {
        return Optional.of(this);
    }

    @Override
    public String stringEmoji() {
        return stringEmoji;
    }

    public String stringColor() {
        return stringColor;
    }

}
