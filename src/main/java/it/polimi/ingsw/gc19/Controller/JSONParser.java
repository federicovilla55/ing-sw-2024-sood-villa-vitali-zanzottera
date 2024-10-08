package it.polimi.ingsw.gc19.Controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.gc19.Costants.ImportantConstants;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * This class is used for JSON file parsing
 */
public final class JSONParser{
    private static final ObjectMapper objMapper = new ObjectMapper();

    /**
     * This static method returns a {@code Stream<PlayableCard>} representing
     * playable cards read from file
     * @return a Stream of PlayableCards
     */
    public static Stream<PlayableCard> readPlayableCardFromFile() throws IOException{
        InputStream playableCardsFile = JSONParser.class.getClassLoader().getResourceAsStream(ImportantConstants.PATH_TO_PLAYABLE_CARD_FILE_JSON);
        return objMapper.readValue(playableCardsFile, new TypeReference<ArrayList<PlayableCard>>(){}).stream();
    }

    /**
     * This static method returns a {@code Stream<GoalCards>} representing
     * goal cards read from file
     * @return a Stream of GoalCards
     */
    public static Stream<GoalCard> readGoalCardFromFile() throws IOException{
        InputStream goalCardsFile = JSONParser.class.getClassLoader().getResourceAsStream(ImportantConstants.PATH_TO_GOAL_CARD_FILE_JSON);
        return objMapper.readValue(goalCardsFile, new TypeReference<ArrayList<GoalCard>>(){}).stream();
    }

}