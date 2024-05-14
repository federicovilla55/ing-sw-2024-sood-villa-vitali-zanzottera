package it.polimi.ingsw.gc19.Controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.gc19.Costants.ImportantConstants;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;

import java.io.File;
import java.io.IOException;
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
        File playableCardsFile = new File(ImportantConstants.pathToPlayableCardFileJSON);
        return objMapper.readValue(playableCardsFile, new TypeReference<ArrayList<PlayableCard>>(){}).stream();
    }

    /**
     * This static method returns a {@code Stream<GoalCards>} representing
     * goal cards read from file
     * @return a Stream of GoalCards
     */
    public static Stream<GoalCard> readGoalCardFromFile() throws IOException{
        File goalCardsFile = new File(ImportantConstants.pathToGoalCardFileJSON);
        return objMapper.readValue(goalCardsFile, new TypeReference<ArrayList<GoalCard>>(){}).stream();
    }

}
