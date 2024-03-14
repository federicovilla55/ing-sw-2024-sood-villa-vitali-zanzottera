package it.polimi.ingsw.gc19.Model.Enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import it.polimi.ingsw.gc19.Model.Card.Corner;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Optional;

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
