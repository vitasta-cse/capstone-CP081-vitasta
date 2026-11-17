package cricstats.models;

public enum DismissalType {
    BOWLED("b", true),
    CAUGHT("c", true),
    LBW("lbw b", true),
    STUMPED("st", true),
    HIT_WICKET("hit wicket b", true),
    RUN_OUT("run out", false),
    RETIRED_HURT("retired hurt", false);

    private final String prefix;
    private final boolean isBowlerWicket;

    DismissalType(String prefix, boolean isBowlerWicket) {
        this.prefix = prefix;
        this.isBowlerWicket = isBowlerWicket;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isBowlerWicket() {
        return isBowlerWicket;
    }
}
