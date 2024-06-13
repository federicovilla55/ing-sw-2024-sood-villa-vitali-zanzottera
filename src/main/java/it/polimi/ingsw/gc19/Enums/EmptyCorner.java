package it.polimi.ingsw.gc19.Enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import it.polimi.ingsw.gc19.Model.Card.Corner;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Optional;

/**
 * This enum stands for an empty corner
 */
@JsonTypeName("empty")
public enum EmptyCorner implements Corner{
    EMPTY;

    @JsonCreator
    EmptyCorner(){

    }

    /**
     * This method returns a boolean indicating whether {@link Corner} has a {@link Symbol}
     * @return always <code>false</code> because the corner is empty
     */
    @Override
    public boolean hasSymbol() {
        return false;
    }

    /**
     * This method returns an optional containing the {@link Symbol} in the corner if exists,
     * return an empty optional
     * @return always an <code>Optional&lt;Symbol&gt;</code> empty
     */
    @Override
    public Optional<Symbol> getSymbol(){
        return Optional.empty();
    }

    /**
     * Getter for UTF-8 code of an empty corner
     * @return the UTF-8 code of an empty corner
     */
    @Override
    public String stringEmoji() {
        return "\uD83D\uDFE8";
    }

}