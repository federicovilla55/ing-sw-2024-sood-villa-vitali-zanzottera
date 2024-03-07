package it.polimi.ingsw.gc19.Model.Enums;

import it.polimi.ingsw.gc19.Model.Card.CornerValue;

import java.util.EnumSet;

public enum Symbol implements CornerValue {
    ANIMAL, VEGETABLE, INSECT, MUSHROOM, INK, FEATHER, SCROLL;

    public static EnumSet<Symbol> getResources(){
        return EnumSet.of(ANIMAL, VEGETABLE, INSECT, MUSHROOM);
    }

    public static EnumSet<Symbol> getObjects(){
        return EnumSet.of(INK, SCROLL, FEATHER);
    }

    @Override
    public boolean hasSymbol() {
        return true;
    }

}
