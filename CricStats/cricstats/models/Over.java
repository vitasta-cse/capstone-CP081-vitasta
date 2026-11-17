package cricstats.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Over implements Serializable {
    private static final long serialVersionUID = 1L;

    private int overNumber;
    private String bowlerName;
    private List<Delivery> deliveries = new ArrayList<>();
    private int legalBalls;
    private int runsConceded; // charged to bowler
    private int totalRuns;    // includes byes and leg byes
    private int wickets;
    private boolean isMaiden;

    public Over() {
    }

    public Over(int overNumber, String bowlerName) {
        this.overNumber = overNumber;
        this.bowlerName = bowlerName;
    }

    public Over(Over other) {
        if (other != null) {
            this.overNumber = other.overNumber;
            this.bowlerName = other.bowlerName;
            this.legalBalls = other.legalBalls;
            this.runsConceded = other.runsConceded;
            this.totalRuns = other.totalRuns;
            this.wickets = other.wickets;
            this.isMaiden = other.isMaiden;
            this.deliveries = new ArrayList<>();
            for (Delivery d : other.deliveries) {
                this.deliveries.add(new Delivery(d));
            }
        }
    }

    public void addDelivery(Delivery d) {
        deliveries.add(d);
        if (d.isLegal()) {
            legalBalls++;
        }
        runsConceded += d.getRunsConcededBowler();
        totalRuns += d.getTotalRuns();
        if (d.getDismissal() != null && d.getDismissal().isBowlerWicket()) {
            wickets++;
        }
        if (legalBalls == 6 && runsConceded == 0) {
            isMaiden = true;
        } else {
            isMaiden = false;
        }
    }

    public int getOverNumber() {
        return overNumber;
    }

    public void setOverNumber(int overNumber) {
        this.overNumber = overNumber;
    }

    public String getBowlerName() {
        return bowlerName;
    }

    public void setBowlerName(String bowlerName) {
        this.bowlerName = bowlerName;
    }

    public List<Delivery> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(List<Delivery> deliveries) {
        this.deliveries = deliveries;
    }

    public int getLegalBalls() {
        return legalBalls;
    }

    public void setLegalBalls(int legalBalls) {
        this.legalBalls = legalBalls;
    }

    public int getRunsConceded() {
        return runsConceded;
    }

    public void setRunsConceded(int runsConceded) {
        this.runsConceded = runsConceded;
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

    public boolean isMaiden() {
        return isMaiden;
    }

    public void setMaiden(boolean maiden) {
        isMaiden = maiden;
    }

    public String getOverSummaryString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < deliveries.size(); i++) {
            if (i > 0) sb.append(" ");
            sb.append(deliveries.get(i).getShortNotation());
        }
        return sb.toString();
    }
}
