package tests;

import cricstats.engine.CricketScoringEngine;
import cricstats.engine.CricketScoringEngine.DeliveryResult;
import cricstats.models.*;
import cricstats.storage.MatchStorage;

import java.io.File;
import java.util.*;

public class CricketScoringTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  CRICSTATS AUTOMATED TEST SUITE (12 TEST CASES) ");
        System.out.println("=================================================\n");

        runTest("1. Normal Scoring (1, 2, 3, 4, 6 runs)", CricketScoringTest::testNormalScoring);
        runTest("2. Wide Deliveries", CricketScoringTest::testWideDeliveries);
        runTest("3. No-ball Deliveries", CricketScoringTest::testNoBallDeliveries);
        runTest("4. Byes and Leg-byes", CricketScoringTest::testByesAndLegByes);
        runTest("5. Bowled and Caught Dismissals", CricketScoringTest::testBowledAndCaughtDismissals);
        runTest("6. Run-outs (Bowler not credited)", CricketScoringTest::testRunOuts);
        runTest("7. Over Completion, Maidens & Strike Rotation", CricketScoringTest::testOverCompletionAndStrikeRotation);
        runTest("8. All-Out Situations", CricketScoringTest::testAllOutSituations);
        runTest("9. Second Innings Target Calculation", CricketScoringTest::testSecondInningsTargetCalculation);
        runTest("10. Successful Run Chase (Immediate Win)", CricketScoringTest::testSuccessfulRunChase);
        runTest("11. Undo Last Delivery (Full State Consistency)", CricketScoringTest::testUndoLastDelivery);
        runTest("12. Saving and Loading Match Data (JSON & CSV)", CricketScoringTest::testSavingAndLoadingMatchData);

        System.out.println("\n=================================================");
        System.out.printf("  TEST RESULTS: %d PASSED, %d FAILED\n", testsPassed, testsFailed);
        System.out.println("=================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void runTest(String testName, Runnable test) {
        try {
            test.run();
            testsPassed++;
            System.out.println(" [PASS] " + testName);
        } catch (Throwable t) {
            testsFailed++;
            System.err.println(" [FAIL] " + testName + " -> " + t.getMessage());
            t.printStackTrace(System.err);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message + " - Expected: true, Actual: false");
        }
    }

    // Helper to setup a standard test match
    private static CricketScoringEngine createTestEngine(int playersPerTeam, int overs) {
        CricketScoringEngine engine = new CricketScoringEngine();
        Match match = engine.createMatch("India", "Australia", overs);

        for (int i = 1; i <= playersPerTeam; i++) {
            match.getTeam1().addPlayer("Ind_Player_" + i);
            match.getTeam2().addPlayer("Aus_Player_" + i);
        }
        match.setTossWinner("India");
        match.setTossDecision("BAT");

        engine.startFirstInnings("Ind_Player_1", "Ind_Player_2", "Aus_Player_1");
        return engine;
    }

    // Test 1: Normal scoring
    public static void testNormalScoring() {
        CricketScoringEngine engine = createTestEngine(5, 5);
        Innings inn = engine.getMatch().getFirstInnings();

        // Ball 1: 1 run off bat -> strike rotates
        engine.recordDelivery(1, ExtraType.NONE, 0, null, null, null);
        assertEquals(1, inn.getTotalRuns(), "Total runs after 1 run");
        assertEquals(1, inn.getLegalBalls(), "Legal balls count");
        assertEquals("Ind_Player_2", inn.getCurrentStriker(), "Strike should rotate to player 2 after 1 run");

        // Ball 2: 4 runs off bat -> boundary
        engine.recordDelivery(4, ExtraType.NONE, 0, null, null, null);
        assertEquals(5, inn.getTotalRuns(), "Total runs after four");
        assertEquals(2, inn.getLegalBalls(), "Legal balls count");
        assertEquals("Ind_Player_2", inn.getCurrentStriker(), "Strike should stay with player 2 after 4 runs");

        Player p2 = inn.getBattingTeam().getPlayer("Ind_Player_2");
        assertEquals(4, p2.getRunsScored(), "Player 2 runs scored");
        assertEquals(1, p2.getBallsFaced(), "Player 2 balls faced");
        assertEquals(1, p2.getFours(), "Player 2 fours");
        assertEquals(400.0, p2.getStrikeRate(), "Player 2 strike rate");

        // Ball 3: 6 runs off bat -> six
        engine.recordDelivery(6, ExtraType.NONE, 0, null, null, null);
        assertEquals(11, inn.getTotalRuns(), "Total runs after six");
        assertEquals(1, p2.getSixes(), "Player 2 sixes");
        assertEquals(10, p2.getRunsScored(), "Player 2 total runs");

        Player bowler = inn.getBowlingTeam().getPlayer("Aus_Player_1");
        assertEquals(11, bowler.getRunsConceded(), "Bowler runs conceded");
        assertEquals(3, bowler.getLegalBallsBowled(), "Bowler legal balls");
    }

    // Test 2: Wide deliveries
    public static void testWideDeliveries() {
        CricketScoringEngine engine = createTestEngine(5, 5);
        Innings inn = engine.getMatch().getFirstInnings();

        // 1 standard wide: 1 run extra, no legal ball, bowler conceded 1 run
        engine.recordDelivery(0, ExtraType.WIDE, 1, null, null, null);
        assertEquals(1, inn.getTotalRuns(), "Total score on wide");
        assertEquals(0, inn.getLegalBalls(), "Legal balls should be 0 on wide");
        assertEquals(1, inn.getWideRuns(), "Wide extras");

        Player striker = inn.getBattingTeam().getPlayer("Ind_Player_1");
        assertEquals(0, striker.getBallsFaced(), "Striker balls faced should not increment on wide");
        assertEquals("Ind_Player_1", inn.getCurrentStriker(), "Striker should remain same on single wide");

        Player bowler = inn.getBowlingTeam().getPlayer("Aus_Player_1");
        assertEquals(1, bowler.getRunsConceded(), "Bowler runs conceded on wide");
        assertEquals(1, bowler.getWidesConceded(), "Bowler wides conceded");

        // Wide with extra run (batsmen ran 1 run on wide overthrow): total 2 wides, strike rotates
        engine.recordDelivery(0, ExtraType.WIDE, 2, null, null, null);
        assertEquals(3, inn.getTotalRuns(), "Total score after wide+1");
        assertEquals(0, inn.getLegalBalls(), "Legal balls still 0");
        assertEquals("Ind_Player_2", inn.getCurrentStriker(), "Strike should rotate on wide+1 run taken");
    }

    // Test 3: No-ball deliveries
    public static void testNoBallDeliveries() {
        CricketScoringEngine engine = createTestEngine(5, 5);
        Innings inn = engine.getMatch().getFirstInnings();

        // No-ball + 4 runs off bat: total 5 runs (1 NB extra + 4 bat runs)
        engine.recordDelivery(4, ExtraType.NO_BALL, 0, null, null, null);
        assertEquals(5, inn.getTotalRuns(), "Total runs on NB+4");
        assertEquals(0, inn.getLegalBalls(), "No-ball does not count as legal ball");
        assertEquals(1, inn.getNoBallRuns(), "No ball extras");

        Player striker = inn.getBattingTeam().getPlayer("Ind_Player_1");
        assertEquals(4, striker.getRunsScored(), "Striker credited with 4 bat runs");
        assertEquals(1, striker.getBallsFaced(), "Striker faced 1 ball on no-ball");
        assertEquals(1, striker.getFours(), "Striker hit 1 four on no-ball");

        Player bowler = inn.getBowlingTeam().getPlayer("Aus_Player_1");
        assertEquals(5, bowler.getRunsConceded(), "Bowler conceded 1 NB + 4 bat runs = 5 runs");
        assertEquals(1, bowler.getNoBallsConceded(), "Bowler 1 no-ball conceded");
    }

    // Test 4: Byes and Leg-byes
    public static void testByesAndLegByes() {
        CricketScoringEngine engine = createTestEngine(5, 5);
        Innings inn = engine.getMatch().getFirstInnings();

        // Ball 1: 1 Bye -> 1 legal ball, 1 team extra, striker balls faced++, bowler runs conceded 0!
        engine.recordDelivery(0, ExtraType.BYE, 1, null, null, null);
        assertEquals(1, inn.getTotalRuns(), "Total runs after bye");
        assertEquals(1, inn.getLegalBalls(), "Legal ball increments on bye");
        assertEquals(1, inn.getByeRuns(), "Bye extras");

        Player striker1 = inn.getBattingTeam().getPlayer("Ind_Player_1");
        assertEquals(0, striker1.getRunsScored(), "Striker gets 0 runs on bye");
        assertEquals(1, striker1.getBallsFaced(), "Striker balls faced increments on legal delivery");
        assertEquals("Ind_Player_2", inn.getCurrentStriker(), "Strike rotates on 1 bye");

        Player bowler = inn.getBowlingTeam().getPlayer("Aus_Player_1");
        assertEquals(0, bowler.getRunsConceded(), "Bowler does NOT concede runs on byes");
        assertEquals(1, bowler.getLegalBallsBowled(), "Bowler legal balls increments on bye");

        // Ball 2: 2 Leg-byes -> 2 team extras, striker balls faced++, bowler runs conceded 0
        engine.recordDelivery(0, ExtraType.LEG_BYE, 2, null, null, null);
        assertEquals(3, inn.getTotalRuns(), "Total runs after leg bye");
        assertEquals(2, inn.getLegalBalls(), "Legal balls increments");
        assertEquals(2, inn.getLegByeRuns(), "Leg bye extras");
        assertEquals(0, bowler.getRunsConceded(), "Bowler still has 0 runs conceded");
    }

    // Test 5: Bowled and Caught dismissals
    public static void testBowledAndCaughtDismissals() {
        CricketScoringEngine engine = createTestEngine(5, 5);
        Innings inn = engine.getMatch().getFirstInnings();

        // Ball 1: Bowled
        engine.recordDelivery(0, ExtraType.NONE, 0, DismissalType.BOWLED, "Ind_Player_1", null);
        assertEquals(1, inn.getWickets(), "Wickets count after bowled");
        assertEquals(1, inn.getLegalBalls(), "Legal ball count increments on bowled");

        Player p1 = inn.getBattingTeam().getPlayer("Ind_Player_1");
        assertTrue(p1.isOut(), "Player 1 is out");
        assertEquals("b Aus_Player_1", p1.getDismissal().getDescription(), "Dismissal string");

        Player bowler = inn.getBowlingTeam().getPlayer("Aus_Player_1");
        assertEquals(1, bowler.getWickets(), "Bowler credited with wicket on bowled");

        assertEquals(1, inn.getFallOfWickets().size(), "Fall of wickets recorded");
        FallOfWicket fow1 = inn.getFallOfWickets().get(0);
        assertEquals("Ind_Player_1", fow1.getDismissedPlayerName(), "FOW player");
        assertEquals(0, fow1.getTeamScore(), "Score at 1st wicket");

        // Introduce new batsman
        engine.setNextBatsman("Ind_Player_3");
        assertEquals("Ind_Player_3", inn.getCurrentStriker(), "New batsman at crease");

        // Ball 2: Caught by Aus_Player_2
        engine.recordDelivery(0, ExtraType.NONE, 0, DismissalType.CAUGHT, "Ind_Player_3", "Aus_Player_2");
        assertEquals(2, inn.getWickets(), "Wickets count after caught");
        assertEquals(2, bowler.getWickets(), "Bowler credited with 2nd wicket");

        Player p3 = inn.getBattingTeam().getPlayer("Ind_Player_3");
        assertTrue(p3.isOut(), "Player 3 is out");
        assertEquals("c Aus_Player_2 b Aus_Player_1", p3.getDismissal().getDescription(), "Dismissal string");

        Player fielder = inn.getBowlingTeam().getPlayer("Aus_Player_2");
        assertEquals(1, fielder.getCatches(), "Fielder credited with catch");
    }

    // Test 6: Run-outs (bowler NOT credited)
    public static void testRunOuts() {
        CricketScoringEngine engine = createTestEngine(5, 5);
        Innings inn = engine.getMatch().getFirstInnings();

        // 1 completed run, then non-striker run out by Aus_Player_3
        engine.recordDelivery(1, ExtraType.NONE, 0, DismissalType.RUN_OUT, "Ind_Player_2", "Aus_Player_3");
        assertEquals(1, inn.getTotalRuns(), "Total runs with completed run");
        assertEquals(1, inn.getWickets(), "Wickets count after run out");

        Player bowler = inn.getBowlingTeam().getPlayer("Aus_Player_1");
        assertEquals(0, bowler.getWickets(), "Bowler must NOT be credited with run-out wicket!");

        Player nonStriker = inn.getBattingTeam().getPlayer("Ind_Player_2");
        assertTrue(nonStriker.isOut(), "Non-striker is out");
        assertEquals("run out (Aus_Player_3)", nonStriker.getDismissal().getDescription(), "Run out description");

        Player fielder = inn.getBowlingTeam().getPlayer("Aus_Player_3");
        assertEquals(1, fielder.getRunOuts(), "Fielder credited with run out");
    }

    // Test 7: Over completion, maidens, and strike rotation
    public static void testOverCompletionAndStrikeRotation() {
        CricketScoringEngine engine = createTestEngine(5, 5);
        Innings inn = engine.getMatch().getFirstInnings();

        // Bowl 6 dot balls -> Maiden over!
        for (int i = 0; i < 6; i++) {
            engine.recordDelivery(0, ExtraType.NONE, 0, null, null, null);
        }

        assertEquals(6, inn.getLegalBalls(), "6 legal balls completed");
        assertEquals(1, inn.getCompletedOvers().size(), "1 completed over");
        Over over1 = inn.getCompletedOvers().get(0);
        assertTrue(over1.isMaiden(), "Over 1 should be a maiden over");

        Player bowler1 = inn.getBowlingTeam().getPlayer("Aus_Player_1");
        assertEquals(1, bowler1.getMaidens(), "Bowler maiden count");
        assertEquals(0, bowler1.getRunsConceded(), "Bowler runs conceded 0");

        // Strike should swap at end of over
        assertEquals("Ind_Player_2", inn.getCurrentStriker(), "Strike should swap at end of over");

        // Bowler cannot bowl consecutive overs
        boolean invalidConsecutive = engine.setNextBowler("Aus_Player_1");
        assertTrue(!invalidConsecutive, "Bowler cannot bowl consecutive overs");

        boolean validNextBowler = engine.setNextBowler("Aus_Player_2");
        assertTrue(validNextBowler, "Different bowler allowed for next over");
        assertEquals("Aus_Player_2", inn.getCurrentBowler(), "Current bowler updated");
    }

    // Test 8: All-out situations
    public static void testAllOutSituations() {
        // Team of 3 players -> 2 wickets = All Out
        CricketScoringEngine engine = createTestEngine(3, 5);
        Innings inn = engine.getMatch().getFirstInnings();

        // Wicket 1
        engine.recordDelivery(0, ExtraType.NONE, 0, DismissalType.BOWLED, "Ind_Player_1", null);
        assertTrue(!inn.isAllOut(), "Not all out yet with 1 wicket down in 3-player team");

        engine.setNextBatsman("Ind_Player_3");

        // Wicket 2
        engine.recordDelivery(0, ExtraType.NONE, 0, DismissalType.BOWLED, "Ind_Player_2", null);
        assertTrue(inn.isAllOut(), "Team is all out after 2 wickets in 3-player team");
        assertTrue(inn.isCompleted(), "Innings marked as completed");
        assertEquals("All Out", inn.getCompletionReason(), "Completion reason");
        assertEquals(MatchStatus.INNINGS_BREAK, engine.getMatch().getStatus(), "Match status at innings break");
    }

    // Test 9: Second innings target calculation
    public static void testSecondInningsTargetCalculation() {
        CricketScoringEngine engine = createTestEngine(5, 5);
        Innings inn1 = engine.getMatch().getFirstInnings();

        // Score 45 runs in 1st innings
        for (int i = 0; i < 7; i++) {
            engine.recordDelivery(6, ExtraType.NONE, 0, null, null, null);
        }
        engine.recordDelivery(3, ExtraType.NONE, 0, null, null, null); // 7*6 + 3 = 45 runs
        assertEquals(45, inn1.getTotalRuns(), "1st innings total runs");

        // Complete 1st innings
        inn1.setCompleted(true);
        inn1.setCompletionReason("Overs Completed");

        // Start 2nd Innings
        engine.startSecondInnings("Aus_Player_1", "Aus_Player_2", "Ind_Player_1");
        Innings inn2 = engine.getMatch().getSecondInnings();

        assertEquals(46, inn2.getTarget(), "Target must be 1st innings total + 1 (45 + 1 = 46)");
        assertEquals(46, inn2.getRunsRequired(), "Runs required at start of chase");
        assertEquals(30, inn2.getBallsRemaining(), "Balls remaining (5 overs * 6 = 30 balls)");
        assertEquals(9.2, inn2.getRequiredRunRate(), "Required run rate (46 / 5 = 9.20)");
    }

    // Test 10: Successful run chase (Immediate Win)
    public static void testSuccessfulRunChase() {
        CricketScoringEngine engine = createTestEngine(5, 5);
        Innings inn1 = engine.getMatch().getFirstInnings();
        // 1st innings scores 10 runs
        engine.recordDelivery(4, ExtraType.NONE, 0, null, null, null);
        engine.recordDelivery(6, ExtraType.NONE, 0, null, null, null);
        inn1.setCompleted(true);

        // 2nd innings target is 11
        engine.startSecondInnings("Aus_Player_1", "Aus_Player_2", "Ind_Player_1");
        Innings inn2 = engine.getMatch().getSecondInnings();
        assertEquals(11, inn2.getTarget(), "Target 11");

        // Aus scores 6 runs
        engine.recordDelivery(6, ExtraType.NONE, 0, null, null, null);
        assertTrue(!inn2.isCompleted(), "Match in progress");

        // Aus scores 6 runs (Total 12 >= Target 11) -> Immediate victory!
        DeliveryResult res = engine.recordDelivery(6, ExtraType.NONE, 0, null, null, null);
        assertTrue(inn2.isCompleted(), "2nd innings should end immediately on target reached");
        assertEquals(MatchStatus.COMPLETED, engine.getMatch().getStatus(), "Match completed");
        assertTrue(engine.getMatch().getResult().contains("Australia won by 4 wickets"), "Australia wins by 4 wickets: " + engine.getMatch().getResult());
    }

    // Test 11: Undo last delivery
    public static void testUndoLastDelivery() {
        CricketScoringEngine engine = createTestEngine(5, 5);
        Innings inn = engine.getMatch().getFirstInnings();

        // Ball 1: 4 runs
        engine.recordDelivery(4, ExtraType.NONE, 0, null, null, null);
        assertEquals(4, inn.getTotalRuns(), "Score 4");
        assertEquals(1, inn.getLegalBalls(), "Legal balls 1");

        // Ball 2: Wicket (Bowled)
        engine.recordDelivery(0, ExtraType.NONE, 0, DismissalType.BOWLED, "Ind_Player_1", null);
        inn = engine.getMatch().getFirstInnings();
        assertEquals(1, inn.getWickets(), "Wickets 1");

        // Undo Ball 2!
        boolean undone = engine.undoLastDelivery();
        assertTrue(undone, "Undo succeeded");

        inn = engine.getMatch().getFirstInnings();
        assertEquals(4, inn.getTotalRuns(), "Score restored to 4");
        assertEquals(1, inn.getLegalBalls(), "Legal balls restored to 1");
        assertEquals(0, inn.getWickets(), "Wickets restored to 0");
        assertTrue(inn.getFallOfWickets().isEmpty(), "Fall of wickets list restored to empty");

        Player p1 = inn.getBattingTeam().getPlayer("Ind_Player_1");
        assertTrue(!p1.isOut(), "Player 1 restored to not out");
        assertEquals(4, p1.getRunsScored(), "Player 1 runs restored to 4");
        assertEquals(1, p1.getBallsFaced(), "Player 1 balls faced restored to 1");

        Player bowler = inn.getBowlingTeam().getPlayer("Aus_Player_1");
        assertEquals(0, bowler.getWickets(), "Bowler wickets restored to 0");
        assertEquals(4, bowler.getRunsConceded(), "Bowler runs conceded restored to 4");

        // Undo Ball 1!
        undone = engine.undoLastDelivery();
        assertTrue(undone, "Undo 2nd ball succeeded");

        inn = engine.getMatch().getFirstInnings();
        assertEquals(0, inn.getTotalRuns(), "Score restored to 0");
        assertEquals(0, inn.getLegalBalls(), "Legal balls restored to 0");
        p1 = inn.getBattingTeam().getPlayer("Ind_Player_1");
        assertEquals(0, p1.getRunsScored(), "Player 1 runs 0");
        assertEquals(0, p1.getBallsFaced(), "Player 1 balls faced 0");
    }

    // Test 12: Saving and Loading match data
    public static void testSavingAndLoadingMatchData() {
        CricketScoringEngine engine = createTestEngine(5, 5);
        Innings inn = engine.getMatch().getFirstInnings();

        engine.recordDelivery(4, ExtraType.NONE, 0, null, null, null);
        engine.recordDelivery(0, ExtraType.WIDE, 1, null, null, null);
        engine.recordDelivery(1, ExtraType.NONE, 0, null, null, null);

        Match originalMatch = engine.getMatch();

        File jsonFile = new File("sample_data", "test_roundtrip.json");
        File csvFile = new File("sample_data", "test_roundtrip.csv");

        try {
            MatchStorage.saveToJson(originalMatch, jsonFile);
            assertTrue(jsonFile.exists(), "JSON file created");

            MatchStorage.saveToCsv(originalMatch, csvFile);
            assertTrue(csvFile.exists(), "CSV file created");

            // Load back from JSON
            Match loadedMatch = MatchStorage.loadFromJson(jsonFile);
            assertEquals(originalMatch.getMatchId(), loadedMatch.getMatchId(), "Loaded match ID matches");
            assertEquals(originalMatch.getTeam1().getName(), loadedMatch.getTeam1().getName(), "Team 1 name");
            assertEquals(originalMatch.getTeam2().getName(), loadedMatch.getTeam2().getName(), "Team 2 name");
            assertEquals(originalMatch.getAllocatedOvers(), loadedMatch.getAllocatedOvers(), "Allocated overs");

            Innings loadedInn = loadedMatch.getFirstInnings();
            assertEquals(inn.getTotalRuns(), loadedInn.getTotalRuns(), "Total runs match");
            assertEquals(inn.getLegalBalls(), loadedInn.getLegalBalls(), "Legal balls match");
            assertEquals(inn.getWideRuns(), loadedInn.getWideRuns(), "Wide runs match");
            assertEquals(inn.getDeliveries().size(), loadedInn.getDeliveries().size(), "Deliveries count match");

        } catch (Exception e) {
            throw new RuntimeException("Save/Load failed: " + e.getMessage(), e);
        } finally {
            if (jsonFile.exists()) jsonFile.delete();
            if (csvFile.exists()) csvFile.delete();
        }
    }
}
