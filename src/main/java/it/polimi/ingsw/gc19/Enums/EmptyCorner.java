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
    private EmptyCorner(){

    }

    @Override
    public boolean hasSymbol() {
        return false;
    }

    @Override
    public Optional<Symbol> getSymbol(){
        return Optional.empty();
    }

}
