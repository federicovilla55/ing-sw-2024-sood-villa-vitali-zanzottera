package it.polimi.ingsw.gc19.Model.Card;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.polimi.ingsw.gc19.Model.Enums.EmptyCorner;
import it.polimi.ingsw.gc19.Model.Enums.NotAvailableCorner;
import it.polimi.ingsw.gc19.Model.Enums.Symbol;

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
public interface Corner {
    boolean hasSymbol();
    Optional<Symbol> getSymbol();
}
