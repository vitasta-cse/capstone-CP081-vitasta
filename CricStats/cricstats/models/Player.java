package cricstats.models;

import java.io.Serializable;
import java.util.Objects;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    // Batting stats
    private int runsScored;
    private int ballsFaced;
    private int fours;
    private int sixes;
    private boolean hasBatted;
    private boolean isOut;
    private Dismissal dismissal;

    // Bowling stats
    private int legalBallsBowled;
    private int maidens;
    private int runsConceded; // off the bat, wides, no-balls (excludes byes and leg-byes)
    private int wickets;      // excludes run outs
    private int widesConceded;
    private int noBallsConceded;

    // Fielding stats
    private int catches;
    private int runOuts;
    private int stumpings;

    public Player() {
    }

    public Player(String name) {
        this.name = name != null ? name.trim() : "";
    }

    public Player(Player other) {
        if (other != null) {
            this.name = other.name;
            this.runsScored = other.runsScored;
            this.ballsFaced = other.ballsFaced;
            this.fours = other.fours;
            this.sixes = other.sixes;
            this.hasBatted = other.hasBatted;
            this.isOut = other.isOut;
            this.dismissal = other.dismissal != null ? new Dismissal(other.dismissal) : null;
            this.legalBallsBowled = other.legalBallsBowled;
            this.maidens = other.maidens;
            this.runsConceded = other.runsConceded;
            this.wickets = other.wickets;
            this.widesConceded = other.widesConceded;
            this.noBallsConceded = other.noBallsConceded;
            this.catches = other.catches;
            this.runOuts = other.runOuts;
            this.stumpings = other.stumpings;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Batting getters & setters
    public int getRunsScored() {
        return runsScored;
    }

    public void setRunsScored(int runsScored) {
        this.runsScored = runsScored;
    }

    public void addRuns(int runs) {
        this.runsScored += runs;
        if (runs == 4) {
            this.fours++;
        } else if (runs == 6) {
            this.sixes++;
        }
    }

    public int getBallsFaced() {
        return ballsFaced;
    }

    public void setBallsFaced(int ballsFaced) {
        this.ballsFaced = ballsFaced;
    }

    public void incrementBallsFaced() {
        this.ballsFaced++;
    }

    public int getFours() {
        return fours;
    }

    public void setFours(int fours) {
        this.fours = fours;
    }

    public int getSixes() {
        return sixes;
    }

    public void setSixes(int sixes) {
        this.sixes = sixes;
    }

    public boolean isHasBatted() {
        return hasBatted;
    }

    public void setHasBatted(boolean hasBatted) {
        this.hasBatted = hasBatted;
    }

    public boolean isOut() {
        return isOut;
    }

    public void setOut(boolean out) {
        isOut = out;
    }

    public Dismissal getDismissal() {
        return dismissal;
    }

    public void setDismissal(Dismissal dismissal) {
        this.dismissal = dismissal;
        this.isOut = (dismissal != null);
    }

    public double getStrikeRate() {
        if (ballsFaced == 0) {
            return 0.0;
        }
        return (runsScored * 100.0) / ballsFaced;
    }

    public String getDismissalString() {
        if (!hasBatted) {
            return "yet to bat";
        }
        if (!isOut) {
            return "not out";
        }
        return dismissal != null ? dismissal.getDescription() : "out";
    }

    // Bowling getters & setters
    public int getLegalBallsBowled() {
        return legalBallsBowled;
    }

    public void setLegalBallsBowled(int legalBallsBowled) {
        this.legalBallsBowled = legalBallsBowled;
    }

    public void incrementLegalBallsBowled() {
        this.legalBallsBowled++;
    }

    public int getMaidens() {
        return maidens;
    }

    public void setMaidens(int maidens) {
        this.maidens = maidens;
    }

    public void incrementMaidens() {
        this.maidens++;
    }

    public int getRunsConceded() {
        return runsConceded;
    }

    public void setRunsConceded(int runsConceded) {
        this.runsConceded = runsConceded;
    }

    public void addRunsConceded(int runs) {
        this.runsConceded += runs;
    }

    public int getWickets() {
        return wickets;
    }

    public void setWickets(int wickets) {
        this.wickets = wickets;
    }

    public void incrementWickets() {
        this.wickets++;
    }

    public int getWidesConceded() {
        return widesConceded;
    }

    public void setWidesConceded(int widesConceded) {
        this.widesConceded = widesConceded;
    }

    public void addWidesConceded(int wides) {
        this.widesConceded += wides;
    }

    public int getNoBallsConceded() {
        return noBallsConceded;
    }

    public void setNoBallsConceded(int noBallsConceded) {
        this.noBallsConceded = noBallsConceded;
    }

    public void addNoBallsConceded(int noBalls) {
        this.noBallsConceded += noBalls;
    }

    public String getOversBowledString() {
        int completedOvers = legalBallsBowled / 6;
        int remainingBalls = legalBallsBowled % 6;
        return completedOvers + "." + remainingBalls;
    }

    public double getOversBowledDecimal() {
        int completedOvers = legalBallsBowled / 6;
        int remainingBalls = legalBallsBowled % 6;
        return completedOvers + (remainingBalls / 6.0);
    }

    public double getEconomyRate() {
        double overs = getOversBowledDecimal();
        if (overs <= 0.0) {
            return 0.0;
        }
        return runsConceded / overs;
    }

    // Fielding getters & setters
    public int getCatches() {
        return catches;
    }

    public void setCatches(int catches) {
        this.catches = catches;
    }

    public void incrementCatches() {
        this.catches++;
    }

    public int getRunOuts() {
        return runOuts;
    }

    public void setRunOuts(int runOuts) {
        this.runOuts = runOuts;
    }

    public void incrementRunOuts() {
        this.runOuts++;
    }

    public int getStumpings() {
        return stumpings;
    }

    public void setStumpings(int stumpings) {
        this.stumpings = stumpings;
    }

    public void incrementStumpings() {
        this.stumpings++;
    }

    // Player of the Match Points
    public int calculatePoints(PlayerOfMatchConfig config) {
        if (config == null) {
            config = new PlayerOfMatchConfig();
        }
        int points = 0;
        points += runsScored * config.getPointsPerRun();
        points += fours * config.getBonusPerFour();
        points += sixes * config.getBonusPerSix();
        points += wickets * config.getPointsPerWicket();
        points += maidens * config.getPointsPerMaiden();

        if (runsScored >= 100) {
            points += config.getBonusCentury();
        } else if (runsScored >= 50) {
            points += config.getBonusHalfCentury();
        }

        points += catches * config.getPointsPerCatch();
        points += runOuts * config.getPointsPerRunOut();
        points += stumpings * config.getPointsPerStumping();

        return points;
    }

    public String getPointsBreakdown(PlayerOfMatchConfig config) {
        if (config == null) config = new PlayerOfMatchConfig();
        StringBuilder sb = new StringBuilder();
        if (runsScored > 0) sb.append(runsScored).append(" runs (").append(runsScored * config.getPointsPerRun()).append(" pts), ");
        if (fours > 0) sb.append(fours).append(" fours (").append(fours * config.getBonusPerFour()).append(" pts), ");
        if (sixes > 0) sb.append(sixes).append(" sixes (").append(sixes * config.getBonusPerSix()).append(" pts), ");
        if (wickets > 0) sb.append(wickets).append(" wkts (").append(wickets * config.getPointsPerWicket()).append(" pts), ");
        if (maidens > 0) sb.append(maidens).append(" maidens (").append(maidens * config.getPointsPerMaiden()).append(" pts), ");
        if (runsScored >= 100) sb.append("century bonus (").append(config.getBonusCentury()).append(" pts), ");
        else if (runsScored >= 50) sb.append("50 bonus (").append(config.getBonusHalfCentury()).append(" pts), ");
        if (catches > 0) sb.append(catches).append(" catches (").append(catches * config.getPointsPerCatch()).append(" pts), ");
        if (runOuts > 0) sb.append(runOuts).append(" run-outs (").append(runOuts * config.getPointsPerRunOut()).append(" pts), ");
        if (stumpings > 0) sb.append(stumpings).append(" stumpings (").append(stumpings * config.getPointsPerStumping()).append(" pts), ");
        
        String res = sb.toString();
        if (res.endsWith(", ")) {
            res = res.substring(0, res.length() - 2);
        }
        return res.isEmpty() ? "0 pts" : res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        Player player = (Player) o;
        return Objects.equals(name != null ? name.toLowerCase() : null,
                              player.name != null ? player.name.toLowerCase() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name != null ? name.toLowerCase() : null);
    }

    @Override
    public String toString() {
        return name;
    }
}
