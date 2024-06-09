package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.gc19.Enums.EmptyCorner;
import it.polimi.ingsw.gc19.Enums.NotAvailableCorner;
import it.polimi.ingsw.gc19.Enums.Symbol;

import java.io.Serializable;
import java.util.Optional;

/**
 * This interface represents the corner of a PlayableCard
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.WRAPPER_ARRAY
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Symbol.class, name = "symbol"),
        @JsonSubTypes.Type(value = NotAvailableCorner.class, name = "not_available"),
        @JsonSubTypes.Type(value = EmptyCorner.class, name = "empty")
})
public interface Corner extends Serializable{

    /**
     * This method returns a boolean indicating whether Corner has a symbol
     * @return <code>true</code> if {@link Corner} contains a symbol
     */
    boolean hasSymbol();

    /**
     * This method returns an <code>Optional&lt;Symbol&gt;</code> containing the symbol in the corner if exists,
     * return an empty <code>Optional&lt;Symbol&gt;</code>
     * @return symbol in the corner
     */
    Optional<Symbol> getSymbol();

    /**
     * Getter for UTF-8 emoji of the {@link Symbol} contained in {@link Corner}
     * @return the UTF-8 code of the emoji describing the {@link Symbol} contained in {@link Corner}
     */
    String stringEmoji();
}