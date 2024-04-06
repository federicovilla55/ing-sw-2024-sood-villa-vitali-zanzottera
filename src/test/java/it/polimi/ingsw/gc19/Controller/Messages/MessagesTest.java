package it.polimi.ingsw.gc19.Controller.Messages;

import it.polimi.ingsw.gc19.Controller.MainController;
import it.polimi.ingsw.gc19.Controller.JSONParser;
import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Color;
import it.polimi.ingsw.gc19.Enums.GameState;
import it.polimi.ingsw.gc19.Enums.Symbol;
import it.polimi.ingsw.gc19.Model.Card.Card;
import it.polimi.ingsw.gc19.Model.Card.GoalCard;
import it.polimi.ingsw.gc19.Model.Card.PlayableCard;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.GameConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OtherStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.OwnStationConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.TableConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.AvailableColorsMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.CreatedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.NewPlayerConnectedToGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class MessagesTest{

    private MainController mainController;
    private Map<String, PlayableCard> playableCards;
    private Map<String, GoalCard> goalCards;
    private ClientStub player1, player2, player3, player4;

    @BeforeEach
    public void setUp(){
        this.mainController = MainController.getMainServer();

        try {
            this.playableCards = JSONParser.readPlayableCardFromFile().collect(Collectors.toMap(Card::getCardCode, p -> p));
            this.goalCards = JSONParser.readGoalCardFromFile().collect(Collectors.toMap(Card::getCardCode, p -> p));
        }
        catch(IOException e){
            e.printStackTrace();
        }

        this.player1 = new ClientStub("player1");
        this.player2 = new ClientStub("player2");
        this.player3 = new ClientStub("player3");
        this.player4 = new ClientStub("player4");
    }

    @Test
    public void setUpGameTest(){
        this.mainController.createClient(player1, this.player1.getName());
        assertEquals(new CreatedPlayerMessage("player1"), this.player1.getMessage());

        this.mainController.createClient(player2, this.player2.getName());
        assertEquals(new CreatedPlayerMessage("player2"), this.player2.getMessage());
        //No new messages has been sent to player1
        assertNull(player1.getMessage());

        this.mainController.createClient(player3, this.player3.getName());
        assertEquals(new CreatedPlayerMessage("player3"), this.player3.getMessage());

        this.mainController.createClient(player4, this.player4.getName());
        assertEquals(new CreatedPlayerMessage("player4"), this.player4.getMessage());

        this.mainController.createGame("game1", 4, this.player1, 1);
        assertEquals(new CreatedGameMessage("game1"), player1.getMessage());
        assertEquals(new TableConfigurationMessage(
                playableCards.get("resource_05").setCardState(CardOrientation.UP),
                playableCards.get("resource_21").setCardState(CardOrientation.UP),
                playableCards.get("gold_19").setCardState(CardOrientation.UP),
                playableCards.get("gold_23").setCardState(CardOrientation.UP),
                goalCards.get("goal_11"),
                goalCards.get("goal_15"),
                Symbol.VEGETABLE,
                Symbol.ANIMAL
        ).setHeader("player1"), player1.getMessage());
        assertEquals(new OwnStationConfigurationMessage(
                "player1",
                null,
                List.of(
                        playableCards.get("resource_23"),
                        playableCards.get("resource_01"),
                        playableCards.get("gold_28")
                ),
                Map.of(
                        Symbol.ANIMAL, 0,
                        Symbol.MUSHROOM, 0,
                        Symbol.VEGETABLE, 0,
                        Symbol.INSECT, 0,
                        Symbol.INK, 0,
                        Symbol.FEATHER, 0,
                        Symbol.SCROLL, 0
                ),
                null,
                0,
                playableCards.get("initial_05"),
                goalCards.get("goal_09"),
                goalCards.get("goal_14"),
                List.of()
        ).setHeader("player1"), player1.getMessage());
        assertEquals(
                new AvailableColorsMessage(List.of(Color.values())).setHeader("player1"), player1.getMessage()
        );
        assertEquals(
                new GameConfigurationMessage(
                        GameState.SETUP,
                        null,
                        null,
                        null,
                        false,
                        4
                ).setHeader("player1"), player1.getMessage()
        );

        //No new messages has been sent to player2
        assertNull(player2.getMessage());

        this.mainController.registerToGame(player2, "game1");
        assertEquals(new NewPlayerConnectedToGameMessage("player2").setHeader("player1"), player1.getMessage());
        assertEquals(new OtherStationConfigurationMessage(
                "player2",
                null,
                Map.of(
                        Symbol.ANIMAL, 0,
                        Symbol.MUSHROOM, 0,
                        Symbol.VEGETABLE, 0,
                        Symbol.INSECT, 0,
                        Symbol.INK, 0,
                        Symbol.FEATHER, 0,
                        Symbol.SCROLL, 0
                ),
                0,
                List.of()
        ).setHeader("player1"), player1.getMessage());
        MessageToClient tableConfigurationMessage = new TableConfigurationMessage(
                playableCards.get("resource_05").setCardState(CardOrientation.UP),
                playableCards.get("resource_21").setCardState(CardOrientation.UP),
                playableCards.get("gold_19").setCardState(CardOrientation.UP),
                playableCards.get("gold_23").setCardState(CardOrientation.UP),
                goalCards.get("goal_11"),
                goalCards.get("goal_15"),
                Symbol.ANIMAL,
                Symbol.VEGETABLE
        );
        assertEquals(tableConfigurationMessage.setHeader("player1"), player1.getMessage());

        assertEquals(tableConfigurationMessage.setHeader("player2"), player2.getMessage());
        assertEquals(new OwnStationConfigurationMessage(
                "player2",
                null,
                List.of(
                        playableCards.get("resource_15"),
                        playableCards.get("resource_37"),
                        playableCards.get("gold_21")
                ),
                Map.of(
                        Symbol.ANIMAL, 0,
                        Symbol.MUSHROOM, 0,
                        Symbol.VEGETABLE, 0,
                        Symbol.INSECT, 0,
                        Symbol.INK, 0,
                        Symbol.FEATHER, 0,
                        Symbol.SCROLL, 0
                ),
                null,
                0,
                playableCards.get("initial_01"),
                goalCards.get("goal_16"),
                goalCards.get("goal_01"),
                List.of()
        ).setHeader("player2"), player2.getMessage());
        assertEquals(new OtherStationConfigurationMessage(
                "player1",
                null,
                Map.of(
                        Symbol.ANIMAL, 0,
                        Symbol.MUSHROOM, 0,
                        Symbol.VEGETABLE, 0,
                        Symbol.INSECT, 0,
                        Symbol.INK, 0,
                        Symbol.FEATHER, 0,
                        Symbol.SCROLL, 0
                ),
                0,
                List.of()
        ).setHeader("player2"), player2.getMessage());
        assertEquals(
                new AvailableColorsMessage(List.of(Color.values())).setHeader("player2"), player2.getMessage()
        );
        assertEquals(
                new GameConfigurationMessage(
                        GameState.SETUP,
                        null,
                        null,
                        null,
                        false,
                        4
                ).setHeader("player2"), player2.getMessage()
        );

        this.mainController.registerToGame(player3, "game1");
        tableConfigurationMessage = new TableConfigurationMessage(
            playableCards.get("resource_05").setCardState(CardOrientation.UP),
            playableCards.get("resource_21").setCardState(CardOrientation.UP),
            playableCards.get("gold_19").setCardState(CardOrientation.UP),
            playableCards.get("gold_23").setCardState(CardOrientation.UP),
            goalCards.get("goal_11"),
            goalCards.get("goal_15"),
            Symbol.VEGETABLE,
            Symbol.MUSHROOM
        );
        List<ClientStub> receivers = List.of(player1,player2);
        List<String> receiversName = receivers.stream().map(ClientStub::getName).collect(Collectors.toList());
        for(ClientStub receiver : receivers) {
            assertEquals(new NewPlayerConnectedToGameMessage("player3").setHeader(receiversName), receiver.getMessage());
            assertEquals(new OtherStationConfigurationMessage(
                    "player3",
                    null,
                    Map.of(
                            Symbol.ANIMAL, 0,
                            Symbol.MUSHROOM, 0,
                            Symbol.VEGETABLE, 0,
                            Symbol.INSECT, 0,
                            Symbol.INK, 0,
                            Symbol.FEATHER, 0,
                            Symbol.SCROLL, 0
                    ),
                    0,
                    List.of()
            ).setHeader(receiversName), receiver.getMessage());
            assertEquals(tableConfigurationMessage.setHeader(receiversName), receiver.getMessage());
        }

        assertEquals(tableConfigurationMessage.setHeader("player3"), player3.getMessage());
        assertEquals(new OwnStationConfigurationMessage(
                "player3",
                null,
                List.of(
                        playableCards.get("resource_24"),
                        playableCards.get("resource_09"),
                        playableCards.get("gold_14")
                ),
                Map.of(
                        Symbol.ANIMAL, 0,
                        Symbol.MUSHROOM, 0,
                        Symbol.VEGETABLE, 0,
                        Symbol.INSECT, 0,
                        Symbol.INK, 0,
                        Symbol.FEATHER, 0,
                        Symbol.SCROLL, 0
                ),
                null,
                0,
                playableCards.get("initial_06"),
                goalCards.get("goal_07"),
                goalCards.get("goal_06"),
                List.of()
        ).setHeader("player3"), player3.getMessage());
        for(String nickname : List.of("player1","player2")) {
            assertEquals(new OtherStationConfigurationMessage(
                    nickname,
                    null,
                    Map.of(
                            Symbol.ANIMAL, 0,
                            Symbol.MUSHROOM, 0,
                            Symbol.VEGETABLE, 0,
                            Symbol.INSECT, 0,
                            Symbol.INK, 0,
                            Symbol.FEATHER, 0,
                            Symbol.SCROLL, 0
                    ),
                    0,
                    List.of()
            ).setHeader("player3"), player3.getMessage());
        }
        assertEquals(
                new AvailableColorsMessage(List.of(Color.values())).setHeader("player3"), player3.getMessage()
        );
        assertEquals(
                new GameConfigurationMessage(
                        GameState.SETUP,
                        null,
                        null,
                        null,
                        false,
                        4
                ).setHeader("player3"), player3.getMessage()
        );

        this.mainController.registerToGame(player4, "game1");
        for(ClientStub player : List.of(player1,player2,player3,player4)) {
            player.clearQueue();
        }
    }
}
