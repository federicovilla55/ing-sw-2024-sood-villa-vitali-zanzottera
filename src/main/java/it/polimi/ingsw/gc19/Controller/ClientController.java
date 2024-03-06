package it.polimi.ingsw.gc19.Controller;
import it.polimi.ingsw.gc19.Model.Player.Player;
import java.util.List;
public class ClientController {

    public boolean CheckName(Player new_player, List<Player> List_Player) {

        for(Player old_player : List_Player) {
            if(old_player.getName().equals(new_player.getName())){
                return true;
            }
        }
        return false;
    }
    public void AddClient(Player new_player, List<Player> List_Player) throws Exception{
        if(CheckName(new_player, List_Player)) {
            throw new Exception("Username already present");
        }
        List_Player.add(new_player);
    }

    public void CreateGame(Player player_id){ // parameter pass also metadata of game

    }

    public void AddToGame(Player player_id){ //as parameter pass also game to join and list of all games

    }
}
