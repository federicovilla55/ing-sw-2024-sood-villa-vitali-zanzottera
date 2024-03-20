package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.gc19.Enums.EmptyCorner;
import it.polimi.ingsw.gc19.Enums.NotAvailableCorner;
import it.polimi.ingsw.gc19.Enums.Symbol;

import java.util.Optional;
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.WRAPPER_ARRAY
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Symbol.class, name = "symbol"),
        @JsonSubTypes.Type(value = NotAvailableCorner.class, name = "not_available"),
        @JsonSubTypes.Type(value = EmptyCorner.class, name = "empty")
})

/**
 * This interface represents the corner of a PlayableCard
 */
public interface Corner{
    /**
     * This method returns a boolean indicating whether Corner has a symbol
     */
    boolean hasSymbol();

    /**
     * This method returns an optional containing the symbol in the corner if exists,
     * return an empty optional
     * @return symbol in the corner
     */
    Optional<Symbol> getSymbol();
}
