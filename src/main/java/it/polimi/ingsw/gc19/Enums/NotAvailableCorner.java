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

    /**
     * This method returns a boolean indicating whether {@link Corner} has a symbol
     * @return always <code>false</code> because the corner is not available
     */
    @Override
    public boolean hasSymbol() {
        return false;
    }

    /**
     * This method returns an optional containing the symbol in the corner if exists,
     * return an empty optional
     * @return always an <code>Optional&lt;Symbol&gt;</code> empty
     */
    @Override
    public Optional<Symbol> getSymbol(){
        return Optional.empty();
    }

    /**
     * Getter for UTF-8 code of an empty corner
     * @return an empty string
     */
    @Override
    public String stringEmoji() {
        return "  ";
    }

}
