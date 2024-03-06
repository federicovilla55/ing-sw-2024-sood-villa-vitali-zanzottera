package it.polimi.ingsw.gc19.Model.Enums;

import it.polimi.ingsw.gc19.Model.Card.CornerValue;

public enum EmptyCorner implements CornerValue {
    EMPTY;

    @Override
    public boolean hasSymbol() {
        return false;
    }

}
