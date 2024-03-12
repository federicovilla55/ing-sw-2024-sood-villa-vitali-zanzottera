package it.polimi.ingsw.gc19.Model.Card;

import it.polimi.ingsw.gc19.Model.Enums.Symbol;

import java.util.Optional;

public interface Corner {
    public boolean hasSymbol();
    public Optional<Symbol> getSymbol();
}
