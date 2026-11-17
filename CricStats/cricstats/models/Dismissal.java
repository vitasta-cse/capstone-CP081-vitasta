package cricstats.models;

import java.io.Serializable;

public class Dismissal implements Serializable {
    private static final long serialVersionUID = 1L;

    private DismissalType type;
    private String dismissedPlayer;
    private String bowler;
    private String fielder; // catcher, stumper, or run-out thrower/fielder
    private String description;

    public Dismissal() {
    }

    public Dismissal(DismissalType type, String dismissedPlayer, String bowler, String fielder) {
        this.type = type;
        this.dismissedPlayer = dismissedPlayer;
        this.bowler = bowler;
        this.fielder = fielder;
        this.description = buildDescription();
    }

    public Dismissal(Dismissal other) {
        if (other != null) {
            this.type = other.type;
            this.dismissedPlayer = other.dismissedPlayer;
            this.bowler = other.bowler;
            this.fielder = other.fielder;
            this.description = other.description;
        }
    }

    private String buildDescription() {
        if (type == null) return "not out";
        switch (type) {
            case BOWLED:
                return "b " + bowler;
            case CAUGHT:
                if (fielder != null && !fielder.trim().isEmpty() && !fielder.equalsIgnoreCase(bowler)) {
                    return "c " + fielder + " b " + bowler;
                } else if (fielder != null && fielder.equalsIgnoreCase(bowler)) {
                    return "c & b " + bowler;
                } else {
                    return "c ? b " + bowler;
                }
            case LBW:
                return "lbw b " + bowler;
            case STUMPED:
                if (fielder != null && !fielder.trim().isEmpty()) {
                    return "st " + fielder + " b " + bowler;
                } else {
                    return "st ? b " + bowler;
                }
            case HIT_WICKET:
                return "hit wicket b " + bowler;
            case RUN_OUT:
                if (fielder != null && !fielder.trim().isEmpty()) {
                    return "run out (" + fielder + ")";
                } else {
                    return "run out";
                }
            case RETIRED_HURT:
                return "retired hurt";
            default:
                return type.name().toLowerCase();
        }
    }

    public DismissalType getType() {
        return type;
    }

    public void setType(DismissalType type) {
        this.type = type;
        this.description = buildDescription();
    }

    public String getDismissedPlayer() {
        return dismissedPlayer;
    }

    public void setDismissedPlayer(String dismissedPlayer) {
        this.dismissedPlayer = dismissedPlayer;
    }

    public String getBowler() {
        return bowler;
    }

    public void setBowler(String bowler) {
        this.bowler = bowler;
        this.description = buildDescription();
    }

    public String getFielder() {
        return fielder;
    }

    public void setFielder(String fielder) {
        this.fielder = fielder;
        this.description = buildDescription();
    }

    public String getDescription() {
        if (description == null || description.isEmpty()) {
            description = buildDescription();
        }
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isBowlerWicket() {
        return type != null && type.isBowlerWicket();
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
