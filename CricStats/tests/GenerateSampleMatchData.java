package tests;

import cricstats.engine.CricketScoringEngine;
import cricstats.models.*;
import cricstats.storage.MatchStorage;
import cricstats.ui.ScorecardFormatter;

import java.io.File;

public class GenerateSampleMatchData {
    public static void main(String[] args) {
        System.out.println("Generating sample match data...");

        CricketScoringEngine engine = new CricketScoringEngine();
        Match match = engine.createMatch("Royal Challengers", "Super Kings", 5);

        // Add 5 players each for a thrilling 5-over match
        match.getTeam1().addPlayer("Virat Kohli");
        match.getTeam1().addPlayer("Faf du Plessis");
        match.getTeam1().addPlayer("Glenn Maxwell");
        match.getTeam1().addPlayer("Dinesh Karthik");
        match.getTeam1().addPlayer("Mohammed Siraj");

        match.getTeam2().addPlayer("Ruturaj Gaikwad");
        match.getTeam2().addPlayer("Devon Conway");
        match.getTeam2().addPlayer("Shivam Dube");
        match.getTeam2().addPlayer("MS Dhoni");
        match.getTeam2().addPlayer("Ravindra Jadeja");

        match.setTossWinner("Royal Challengers");
        match.setTossDecision("BAT");

        // 1st Innings: Royal Challengers Batting
        engine.startFirstInnings("Virat Kohli", "Faf du Plessis", "Ravindra Jadeja");

        // Over 1 (Jadeja bowling to Kohli): 1, 4, 0, 6, 2, 1 = 14 runs
        engine.recordDelivery(1, ExtraType.NONE, 0, null, null, null); // Kohli -> 1, strike Faf
        engine.recordDelivery(4, ExtraType.NONE, 0, null, null, null); // Faf -> 4
        engine.recordDelivery(0, ExtraType.NONE, 0, null, null, null); // Faf -> dot
        engine.recordDelivery(6, ExtraType.NONE, 0, null, null, null); // Faf -> 6
        engine.recordDelivery(2, ExtraType.NONE, 0, null, null, null); // Faf -> 2
        engine.recordDelivery(1, ExtraType.NONE, 0, null, null, null); // Faf -> 1, strike Kohli. Over ends -> strike swaps back to Faf!

        // Over 2 (Dube bowling to Faf)
        engine.setNextBowler("Shivam Dube");
        engine.recordDelivery(0, ExtraType.WIDE, 1, null, null, null); // Wide
        engine.recordDelivery(4, ExtraType.NONE, 0, null, null, null); // Faf -> 4
        engine.recordDelivery(0, ExtraType.NONE, 0, DismissalType.CAUGHT, "Faf du Plessis", "MS Dhoni"); // Faf OUT!
        engine.setNextBatsman("Glenn Maxwell");
        engine.recordDelivery(1, ExtraType.NONE, 0, null, null, null); // Maxwell -> 1, strike Kohli
        engine.recordDelivery(6, ExtraType.NONE, 0, null, null, null); // Kohli -> 6
        engine.recordDelivery(2, ExtraType.NONE, 0, null, null, null); // Kohli -> 2
        engine.recordDelivery(0, ExtraType.NONE, 0, null, null, null); // Kohli -> dot. Over ends.

        // Over 3 (Jadeja bowling to Maxwell)
        engine.setNextBowler("Ravindra Jadeja");
        engine.recordDelivery(6, ExtraType.NONE, 0, null, null, null); // Maxwell -> 6
        engine.recordDelivery(1, ExtraType.NONE, 0, null, null, null); // Maxwell -> 1
        engine.recordDelivery(4, ExtraType.NONE, 0, null, null, null); // Kohli -> 4
        engine.recordDelivery(0, ExtraType.BYE, 1, null, null, null);  // 1 bye
        engine.recordDelivery(6, ExtraType.NONE, 0, null, null, null); // Maxwell -> 6
        engine.recordDelivery(1, ExtraType.NO_BALL, 0, null, null, null); // NB + 1 bat run
        engine.recordDelivery(2, ExtraType.NONE, 0, null, null, null); // Maxwell -> 2. Over ends.

        // Over 4 (Dube bowling)
        engine.setNextBowler("Shivam Dube");
        engine.recordDelivery(0, ExtraType.NONE, 0, DismissalType.BOWLED, "Virat Kohli", null); // Kohli bowled!
        engine.setNextBatsman("Dinesh Karthik");
        engine.recordDelivery(1, ExtraType.NONE, 0, null, null, null); // Karthik -> 1
        engine.recordDelivery(4, ExtraType.NONE, 0, null, null, null); // Maxwell -> 4
        engine.recordDelivery(0, ExtraType.NONE, 0, null, null, null); // dot
        engine.recordDelivery(6, ExtraType.NONE, 0, null, null, null); // Maxwell -> 6
        engine.recordDelivery(1, ExtraType.NONE, 0, null, null, null); // Maxwell -> 1. Over ends.

        // Over 5 (Jadeja bowling)
        engine.setNextBowler("Ravindra Jadeja");
        engine.recordDelivery(4, ExtraType.NONE, 0, null, null, null); // Maxwell -> 4
        engine.recordDelivery(6, ExtraType.NONE, 0, null, null, null); // Maxwell -> 6
        engine.recordDelivery(0, ExtraType.NONE, 0, DismissalType.STUMPED, "Glenn Maxwell", "MS Dhoni"); // Maxwell stumped!
        engine.setNextBatsman("Mohammed Siraj");
        engine.recordDelivery(1, ExtraType.NONE, 0, null, null, null); // Siraj -> 1
        engine.recordDelivery(2, ExtraType.NONE, 0, null, null, null); // Karthik -> 2
        engine.recordDelivery(4, ExtraType.NONE, 0, null, null, null); // Karthik -> 4. Innings 1 ends!

        // Start 2nd Innings: Super Kings Chasing
        engine.startSecondInnings("Ruturaj Gaikwad", "Devon Conway", "Mohammed Siraj");

        // Over 1 (Siraj bowling to Gaikwad)
        engine.recordDelivery(4, ExtraType.NONE, 0, null, null, null); // Gaikwad -> 4
        engine.recordDelivery(4, ExtraType.NONE, 0, null, null, null); // Gaikwad -> 4
        engine.recordDelivery(1, ExtraType.NONE, 0, null, null, null); // Gaikwad -> 1
        engine.recordDelivery(6, ExtraType.NONE, 0, null, null, null); // Conway -> 6
        engine.recordDelivery(0, ExtraType.NONE, 0, DismissalType.BOWLED, "Devon Conway", null); // Conway OUT!
        engine.setNextBatsman("Shivam Dube");
        engine.recordDelivery(1, ExtraType.NONE, 0, null, null, null); // Dube -> 1. Over ends.

        // Over 2 (Maxwell bowling to Gaikwad)
        engine.setNextBowler("Glenn Maxwell");
        engine.recordDelivery(1, ExtraType.NONE, 0, null, null, null); // Gaikwad -> 1
        engine.recordDelivery(6, ExtraType.NONE, 0, null, null, null); // Dube -> 6
        engine.recordDelivery(0, ExtraType.WIDE, 1, null, null, null); // Wide
        engine.recordDelivery(4, ExtraType.NONE, 0, null, null, null); // Dube -> 4
        engine.recordDelivery(0, ExtraType.NONE, 0, DismissalType.CAUGHT, "Shivam Dube", "Virat Kohli"); // Dube OUT!
        engine.setNextBatsman("MS Dhoni");
        engine.recordDelivery(1, ExtraType.NONE, 0, null, null, null); // Dhoni -> 1
        engine.recordDelivery(2, ExtraType.NONE, 0, null, null, null); // Gaikwad -> 2. Over ends.

        // Over 3 (Siraj bowling)
        engine.setNextBowler("Mohammed Siraj");
        engine.recordDelivery(4, ExtraType.NONE, 0, null, null, null); // Gaikwad -> 4
        engine.recordDelivery(1, ExtraType.NONE, 0, DismissalType.RUN_OUT, "Ruturaj Gaikwad", "Dinesh Karthik"); // Gaikwad run out!
        engine.setNextBatsman("Ravindra Jadeja");
        engine.recordDelivery(0, ExtraType.NONE, 0, null, null, null); // dot
        engine.recordDelivery(1, ExtraType.NONE, 0, null, null, null); // 1
        engine.recordDelivery(0, ExtraType.NONE, 0, null, null, null); // dot
        engine.recordDelivery(1, ExtraType.NONE, 0, null, null, null); // 1. Over ends.

        // Over 4 (Maxwell bowling)
        engine.setNextBowler("Glenn Maxwell");
        engine.recordDelivery(6, ExtraType.NONE, 0, null, null, null); // Dhoni -> 6
        engine.recordDelivery(4, ExtraType.NONE, 0, null, null, null); // Dhoni -> 4
        engine.recordDelivery(1, ExtraType.NONE, 0, null, null, null); // Dhoni -> 1
        engine.recordDelivery(2, ExtraType.NONE, 0, null, null, null); // Jadeja -> 2
        engine.recordDelivery(0, ExtraType.NONE, 0, null, null, null); // dot
        engine.recordDelivery(6, ExtraType.NONE, 0, null, null, null); // Jadeja -> 6. Over ends.

        // Over 5 (Siraj bowling)
        engine.setNextBowler("Mohammed Siraj");
        engine.recordDelivery(1, ExtraType.NONE, 0, null, null, null); // 1
        engine.recordDelivery(2, ExtraType.NONE, 0, null, null, null); // 2
        engine.recordDelivery(0, ExtraType.NONE, 0, DismissalType.BOWLED, "Ravindra Jadeja", null); // Jadeja OUT! All out!

        // Match finishes!
        File sampleDir = new File("sample_data");
        sampleDir.mkdirs();
        File jsonFile = new File(sampleDir, "sample_match.json");
        File csvFile = new File(sampleDir, "sample_match.csv");

        try {
            MatchStorage.saveToJson(match, jsonFile);
            MatchStorage.saveToCsv(match, csvFile);
            System.out.println("Sample match generated successfully!");
            System.out.println("JSON: " + jsonFile.getAbsolutePath());
            System.out.println("CSV: " + csvFile.getAbsolutePath());
            System.out.println("\n" + ScorecardFormatter.formatMatchSummary(match));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
