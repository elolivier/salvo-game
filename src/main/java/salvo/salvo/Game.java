package salvo.salvo;

import java.time.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.*;

@Entity
public class Game {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long game_id;
    private LocalDateTime gameDate;

    public Game() {
        gameDate = LocalDateTime.now();
    }

    public LocalDateTime getGameDate() {
        return gameDate;
    }

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
    Set<GamePlayer> games;

    public void addGamePlayer(GamePlayer game_player) {
        game_player.setGame(this);
        games.add(game_player);
    }

    public long getGameId() {
        return game_id;
    }

    public List<Object> getPlayers() {

        return games
                .stream()
                .map(sub -> sub.getGamePlayerInfo())
                .collect(Collectors.toList());
    }
}
