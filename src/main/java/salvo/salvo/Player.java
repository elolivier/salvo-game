package salvo.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Entity
public class Player {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long player_id;
    private String userName;
    private String email;
    private String password;

    public Player() {}

    public Player(String userName, String email, String password) {
        this.userName = userName;
        this.email = email;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getemail() {
        return email;
    }

    public void setemail(String email) {
        this.email = email;
    }

    @JsonIgnore
    public String getpassword() {
        return password;
    }

    public void setpassword(String password) {
        this.password = password;
    }

    public long getPlayerId() {
        return player_id;
    }

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    Set<GamePlayer> games;

    public void addGamePlayer(GamePlayer game_player) {
        game_player.setPlayer(this);
        games.add(game_player);
    }

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    Set<Score> scores;

    public void addScore(Score score) {
        score.setPlayer(this);
        scores.add(score);
    }
    @JsonIgnore
    public Set<Score> getScores() {
        return scores;
    }

    public Double getScore(Game game) {
        Double score;
        List<Score> scoreFilter2 = scores.stream()
                .filter(scoreTest -> scoreTest.getPlayer().equals(this)
                        && scoreTest.getGame().equals(game))
                .collect(Collectors.toList());
        if(scoreFilter2.size() < 1) {
            score = null;
        } else {
            score = scoreFilter2.get(0).getScore();
        }
        return score;
    }

    @JsonIgnore
    public List<Game> getGames() {
        return games.stream().map(sub -> sub.getGame()).collect(Collectors.toList());
    }
}
