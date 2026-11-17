package cricstats.models;

import java.io.Serializable;

public class Partnership implements Serializable {
    private static final long serialVersionUID = 1L;

    private int wicketNumber; // 1 for 1st wicket, 2 for 2nd, etc.
    private String player1Name;
    private String player2Name;
    private int runs;
    private int balls; // legal balls or all balls in partnership
    private int player1Runs;
    private int player1Balls;
    private int player2Runs;
    private int player2Balls;
    private int extras;
    private boolean isCurrent;

    public Partnership() {
        this.isCurrent = true;
    }

    public Partnership(int wicketNumber, String player1Name, String player2Name) {
        this.wicketNumber = wicketNumber;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.isCurrent = true;
    }

    public Partnership(Partnership other) {
        if (other != null) {
            this.wicketNumber = other.wicketNumber;
            this.player1Name = other.player1Name;
            this.player2Name = other.player2Name;
            this.runs = other.runs;
            this.balls = other.balls;
            this.player1Runs = other.player1Runs;
            this.player1Balls = other.player1Balls;
            this.player2Runs = other.player2Runs;
            this.player2Balls = other.player2Balls;
            this.extras = other.extras;
            this.isCurrent = other.isCurrent;
        }
    }

    public void addDelivery(String strikerName, int runsBat, int extraRuns, boolean countsAsBall) {
        this.runs += (runsBat + extraRuns);
        if (countsAsBall) {
            this.balls++;
        }
        this.extras += extraRuns;

        if (strikerName != null) {
            if (strikerName.equalsIgnoreCase(player1Name)) {
                this.player1Runs += runsBat;
                if (countsAsBall) {
                    this.player1Balls++;
                }
            } else if (strikerName.equalsIgnoreCase(player2Name)) {
                this.player2Runs += runsBat;
                if (countsAsBall) {
                    this.player2Balls++;
                }
            }
        }
    }

    public int getWicketNumber() {
        return wicketNumber;
    }

    public void setWicketNumber(int wicketNumber) {
        this.wicketNumber = wicketNumber;
    }

    public String getPlayer1Name() {
        return player1Name;
    }

    public void setPlayer1Name(String player1Name) {
        this.player1Name = player1Name;
    }

    public String getPlayer2Name() {
        return player2Name;
    }

    public void setPlayer2Name(String player2Name) {
        this.player2Name = player2Name;
    }

    public int getRuns() {
        return runs;
    }

    public void setRuns(int runs) {
        this.runs = runs;
    }

    public int getBalls() {
        return balls;
    }

    public void setBalls(int balls) {
        this.balls = balls;
    }

    public int getPlayer1Runs() {
        return player1Runs;
    }

    public void setPlayer1Runs(int player1Runs) {
        this.player1Runs = player1Runs;
    }

    public int getPlayer1Balls() {
        return player1Balls;
    }

    public void setPlayer1Balls(int player1Balls) {
        this.player1Balls = player1Balls;
    }

    public int getPlayer2Runs() {
        return player2Runs;
    }

    public void setPlayer2Runs(int player2Runs) {
        this.player2Runs = player2Runs;
    }

    public int getPlayer2Balls() {
        return player2Balls;
    }

    public void setPlayer2Balls(int player2Balls) {
        this.player2Balls = player2Balls;
    }

    public int getExtras() {
        return extras;
    }

    public void setExtras(int extras) {
        this.extras = extras;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    @Override
    public String toString() {
        return (wicketNumber > 0 ? wicketNumber + (wicketNumber == 1 ? "st" : wicketNumber == 2 ? "nd" : wicketNumber == 3 ? "rd" : "th") + " Wicket: " : "Partnership: ")
                + runs + " runs (" + balls + " balls) - "
                + player1Name + " " + player1Runs + " (" + player1Balls + "), "
                + player2Name + " " + player2Runs + " (" + player2Balls + "), Extras: " + extras;
    }
}
