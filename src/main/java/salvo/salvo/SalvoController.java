package salvo.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    GameRepository repoGame;

    @RequestMapping("/games")
    public List<Object> getApiGames() {
        return repoGame
                .findAll()
                .stream()
                .map(game -> gameDTO(game))
                .collect(Collectors.toList());
    }

    private Map<String, Object> gameDTO(Game game) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("game_id", game.getGameId());
        dto.put("created", game.getGameDate());
        dto.put("gamePlayers", game.getGames()
                        .stream()
                        .map(game_player -> gamePlayerDTO(game_player))
                        .collect(Collectors.toList()));
        return dto;
    }

    private Map<String, Object> gamePlayerDTO(GamePlayer game_player) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("game_player_id", game_player.getGamePlayerId());
        dto.put("player", game_player.getPlayer());
        return dto;
    }
//-------------------TASK 3--------------------
    @Autowired
    GamePlayerRepository repoGamePlayer;

    @RequestMapping("/game_view/{game_player_id}")
    public Map<String, Object> getGameInfo(@PathVariable long game_player_id) {
        GamePlayer gpOfId = getGpOfId(game_player_id);
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("game_id", gpOfId.getGame().getGameId());
        dto.put("created", gpOfId.getGame().getGameDate());
        dto.put("gamePlayers", gpOfId.getGame().getGames()
                .stream()
                .map(game_player -> gamePlayerDTO(game_player))
                .collect(Collectors.toList()));
        dto.put("ships", gpOfId.getShips()
                .stream()
                .map(ship -> shipsDTO(ship))
                .collect(Collectors.toList()));
        dto.put("salvos", gpOfId.getSalvos()
                .stream()
                .map(salvo -> salvosDTO(salvo))
                .collect(Collectors.toList()));
        return dto;
    }

    private GamePlayer getGpOfId(long game_player_id) {
        return repoGamePlayer
                .findOne(game_player_id);
    }

    private Map<String, Object> shipsDTO(Ship ship) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("type", ship.getShipType());
        dto.put("locations", ship.getLocations());
        return dto;
    }

    //-------------------TASK 4--------------------

    private Map<String, Object> salvosDTO(Salvo salvo) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("turn", salvo.getTurn());
        dto.put("locations", salvo.getSalvo_locations());
        return dto;
    }
}
