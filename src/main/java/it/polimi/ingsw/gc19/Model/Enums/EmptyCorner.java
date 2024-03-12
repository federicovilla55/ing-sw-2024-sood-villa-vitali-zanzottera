package it.polimi.ingsw.gc19.Model.Enums;

import it.polimi.ingsw.gc19.Model.Card.Corner;

import java.util.Optional;

public enum EmptyCorner implements Corner {
    EMPTY;

    @Override
    public boolean hasSymbol() {
        return false;
    }

    public Optional<Symbol> getSymbol(){
        return Optional.empty();
    }

}
