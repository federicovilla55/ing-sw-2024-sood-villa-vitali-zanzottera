package it.polimi.ingsw.gc19.Utils;

import java.io.Serializable;

/**
 * This record represent a triplet
 */
public record Triplet<R, T, S> (
        R x,
        T y,
        S z
){}