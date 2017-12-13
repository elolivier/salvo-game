package salvo.salvo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
public class Ship {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long ship_id;
    private String shipType;

    @ElementCollection
    @Column(name="locations")
    private List<String> locations = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="game_player_id")
    private GamePlayer gp;

    public Ship() {}

    public Ship(String shipType, List<String> location, GamePlayer gamePlayer) {
        this.shipType = shipType;
        this.locations = location;
        this.gp = gamePlayer;
    }

    public long getShip_id() {
        return ship_id;
    }

    public String getShipType() {
        return shipType;
    }

    public void setShipType(String shipType) {
        this.shipType = shipType;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }

    public GamePlayer getGamePlayer() {
        return gp;
    }

    public void setGamePlayer(GamePlayer game_player) {
        gp = game_player;
    }
}
