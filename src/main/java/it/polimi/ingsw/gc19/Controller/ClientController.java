package it.polimi.ingsw.gc19.Controller;
import it.polimi.ingsw.gc19.Model.Player.Player;
import java.util.List;
public class ClientController {

    public boolean checkName(Player new_player, List<Player> List_Player) {

        for(Player old_player : List_Player) {
            if(old_player.getName().equals(new_player.getName())){
                return true;
            }
        }
        return false;
    }
    public boolean addClient(Player new_player, List<Player> List_Player) {
        if(checkName(new_player, List_Player)){
            return false;
        }
        List_Player.add(new_player);
        return true;
    }

    public void createGame(Player player_id){ // parameter pass also metadata of game

    }

    public void addToGame(Player player_id){ //as parameter pass also game to join and list of all games

    }
}
