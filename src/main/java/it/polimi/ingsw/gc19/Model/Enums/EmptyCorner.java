package it.polimi.ingsw.gc19.Model.Enums;

import it.polimi.ingsw.gc19.Model.Card.Corner;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Optional;

@JsonTypeName("empty")
public enum EmptyCorner implements Corner{
    EMPTY;

    @Override
    public boolean hasSymbol() {
        return false;
    }

    @Override
    public Optional<Symbol> getSymbol(){
        return Optional.empty();
    }

}
