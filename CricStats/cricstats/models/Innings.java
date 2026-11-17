package cricstats.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Innings implements Serializable {
    private static final long serialVersionUID = 1L;

    private int inningsNumber;
    private Team battingTeam;
    private Team bowlingTeam;
    private int allocatedOvers;
    private int target = -1; // -1 for 1st innings; >0 for 2nd innings

    private int totalRuns;
    private int wickets;
    private int legalBalls;

    // Extras breakdown
    private int wideRuns;
    private int noBallRuns;
    private int byeRuns;
    private int legByeRuns;
    private int penaltyRuns;

    // Live state
    private String currentStriker;
    private String currentNonStriker;
    private String currentBowler;
    private String previousBowler;
    private int nextBattingIndex; // points to the next available batsman in batting order

    private List<Delivery> deliveries = new ArrayList<>();
    private List<Over> completedOvers = new ArrayList<>();
    private Over currentOver;
    private List<FallOfWicket> fallOfWickets = new ArrayList<>();
    private List<Partnership> pastPartnerships = new ArrayList<>();
    private Partnership currentPartnership;

    private boolean isCompleted;
    private String completionReason;

    public Innings() {
    }

    public Innings(int inningsNumber, Team battingTeam, Team bowlingTeam, int allocatedOvers, int target) {
        this.inningsNumber = inningsNumber;
        this.battingTeam = battingTeam;
        this.bowlingTeam = bowlingTeam;
        this.allocatedOvers = allocatedOvers;
        this.target = target;
        this.currentOver = new Over(1, "");
    }

    public Innings(Innings other) {
        if (other != null) {
            this.inningsNumber = other.inningsNumber;
            this.battingTeam = other.battingTeam != null ? new Team(other.battingTeam) : null;
            this.bowlingTeam = other.bowlingTeam != null ? new Team(other.bowlingTeam) : null;
            this.allocatedOvers = other.allocatedOvers;
            this.target = other.target;
            this.totalRuns = other.totalRuns;
            this.wickets = other.wickets;
            this.legalBalls = other.legalBalls;
            this.wideRuns = other.wideRuns;
            this.noBallRuns = other.noBallRuns;
            this.byeRuns = other.byeRuns;
            this.legByeRuns = other.legByeRuns;
            this.penaltyRuns = other.penaltyRuns;
            this.currentStriker = other.currentStriker;
            this.currentNonStriker = other.currentNonStriker;
            this.currentBowler = other.currentBowler;
            this.previousBowler = other.previousBowler;
            this.nextBattingIndex = other.nextBattingIndex;
            this.isCompleted = other.isCompleted;
            this.completionReason = other.completionReason;

            this.deliveries = new ArrayList<>();
            for (Delivery d : other.deliveries) {
                this.deliveries.add(new Delivery(d));
            }

            this.completedOvers = new ArrayList<>();
            for (Over o : other.completedOvers) {
                this.completedOvers.add(new Over(o));
            }

            this.currentOver = other.currentOver != null ? new Over(other.currentOver) : null;

            this.fallOfWickets = new ArrayList<>();
            for (FallOfWicket f : other.fallOfWickets) {
                this.fallOfWickets.add(new FallOfWicket(f));
            }

            this.pastPartnerships = new ArrayList<>();
            for (Partnership p : other.pastPartnerships) {
                this.pastPartnerships.add(new Partnership(p));
            }

            this.currentPartnership = other.currentPartnership != null ? new Partnership(other.currentPartnership) : null;
        }
    }

    public int getInningsNumber() {
        return inningsNumber;
    }

    public void setInningsNumber(int inningsNumber) {
        this.inningsNumber = inningsNumber;
    }

    public Team getBattingTeam() {
        return battingTeam;
    }

    public void setBattingTeam(Team battingTeam) {
        this.battingTeam = battingTeam;
    }

    public Team getBowlingTeam() {
        return bowlingTeam;
    }

    public void setBowlingTeam(Team bowlingTeam) {
        this.bowlingTeam = bowlingTeam;
    }

    public int getAllocatedOvers() {
        return allocatedOvers;
    }

    public void setAllocatedOvers(int allocatedOvers) {
        this.allocatedOvers = allocatedOvers;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(int totalRuns) {
        this.totalRuns = totalRuns;
    }

    public int getWickets() {
        return wickets;
    }

    public void setWickets(int wickets) {
        this.wickets = wickets;
    }

    public int getLegalBalls() {
        return legalBalls;
    }

    public void setLegalBalls(int legalBalls) {
        this.legalBalls = legalBalls;
    }

    public int getWideRuns() {
        return wideRuns;
    }

    public void setWideRuns(int wideRuns) {
        this.wideRuns = wideRuns;
    }

    public int getNoBallRuns() {
        return noBallRuns;
    }

    public void setNoBallRuns(int noBallRuns) {
        this.noBallRuns = noBallRuns;
    }

    public int getByeRuns() {
        return byeRuns;
    }

    public void setByeRuns(int byeRuns) {
        this.byeRuns = byeRuns;
    }

    public int getLegByeRuns() {
        return legByeRuns;
    }

    public void setLegByeRuns(int legByeRuns) {
        this.legByeRuns = legByeRuns;
    }

    public int getPenaltyRuns() {
        return penaltyRuns;
    }

    public void setPenaltyRuns(int penaltyRuns) {
        this.penaltyRuns = penaltyRuns;
    }

    public int getTotalExtras() {
        return wideRuns + noBallRuns + byeRuns + legByeRuns + penaltyRuns;
    }

    public String getCurrentStriker() {
        return currentStriker;
    }

    public void setCurrentStriker(String currentStriker) {
        this.currentStriker = currentStriker;
    }

    public String getCurrentNonStriker() {
        return currentNonStriker;
    }

    public void setCurrentNonStriker(String currentNonStriker) {
        this.currentNonStriker = currentNonStriker;
    }

    public String getCurrentBowler() {
        return currentBowler;
    }

    public void setCurrentBowler(String currentBowler) {
        this.currentBowler = currentBowler;
        if (currentOver != null) {
            currentOver.setBowlerName(currentBowler);
        }
    }

    public String getPreviousBowler() {
        return previousBowler;
    }

    public void setPreviousBowler(String previousBowler) {
        this.previousBowler = previousBowler;
    }

    public int getNextBattingIndex() {
        return nextBattingIndex;
    }

    public void setNextBattingIndex(int nextBattingIndex) {
        this.nextBattingIndex = nextBattingIndex;
    }

    public List<Delivery> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(List<Delivery> deliveries) {
        this.deliveries = deliveries;
    }

    public List<Over> getCompletedOvers() {
        return completedOvers;
    }

    public void setCompletedOvers(List<Over> completedOvers) {
        this.completedOvers = completedOvers;
    }

    public Over getCurrentOver() {
        return currentOver;
    }

    public void setCurrentOver(Over currentOver) {
        this.currentOver = currentOver;
    }

    public List<FallOfWicket> getFallOfWickets() {
        return fallOfWickets;
    }

    public void setFallOfWickets(List<FallOfWicket> fallOfWickets) {
        this.fallOfWickets = fallOfWickets;
    }

    public List<Partnership> getPastPartnerships() {
        return pastPartnerships;
    }

    public void setPastPartnerships(List<Partnership> pastPartnerships) {
        this.pastPartnerships = pastPartnerships;
    }

    public Partnership getCurrentPartnership() {
        return currentPartnership;
    }

    public void setCurrentPartnership(Partnership currentPartnership) {
        this.currentPartnership = currentPartnership;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getCompletionReason() {
        return completionReason;
    }

    public void setCompletionReason(String completionReason) {
        this.completionReason = completionReason;
    }

    // Calculations
    public String getCurrentOversFormatted() {
        int overs = legalBalls / 6;
        int balls = legalBalls % 6;
        return overs + "." + balls;
    }

    public double getCurrentOversDecimal() {
        int overs = legalBalls / 6;
        int balls = legalBalls % 6;
        return overs + (balls / 6.0);
    }

    public double getCurrentRunRate() {
        double overs = getCurrentOversDecimal();
        if (overs <= 0.0) {
            return 0.0;
        }
        return totalRuns / overs;
    }

    public int getRunsRequired() {
        if (target <= 0) return 0;
        return Math.max(0, target - totalRuns);
    }

    public int getBallsRemaining() {
        int totalBalls = allocatedOvers * 6;
        return Math.max(0, totalBalls - legalBalls);
    }

    public double getRequiredRunRate() {
        if (target <= 0) return 0.0;
        int ballsLeft = getBallsRemaining();
        int runsLeft = getRunsRequired();
        if (ballsLeft <= 0) {
            return runsLeft > 0 ? 999.99 : 0.0;
        }
        double oversLeft = ballsLeft / 6.0;
        return runsLeft / oversLeft;
    }

    public boolean isAllOut() {
        if (battingTeam == null || battingTeam.getPlayers() == null) return false;
        int teamSize = battingTeam.getPlayers().size();
        return wickets >= (teamSize - 1);
    }

    public boolean isAllocatedOversCompleted() {
        return legalBalls >= (allocatedOvers * 6);
    }

    public boolean isTargetReached() {
        return target > 0 && totalRuns >= target;
    }

    public void swapStrike() {
        String temp = currentStriker;
        currentStriker = currentNonStriker;
        currentNonStriker = temp;
    }
}
