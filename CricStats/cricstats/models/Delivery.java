package cricstats.models;

import java.io.Serializable;

public class Delivery implements Serializable {
    private static final long serialVersionUID = 1L;

    private int deliveryIndex;
    private int overNumber;       // 1-based (Over 1, Over 2, ...)
    private int legalBallNumber;  // 1 to 6 in this over (if legal)
    private String displayOverBall; // e.g. "0.1", "0.2 wd", "0.2"
    private String striker;
    private String nonStriker;
    private String bowler;
    private int runsBat;
    private ExtraType extraType;
    private int extraRuns;
    private boolean isLegal;
    private Dismissal dismissal;
    private int runsConcededBowler;
    private int totalRuns;
    private boolean strikeRotated;
    private String commentary;

    public Delivery() {
        this.extraType = ExtraType.NONE;
    }

    public Delivery(Delivery other) {
        if (other != null) {
            this.deliveryIndex = other.deliveryIndex;
            this.overNumber = other.overNumber;
            this.legalBallNumber = other.legalBallNumber;
            this.displayOverBall = other.displayOverBall;
            this.striker = other.striker;
            this.nonStriker = other.nonStriker;
            this.bowler = other.bowler;
            this.runsBat = other.runsBat;
            this.extraType = other.extraType;
            this.extraRuns = other.extraRuns;
            this.isLegal = other.isLegal;
            this.dismissal = other.dismissal != null ? new Dismissal(other.dismissal) : null;
            this.runsConcededBowler = other.runsConcededBowler;
            this.totalRuns = other.totalRuns;
            this.strikeRotated = other.strikeRotated;
            this.commentary = other.commentary;
        }
    }

    public int getDeliveryIndex() {
        return deliveryIndex;
    }

    public void setDeliveryIndex(int deliveryIndex) {
        this.deliveryIndex = deliveryIndex;
    }

    public int getOverNumber() {
        return overNumber;
    }

    public void setOverNumber(int overNumber) {
        this.overNumber = overNumber;
    }

    public int getLegalBallNumber() {
        return legalBallNumber;
    }

    public void setLegalBallNumber(int legalBallNumber) {
        this.legalBallNumber = legalBallNumber;
    }

    public String getDisplayOverBall() {
        return displayOverBall;
    }

    public void setDisplayOverBall(String displayOverBall) {
        this.displayOverBall = displayOverBall;
    }

    public String getStriker() {
        return striker;
    }

    public void setStriker(String striker) {
        this.striker = striker;
    }

    public String getNonStriker() {
        return nonStriker;
    }

    public void setNonStriker(String nonStriker) {
        this.nonStriker = nonStriker;
    }

    public String getBowler() {
        return bowler;
    }

    public void setBowler(String bowler) {
        this.bowler = bowler;
    }

    public int getRunsBat() {
        return runsBat;
    }

    public void setRunsBat(int runsBat) {
        this.runsBat = runsBat;
    }

    public ExtraType getExtraType() {
        return extraType;
    }

    public void setExtraType(ExtraType extraType) {
        this.extraType = extraType;
    }

    public int getExtraRuns() {
        return extraRuns;
    }

    public void setExtraRuns(int extraRuns) {
        this.extraRuns = extraRuns;
    }

    public boolean isLegal() {
        return isLegal;
    }

    public void setLegal(boolean legal) {
        isLegal = legal;
    }

    public Dismissal getDismissal() {
        return dismissal;
    }

    public void setDismissal(Dismissal dismissal) {
        this.dismissal = dismissal;
    }

    public int getRunsConcededBowler() {
        return runsConcededBowler;
    }

    public void setRunsConcededBowler(int runsConcededBowler) {
        this.runsConcededBowler = runsConcededBowler;
    }

    public int getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(int totalRuns) {
        this.totalRuns = totalRuns;
    }

    public boolean isStrikeRotated() {
        return strikeRotated;
    }

    public void setStrikeRotated(boolean strikeRotated) {
        this.strikeRotated = strikeRotated;
    }

    public String getCommentary() {
        return commentary;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public String getShortNotation() {
        if (dismissal != null) {
            if (runsBat > 0) {
                return "W+" + runsBat;
            }
            return "W";
        }
        if (extraType == ExtraType.WIDE) {
            return extraRuns > 1 ? extraRuns + "wd" : "wd";
        }
        if (extraType == ExtraType.NO_BALL) {
            if (runsBat > 0) {
                return (runsBat + 1) + "nb";
            }
            return extraRuns > 1 ? extraRuns + "nb" : "nb";
        }
        if (extraType == ExtraType.BYE) {
            return extraRuns + "b";
        }
        if (extraType == ExtraType.LEG_BYE) {
            return extraRuns + "lb";
        }
        if (runsBat == 4) return "4";
        if (runsBat == 6) return "6";
        if (runsBat == 0) return ".";
        return String.valueOf(runsBat);
    }

    @Override
    public String toString() {
        return (displayOverBall != null ? displayOverBall : "") + " " + bowler + " to " + striker + ": " + getShortNotation();
    }
}
