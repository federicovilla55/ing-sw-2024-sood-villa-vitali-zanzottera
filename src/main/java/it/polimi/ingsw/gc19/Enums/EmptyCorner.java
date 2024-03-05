package it.polimi.ingsw.gc19.Enums;

import it.polimi.ingsw.gc19.Card.CornerValue;

public enum EmptyCorner implements CornerValue {
    EMPTY;

    @Override
    public boolean hasSymbol() {
        return false;
    }

}
