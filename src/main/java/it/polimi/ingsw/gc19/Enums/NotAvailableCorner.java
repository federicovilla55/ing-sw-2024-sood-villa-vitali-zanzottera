package it.polimi.ingsw.gc19.Enums;

import it.polimi.ingsw.gc19.Model.Card.Corner;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Optional;

/**
 * This enum stands for a not available corner
 */
@JsonTypeName("not_available")
public enum NotAvailableCorner implements Corner {
    NOT_AVAILABLE;

    @Override
    public boolean hasSymbol() {
        return false;
    }

    @Override
    public Optional<Symbol> getSymbol(){
        return Optional.empty();
    }

    @Override
    public String stringEmoji() {
        return "  ";
    }

}
