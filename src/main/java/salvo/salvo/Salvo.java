package salvo.salvo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Salvo {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long salvo_id;
    private long turn;

    @ElementCollection
    @Column(name="salvo_locations")
    private List<String> salvo_locations = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_player_id")
    private GamePlayer gp;

    public Salvo() {}

    public Salvo(long turn, List<String> salvo_locations, GamePlayer gp) {
        this.turn = turn;
        this.salvo_locations = salvo_locations;
        this.gp = gp;
    }

    public long getTurn() {
        return turn;
    }

    public void setTurn(long turn) {
        this.turn = turn;
    }

    public List<String> getSalvo_locations() {
        return salvo_locations;
    }

    public void setSalvo_locations(List<String> salvo_locations) {
        this.salvo_locations = salvo_locations;
    }

    public GamePlayer getGp() {
        return gp;
    }

    public void setGp(GamePlayer gp) {
        this.gp = gp;
    }

    public long getSalvo_id() {
        return salvo_id;
    }

    public void setGamePlayer(GamePlayer game_player) {
        gp = game_player;
    }
}
