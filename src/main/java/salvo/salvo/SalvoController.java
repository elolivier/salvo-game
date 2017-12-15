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
        dto.put("salvos", getTurnsMap(gpOfId, gpOfId.getGame().getGames())); //MAP OF TURN:OBJECT
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
    //This function should return an object Turn:Object
    private Map<Long, Object> getTurnsMap(GamePlayer gpOfId, Set<GamePlayer> bothGps) {
        Map<Long, Object> turnObject = new LinkedHashMap<>();
        Long turns = (long) getTurnsQuantity(bothGps);
        for (Long i = 1L; i <= turns; i++) {
            turnObject.put(i, getMapPlayers(i,bothGps,gpOfId));
        }
        return turnObject;
    }

    //This function should return an object {PlayerId1:Locations1, PlayerId2:Locations2}
    private Map<Long, Object> getMapPlayers(Long turn, Set<GamePlayer> bothGps, GamePlayer gpOfId) {
        Map<Long, Object> eachPlayerPerTurn=new LinkedHashMap<>();
        eachPlayerPerTurn.put(gpOfId.getPlayer().getPlayerId(),getSalvoLocations(turn, gpOfId));
        GamePlayer opponent = getOpponent(bothGps, gpOfId.getGamePlayerId());
        if (opponent != null){
            eachPlayerPerTurn.put(opponent.getPlayer().getPlayerId(),getSalvoLocations(turn, opponent));
        }
        return eachPlayerPerTurn;
    }

    //This function return the GamePlayer of the opponent
    private GamePlayer getOpponent(Set<GamePlayer> bothGps, Long ownerId) {
        GamePlayer opponent = new GamePlayer();
        for (GamePlayer eachGp:bothGps
             ) {
            if (eachGp.getGamePlayerId() != ownerId) {
                opponent = eachGp;
            } else {
                opponent = null;
            }
        }
        return opponent;
    }

    //This function return Salvo Locations for a turn and GamePlayer give it
    private List<String> getSalvoLocations(Long turn, GamePlayer gp) {
        List<String> locations;
        List<Salvo> salvoList = new ArrayList<>(gp.getSalvos());
        Salvo thisTurnSalvo = salvoList.get((int) (turn-1));
        locations = thisTurnSalvo.getSalvo_locations();
        return locations;
    }

    //This function return the quantity of turns played in a game
    private int getTurnsQuantity(Set<GamePlayer> bothGp) {
        int turns=0;
        List<GamePlayer> list = new ArrayList<>(bothGp);
        if (list.size()>1) {
            if (list.get(0).getSalvos().size() >= list.get(1).getSalvos().size()) {
                turns = list.get(0).getSalvos().size();
            } else {
                turns = list.get(1).getSalvos().size();
            }
        } else {
            list.get(0).getSalvos().size();
        }
        return turns;
    }
}
