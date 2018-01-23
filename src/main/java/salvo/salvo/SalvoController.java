package salvo.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SalvoController {

    @Autowired
    GameRepository repoGame;

    @RequestMapping("/games")
    public Map<String, Object> getApiGames2() {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("player",getPlayerLoggedIn());
        dto.put("games",getApiGames());
        return dto;
    }

    private Map<String, Object> getPlayerLoggedIn() {
        Map<String, Object> playerInfo = new LinkedHashMap<>();
        String playerName = getLoggedUserName();
        Player playerLogged;
        try {
            playerLogged = repoPlayer
                    .findAll()
                    .stream()
                    .parallel()
                    .filter(player -> player.getUserName() == playerName)
                    .findAny()
                    .get();
        } catch (Exception NoSuchElementException) {
            playerLogged = null;
        }
        Long idLogged;
        if(playerLogged != null) {
            idLogged = playerLogged.getPlayerId();
            playerInfo.put("id", idLogged);
            playerInfo.put("name", playerName);
        } else {
            playerInfo = null;
        }

        return playerInfo;
    }

    private String getLoggedUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();
            return currentUserName;
        }
        return null;
    }

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
        dto.put("score", game_player.getScore());
        return dto;
    }
//-------------------TASK 3--------------------
    @Autowired
    GamePlayerRepository repoGamePlayer;

    @RequestMapping("/game_view/{game_player_id}")
    public ResponseEntity<Map<String, Object>> secureGameInfo(@PathVariable long game_player_id) {
        String loggedId = getLoggedUserName();
        GamePlayer requestedGp = getGpOfId(game_player_id);
        String requesterId = requestedGp.getPlayer().getUserName();
        if (requesterId != loggedId) {
            return new ResponseEntity<>(makeMap("error", "You're not authorized to see this information"), HttpStatus.UNAUTHORIZED);
        }else {
            return new ResponseEntity<>(getGameInfo(game_player_id), HttpStatus.OK);
        }
    }

    private Map<String, Object> getGameInfo(long game_player_id) {
        Map<String, Object> dto = new LinkedHashMap<>();
        GamePlayer gpOfId = getGpOfId(game_player_id);
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
        return repoGamePlayer.findOne(game_player_id);
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

    //This function return the GamePlayer of the opponent or null
    private GamePlayer getOpponent(Set<GamePlayer> bothGps, Long ownerId) {
        Predicate<GamePlayer> isOpponent = gamePlayer -> gamePlayer.getGamePlayerId() != ownerId;
        Optional<GamePlayer> returnGp = bothGps
                .stream()
                .filter(isOpponent)
                .findAny();
        GamePlayer opponent;
        try {
            opponent = returnGp.get();
        } catch (Exception e) {
            opponent = null;
        }
        return opponent;
    }

    //This function return Salvo Locations for a turn and GamePlayer give it
    private List<String> getSalvoLocations(Long turn, GamePlayer gp) {
        Predicate<Salvo> isTurn = salvo -> salvo.getTurn() == turn;
        Optional<Salvo> returnSalvo = gp.getSalvos()
                .stream()
                .filter(isTurn)
                .findAny();
        List<String> locations;
        try {
            locations = returnSalvo.get().getSalvo_locations();
        } catch (Exception NoSuchElementException) {
            locations = null;
        }
        return locations;
    }

    //This function returns the quantity of turns played in a game
    private int getTurnsQuantity(Set<GamePlayer> bothGp) {
        int turns;
        turns = bothGp.stream()
                .max(Comparator.comparingInt(gp -> gp.getSalvos().size()))
                .get()
                .getSalvos()
                .size();
        return turns;
    }

    //-------------------TASK 5--------------------
    @Autowired
    PlayerRepository repoPlayer;

    @RequestMapping("/leaderboard")
    public List<Object> getApiLeaderboard() {
        return repoPlayer
                .findAll()
                .stream()
                .map(player -> boardDTO(player))
                .collect(Collectors.toList());
    }

    private Map<String, Object> boardDTO(Player player) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("player", player.getUserName());
        dto.put("scores", getPlayerScores(player.getScores()));
        return dto;
    }

    private Map<String, Object> getPlayerScores(Set<Score> scores) {
        Map<String, Object> scoreMap = new LinkedHashMap<>();
        Predicate<Score> filterWon = score -> score.getScore().equals(1.0);
        Predicate<Score> filterTied = score -> score.getScore().equals(0.5);
        Predicate<Score> filterLost = score -> score.getScore().equals(0.0);
        scoreMap.put("won", scores.stream().filter(filterWon).count());
        scoreMap.put("lost", scores.stream().filter(filterLost).count());
        scoreMap.put("tied", scores.stream().filter(filterTied).count());
        scoreMap.put("score", (scores.stream().filter(filterWon).count())*1.0 +
                (scores.stream().filter(filterTied).count())*0.5);
        return scoreMap;
    }

    //-------------------CREATE NEW USER--------------------
    @RequestMapping(path = "/players", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> createPlayer(@RequestParam String username, String password, String email) {
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            return new ResponseEntity<>(makeMap("error", "Please, fill all fields"), HttpStatus.FORBIDDEN);
        }
        Player user = repoPlayer.findByUserNameOrEmail(username, email);
        if (user != null) {
            return new ResponseEntity<>(makeMap("error", "username or email already exist"), HttpStatus.CONFLICT);
        }
        user = repoPlayer.save(new Player(username, email, password));
        return new ResponseEntity<>(makeMap("username", user.getUserName()), HttpStatus.CREATED);
    }

    //-------------------JOIN A GAME--------------------
    @RequestMapping(path = "/join", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> addPlayerToGame(@RequestParam Long gid, Long pid) {
        Game game = repoGame.findOne(gid);
        Player player = repoPlayer.findOne(pid);
        GamePlayer addingPlayer = repoGamePlayer.save(new GamePlayer(game, player));
        return new ResponseEntity<>(makeMap("GamePlayerId", addingPlayer.getGamePlayerId()), HttpStatus.CREATED);
    }

    //-------------------PLAY ONE OF YOUR GAMES--------------------
    @RequestMapping(path = "/play", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> backPlayerToGame(@RequestParam Long gid, Long pid) {
        Game game = repoGame.findOne(gid);
        Player player = repoPlayer.findOne(pid);
        GamePlayer goToGamePlayer = repoGamePlayer.findByPlayerAndGame(player, game);
        return new ResponseEntity<>(makeMap("GamePlayerId", goToGamePlayer.getGamePlayerId()), HttpStatus.CREATED);
    }

    //-------------------CREATE NEW GAME--------------------
    @RequestMapping(path = "/new", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> newGame() {
        Game game = new Game();
        repoGame.save(game);
        Player player = repoPlayer.findOne(getIdLogged());
        GamePlayer newGamePlayer = new GamePlayer(game, player);
        repoGamePlayer.save(newGamePlayer);
        return new ResponseEntity<>(makeMap("GamePlayerId", newGamePlayer.getGamePlayerId()), HttpStatus.CREATED);
    }

    public Long getIdLogged(){
        Map<String,Object> playerInfo = getPlayerLoggedIn();
        Object loggedId = playerInfo.get("id");
        String stringToConvert = String.valueOf(loggedId);
        Long convertedLong = Long.parseLong(stringToConvert);
        return convertedLong;
    }

    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    @Autowired
    ShipRepository repoShip;

    @RequestMapping(path= "/games/players/{gpId}/ships", method = RequestMethod.POST)
    public ResponseEntity<String> addShips(@PathVariable long gpId, @RequestBody Set<Ship> ships) {
        GamePlayer gpOfShips = repoGamePlayer.findOne(gpId);//salvar barco
        ships.stream().forEach((ship)-> {

            gpOfShips.addShip(ship);
            repoShip.save(ship);
        });
        return new ResponseEntity<>("success" , HttpStatus.CREATED);
    }
}



