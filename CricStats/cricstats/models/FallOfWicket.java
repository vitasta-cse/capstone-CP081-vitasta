package cricstats.models;

import java.io.Serializable;

public class FallOfWicket implements Serializable {
    private static final long serialVersionUID = 1L;

    private int wicketNumber;
    private String dismissedPlayerName;
    private int teamScore;
    private String oversFormatted;
    private int partnershipRuns;
    private int partnershipBalls;

    public FallOfWicket() {
    }

    public FallOfWicket(int wicketNumber, String dismissedPlayerName, int teamScore,
                        String oversFormatted, int partnershipRuns, int partnershipBalls) {
        this.wicketNumber = wicketNumber;
        this.dismissedPlayerName = dismissedPlayerName;
        this.teamScore = teamScore;
        this.oversFormatted = oversFormatted;
        this.partnershipRuns = partnershipRuns;
        this.partnershipBalls = partnershipBalls;
    }

    public FallOfWicket(FallOfWicket other) {
        if (other != null) {
            this.wicketNumber = other.wicketNumber;
            this.dismissedPlayerName = other.dismissedPlayerName;
            this.teamScore = other.teamScore;
            this.oversFormatted = other.oversFormatted;
            this.partnershipRuns = other.partnershipRuns;
            this.partnershipBalls = other.partnershipBalls;
        }
    }

    public int getWicketNumber() {
        return wicketNumber;
    }

    public void setWicketNumber(int wicketNumber) {
        this.wicketNumber = wicketNumber;
    }

    public String getDismissedPlayerName() {
        return dismissedPlayerName;
    }

    public void setDismissedPlayerName(String dismissedPlayerName) {
        this.dismissedPlayerName = dismissedPlayerName;
    }

    public int getTeamScore() {
        return teamScore;
    }

    public void setTeamScore(int teamScore) {
        this.teamScore = teamScore;
    }

    public String getOversFormatted() {
        return oversFormatted;
    }

    public void setOversFormatted(String oversFormatted) {
        this.oversFormatted = oversFormatted;
    }

    public int getPartnershipRuns() {
        return partnershipRuns;
    }

    public void setPartnershipRuns(int partnershipRuns) {
        this.partnershipRuns = partnershipRuns;
    }

    public int getPartnershipBalls() {
        return partnershipBalls;
    }

    public void setPartnershipBalls(int partnershipBalls) {
        this.partnershipBalls = partnershipBalls;
    }

    @Override
    public String toString() {
        return wicketNumber + "-" + teamScore + " (" + dismissedPlayerName + ", " + oversFormatted + " ov)";
    }
}
