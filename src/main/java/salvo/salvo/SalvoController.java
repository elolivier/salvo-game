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

    //Return a map with ID and Username of player logged
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

    //return the username of player logged
    private String getLoggedUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String currentUserName = authentication.getName();
            return currentUserName;
        }
        return null;
    }

    //return all games saved in DB
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
        String loggedUserName = getLoggedUserName();
        GamePlayer requesterGp = getRequesterGp(game_player_id);
        String requesterUserName = requesterGp.getPlayer().getUserName();
        if (requesterUserName != loggedUserName) {
            return new ResponseEntity<>(makeMap("error", "You're not authorized to see this information"), HttpStatus.UNAUTHORIZED);
        }else {
            Game thisGame = requesterGp.getGame();
            GamePlayer opponent1 = getOpponent(thisGame.getGames(), game_player_id);
            return new ResponseEntity<>(getGameInfo(thisGame, requesterGp, opponent1), HttpStatus.OK);
        }
    }

    private Map<String, Object> getGameInfo(Game thisGame, GamePlayer requesterGp, GamePlayer opponent) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("game_id", thisGame.getGameId());
        dto.put("stateOfGame", getStateOfGame(requesterGp, opponent));
        dto.put("created", thisGame.getGameDate());
        dto.put("gamePlayers", thisGame.getGames()
                .stream()
                .map(game_player -> gamePlayerDTO(game_player))
                .collect(Collectors.toList()));
        dto.put("ships", requesterGp.getShips()
                .stream()
                .map(ship -> shipsDTO(ship))
                .collect(Collectors.toList()));
        dto.put("salvos", getTurnsMap(requesterGp, thisGame.getGames(), opponent)); //MAP OF TURN:OBJECT
        return dto;
    }

    private GamePlayer getRequesterGp(long requesterGpId) {
        return repoGamePlayer.findOne(requesterGpId);
    }

    @Autowired
    ScoreRepository repoScore;

    private int getStateOfGame(GamePlayer gpRequester, GamePlayer opponent) {
        int stateOfGame;
        //State 1: Place your ships
        if(gpRequester.getShips().size() == 0) {
            stateOfGame = 1;
        }
        //State 2: Waiting for opponent
        else if(opponent == null) {
            stateOfGame = 2;
        }
        //State 3: Waiting for opponent place ships
        else if(opponent.getShips().size() == 0) {
            stateOfGame = 3;
        }
        //State 6: All your ships sunk (you lose)
        else if(allSunk(gpRequester)) {
            stateOfGame = 6;
            Score testLoser = repoScore.findByGameAndPlayer(gpRequester.getGame(), gpRequester.getPlayer());
            Score loser = new Score(gpRequester.getGame(), gpRequester.getPlayer(), 0.0);
            if(testLoser == null) {
                repoScore.save(loser);
            }
        }
        //State 7: All your enemy ships sunk (you win)
        else if(allSunk(opponent)) {
            stateOfGame = 7;
            Score test = repoScore.findByGameAndPlayer(gpRequester.getGame(), gpRequester.getPlayer());
            Score winner = new Score(gpRequester.getGame(), gpRequester.getPlayer(), 1.0);
            if(test == null) {
                repoScore.save(winner);
            }
        }

        //State 4-5: Waiting for opponent to shoot or shoot
        else if(gpRequester.getGamePlayerId() < opponent.getGamePlayerId()) {
            if(gpRequester.salvos.size() > opponent.salvos.size()) {
                //State 4: Waiting for opponent to shoot
                stateOfGame = 4;
            }else {
                //State 5: Shoot your salvo
                stateOfGame = 5;
            }
        }
        //State 4-5: Waiting for opponent to shoot or shoot
        else if(gpRequester.getGamePlayerId() > opponent.getGamePlayerId()) {
            if(gpRequester.salvos.size() == opponent.salvos.size()) {
                //State 4: Waiting for opponent to shoot
                stateOfGame = 4;
            }else {
                //State 5: Shoot your salvo
                stateOfGame = 5;
            }
        }

        else {
            stateOfGame = 9;
        }
        return stateOfGame;
    }

    private boolean allSunk(GamePlayer gp) {
        boolean gameOver;
        int ships=0;
        for (Ship ship : gp.getShips()) {
            boolean sunk = isSink(gp, ship);
            if (sunk) {
                ships++;
            }
        }
        if(ships == gp.getShips().size()) {
            gameOver = true;
        }else {
            gameOver = false;
        }
        return gameOver;
    }

    private Map<String, Object> shipsDTO(Ship ship) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("type", ship.getShipType());
        dto.put("locations", ship.getLocations());
        return dto;
    }

    //-------------------TASK 4--------------------
    //This function should return an object Turn:Object
    private Map<Long, Object> getTurnsMap(GamePlayer requesterGp, Set<GamePlayer> bothGps, GamePlayer opponent) {
        Map<Long, Object> turnObject = new LinkedHashMap<>();
        Long turns = (long) getTurnsQuantity(bothGps);
        //System.out.println("quantity of turns " + turns);
        for (Long i = 1L; i <= turns; i++) {
            turnObject.put(i, getMapPlayers(i,opponent,requesterGp));
        }
        return turnObject;
    }

    //This function should return an object {PlayerId1:Locations1, PlayerId2:Locations2}
    private Map<Long, Object> getMapPlayers(Long turn, GamePlayer opponent, GamePlayer requesterGp) {
        Map<Long, Object> eachPlayerPerTurn = new LinkedHashMap<>();

        if (opponent != null){
            eachPlayerPerTurn.put(requesterGp.getPlayer().getPlayerId(),getSalvoInfo(turn, requesterGp));
            eachPlayerPerTurn.put(opponent.getPlayer().getPlayerId(),getSalvoInfo(turn, opponent));
        }
        return eachPlayerPerTurn;
    }

    private Map<String, Object> getSalvoInfo(Long turn, GamePlayer gamePlayer) {
        Map<String, Object> eachPlayerSalvoPerTurn = new LinkedHashMap<>();
        eachPlayerSalvoPerTurn.put("salvo", getSalvoInformation(turn, gamePlayer));
        eachPlayerSalvoPerTurn.put("shipsStatus", getShipsStatus(turn, gamePlayer));
        return eachPlayerSalvoPerTurn;
    }

    private List<Map<String, Object>> getShipsStatus(Long turn, GamePlayer gamePlayer) {
        List<Map<String, Object>> allShips = new ArrayList<>();
        gamePlayer.getShips().forEach((ship) -> {
            Map<String, Object> eachShip = new LinkedHashMap<>();
            eachShip.put("ship", ship.getShipType());
            eachShip.put("cellsHitted", getHitStatus(turn, gamePlayer, ship));
            eachShip.put("shipSink", isSink(gamePlayer, ship));
            allShips.add(eachShip);
        });
        return allShips;
    }

    private List<String> getHitStatus(Long turn, GamePlayer gamePlayer, Ship ship) {
        List<String> hits = new ArrayList<>();
        GamePlayer opponent = getOpponent(gamePlayer.getGame().getGames(),gamePlayer.getGamePlayerId());
        List<String> opponentSalvo = getSalvoLocations(turn, opponent);
        if (opponentSalvo != null && opponent != gamePlayer) {
            opponentSalvo.forEach((shoot)-> {
                ship.getLocations().forEach((position)-> {
                    if (position == shoot) {
                        hits.add(position);
                    }
                });
            });
        }
        return hits;
    }

    private boolean isSink(GamePlayer gamePlayer, Ship ship) {
        boolean sunk;
        GamePlayer opponent = getOpponent(gamePlayer.getGame().getGames(), gamePlayer.getGamePlayerId());
        Set<Salvo> opponentSalvo = opponent.getSalvos();
        List<String> shipPositions = ship.getLocations();
        List<String> allShoots = opponentSalvo.stream().map(salvo -> salvo.getSalvo_locations()).flatMap(shoot -> shoot.stream()).collect(Collectors.toList());
        sunk = allShoots.containsAll(shipPositions);
        return sunk;
    }

    //This function return the GamePlayer of the opponent or null
    private GamePlayer getOpponent(Set<GamePlayer> bothGps, Long ownerGpId) {
        Predicate<GamePlayer> isOpponent = gamePlayer -> gamePlayer.getGamePlayerId() != ownerGpId;
        Optional<GamePlayer> returnGp = bothGps
                .stream()
                .filter(isOpponent)
                .findAny();
        GamePlayer opponent2 = returnGp.orElse(null);
        return opponent2;
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

    private List<Map<String, Object>> getSalvoInformation(Long turn, GamePlayer gp) {
        List<Map<String, Object>> salvoInfo = new ArrayList<>();
        List<String> locations = getSalvoLocations(turn, gp);
        if(locations != null) {
            for (String shoot : locations) {
                Map<String, Object> eachSalvoPerTurn = new LinkedHashMap<>();
                eachSalvoPerTurn.put("cell", shoot);
                eachSalvoPerTurn.put("hit", shootHit(shoot, gp));
                salvoInfo.add(eachSalvoPerTurn);
            }
        }
        return salvoInfo;
    }

    private boolean shootHit(String shoot, GamePlayer gp) {
        boolean hit = false;
        GamePlayer opponent2 = getOpponent(gp.getGame().getGames(),gp.getGamePlayerId());
        List<String> allShipsLocations = opponent2.getShips().stream().map(ship -> ship.getLocations()).flatMap(cell->cell.stream()).collect(Collectors.toList());
        for (String cell : allShipsLocations) {
            if(shoot == cell) {
                hit = true;
            }
        }
        return hit;
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
    //Return Long number of ID user logged
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

    //-------------------SAVE SHIPS--------------------
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

    //-------------------SAVE SALVOS--------------------
    @Autowired
    SalvoRepository repoSalvo;

    @RequestMapping(path= "/games/players/{gpId}/salvos", method = RequestMethod.POST)
    public ResponseEntity<String> addSalvo(@PathVariable long gpId, @RequestBody List<String> salvo) {
        GamePlayer gpOfSalvo = repoGamePlayer.findOne(gpId);
        Set<GamePlayer> bothGp = gpOfSalvo.getGame().getGames();
        GamePlayer playerMostPlay = bothGp.stream()
                .max(Comparator.comparingInt(gp -> gp.getSalvos().size()))
                .get();
        int turns = getTurnsQuantity(bothGp);
        long thisTurn;
        if(gpOfSalvo.getGamePlayerId() == playerMostPlay.getGamePlayerId()) {
            thisTurn = turns + 1;
        }else {
            if(turns == 0) {
                thisTurn = turns + 1;
            }else {
                thisTurn = turns;
            }
        }
        repoSalvo.save(new Salvo(thisTurn, salvo, gpOfSalvo));
        return new ResponseEntity<>("success" , HttpStatus.CREATED);
    }
}