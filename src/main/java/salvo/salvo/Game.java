package salvo.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    public Set<GamePlayer> getGames() {
        return games;
    }

    public long getGameId() {
        return game_id;
    }

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER)
    Set<Score> scores;

    public void addScore(Score score) {
        score.setGame(this);
        scores.add(score);
    }
    @JsonIgnore
    public Set<Score> getScores() {
        return scores;
    }
}
