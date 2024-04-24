package it.polimi.ingsw.gc19.Utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)

/**
 * This record represents tuple of T first element and S second element
 */
public record Tuple<T, S> (
        @JsonProperty("x") T x,
        @JsonProperty("y") S y
) implements Serializable {}
