package it.polimi.ingsw.gc19.Controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.gc19.Costants.ImportantConstants;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

public final class JSONParser{
    private static final ObjectMapper objMapper = new ObjectMapper();

    public static Stream<PlayableCard> readPlayableCardFromFile() throws IOException{
        File playableCardsFile = new File(ImportantConstants.pathToPlayableCardFileJSON);
        return objMapper.readValue(playableCardsFile, new TypeReference<Stream<PlayableCard>>(){});
    }

    public static Stream<GoalCard> readGoalCardFromFile() throws IOException{
        File playableCardsFile = new File(ImportantConstants.pathToPlayableCardFileJSON);
        return objMapper.readValue(playableCardsFile, new TypeReference<Stream<GoalCard>>(){});
    }

}
