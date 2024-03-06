package it.polimi.ingsw.gc19.Model.Enums;

import it.polimi.ingsw.gc19.Model.Card.CornerValue;

public enum NotAvailableCorner implements CornerValue {
    NOT_AVAILABLE;

    @Override
    public boolean hasSymbol() {
        return false;
    }

}
