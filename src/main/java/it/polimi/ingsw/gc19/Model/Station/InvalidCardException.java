package it.polimi.ingsw.gc19.Model.Station;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;

/**
 * This exception is thrown when an invalid card code
 * (or invalid {@link PlayableCard}) is requested to be used
 * in "place card" commands
 */
public class InvalidCardException extends Exception{
}
