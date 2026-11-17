package cricstats.storage;

import cricstats.models.*;
import cricstats.storage.JsonHelper.JsonValue;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MatchStorage {

    // Save match to JSON
    public static void saveToJson(Match match, File file) throws IOException {
        String json = matchToJsonString(match);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(json);
        }
    }

    // Load match from JSON
    public static Match loadFromJson(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        JsonValue root = JsonHelper.parse(sb.toString());
        return jsonValueToMatch(root);
    }

    // Export match summary & ball-by-ball to CSV
    public static void saveToCsv(Match match, File file) throws IOException {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            pw.println("MATCH DETAILS");
            pw.println("Match ID," + escapeCsv(match.getMatchId()));
            pw.println("Date," + escapeCsv(match.getDate()));
            pw.println("Team 1," + escapeCsv(match.getTeam1() != null ? match.getTeam1().getName() : ""));
            pw.println("Team 2," + escapeCsv(match.getTeam2() != null ? match.getTeam2().getName() : ""));
            pw.println("Allocated Overs," + match.getAllocatedOvers());
            pw.println("Toss Winner," + escapeCsv(match.getTossWinner()));
            pw.println("Toss Decision," + escapeCsv(match.getTossDecision()));
            pw.println("Status," + match.getStatus());
            pw.println("Result," + escapeCsv(match.getResult() != null ? match.getResult() : "In Progress"));
            pw.println("Player of the Match," + escapeCsv(match.getPlayerOfTheMatch() != null ? match.getPlayerOfTheMatch() : ""));
            pw.println();

            // Export First Innings
            if (match.getFirstInnings() != null) {
                exportInningsCsv(pw, "FIRST INNINGS", match.getFirstInnings());
            }

            // Export Second Innings
            if (match.getSecondInnings() != null) {
                exportInningsCsv(pw, "SECOND INNINGS", match.getSecondInnings());
            }
        }
    }

    private static void exportInningsCsv(PrintWriter pw, String title, Innings inn) {
        pw.println(title + " - " + escapeCsv(inn.getBattingTeam().getName()) + " vs " + escapeCsv(inn.getBowlingTeam().getName()));
        pw.println("Total Score," + inn.getTotalRuns() + "/" + inn.getWickets() + " in " + inn.getCurrentOversFormatted() + " overs");
        pw.println("Extras," + inn.getTotalExtras() + " (wd " + inn.getWideRuns() + ", nb " + inn.getNoBallRuns() + ", b " + inn.getByeRuns() + ", lb " + inn.getLegByeRuns() + ")");
        pw.println();

        pw.println("BATTING SCORECARD");
        pw.println("Batsman,Dismissal,Runs,Balls,4s,6s,Strike Rate");
        for (Player p : inn.getBattingTeam().getPlayers()) {
            if (p.isHasBatted()) {
                pw.println(escapeCsv(p.getName()) + ","
                        + escapeCsv(p.getDismissalString()) + ","
                        + p.getRunsScored() + ","
                        + p.getBallsFaced() + ","
                        + p.getFours() + ","
                        + p.getSixes() + ","
                        + String.format(Locale.US, "%.2f", p.getStrikeRate()));
            }
        }
        pw.println();

        pw.println("BOWLING SCORECARD");
        pw.println("Bowler,Overs,Maidens,Runs,Wickets,Economy");
        for (Player p : inn.getBowlingTeam().getPlayers()) {
            if (p.getLegalBallsBowled() > 0 || p.getRunsConceded() > 0) {
                pw.println(escapeCsv(p.getName()) + ","
                        + p.getOversBowledString() + ","
                        + p.getMaidens() + ","
                        + p.getRunsConceded() + ","
                        + p.getWickets() + ","
                        + String.format(Locale.US, "%.2f", p.getEconomyRate()));
            }
        }
        pw.println();

        pw.println("FALL OF WICKETS");
        pw.println("Wicket,Score,Dismissed Player,Over,Partnership Runs,Partnership Balls");
        for (FallOfWicket f : inn.getFallOfWickets()) {
            pw.println(f.getWicketNumber() + ","
                    + f.getTeamScore() + ","
                    + escapeCsv(f.getDismissedPlayerName()) + ","
                    + f.getOversFormatted() + ","
                    + f.getPartnershipRuns() + ","
                    + f.getPartnershipBalls());
        }
        pw.println();

        pw.println("DELIVERY LOG");
        pw.println("Ball,Bowler,Striker,NonStriker,RunsBat,ExtraType,ExtraRuns,TotalRuns,Dismissal");
        for (Delivery d : inn.getDeliveries()) {
            pw.println(d.getDisplayOverBall() + ","
                    + escapeCsv(d.getBowler()) + ","
                    + escapeCsv(d.getStriker()) + ","
                    + escapeCsv(d.getNonStriker()) + ","
                    + d.getRunsBat() + ","
                    + d.getExtraType() + ","
                    + d.getExtraRuns() + ","
                    + d.getTotalRuns() + ","
                    + escapeCsv(d.getDismissal() != null ? d.getDismissal().getDescription() : ""));
        }
        pw.println();
    }

    private static String escapeCsv(String text) {
        if (text == null) return "";
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    // Match to JSON Serializer
    public static String matchToJsonString(Match match) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"matchId\": ").append(JsonHelper.escape(match.getMatchId())).append(",\n");
        sb.append("  \"date\": ").append(JsonHelper.escape(match.getDate())).append(",\n");
        sb.append("  \"allocatedOvers\": ").append(match.getAllocatedOvers()).append(",\n");
        sb.append("  \"tossWinner\": ").append(JsonHelper.escape(match.getTossWinner())).append(",\n");
        sb.append("  \"tossDecision\": ").append(JsonHelper.escape(match.getTossDecision())).append(",\n");
        sb.append("  \"currentInningsNumber\": ").append(match.getCurrentInningsNumber()).append(",\n");
        sb.append("  \"status\": ").append(JsonHelper.escape(match.getStatus().name())).append(",\n");
        sb.append("  \"result\": ").append(JsonHelper.escape(match.getResult())).append(",\n");
        sb.append("  \"playerOfTheMatch\": ").append(JsonHelper.escape(match.getPlayerOfTheMatch())).append(",\n");
        sb.append("  \"playerOfTheMatchReason\": ").append(JsonHelper.escape(match.getPlayerOfTheMatchReason())).append(",\n");
        sb.append("  \"playerOfTheMatchPoints\": ").append(match.getPlayerOfTheMatchPoints()).append(",\n");

        // POM Config
        sb.append("  \"pomConfig\": {\n");
        PlayerOfMatchConfig cfg = match.getPomConfig();
        sb.append("    \"pointsPerRun\": ").append(cfg.getPointsPerRun()).append(",\n");
        sb.append("    \"bonusPerFour\": ").append(cfg.getBonusPerFour()).append(",\n");
        sb.append("    \"bonusPerSix\": ").append(cfg.getBonusPerSix()).append(",\n");
        sb.append("    \"pointsPerWicket\": ").append(cfg.getPointsPerWicket()).append(",\n");
        sb.append("    \"pointsPerMaiden\": ").append(cfg.getPointsPerMaiden()).append(",\n");
        sb.append("    \"bonusHalfCentury\": ").append(cfg.getBonusHalfCentury()).append(",\n");
        sb.append("    \"bonusCentury\": ").append(cfg.getBonusCentury()).append("\n");
        sb.append("  },\n");

        // Team 1
        sb.append("  \"team1\": ");
        appendTeamJson(sb, match.getTeam1(), "  ");
        sb.append(",\n");

        // Team 2
        sb.append("  \"team2\": ");
        appendTeamJson(sb, match.getTeam2(), "  ");
        sb.append(",\n");

        // First Innings
        sb.append("  \"firstInnings\": ");
        if (match.getFirstInnings() != null) {
            appendInningsJson(sb, match.getFirstInnings(), "  ");
        } else {
            sb.append("null");
        }
        sb.append(",\n");

        // Second Innings
        sb.append("  \"secondInnings\": ");
        if (match.getSecondInnings() != null) {
            appendInningsJson(sb, match.getSecondInnings(), "  ");
        } else {
            sb.append("null");
        }
        sb.append("\n");

        sb.append("}");
        return sb.toString();
    }

    private static void appendTeamJson(StringBuilder sb, Team team, String indent) {
        if (team == null) {
            sb.append("null");
            return;
        }
        sb.append("{\n");
        sb.append(indent).append("  \"name\": ").append(JsonHelper.escape(team.getName())).append(",\n");

        // Batting order
        sb.append(indent).append("  \"battingOrder\": [");
        for (int i = 0; i < team.getBattingOrder().size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(JsonHelper.escape(team.getBattingOrder().get(i)));
        }
        sb.append("],\n");

        // Players
        sb.append(indent).append("  \"players\": [\n");
        for (int i = 0; i < team.getPlayers().size(); i++) {
            Player p = team.getPlayers().get(i);
            appendPlayerJson(sb, p, indent + "    ");
            if (i < team.getPlayers().size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(indent).append("  ]\n");
        sb.append(indent).append("}");
    }

    private static void appendPlayerJson(StringBuilder sb, Player p, String indent) {
        sb.append(indent).append("{\n");
        sb.append(indent).append("  \"name\": ").append(JsonHelper.escape(p.getName())).append(",\n");
        sb.append(indent).append("  \"runsScored\": ").append(p.getRunsScored()).append(",\n");
        sb.append(indent).append("  \"ballsFaced\": ").append(p.getBallsFaced()).append(",\n");
        sb.append(indent).append("  \"fours\": ").append(p.getFours()).append(",\n");
        sb.append(indent).append("  \"sixes\": ").append(p.getSixes()).append(",\n");
        sb.append(indent).append("  \"hasBatted\": ").append(p.isHasBatted()).append(",\n");
        sb.append(indent).append("  \"isOut\": ").append(p.isOut()).append(",\n");
        sb.append(indent).append("  \"legalBallsBowled\": ").append(p.getLegalBallsBowled()).append(",\n");
        sb.append(indent).append("  \"maidens\": ").append(p.getMaidens()).append(",\n");
        sb.append(indent).append("  \"runsConceded\": ").append(p.getRunsConceded()).append(",\n");
        sb.append(indent).append("  \"wickets\": ").append(p.getWickets()).append(",\n");
        sb.append(indent).append("  \"widesConceded\": ").append(p.getWidesConceded()).append(",\n");
        sb.append(indent).append("  \"noBallsConceded\": ").append(p.getNoBallsConceded()).append(",\n");
        sb.append(indent).append("  \"catches\": ").append(p.getCatches()).append(",\n");
        sb.append(indent).append("  \"runOuts\": ").append(p.getRunOuts()).append(",\n");
        sb.append(indent).append("  \"stumpings\": ").append(p.getStumpings()).append(",\n");
        sb.append(indent).append("  \"dismissal\": ");
        if (p.getDismissal() != null) {
            appendDismissalJson(sb, p.getDismissal());
        } else {
            sb.append("null");
        }
        sb.append("\n").append(indent).append("}");
    }

    private static void appendDismissalJson(StringBuilder sb, Dismissal d) {
        sb.append("{");
        sb.append("\"type\": ").append(JsonHelper.escape(d.getType().name())).append(", ");
        sb.append("\"dismissedPlayer\": ").append(JsonHelper.escape(d.getDismissedPlayer())).append(", ");
        sb.append("\"bowler\": ").append(JsonHelper.escape(d.getBowler())).append(", ");
        sb.append("\"fielder\": ").append(JsonHelper.escape(d.getFielder())).append(", ");
        sb.append("\"description\": ").append(JsonHelper.escape(d.getDescription()));
        sb.append("}");
    }

    private static void appendInningsJson(StringBuilder sb, Innings inn, String indent) {
        sb.append("{\n");
        sb.append(indent).append("  \"inningsNumber\": ").append(inn.getInningsNumber()).append(",\n");
        sb.append(indent).append("  \"battingTeamName\": ").append(JsonHelper.escape(inn.getBattingTeam().getName())).append(",\n");
        sb.append(indent).append("  \"bowlingTeamName\": ").append(JsonHelper.escape(inn.getBowlingTeam().getName())).append(",\n");
        sb.append(indent).append("  \"allocatedOvers\": ").append(inn.getAllocatedOvers()).append(",\n");
        sb.append(indent).append("  \"target\": ").append(inn.getTarget()).append(",\n");
        sb.append(indent).append("  \"totalRuns\": ").append(inn.getTotalRuns()).append(",\n");
        sb.append(indent).append("  \"wickets\": ").append(inn.getWickets()).append(",\n");
        sb.append(indent).append("  \"legalBalls\": ").append(inn.getLegalBalls()).append(",\n");
        sb.append(indent).append("  \"wideRuns\": ").append(inn.getWideRuns()).append(",\n");
        sb.append(indent).append("  \"noBallRuns\": ").append(inn.getNoBallRuns()).append(",\n");
        sb.append(indent).append("  \"byeRuns\": ").append(inn.getByeRuns()).append(",\n");
        sb.append(indent).append("  \"legByeRuns\": ").append(inn.getLegByeRuns()).append(",\n");
        sb.append(indent).append("  \"currentStriker\": ").append(JsonHelper.escape(inn.getCurrentStriker())).append(",\n");
        sb.append(indent).append("  \"currentNonStriker\": ").append(JsonHelper.escape(inn.getCurrentNonStriker())).append(",\n");
        sb.append(indent).append("  \"currentBowler\": ").append(JsonHelper.escape(inn.getCurrentBowler())).append(",\n");
        sb.append(indent).append("  \"previousBowler\": ").append(JsonHelper.escape(inn.getPreviousBowler())).append(",\n");
        sb.append(indent).append("  \"nextBattingIndex\": ").append(inn.getNextBattingIndex()).append(",\n");
        sb.append(indent).append("  \"isCompleted\": ").append(inn.isCompleted()).append(",\n");
        sb.append(indent).append("  \"completionReason\": ").append(JsonHelper.escape(inn.getCompletionReason())).append(",\n");

        // Batting Team
        sb.append(indent).append("  \"battingTeam\": ");
        appendTeamJson(sb, inn.getBattingTeam(), indent + "  ");
        sb.append(",\n");

        // Bowling Team
        sb.append(indent).append("  \"bowlingTeam\": ");
        appendTeamJson(sb, inn.getBowlingTeam(), indent + "  ");
        sb.append(",\n");

        // Fall of wickets
        sb.append(indent).append("  \"fallOfWickets\": [\n");
        for (int i = 0; i < inn.getFallOfWickets().size(); i++) {
            FallOfWicket f = inn.getFallOfWickets().get(i);
            sb.append(indent).append("    {");
            sb.append("\"wicketNumber\": ").append(f.getWicketNumber()).append(", ");
            sb.append("\"dismissedPlayerName\": ").append(JsonHelper.escape(f.getDismissedPlayerName())).append(", ");
            sb.append("\"teamScore\": ").append(f.getTeamScore()).append(", ");
            sb.append("\"oversFormatted\": ").append(JsonHelper.escape(f.getOversFormatted())).append(", ");
            sb.append("\"partnershipRuns\": ").append(f.getPartnershipRuns()).append(", ");
            sb.append("\"partnershipBalls\": ").append(f.getPartnershipBalls());
            sb.append("}");
            if (i < inn.getFallOfWickets().size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(indent).append("  ],\n");

        // Past Partnerships
        sb.append(indent).append("  \"pastPartnerships\": [\n");
        for (int i = 0; i < inn.getPastPartnerships().size(); i++) {
            Partnership p = inn.getPastPartnerships().get(i);
            sb.append(indent).append("    ");
            appendPartnershipJson(sb, p);
            if (i < inn.getPastPartnerships().size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(indent).append("  ],\n");

        // Current Partnership
        sb.append(indent).append("  \"currentPartnership\": ");
        if (inn.getCurrentPartnership() != null) {
            appendPartnershipJson(sb, inn.getCurrentPartnership());
        } else {
            sb.append("null");
        }
        sb.append(",\n");

        // Deliveries
        sb.append(indent).append("  \"deliveries\": [\n");
        for (int i = 0; i < inn.getDeliveries().size(); i++) {
            Delivery d = inn.getDeliveries().get(i);
            appendDeliveryJson(sb, d, indent + "    ");
            if (i < inn.getDeliveries().size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(indent).append("  ]\n");

        sb.append(indent).append("}");
    }

    private static void appendPartnershipJson(StringBuilder sb, Partnership p) {
        sb.append("{");
        sb.append("\"wicketNumber\": ").append(p.getWicketNumber()).append(", ");
        sb.append("\"player1Name\": ").append(JsonHelper.escape(p.getPlayer1Name())).append(", ");
        sb.append("\"player2Name\": ").append(JsonHelper.escape(p.getPlayer2Name())).append(", ");
        sb.append("\"runs\": ").append(p.getRuns()).append(", ");
        sb.append("\"balls\": ").append(p.getBalls()).append(", ");
        sb.append("\"player1Runs\": ").append(p.getPlayer1Runs()).append(", ");
        sb.append("\"player1Balls\": ").append(p.getPlayer1Balls()).append(", ");
        sb.append("\"player2Runs\": ").append(p.getPlayer2Runs()).append(", ");
        sb.append("\"player2Balls\": ").append(p.getPlayer2Balls()).append(", ");
        sb.append("\"extras\": ").append(p.getExtras()).append(", ");
        sb.append("\"isCurrent\": ").append(p.isCurrent());
        sb.append("}");
    }

    private static void appendDeliveryJson(StringBuilder sb, Delivery d, String indent) {
        sb.append(indent).append("{");
        sb.append("\"deliveryIndex\": ").append(d.getDeliveryIndex()).append(", ");
        sb.append("\"overNumber\": ").append(d.getOverNumber()).append(", ");
        sb.append("\"legalBallNumber\": ").append(d.getLegalBallNumber()).append(", ");
        sb.append("\"displayOverBall\": ").append(JsonHelper.escape(d.getDisplayOverBall())).append(", ");
        sb.append("\"striker\": ").append(JsonHelper.escape(d.getStriker())).append(", ");
        sb.append("\"nonStriker\": ").append(JsonHelper.escape(d.getNonStriker())).append(", ");
        sb.append("\"bowler\": ").append(JsonHelper.escape(d.getBowler())).append(", ");
        sb.append("\"runsBat\": ").append(d.getRunsBat()).append(", ");
        sb.append("\"extraType\": ").append(JsonHelper.escape(d.getExtraType().name())).append(", ");
        sb.append("\"extraRuns\": ").append(d.getExtraRuns()).append(", ");
        sb.append("\"isLegal\": ").append(d.isLegal()).append(", ");
        sb.append("\"runsConcededBowler\": ").append(d.getRunsConcededBowler()).append(", ");
        sb.append("\"totalRuns\": ").append(d.getTotalRuns()).append(", ");
        sb.append("\"strikeRotated\": ").append(d.isStrikeRotated()).append(", ");
        sb.append("\"dismissal\": ");
        if (d.getDismissal() != null) {
            appendDismissalJson(sb, d.getDismissal());
        } else {
            sb.append("null");
        }
        sb.append("}");
    }

    // JSON Deserializer
    public static Match jsonValueToMatch(JsonValue root) {
        if (root == null || !root.isObject()) return null;

        Match match = new Match();
        match.setMatchId(root.get("matchId") != null ? root.get("matchId").asString() : "");
        match.setDate(root.get("date") != null ? root.get("date").asString() : "");
        match.setAllocatedOvers(root.get("allocatedOvers") != null ? root.get("allocatedOvers").asInt(20) : 20);
        match.setTossWinner(root.get("tossWinner") != null ? root.get("tossWinner").asString() : "");
        match.setTossDecision(root.get("tossDecision") != null ? root.get("tossDecision").asString() : "");
        match.setCurrentInningsNumber(root.get("currentInningsNumber") != null ? root.get("currentInningsNumber").asInt(1) : 1);
        if (root.get("status") != null) {
            try {
                match.setStatus(MatchStatus.valueOf(root.get("status").asString()));
            } catch (Exception ignored) {}
        }
        match.setResult(root.get("result") != null ? root.get("result").asString() : null);
        match.setPlayerOfTheMatch(root.get("playerOfTheMatch") != null ? root.get("playerOfTheMatch").asString() : null);
        match.setPlayerOfTheMatchReason(root.get("playerOfTheMatchReason") != null ? root.get("playerOfTheMatchReason").asString() : null);
        match.setPlayerOfTheMatchPoints(root.get("playerOfTheMatchPoints") != null ? root.get("playerOfTheMatchPoints").asInt(0) : 0);

        // POM config
        JsonValue cfgVal = root.get("pomConfig");
        if (cfgVal != null && cfgVal.isObject()) {
            PlayerOfMatchConfig cfg = new PlayerOfMatchConfig();
            cfg.setPointsPerRun(cfgVal.get("pointsPerRun").asInt(1));
            cfg.setBonusPerFour(cfgVal.get("bonusPerFour").asInt(1));
            cfg.setBonusPerSix(cfgVal.get("bonusPerSix").asInt(2));
            cfg.setPointsPerWicket(cfgVal.get("pointsPerWicket").asInt(20));
            cfg.setPointsPerMaiden(cfgVal.get("pointsPerMaiden").asInt(1));
            cfg.setBonusHalfCentury(cfgVal.get("bonusHalfCentury").asInt(5));
            cfg.setBonusCentury(cfgVal.get("bonusCentury").asInt(10));
            match.setPomConfig(cfg);
        }

        // Teams
        match.setTeam1(parseTeam(root.get("team1")));
        match.setTeam2(parseTeam(root.get("team2")));

        // Innings
        match.setFirstInnings(parseInnings(root.get("firstInnings")));
        match.setSecondInnings(parseInnings(root.get("secondInnings")));

        return match;
    }

    private static Team parseTeam(JsonValue val) {
        if (val == null || !val.isObject()) return null;
        Team team = new Team(val.get("name") != null ? val.get("name").asString() : "");

        JsonValue boVal = val.get("battingOrder");
        if (boVal != null && boVal.isArray()) {
            List<String> bo = new ArrayList<>();
            for (JsonValue v : boVal.asArray()) {
                bo.add(v.asString());
            }
            team.setBattingOrder(bo);
        }

        JsonValue pVal = val.get("players");
        if (pVal != null && pVal.isArray()) {
            for (JsonValue pv : pVal.asArray()) {
                Player p = parsePlayer(pv);
                if (p != null) {
                    team.getPlayers().add(p);
                }
            }
        }
        return team;
    }

    private static Player parsePlayer(JsonValue pv) {
        if (pv == null || !pv.isObject()) return null;
        Player p = new Player(pv.get("name") != null ? pv.get("name").asString() : "");
        p.setRunsScored(pv.get("runsScored") != null ? pv.get("runsScored").asInt(0) : 0);
        p.setBallsFaced(pv.get("ballsFaced") != null ? pv.get("ballsFaced").asInt(0) : 0);
        p.setFours(pv.get("fours") != null ? pv.get("fours").asInt(0) : 0);
        p.setSixes(pv.get("sixes") != null ? pv.get("sixes").asInt(0) : 0);
        p.setHasBatted(pv.get("hasBatted") != null && pv.get("hasBatted").asBoolean(false));
        p.setOut(pv.get("isOut") != null && pv.get("isOut").asBoolean(false));
        p.setLegalBallsBowled(pv.get("legalBallsBowled") != null ? pv.get("legalBallsBowled").asInt(0) : 0);
        p.setMaidens(pv.get("maidens") != null ? pv.get("maidens").asInt(0) : 0);
        p.setRunsConceded(pv.get("runsConceded") != null ? pv.get("runsConceded").asInt(0) : 0);
        p.setWickets(pv.get("wickets") != null ? pv.get("wickets").asInt(0) : 0);
        p.setWidesConceded(pv.get("widesConceded") != null ? pv.get("widesConceded").asInt(0) : 0);
        p.setNoBallsConceded(pv.get("noBallsConceded") != null ? pv.get("noBallsConceded").asInt(0) : 0);
        p.setCatches(pv.get("catches") != null ? pv.get("catches").asInt(0) : 0);
        p.setRunOuts(pv.get("runOuts") != null ? pv.get("runOuts").asInt(0) : 0);
        p.setStumpings(pv.get("stumpings") != null ? pv.get("stumpings").asInt(0) : 0);

        JsonValue disVal = pv.get("dismissal");
        if (disVal != null && disVal.isObject()) {
            p.setDismissal(parseDismissal(disVal));
        }
        return p;
    }

    private static Dismissal parseDismissal(JsonValue dv) {
        if (dv == null || !dv.isObject()) return null;
        Dismissal d = new Dismissal();
        try {
            d.setType(DismissalType.valueOf(dv.get("type").asString()));
        } catch (Exception ignored) {}
        d.setDismissedPlayer(dv.get("dismissedPlayer") != null ? dv.get("dismissedPlayer").asString() : "");
        d.setBowler(dv.get("bowler") != null ? dv.get("bowler").asString() : "");
        d.setFielder(dv.get("fielder") != null ? dv.get("fielder").asString() : "");
        d.setDescription(dv.get("description") != null ? dv.get("description").asString() : "");
        return d;
    }

    private static Innings parseInnings(JsonValue val) {
        if (val == null || !val.isObject()) return null;
        Innings inn = new Innings();
        inn.setInningsNumber(val.get("inningsNumber").asInt(1));
        inn.setAllocatedOvers(val.get("allocatedOvers").asInt(20));
        inn.setTarget(val.get("target").asInt(-1));
        inn.setTotalRuns(val.get("totalRuns").asInt(0));
        inn.setWickets(val.get("wickets").asInt(0));
        inn.setLegalBalls(val.get("legalBalls").asInt(0));
        inn.setWideRuns(val.get("wideRuns").asInt(0));
        inn.setNoBallRuns(val.get("noBallRuns").asInt(0));
        inn.setByeRuns(val.get("byeRuns").asInt(0));
        inn.setLegByeRuns(val.get("legByeRuns").asInt(0));
        inn.setCurrentStriker(val.get("currentStriker") != null ? val.get("currentStriker").asString() : "");
        inn.setCurrentNonStriker(val.get("currentNonStriker") != null ? val.get("currentNonStriker").asString() : "");
        inn.setCurrentBowler(val.get("currentBowler") != null ? val.get("currentBowler").asString() : "");
        inn.setPreviousBowler(val.get("previousBowler") != null ? val.get("previousBowler").asString() : "");
        inn.setNextBattingIndex(val.get("nextBattingIndex").asInt(2));
        inn.setCompleted(val.get("isCompleted") != null && val.get("isCompleted").asBoolean(false));
        inn.setCompletionReason(val.get("completionReason") != null ? val.get("completionReason").asString() : "");

        inn.setBattingTeam(parseTeam(val.get("battingTeam")));
        inn.setBowlingTeam(parseTeam(val.get("bowlingTeam")));

        // Fall of wickets
        JsonValue fowVal = val.get("fallOfWickets");
        if (fowVal != null && fowVal.isArray()) {
            for (JsonValue fv : fowVal.asArray()) {
                FallOfWicket f = new FallOfWicket(
                        fv.get("wicketNumber").asInt(1),
                        fv.get("dismissedPlayerName").asString(),
                        fv.get("teamScore").asInt(0),
                        fv.get("oversFormatted").asString(),
                        fv.get("partnershipRuns").asInt(0),
                        fv.get("partnershipBalls").asInt(0)
                );
                inn.getFallOfWickets().add(f);
            }
        }

        // Past partnerships
        JsonValue ppVal = val.get("pastPartnerships");
        if (ppVal != null && ppVal.isArray()) {
            for (JsonValue pv : ppVal.asArray()) {
                inn.getPastPartnerships().add(parsePartnership(pv));
            }
        }

        // Current partnership
        inn.setCurrentPartnership(parsePartnership(val.get("currentPartnership")));

        // Deliveries
        JsonValue dVal = val.get("deliveries");
        if (dVal != null && dVal.isArray()) {
            for (JsonValue dv : dVal.asArray()) {
                Delivery d = new Delivery();
                d.setDeliveryIndex(dv.get("deliveryIndex").asInt(0));
                d.setOverNumber(dv.get("overNumber").asInt(1));
                d.setLegalBallNumber(dv.get("legalBallNumber").asInt(0));
                d.setDisplayOverBall(dv.get("displayOverBall").asString());
                d.setStriker(dv.get("striker").asString());
                d.setNonStriker(dv.get("nonStriker").asString());
                d.setBowler(dv.get("bowler").asString());
                d.setRunsBat(dv.get("runsBat").asInt(0));
                try {
                    d.setExtraType(ExtraType.valueOf(dv.get("extraType").asString()));
                } catch (Exception ignored) {}
                d.setExtraRuns(dv.get("extraRuns").asInt(0));
                d.setLegal(dv.get("isLegal").asBoolean(true));
                d.setRunsConcededBowler(dv.get("runsConcededBowler").asInt(0));
                d.setTotalRuns(dv.get("totalRuns").asInt(0));
                d.setStrikeRotated(dv.get("strikeRotated").asBoolean(false));

                JsonValue disVal = dv.get("dismissal");
                if (disVal != null && disVal.isObject()) {
                    d.setDismissal(parseDismissal(disVal));
                }
                inn.getDeliveries().add(d);
            }
        }

        return inn;
    }

    private static Partnership parsePartnership(JsonValue pv) {
        if (pv == null || !pv.isObject()) return null;
        Partnership p = new Partnership();
        p.setWicketNumber(pv.get("wicketNumber").asInt(1));
        p.setPlayer1Name(pv.get("player1Name").asString());
        p.setPlayer2Name(pv.get("player2Name").asString());
        p.setRuns(pv.get("runs").asInt(0));
        p.setBalls(pv.get("balls").asInt(0));
        p.setPlayer1Runs(pv.get("player1Runs").asInt(0));
        p.setPlayer1Balls(pv.get("player1Balls").asInt(0));
        p.setPlayer2Runs(pv.get("player2Runs").asInt(0));
        p.setPlayer2Balls(pv.get("player2Balls").asInt(0));
        p.setExtras(pv.get("extras").asInt(0));
        p.setCurrent(pv.get("isCurrent").asBoolean(false));
        return p;
    }
}
