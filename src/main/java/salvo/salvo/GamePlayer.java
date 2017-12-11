package salvo.salvo;

import java.time.*;
import java.util.LinkedHashMap;
import java.util.Map;
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

    public Object getGamePlayerInfo() {
        Object game_player_info;
        game_player_info = gamePlayerDTO(this);
        return game_player_info;
    }

    private Map<String, Object> gamePlayerDTO(GamePlayer game_player) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", game_player.getGamePlayerId());
        dto.put("player", game_player.getPlayer());
        return dto;
    }

    public LocalDateTime getUserJoinDate() {
        return userJoinDate;
    }

    public void setUserJoinDate(LocalDateTime userJoinDate) {
        this.userJoinDate = userJoinDate;
    }
}
