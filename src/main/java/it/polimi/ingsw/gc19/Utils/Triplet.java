package it.polimi.ingsw.gc19.Utils;

import java.io.Serializable;

/**
 * This record represents a triplet
 * @param x first parameter of the triplet
 * @param y second parameter of the triplet
 * @param z third parameter of the triplet
 * @param <R> first parameter of the triplet
 * @param <T> second parameter of the triplet
 * @param <S> third parameter of the triplet
 */
public record Triplet<R, T, S> (
        R x,
        T y,
        S z
){}