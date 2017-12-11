package salvo.salvo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Player {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long player_id;
    private String userName;
    private String firstName;
    private String lastName;

    public Player() {}

    public Player(String userName, String firstName, String lastName) {
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    @JsonIgnore
    public List<Game> getGames() {
        return games.stream().map(sub -> sub.getGame()).collect(Collectors.toList());
    }
}
