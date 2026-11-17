package cricstats.models;

import java.io.Serializable;

public class PlayerOfMatchConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    // Configurable weights as per requirements:
    // - 1 point per run
    // - 1 bonus point per four
    // - 2 bonus points per six
    // - 20 points per wicket
    // - 1 point per maiden over
    // - 5 bonus points for a half-century
    // - 10 bonus points for a century
    private int pointsPerRun = 1;
    private int bonusPerFour = 1;
    private int bonusPerSix = 2;
    private int pointsPerWicket = 20;
    private int pointsPerMaiden = 1;
    private int bonusHalfCentury = 5;
    private int bonusCentury = 10;
    private int pointsPerCatch = 5;
    private int pointsPerRunOut = 5;
    private int pointsPerStumping = 5;

    public PlayerOfMatchConfig() {
    }

    public PlayerOfMatchConfig(PlayerOfMatchConfig other) {
        if (other != null) {
            this.pointsPerRun = other.pointsPerRun;
            this.bonusPerFour = other.bonusPerFour;
            this.bonusPerSix = other.bonusPerSix;
            this.pointsPerWicket = other.pointsPerWicket;
            this.pointsPerMaiden = other.pointsPerMaiden;
            this.bonusHalfCentury = other.bonusHalfCentury;
            this.bonusCentury = other.bonusCentury;
            this.pointsPerCatch = other.pointsPerCatch;
            this.pointsPerRunOut = other.pointsPerRunOut;
            this.pointsPerStumping = other.pointsPerStumping;
        }
    }

    public int getPointsPerRun() { return pointsPerRun; }
    public void setPointsPerRun(int pointsPerRun) { this.pointsPerRun = pointsPerRun; }

    public int getBonusPerFour() { return bonusPerFour; }
    public void setBonusPerFour(int bonusPerFour) { this.bonusPerFour = bonusPerFour; }

    public int getBonusPerSix() { return bonusPerSix; }
    public void setBonusPerSix(int bonusPerSix) { this.bonusPerSix = bonusPerSix; }

    public int getPointsPerWicket() { return pointsPerWicket; }
    public void setPointsPerWicket(int pointsPerWicket) { this.pointsPerWicket = pointsPerWicket; }

    public int getPointsPerMaiden() { return pointsPerMaiden; }
    public void setPointsPerMaiden(int pointsPerMaiden) { this.pointsPerMaiden = pointsPerMaiden; }

    public int getBonusHalfCentury() { return bonusHalfCentury; }
    public void setBonusHalfCentury(int bonusHalfCentury) { this.bonusHalfCentury = bonusHalfCentury; }

    public int getBonusCentury() { return bonusCentury; }
    public void setBonusCentury(int bonusCentury) { this.bonusCentury = bonusCentury; }

    public int getPointsPerCatch() { return pointsPerCatch; }
    public void setPointsPerCatch(int pointsPerCatch) { this.pointsPerCatch = pointsPerCatch; }

    public int getPointsPerRunOut() { return pointsPerRunOut; }
    public void setPointsPerRunOut(int pointsPerRunOut) { this.pointsPerRunOut = pointsPerRunOut; }

    public int getPointsPerStumping() { return pointsPerStumping; }
    public void setPointsPerStumping(int pointsPerStumping) { this.pointsPerStumping = pointsPerStumping; }
}
