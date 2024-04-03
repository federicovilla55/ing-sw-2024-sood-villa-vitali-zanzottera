package it.polimi.ingsw.gc19.Controller;

import it.polimi.ingsw.gc19.Enums.CardOrientation;
import it.polimi.ingsw.gc19.Enums.Direction;
import it.polimi.ingsw.gc19.Enums.PlayableCardType;
import it.polimi.ingsw.gc19.Model.Game.Game;
import it.polimi.ingsw.gc19.Networking.Server.Message.MessageToClient;
import it.polimi.ingsw.gc19.ObserverPattern.Observer;
import java.io.IOException;
import java.util.*;

public class Controller {
    private final CurrentGameStructure gameStructure;
    private final Object PlayerLock;

    public Controller() {
        gameStructure = new CurrentGameStructure();
        this.PlayerLock = new Object();
    }

    private boolean checkAlreadyExist(String gameToCheck){
        return gameStructure.checkGameAlreadyExist(gameToCheck);
    }

    public boolean clientNameAlreadyTaken(String userName){
        return this.gameStructure.userNameAlreadyTaken(userName);
    }

    public synchronized boolean createClient(String playerNickname, Observer<MessageToClient> client){
        if(!clientNameAlreadyTaken(playerNickname)){
            this.gameStructure.registerPlayer(playerNickname, client);
            this.gameStructure.setActivePlayer(playerNickname);
            return true;
        }
        else{
            return false;
        }

    }

    public synchronized void createGame(String gameName, int numPlayer) throws IllegalArgumentException {
        Game gameToBuild = null;

        if(checkAlreadyExist(gameName)){
            throw new IllegalArgumentException("Name already in use");
        }

        try {
            gameToBuild = new Game(numPlayer);
            //SendGameToAll(gameName);
        }
        catch(IOException exception){
            System.err.println("Occurred IOException while trying to build game " + gameName);
            //@TODO: implement the logic to handle this exception
        }

        GameController temp = new GameController(gameToBuild);
        gameStructure.insertGame(gameName, gameToBuild);
        gameStructure.registerGameController(gameName, temp);
    }

    public void registerToGame(String playerName, String gameName){
        //@TODO: check if playerName exists
        this.gameStructure.getGameControllerForGame(gameName).addClient(playerName, this.gameStructure.getObserverOfPlayer(playerName));
        this.gameStructure.insertGameControllerForPlayer(playerName, this.gameStructure.getGameControllerForGame(gameName));
    }

    public void joinGame(String player, String gameName, Observer<MessageToClient> Client) {
        Game gameToJoin = gameStructure.getGameFromName(gameName);
        GameController gameController = gameStructure.getGameControllerForGame(gameName);
        gameController.addClient(player, Client);
    }

    public void makeMove(String nickName, String cardToInsert, String anchorCard, Direction directionToInsert) {
        GameController temp = gameStructure.getGameControllerFromPlayer(nickName);
        temp.placeCard(nickName, cardToInsert, anchorCard, directionToInsert, CardOrientation.UP);
    }

    public void setInitialCard(String nickName, CardOrientation cardOrientation){
        GameController temp = gameStructure.getGameControllerFromPlayer(nickName);
        temp.placeInitialCard(nickName, cardOrientation);
    }

    /*
     * Check if Player Exists, Check if there is game associated, if
     * Yes, send the state of the Game, if no, send list of nonActive games that it can join.
     * */
    public void Recconect() {

    }

    public void SendChatMessage(String nickName, ArrayList<String> usersToSend, String messageToSend){
        GameController temp = gameStructure.getGameControllerFromPlayer(nickName);
        temp.sendChatMessage(usersToSend, nickName, messageToSend);
    }

}
