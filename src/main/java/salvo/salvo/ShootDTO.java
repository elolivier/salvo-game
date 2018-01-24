package salvo.salvo;

import java.util.List;

public class ShootDTO {
    private long turn;
    private List<String> locations;

    public ShootDTO() {
    }

    public ShootDTO(long turn, List<String> locations) {
        this.turn = turn;
        this.locations = locations;
    }

    public long getTurn() {
        return turn;
    }

    public void setTurn(long turn) {
        this.turn = turn;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }
}
