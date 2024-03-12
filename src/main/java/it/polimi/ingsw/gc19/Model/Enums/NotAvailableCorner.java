package it.polimi.ingsw.gc19.Model.Enums;

import it.polimi.ingsw.gc19.Model.Card.Corner;

import java.util.Optional;

public enum NotAvailableCorner implements Corner {
    NOT_AVAILABLE;

    @Override
    public boolean hasSymbol() {
        return false;
    }

    public Optional<Symbol> getSymbol(){
        return Optional.empty();
    }
}
