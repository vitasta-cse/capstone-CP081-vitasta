package cricstats.ui;

import cricstats.models.*;

import java.util.Locale;

public class ScorecardFormatter {

    private static final String LINE_SEP_HEAVY = "=========================================================================================";
    private static final String LINE_SEP_LIGHT = "-----------------------------------------------------------------------------------------";

    // 1. Live Match Banner
    public static String formatLiveBanner(Match match) {
        if (match == null) {
            return "No active match.";
        }
        Innings inn = match.getCurrentInnings();
        if (inn == null) {
            return "Match created (" + match.getTeam1().getName() + " vs " + match.getTeam2().getName() + "), innings not started.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(LINE_SEP_HEAVY).append("\n");
        sb.append(String.format(" %-20s  |  INNINGS %d  |  %s\n",
                match.getTeam1().getName() + " vs " + match.getTeam2().getName(),
                inn.getInningsNumber(),
                match.getStatus()));
        sb.append(LINE_SEP_LIGHT).append("\n");

        sb.append(String.format(" BATTING: %-15s   SCORE: %d/%d   OVERS: %s / %d   CRR: %.2f\n",
                inn.getBattingTeam().getName(),
                inn.getTotalRuns(),
                inn.getWickets(),
                inn.getCurrentOversFormatted(),
                inn.getAllocatedOvers(),
                inn.getCurrentRunRate()));

        if (inn.getInningsNumber() == 2 && inn.getTarget() > 0) {
            sb.append(String.format(" TARGET:  %-15d   NEED:  %d runs from %d balls   RRR: %.2f\n",
                    inn.getTarget(),
                    inn.getRunsRequired(),
                    inn.getBallsRemaining(),
                    inn.getRequiredRunRate()));
        }

        sb.append(LINE_SEP_LIGHT).append("\n");

        // Striker & Non-Striker
        Player striker = inn.getBattingTeam().getPlayer(inn.getCurrentStriker());
        Player nonStriker = inn.getBattingTeam().getPlayer(inn.getCurrentNonStriker());
        Player bowler = inn.getBowlingTeam().getPlayer(inn.getCurrentBowler());

        if (striker != null) {
            sb.append(String.format(" *Striker:     %-16s %3d runs (%3db, %2dx4, %2dx6)  SR: %6.2f\n",
                    striker.getName(), striker.getRunsScored(), striker.getBallsFaced(),
                    striker.getFours(), striker.getSixes(), striker.getStrikeRate()));
        }
        if (nonStriker != null) {
            sb.append(String.format("  Non-Striker: %-16s %3d runs (%3db, %2dx4, %2dx6)  SR: %6.2f\n",
                    nonStriker.getName(), nonStriker.getRunsScored(), nonStriker.getBallsFaced(),
                    nonStriker.getFours(), nonStriker.getSixes(), nonStriker.getStrikeRate()));
        }
        if (bowler != null) {
            sb.append(String.format("  Bowler:      %-16s %s ov, %d mdns, %d runs, %d wkts  Econ: %5.2f\n",
                    bowler.getName(), bowler.getOversBowledString(), bowler.getMaidens(),
                    bowler.getRunsConceded(), bowler.getWickets(), bowler.getEconomyRate()));
        }

        // Current Over Balls
        if (inn.getCurrentOver() != null && !inn.getCurrentOver().getDeliveries().isEmpty()) {
            sb.append("  This Over:   ").append(inn.getCurrentOver().getOverSummaryString()).append("\n");
        }

        // Current Partnership
        if (inn.getCurrentPartnership() != null) {
            Partnership p = inn.getCurrentPartnership();
            sb.append(String.format("  Partnership: %d runs (%d balls) [ %s: %d, %s: %d, Ext: %d ]\n",
                    p.getRuns(), p.getBalls(),
                    p.getPlayer1Name(), p.getPlayer1Runs(),
                    p.getPlayer2Name(), p.getPlayer2Runs(),
                    p.getExtras()));
        }

        sb.append(LINE_SEP_HEAVY);
        return sb.toString();
    }

    // 2. Batting Scorecard
    public static String formatBattingScorecard(Innings inn) {
        if (inn == null) return "No innings data.";
        StringBuilder sb = new StringBuilder();
        sb.append(LINE_SEP_HEAVY).append("\n");
        sb.append(" BATTING SCORECARD - ").append(inn.getBattingTeam().getName().toUpperCase()).append("\n");
        sb.append(LINE_SEP_LIGHT).append("\n");
        sb.append(String.format(" %-22s %-28s %5s %5s %4s %4s %7s\n",
                "Batter", "Dismissal", "R", "B", "4s", "6s", "SR"));
        sb.append(LINE_SEP_LIGHT).append("\n");

        for (String pName : inn.getBattingTeam().getBattingOrder()) {
            Player p = inn.getBattingTeam().getPlayer(pName);
            if (p != null) {
                String namePrefix = "";
                if (p.getName().equalsIgnoreCase(inn.getCurrentStriker()) && !p.isOut()) {
                    namePrefix = "*";
                }
                String displayName = namePrefix + p.getName();
                if (displayName.length() > 22) displayName = displayName.substring(0, 22);

                String disStr = p.getDismissalString();
                if (disStr.length() > 28) disStr = disStr.substring(0, 28);

                sb.append(String.format(Locale.US, " %-22s %-28s %5d %5d %4d %4d %7.2f\n",
                        displayName, disStr, p.getRunsScored(), p.getBallsFaced(),
                        p.getFours(), p.getSixes(), p.getStrikeRate()));
            }
        }
        sb.append(LINE_SEP_LIGHT).append("\n");

        // Extras
        sb.append(String.format(" Extras: %-3d (wd %d, nb %d, b %d, lb %d, pen %d)\n",
                inn.getTotalExtras(), inn.getWideRuns(), inn.getNoBallRuns(),
                inn.getByeRuns(), inn.getLegByeRuns(), inn.getPenaltyRuns()));

        // Total
        sb.append(String.format(Locale.US, " TOTAL:  %-3d / %d (%s Overs, RR: %.2f)\n",
                inn.getTotalRuns(), inn.getWickets(), inn.getCurrentOversFormatted(), inn.getCurrentRunRate()));
        sb.append(LINE_SEP_HEAVY);
        return sb.toString();
    }

    // 3. Bowling Scorecard
    public static String formatBowlingScorecard(Innings inn) {
        if (inn == null) return "No innings data.";
        StringBuilder sb = new StringBuilder();
        sb.append(LINE_SEP_HEAVY).append("\n");
        sb.append(" BOWLING SCORECARD - ").append(inn.getBowlingTeam().getName().toUpperCase()).append("\n");
        sb.append(LINE_SEP_LIGHT).append("\n");
        sb.append(String.format(" %-22s %6s %4s %5s %4s %7s %4s %4s\n",
                "Bowler", "O", "M", "R", "W", "Econ", "WD", "NB"));
        sb.append(LINE_SEP_LIGHT).append("\n");

        boolean anyBowled = false;
        for (Player p : inn.getBowlingTeam().getPlayers()) {
            if (p.getLegalBallsBowled() > 0 || p.getRunsConceded() > 0 || p.getName().equalsIgnoreCase(inn.getCurrentBowler())) {
                anyBowled = true;
                String namePrefix = p.getName().equalsIgnoreCase(inn.getCurrentBowler()) ? "*" : "";
                String displayName = namePrefix + p.getName();
                if (displayName.length() > 22) displayName = displayName.substring(0, 22);

                sb.append(String.format(Locale.US, " %-22s %6s %4d %5d %4d %7.2f %4d %4d\n",
                        displayName, p.getOversBowledString(), p.getMaidens(),
                        p.getRunsConceded(), p.getWickets(), p.getEconomyRate(),
                        p.getWidesConceded(), p.getNoBallsConceded()));
            }
        }

        if (!anyBowled) {
            sb.append(" (No bowling statistics recorded yet)\n");
        }

        sb.append(LINE_SEP_HEAVY);
        return sb.toString();
    }

    // 4. Fall of Wickets
    public static String formatFallOfWickets(Innings inn) {
        if (inn == null) return "No innings data.";
        StringBuilder sb = new StringBuilder();
        sb.append(LINE_SEP_HEAVY).append("\n");
        sb.append(" FALL OF WICKETS - ").append(inn.getBattingTeam().getName().toUpperCase()).append("\n");
        sb.append(LINE_SEP_LIGHT).append("\n");

        if (inn.getFallOfWickets().isEmpty()) {
            sb.append(" No wickets have fallen yet.\n");
        } else {
            sb.append(String.format(" %-6s %-12s %-24s %-10s %-16s\n",
                    "Wkt #", "Score", "Dismissed Batter", "Over", "Partnership"));
            sb.append(LINE_SEP_LIGHT).append("\n");
            for (FallOfWicket f : inn.getFallOfWickets()) {
                String partStr = f.getPartnershipRuns() + " r (" + f.getPartnershipBalls() + " b)";
                sb.append(String.format(" %-6d %-12s %-24s %-10s %-16s\n",
                        f.getWicketNumber(),
                        f.getTeamScore() + "/" + f.getWicketNumber(),
                        f.getDismissedPlayerName(),
                        f.getOversFormatted(),
                        partStr));
            }
        }
        sb.append(LINE_SEP_HEAVY);
        return sb.toString();
    }

    // 5. Partnership Statistics
    public static String formatPartnerships(Innings inn) {
        if (inn == null) return "No innings data.";
        StringBuilder sb = new StringBuilder();
        sb.append(LINE_SEP_HEAVY).append("\n");
        sb.append(" PARTNERSHIP STATISTICS - ").append(inn.getBattingTeam().getName().toUpperCase()).append("\n");
        sb.append(LINE_SEP_LIGHT).append("\n");

        sb.append(String.format(" %-10s %-8s %-8s %-26s %-26s\n",
                "Wicket", "Runs", "Balls", "Batter 1", "Batter 2"));
        sb.append(LINE_SEP_LIGHT).append("\n");

        for (Partnership p : inn.getPastPartnerships()) {
            String b1 = p.getPlayer1Name() + " (" + p.getPlayer1Runs() + " off " + p.getPlayer1Balls() + "b)";
            String b2 = p.getPlayer2Name() + " (" + p.getPlayer2Runs() + " off " + p.getPlayer2Balls() + "b)";
            sb.append(String.format(" %-10s %-8d %-8d %-26s %-26s\n",
                    p.getWicketNumber() + ordinal(p.getWicketNumber()),
                    p.getRuns(), p.getBalls(), b1, b2));
        }

        if (inn.getCurrentPartnership() != null && !inn.isCompleted()) {
            Partnership cp = inn.getCurrentPartnership();
            String b1 = cp.getPlayer1Name() + " (" + cp.getPlayer1Runs() + " off " + cp.getPlayer1Balls() + "b)";
            String b2 = cp.getPlayer2Name() + " (" + cp.getPlayer2Runs() + " off " + cp.getPlayer2Balls() + "b)";
            sb.append(String.format(" %-10s %-8d %-8d %-26s %-26s\n",
                    cp.getWicketNumber() + ordinal(cp.getWicketNumber()) + "*",
                    cp.getRuns(), cp.getBalls(), b1, b2));
        }

        sb.append(LINE_SEP_HEAVY);
        return sb.toString();
    }

    private static String ordinal(int i) {
        int mod100 = i % 100;
        int mod10 = i % 10;
        if (mod100 - mod10 == 10) {
            return "th";
        }
        switch (mod10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }

    // 6. Complete Match Summary
    public static String formatMatchSummary(Match match) {
        if (match == null) return "No match loaded.";
        StringBuilder sb = new StringBuilder();
        sb.append(LINE_SEP_HEAVY).append("\n");
        sb.append(" CRICSTATS COMPLETE MATCH SUMMARY\n");
        sb.append(" Match ID: ").append(match.getMatchId()).append("  |  Date: ").append(match.getDate()).append("\n");
        sb.append(" Teams:    ").append(match.getTeam1().getName()).append(" vs ").append(match.getTeam2().getName()).append("\n");
        sb.append(" Format:   ").append(match.getAllocatedOvers()).append(" Overs per innings\n");
        if (match.getTossWinner() != null && !match.getTossWinner().isEmpty()) {
            sb.append(" Toss:     ").append(match.getTossWinner()).append(" won the toss and elected to ")
                    .append(match.getTossDecision()).append(" first\n");
        }
        sb.append(LINE_SEP_HEAVY).append("\n\n");

        if (match.getFirstInnings() != null) {
            sb.append("=== FIRST INNINGS ===\n");
            sb.append(formatBattingScorecard(match.getFirstInnings())).append("\n\n");
            sb.append(formatBowlingScorecard(match.getFirstInnings())).append("\n\n");
            sb.append(formatFallOfWickets(match.getFirstInnings())).append("\n\n");
        }

        if (match.getSecondInnings() != null) {
            sb.append("=== SECOND INNINGS ===\n");
            sb.append(formatBattingScorecard(match.getSecondInnings())).append("\n\n");
            sb.append(formatBowlingScorecard(match.getSecondInnings())).append("\n\n");
            sb.append(formatFallOfWickets(match.getSecondInnings())).append("\n\n");
        }

        sb.append(LINE_SEP_HEAVY).append("\n");
        sb.append(" RESULT: ").append(match.getResult() != null ? match.getResult() : "Match in progress (" + match.getStatus() + ")").append("\n");

        if (match.getPlayerOfTheMatch() != null && !match.getPlayerOfTheMatch().isEmpty()) {
            sb.append(" PLAYER OF THE MATCH: ").append(match.getPlayerOfTheMatch()).append("\n");
            sb.append(" Performance: ").append(match.getPlayerOfTheMatchReason()).append("\n");
        }
        sb.append(LINE_SEP_HEAVY);

        return sb.toString();
    }
}
