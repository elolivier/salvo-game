package salvo.salvo;

import java.time.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.persistence.*;

@Entity
public class GamePlayer {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private long game_player_id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_id")
    private Game game;

    @OneToMany(mappedBy = "gp", fetch = FetchType.EAGER)
    Set<Ship> ships;

    public void addShip(Ship ship) {
        ship.setGamePlayer(this);
        ships.add(ship);
    }

    public Set<Ship> getShips() {
        return ships;
    }

    public GamePlayer() { }

    public GamePlayer(Game game, Player player) {
        this.game = game;
        this.player = player;
        userJoinDate = LocalDateTime.now();
    }

    private LocalDateTime userJoinDate;

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

    public long getGamePlayerId() {
        return game_player_id;
    }

    public LocalDateTime getUserJoinDate() {
        return userJoinDate;
    }

    public void setUserJoinDate(LocalDateTime userJoinDate) {
        this.userJoinDate = userJoinDate;
    }
}
