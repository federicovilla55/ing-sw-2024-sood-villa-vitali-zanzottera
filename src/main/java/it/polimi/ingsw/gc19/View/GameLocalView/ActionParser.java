package it.polimi.ingsw.gc19.View.GameLocalView;

import it.polimi.ingsw.gc19.Enums.GameState;
import it.polimi.ingsw.gc19.Enums.TurnState;
import it.polimi.ingsw.gc19.Networking.Client.Message.Action.PickCardFromDeckMessage;
import it.polimi.ingsw.gc19.Networking.Client.Message.Action.PlaceCardMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.AcceptedAnswer.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.Action.RefusedAction.RefusedActionMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.Configuration.GameConfigurationMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameEvents.*;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.CreatedPlayerMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.DisconnectGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.GameHandling.JoinedGameMessage;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.Networking.Server.Message.Turn.TurnStateMessage;

public class ActionParser {
    private String nickname;

    private ClientState viewState;

    private ClientState prevState;

    public ActionParser(String nickname){
        this.nickname = nickname;
        viewState = new NotPlayer();
        prevState = new NotPlayer();
    }

    public String getNickname() {
        return nickname;
    }

    class NotPlayer extends ClientState{

        @Override
        public ViewState getState() {
            return ViewState.NOTPLAYER;
        }

        @Override
        public void parseAction(String action) {
            // permit only create player
        }
    }

    class NotGame extends ClientState{

        @Override
        public ViewState getState() {
            return ViewState.NOTGAME;
        }

        @Override
        public void parseAction(String action) {
            // permit only joingame or creategame, availablegames
        }
    }

    class Setup extends ClientState{

        @Override
        public void nextState(StartPlayingGameMessage message) {
            if(message.getNickFirstPlayer().equals(getNickname())){
                viewState = new Place();
            }else{
                viewState = new OtherTurn();
            }
        }

        @Override
        public ViewState getState() {
            return ViewState.SETUP;
        }

        @Override
        public void parseAction(String action) {
            // permit only chat, select color, select goalcard and place initial
        }
    }

    class Wait extends ClientState{
        @Override
        public void nextState(CreatedPlayerMessage message) {
            viewState = new NotGame();
        }

        @Override
        public void nextState(AcceptedPlaceCardMessage message) {
            viewState = new Pick();
        }

        @Override
        public void nextState(JoinedGameMessage message) {
            viewState = new Setup();
        }

        @Override
        public void nextState(CreatedGameMessage message) {
            viewState = new Setup();
        }

        @Override
        public void nextState(AcceptedPickCardFromTable message) {
            viewState = new OtherTurn();
        }

        @Override
        public ViewState getState() {
            return ViewState.WAIT;
        }

        @Override
        public void parseAction(String action) {
            // permit only send chat messages
        }
    }


    class Place extends ClientState{
        // no nextState because after a single place
        // is done in "parseAction" the attribute
        // viewState should be updated

        @Override
        public ViewState getState() {
            return ViewState.PLACE;
        }

        @Override
        public void parseAction(String action) {
            // permit only to place and send chat messages
        }
    }

    class Pick extends ClientState{
        // no nextState because after a single pick
        // is done in "parseAction" the attribute
        // viewState should be updated

        @Override
        public ViewState getState() {
            return ViewState.PICK;
        }

        @Override
        public void parseAction(String action) {
            // permit only to pick card and to send chat messages
        }
    }

    class OtherTurn extends ClientState {

        public void nextState(TurnStateMessage message) {
            if(message.getNick().equals(nickname)){
                viewState = new Place();
            }
        }

        @Override
        public ViewState getState() {
            return ViewState.OTHERTURN;
        }

        @Override
        public void parseAction(String action) {
            // permit only chat messages
        }
    }

    class Disconnect extends ClientState{

        @Override
        public void nextState(MessageToClient message) {
            // Create a thread to get into this state
        }

        @Override
        public ViewState getState() {
            return ViewState.DISCONNECT;
        }

        @Override
        public void parseAction(String action) {
            // before every action there must be a reconnect
        }
    }

    class Pause extends ClientState{

        @Override
        public void nextState(GameResumedMessage message) {

        }

        @Override
        public ViewState getState() {
            return ViewState.PAUSE;
        }

        @Override
        public void parseAction(String action) {
            // permit only send_message_to_chat
        }
    }

    class End extends ClientState{

        @Override
        public void nextState(CreatedPlayerMessage message) {
            viewState = new NotGame();
        }

        @Override
        public ViewState getState() {
            return ViewState.END;
        }

        @Override
        public void parseAction(String action) {
            // just permit the create_player and send_message_to_chat.
        }
    }

}


