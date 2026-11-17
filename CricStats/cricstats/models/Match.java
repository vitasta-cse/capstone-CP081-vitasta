package cricstats.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class Match implements Serializable {
    private static final long serialVersionUID = 1L;

    private String matchId;
    private String date;
    private Team team1;
    private Team team2;
    private String tossWinner;
    private String tossDecision; // "BAT" or "BOWL"
    private int allocatedOvers;

    private Innings firstInnings;
    private Innings secondInnings;
    private int currentInningsNumber = 1;
    private MatchStatus status = MatchStatus.NOT_STARTED;

    private String result;
    private PlayerOfMatchConfig pomConfig = new PlayerOfMatchConfig();
    private String playerOfTheMatch;
    private String playerOfTheMatchReason;
    private int playerOfTheMatchPoints;

    public Match() {
        this.matchId = "MATCH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
    }

    public Match(Team team1, Team team2, int allocatedOvers) {
        this();
        this.team1 = team1;
        this.team2 = team2;
        this.allocatedOvers = allocatedOvers;
    }

    public Match(Match other) {
        if (other != null) {
            this.matchId = other.matchId;
            this.date = other.date;
            this.team1 = other.team1 != null ? new Team(other.team1) : null;
            this.team2 = other.team2 != null ? new Team(other.team2) : null;
            this.tossWinner = other.tossWinner;
            this.tossDecision = other.tossDecision;
            this.allocatedOvers = other.allocatedOvers;
            this.firstInnings = other.firstInnings != null ? new Innings(other.firstInnings) : null;
            this.secondInnings = other.secondInnings != null ? new Innings(other.secondInnings) : null;
            this.currentInningsNumber = other.currentInningsNumber;
            this.status = other.status;
            this.result = other.result;
            this.pomConfig = other.pomConfig != null ? new PlayerOfMatchConfig(other.pomConfig) : new PlayerOfMatchConfig();
            this.playerOfTheMatch = other.playerOfTheMatch;
            this.playerOfTheMatchReason = other.playerOfTheMatchReason;
            this.playerOfTheMatchPoints = other.playerOfTheMatchPoints;
        }
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Team getTeam1() {
        return team1;
    }

    public void setTeam1(Team team1) {
        this.team1 = team1;
    }

    public Team getTeam2() {
        return team2;
    }

    public void setTeam2(Team team2) {
        this.team2 = team2;
    }

    public String getTossWinner() {
        return tossWinner;
    }

    public void setTossWinner(String tossWinner) {
        this.tossWinner = tossWinner;
    }

    public String getTossDecision() {
        return tossDecision;
    }

    public void setTossDecision(String tossDecision) {
        this.tossDecision = tossDecision;
    }

    public int getAllocatedOvers() {
        return allocatedOvers;
    }

    public void setAllocatedOvers(int allocatedOvers) {
        this.allocatedOvers = allocatedOvers;
    }

    public Innings getFirstInnings() {
        return firstInnings;
    }

    public void setFirstInnings(Innings firstInnings) {
        this.firstInnings = firstInnings;
    }

    public Innings getSecondInnings() {
        return secondInnings;
    }

    public void setSecondInnings(Innings secondInnings) {
        this.secondInnings = secondInnings;
    }

    public int getCurrentInningsNumber() {
        return currentInningsNumber;
    }

    public void setCurrentInningsNumber(int currentInningsNumber) {
        this.currentInningsNumber = currentInningsNumber;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public PlayerOfMatchConfig getPomConfig() {
        return pomConfig;
    }

    public void setPomConfig(PlayerOfMatchConfig pomConfig) {
        this.pomConfig = pomConfig;
    }

    public String getPlayerOfTheMatch() {
        return playerOfTheMatch;
    }

    public void setPlayerOfTheMatch(String playerOfTheMatch) {
        this.playerOfTheMatch = playerOfTheMatch;
    }

    public String getPlayerOfTheMatchReason() {
        return playerOfTheMatchReason;
    }

    public void setPlayerOfTheMatchReason(String playerOfTheMatchReason) {
        this.playerOfTheMatchReason = playerOfTheMatchReason;
    }

    public int getPlayerOfTheMatchPoints() {
        return playerOfTheMatchPoints;
    }

    public void setPlayerOfTheMatchPoints(int playerOfTheMatchPoints) {
        this.playerOfTheMatchPoints = playerOfTheMatchPoints;
    }

    public Innings getCurrentInnings() {
        if (currentInningsNumber == 1) {
            return firstInnings;
        } else if (currentInningsNumber == 2) {
            return secondInnings;
        }
        return null;
    }

    public void evaluateMatchResult() {
        if (firstInnings == null || secondInnings == null) {
            return;
        }
        if (!secondInnings.isCompleted()) {
            return;
        }

        int runs1 = firstInnings.getTotalRuns();
        int runs2 = secondInnings.getTotalRuns();
        String team1Name = firstInnings.getBattingTeam().getName();
        String team2Name = secondInnings.getBattingTeam().getName();

        if (runs2 > runs1) {
            int totalPlayers = secondInnings.getBattingTeam().getPlayers().size();
            int remainingWickets = (totalPlayers - 1) - secondInnings.getWickets();
            this.result = team2Name + " won by " + remainingWickets + " wicket" + (remainingWickets == 1 ? "" : "s") + "!";
        } else if (runs1 > runs2) {
            int runMargin = runs1 - runs2;
            this.result = team1Name + " won by " + runMargin + " run" + (runMargin == 1 ? "" : "s") + "!";
        } else {
            this.result = "Match Tied (" + runs1 + " runs each)!";
        }

        this.status = MatchStatus.COMPLETED;
        determinePlayerOfTheMatch();
    }

    public void determinePlayerOfTheMatch() {
        Map<String, PlayerAggregatedStats> playerMap = new LinkedHashMap<>();

        // Aggregate across both innings
        aggregateInningsPlayers(firstInnings, playerMap);
        aggregateInningsPlayers(secondInnings, playerMap);

        String bestPlayer = null;
        int maxPoints = -1;
        String bestReason = "";

        for (Map.Entry<String, PlayerAggregatedStats> entry : playerMap.entrySet()) {
            PlayerAggregatedStats stats = entry.getValue();
            int pts = stats.calculatePoints(pomConfig);
            if (pts > maxPoints) {
                maxPoints = pts;
                bestPlayer = stats.name + " (" + stats.teamName + ")";
                bestReason = stats.getSummary(pomConfig);
            }
        }

        this.playerOfTheMatch = bestPlayer;
        this.playerOfTheMatchPoints = maxPoints;
        this.playerOfTheMatchReason = bestReason;
    }

    private void aggregateInningsPlayers(Innings inn, Map<String, PlayerAggregatedStats> map) {
        if (inn == null) return;

        if (inn.getBattingTeam() != null) {
            for (Player p : inn.getBattingTeam().getPlayers()) {
                map.putIfAbsent(p.getName(), new PlayerAggregatedStats(p.getName(), inn.getBattingTeam().getName()));
                PlayerAggregatedStats s = map.get(p.getName());
                s.runsScored += p.getRunsScored();
                s.fours += p.getFours();
                s.sixes += p.getSixes();
                s.ballsFaced += p.getBallsFaced();
            }
        }

        if (inn.getBowlingTeam() != null) {
            for (Player p : inn.getBowlingTeam().getPlayers()) {
                map.putIfAbsent(p.getName(), new PlayerAggregatedStats(p.getName(), inn.getBowlingTeam().getName()));
                PlayerAggregatedStats s = map.get(p.getName());
                s.wickets += p.getWickets();
                s.runsConceded += p.getRunsConceded();
                s.legalBallsBowled += p.getLegalBallsBowled();
                s.maidens += p.getMaidens();
                s.catches += p.getCatches();
                s.runOuts += p.getRunOuts();
                s.stumpings += p.getStumpings();
            }
        }
    }

    public static class PlayerAggregatedStats {
        public String name;
        public String teamName;
        public int runsScored;
        public int ballsFaced;
        public int fours;
        public int sixes;
        public int wickets;
        public int runsConceded;
        public int legalBallsBowled;
        public int maidens;
        public int catches;
        public int runOuts;
        public int stumpings;

        public PlayerAggregatedStats(String name, String teamName) {
            this.name = name;
            this.teamName = teamName;
        }

        public int calculatePoints(PlayerOfMatchConfig config) {
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

        public String getSummary(PlayerOfMatchConfig config) {
            List<String> parts = new ArrayList<>();
            if (runsScored > 0) parts.add(runsScored + " runs (" + ballsFaced + "b, " + fours + "x4, " + sixes + "x6)");
            if (wickets > 0) {
                int ov = legalBallsBowled / 6;
                int b = legalBallsBowled % 6;
                parts.add(wickets + "/" + runsConceded + " in " + ov + "." + b + " ov");
            }
            if (maidens > 0) parts.add(maidens + " maiden" + (maidens > 1 ? "s" : ""));
            if (catches > 0) parts.add(catches + " catch" + (catches > 1 ? "es" : ""));
            if (runOuts > 0) parts.add(runOuts + " run-out" + (runOuts > 1 ? "s" : ""));
            if (stumpings > 0) parts.add(stumpings + " stumping" + (stumpings > 1 ? "s" : ""));
            return String.join(" | ", parts) + " [" + calculatePoints(config) + " pts]";
        }
    }
}
