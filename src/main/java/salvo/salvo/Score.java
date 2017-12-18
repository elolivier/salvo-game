package salvo.salvo;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Score {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long score_id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;

    private Double score;

    public Score() { }

    public Score(Game game, Player player, Double score) {
        this.game = game;
        this.player = player;
        this.score = score;
        gameFinishDate = LocalDateTime.now();
    }

    private LocalDateTime gameFinishDate;

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public long getScoreId() {
        return score_id;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public LocalDateTime getGameFinishDateDate() {
        return gameFinishDate;
    }

    public void setGameFinishDateDate(LocalDateTime userJoinDate) {
        this.gameFinishDate = userJoinDate;
    }
}
