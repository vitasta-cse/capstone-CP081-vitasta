package cricstats.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Team implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private List<Player> players = new ArrayList<>();
    private List<String> battingOrder = new ArrayList<>();

    public Team() {
    }

    public Team(String name) {
        this.name = name != null ? name.trim() : "";
    }

    public Team(Team other) {
        if (other != null) {
            this.name = other.name;
            this.players = new ArrayList<>();
            for (Player p : other.players) {
                this.players.add(new Player(p));
            }
            this.battingOrder = new ArrayList<>(other.battingOrder);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<String> getBattingOrder() {
        return battingOrder;
    }

    public void setBattingOrder(List<String> battingOrder) {
        this.battingOrder = battingOrder;
    }

    public boolean addPlayer(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            return false;
        }
        String cleanName = playerName.trim();
        for (Player p : players) {
            if (p.getName().equalsIgnoreCase(cleanName)) {
                return false; // duplicate
            }
        }
        Player newPlayer = new Player(cleanName);
        players.add(newPlayer);
        battingOrder.add(cleanName);
        return true;
    }

    public Player getPlayer(String playerName) {
        if (playerName == null) return null;
        for (Player p : players) {
            if (p.getName().equalsIgnoreCase(playerName.trim())) {
                return p;
            }
        }
        return null;
    }

    public boolean containsPlayer(String playerName) {
        return getPlayer(playerName) != null;
    }

    public Player getPlayerAtBattingPosition(int index) {
        if (index >= 0 && index < battingOrder.size()) {
            return getPlayer(battingOrder.get(index));
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}
