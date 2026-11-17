package cricstats.ui;

import cricstats.engine.CricketScoringEngine;
import cricstats.engine.CricketScoringEngine.DeliveryResult;
import cricstats.models.*;
import cricstats.storage.MatchStorage;

import java.io.File;
import java.util.*;

public class ConsoleUI {
    private final Scanner scanner;
    private final CricketScoringEngine engine;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.engine = new CricketScoringEngine();
    }

    public ConsoleUI(Scanner scanner, CricketScoringEngine engine) {
        this.scanner = scanner;
        this.engine = engine;
    }

    public void run() {
        boolean running = true;
        while (running) {
            displayMainMenu();
            String choice = prompt("Enter your choice: ").trim();

            switch (choice) {
                case "1":
                    handleCreateNewMatch();
                    break;
                case "2":
                    handleSetupTeamsAndPlayers();
                    break;
                case "3":
                    handleStartFirstInnings();
                    break;
                case "4":
                    handleEnterBallByBallScore();
                    break;
                case "5":
                    handleViewLiveScore();
                    break;
                case "6":
                    handleViewBattingScorecard();
                    break;
                case "7":
                    handleViewBowlingScorecard();
                    break;
                case "8":
                    handleViewFallOfWickets();
                    break;
                case "9":
                    handleViewPartnerships();
                    break;
                case "10":
                    handleViewMatchSummary();
                    break;
                case "11":
                    handleSaveMatch();
                    break;
                case "12":
                    handleLoadMatch();
                    break;
                case "13":
                    handleUndoLastDelivery();
                    break;
                case "14":
                    if (confirmExit()) {
                        System.out.println("\nThank you for using CricStats! Goodbye.\n");
                        running = false;
                        return;
                    }
                    break;
                default:
                    System.out.println("\n[!] Invalid choice. Please enter a number between 1 and 14.\n");
                    pause();
                    break;
            }
        }
    }

    private void displayMainMenu() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("CRICSTATS");
        System.out.println("BALL-BY-BALL CRICKET SCORING ENGINE");
        System.out.println("========================================");
        System.out.println("1. Create New Match");
        System.out.println("2. Set Up Teams and Players");
        System.out.println("3. Start First Innings");
        System.out.println("4. Enter Ball-by-Ball Score");
        System.out.println("5. View Live Score");
        System.out.println("6. View Batting Scorecard");
        System.out.println("7. View Bowling Scorecard");
        System.out.println("8. View Fall of Wickets");
        System.out.println("9. View Partnership Statistics");
        System.out.println("10. View Match Summary");
        System.out.println("11. Save Match Data");
        System.out.println("12. Load Previous Match");
        System.out.println("13. Undo Last Delivery");
        System.out.println("14. Exit");
        System.out.println("========================================");
    }

    // Option 1: Create New Match
    private void handleCreateNewMatch() {
        if (engine.getMatch() != null && engine.getMatch().getStatus() != MatchStatus.COMPLETED) {
            String ans = prompt("An active match is in progress. Discard and create a new match? (y/n): ");
            if (!ans.equalsIgnoreCase("y")) {
                System.out.println("Match creation cancelled.");
                pause();
                return;
            }
        }

        System.out.println("\n--- CREATE NEW MATCH ---");
        String t1Name = promptNonEmpty("Enter Team 1 Name (e.g., India): ");
        String t2Name;
        while (true) {
            t2Name = promptNonEmpty("Enter Team 2 Name (e.g., Australia): ");
            if (!t2Name.equalsIgnoreCase(t1Name)) {
                break;
            }
            System.out.println("[!] Team 2 cannot have the same name as Team 1.");
        }

        int overs = promptOvers();
        engine.createMatch(t1Name, t2Name, overs);
        System.out.printf("\n[✓] Match created successfully: %s vs %s (%d Overs)\n", t1Name, t2Name, overs);
        System.out.println("Next step: Select option 2 to set up players and toss.\n");
        pause();
    }

    private int promptOvers() {
        System.out.println("\nSelect match format / over limit:");
        System.out.println("1. 5 Overs (T5)");
        System.out.println("2. 10 Overs (T10)");
        System.out.println("3. 20 Overs (T20)");
        System.out.println("4. 50 Overs (ODI)");
        System.out.println("5. Custom Overs");

        while (true) {
            String choice = prompt("Choose an option (1-5) [Default: 20]: ").trim();
            if (choice.isEmpty() || choice.equals("3")) return 20;
            if (choice.equals("1")) return 5;
            if (choice.equals("2")) return 10;
            if (choice.equals("4")) return 50;
            if (choice.equals("5")) {
                while (true) {
                    try {
                        int custom = Integer.parseInt(prompt("Enter custom overs (1 to 100): ").trim());
                        if (custom >= 1 && custom <= 100) {
                            return custom;
                        }
                        System.out.println("[!] Overs must be between 1 and 100.");
                    } catch (NumberFormatException e) {
                        System.out.println("[!] Please enter a valid integer.");
                    }
                }
            }
            System.out.println("[!] Invalid option. Enter 1 to 5.");
        }
    }

    // Option 2: Set Up Teams and Players
    private void handleSetupTeamsAndPlayers() {
        Match match = engine.getMatch();
        if (match == null) {
            System.out.println("\n[!] Please create a match first (Option 1).");
            pause();
            return;
        }

        System.out.println("\n--- SET UP TEAMS AND PLAYERS ---");
        int numPlayers = 11;
        while (true) {
            String input = prompt("Enter number of players per team (2 to 11) [Default: 11]: ").trim();
            if (input.isEmpty()) {
                numPlayers = 11;
                break;
            }
            try {
                int n = Integer.parseInt(input);
                if (n >= 2 && n <= 11) {
                    numPlayers = n;
                    break;
                }
                System.out.println("[!] Number of players must be between 2 and 11.");
            } catch (NumberFormatException e) {
                System.out.println("[!] Please enter a valid number.");
            }
        }

        // Setup Team 1
        System.out.println("\n--- Entering Players for " + match.getTeam1().getName() + " ---");
        match.getTeam1().getPlayers().clear();
        match.getTeam1().getBattingOrder().clear();
        for (int i = 1; i <= numPlayers; i++) {
            while (true) {
                String pName = promptNonEmpty(String.format("Player %d name: ", i));
                if (match.getTeam1().addPlayer(pName)) {
                    break;
                }
                System.out.println("[!] Duplicate or invalid player name. Enter a unique name.");
            }
        }

        // Setup Team 2
        System.out.println("\n--- Entering Players for " + match.getTeam2().getName() + " ---");
        match.getTeam2().getPlayers().clear();
        match.getTeam2().getBattingOrder().clear();
        for (int i = 1; i <= numPlayers; i++) {
            while (true) {
                String pName = promptNonEmpty(String.format("Player %d name: ", i));
                if (match.getTeam2().addPlayer(pName)) {
                    break;
                }
                System.out.println("[!] Duplicate or invalid player name. Enter a unique name.");
            }
        }

        // Toss setup
        System.out.println("\n--- TOSS SETUP ---");
        System.out.println("Who won the toss?");
        System.out.println("1. " + match.getTeam1().getName());
        System.out.println("2. " + match.getTeam2().getName());
        String tossChoice = prompt("Select winner (1 or 2): ").trim();
        String tossWinner = tossChoice.equals("2") ? match.getTeam2().getName() : match.getTeam1().getName();
        match.setTossWinner(tossWinner);

        System.out.println("\nWhat did " + tossWinner + " elect to do?");
        System.out.println("1. Bat first");
        System.out.println("2. Bowl first");
        String electChoice = prompt("Select decision (1 or 2): ").trim();
        String decision = electChoice.equals("2") ? "BOWL" : "BAT";
        match.setTossDecision(decision);

        System.out.printf("\n[✓] Toss Result: %s won the toss and chose to %s first.\n", tossWinner, decision);
        System.out.println("Next step: Select option 3 to start the first innings.\n");
        pause();
    }

    // Option 3: Start First Innings
    private void handleStartFirstInnings() {
        Match match = engine.getMatch();
        if (match == null) {
            System.out.println("\n[!] Please create a match first (Option 1).");
            pause();
            return;
        }
        if (match.getTeam1().getPlayers().isEmpty() || match.getTeam2().getPlayers().isEmpty()) {
            System.out.println("\n[!] Please set up teams and players first (Option 2).");
            pause();
            return;
        }
        if (match.getFirstInnings() != null) {
            System.out.println("\n[!] First innings has already been started.");
            pause();
            return;
        }

        Team battingTeam = match.getTossDecision().equalsIgnoreCase("BAT")
                ? (match.getTossWinner().equalsIgnoreCase(match.getTeam1().getName()) ? match.getTeam1() : match.getTeam2())
                : (match.getTossWinner().equalsIgnoreCase(match.getTeam1().getName()) ? match.getTeam2() : match.getTeam1());
        Team bowlingTeam = battingTeam == match.getTeam1() ? match.getTeam2() : match.getTeam1();

        System.out.println("\n--- START FIRST INNINGS ---");
        System.out.printf("Batting Team: %s  |  Bowling Team: %s\n\n", battingTeam.getName(), bowlingTeam.getName());

        String striker = selectPlayerFromTeam(battingTeam, "Select Opening Striker (Batter 1):", Collections.emptySet());
        String nonStriker = selectPlayerFromTeam(battingTeam, "Select Opening Non-Striker (Batter 2):", Collections.singleton(striker));
        String bowler = selectPlayerFromTeam(bowlingTeam, "Select Opening Bowler:", Collections.emptySet());

        boolean started = engine.startFirstInnings(striker, nonStriker, bowler);
        if (started) {
            System.out.println("\n[✓] First innings started successfully!");
            System.out.printf("Striker: %s, Non-Striker: %s, Opening Bowler: %s\n\n", striker, nonStriker, bowler);
            System.out.println(ScorecardFormatter.formatLiveBanner(match));
        } else {
            System.out.println("\n[!] Failed to start first innings.");
        }
        pause();
    }

    // Option 4: Enter Ball-by-Ball Score
    private void handleEnterBallByBallScore() {
        Match match = engine.getMatch();
        if (match == null || match.getCurrentInnings() == null) {
            System.out.println("\n[!] No active innings in progress. Start an innings first (Option 3).");
            pause();
            return;
        }

        Innings inn = match.getCurrentInnings();
        if (inn.isCompleted()) {
            if (inn.getInningsNumber() == 1 && match.getSecondInnings() == null) {
                promptStartSecondInnings();
                return;
            } else {
                System.out.println("\n[!] Match has already concluded.");
                System.out.println(ScorecardFormatter.formatMatchSummary(match));
                pause();
                return;
            }
        }

        // Ball by ball entry loop
        boolean inScoringSession = true;
        while (inScoringSession) {
            inn = match.getCurrentInnings();
            if (inn == null || inn.isCompleted()) {
                if (inn != null && inn.getInningsNumber() == 1 && match.getSecondInnings() == null) {
                    promptStartSecondInnings();
                } else {
                    System.out.println("\n" + ScorecardFormatter.formatMatchSummary(match));
                }
                break;
            }

            System.out.println();
            System.out.println(ScorecardFormatter.formatLiveBanner(match));
            System.out.println("\nEnter Delivery Outcome:");
            System.out.println("  [0-6] Runs off bat (0, 1, 2, 3, 4, 6)");
            System.out.println("  [W]   Wide ball");
            System.out.println("  [NB]  No-ball");
            System.out.println("  [B]   Byes");
            System.out.println("  [LB]  Leg Byes");
            System.out.println("  [OUT] Wicket");
            System.out.println("  [U]   Undo last delivery");
            System.out.println("  [S]   Swap strike manually");
            System.out.println("  [M]   Return to Main Menu");

            String input = prompt("Delivery command: ").trim().toUpperCase();

            if (input.equals("M")) {
                inScoringSession = false;
                break;
            } else if (input.equals("U")) {
                if (engine.undoLastDelivery()) {
                    System.out.println("\n[✓] Last delivery successfully undone.");
                } else {
                    System.out.println("\n[!] Cannot undo further.");
                }
                continue;
            } else if (input.equals("S")) {
                engine.manualSwapStrike();
                System.out.println("\n[✓] Strike swapped manually.");
                continue;
            }

            // Process scoring input
            DeliveryResult res = processDeliveryInput(input, inn);
            if (!res.success) {
                System.out.println("\n[!] " + res.message);
                continue;
            }

            // If a wicket fell and team not all-out, ask for new batsman
            if (res.needNewBatsman) {
                promptSelectNextBatsman(inn);
            }

            // If over completed and innings not completed, ask for next bowler
            if (res.overCompleted && !res.inningsCompleted) {
                promptSelectNextBowler(inn);
            }

            // Check if innings ended
            if (res.inningsCompleted) {
                System.out.println("\n" + res.message);
                if (inn.getInningsNumber() == 1) {
                    promptStartSecondInnings();
                    inScoringSession = false;
                } else {
                    System.out.println("\n" + ScorecardFormatter.formatMatchSummary(match));
                    inScoringSession = false;
                    pause();
                }
            }
        }
    }

    private DeliveryResult processDeliveryInput(String input, Innings inn) {
        if (input.matches("^[0-6]$")) {
            int runs = Integer.parseInt(input);
            if (runs == 5) {
                // Rare 5 runs, allowed
            }
            return engine.recordDelivery(runs, ExtraType.NONE, 0, null, null, null);
        } else if (input.equals("W") || input.startsWith("W+")) {
            int extraRuns = 1; // default wide = 1
            if (input.startsWith("W+")) {
                try {
                    extraRuns += Integer.parseInt(input.substring(2));
                } catch (Exception ignored) {}
            } else {
                String addRuns = prompt("Additional runs taken on wide (e.g. overthrows/byes) [0]: ").trim();
                if (!addRuns.isEmpty()) {
                    try {
                        extraRuns += Integer.parseInt(addRuns);
                    } catch (Exception ignored) {}
                }
            }
            return engine.recordDelivery(0, ExtraType.WIDE, extraRuns, null, null, null);
        } else if (input.equals("NB") || input.startsWith("NB+")) {
            int batRuns = 0;
            int byeRuns = 0;
            System.out.println("How were additional runs scored on No-ball?");
            System.out.println("1. Off the bat (credited to batsman)");
            System.out.println("2. Byes / Leg-byes (extras)");
            System.out.println("3. No additional runs (1 NB penalty only)");
            String nbOpt = prompt("Select option (1-3) [Default 3]: ").trim();
            if (nbOpt.equals("1")) {
                batRuns = promptInt("Runs scored off bat (0-6): ", 0, 6);
            } else if (nbOpt.equals("2")) {
                byeRuns = promptInt("Byes taken on no-ball: ", 1, 6);
            }
            return engine.recordDelivery(batRuns, ExtraType.NO_BALL, byeRuns, null, null, null);
        } else if (input.equals("B")) {
            int byes = promptInt("Enter Bye runs (1-6): ", 1, 6);
            return engine.recordDelivery(0, ExtraType.BYE, byes, null, null, null);
        } else if (input.equals("LB")) {
            int legByes = promptInt("Enter Leg Bye runs (1-6): ", 1, 6);
            return engine.recordDelivery(0, ExtraType.LEG_BYE, legByes, null, null, null);
        } else if (input.equals("OUT")) {
            return handleWicketPrompt(inn);
        } else {
            return new DeliveryResult(false, "Unrecognized command. Enter 0-6, W, NB, B, LB, OUT, U, S, or M.", false, false, false);
        }
    }

    private DeliveryResult handleWicketPrompt(Innings inn) {
        System.out.println("\n--- WICKET DISMISSAL ---");
        System.out.println("1. Bowled");
        System.out.println("2. Caught");
        System.out.println("3. LBW");
        System.out.println("4. Stumped");
        System.out.println("5. Hit Wicket");
        System.out.println("6. Run Out");

        String wChoice = prompt("Select Dismissal Type (1-6): ").trim();
        DismissalType type;
        String dismissedPlayer = inn.getCurrentStriker();
        String fielder = null;
        int completedRuns = 0;

        switch (wChoice) {
            case "1":
                type = DismissalType.BOWLED;
                break;
            case "2":
                type = DismissalType.CAUGHT;
                fielder = promptNonEmpty("Enter Fielder name who took the catch: ");
                break;
            case "3":
                type = DismissalType.LBW;
                break;
            case "4":
                type = DismissalType.STUMPED;
                fielder = promptNonEmpty("Enter Wicketkeeper name: ");
                break;
            case "5":
                type = DismissalType.HIT_WICKET;
                break;
            case "6":
                type = DismissalType.RUN_OUT;
                System.out.println("Which batsman was run out?");
                System.out.println("1. Striker (" + inn.getCurrentStriker() + ")");
                System.out.println("2. Non-Striker (" + inn.getCurrentNonStriker() + ")");
                String rChoice = prompt("Choose (1 or 2): ").trim();
                dismissedPlayer = rChoice.equals("2") ? inn.getCurrentNonStriker() : inn.getCurrentStriker();
                fielder = promptNonEmpty("Enter Fielder name involved in run out: ");
                completedRuns = promptInt("Completed runs before run-out (0-3) [Default 0]: ", 0, 3);
                break;
            default:
                return new DeliveryResult(false, "Invalid dismissal option.", false, false, false);
        }

        return engine.recordDelivery(completedRuns, ExtraType.NONE, 0, type, dismissedPlayer, fielder);
    }

    private void promptSelectNextBatsman(Innings inn) {
        System.out.println("\n[!] WICKET FELL! Select the incoming batsman.");
        List<String> unbatted = new ArrayList<>();
        for (String pName : inn.getBattingTeam().getBattingOrder()) {
            Player p = inn.getBattingTeam().getPlayer(pName);
            if (p != null && !p.isHasBatted()) {
                unbatted.add(p.getName());
            }
        }

        if (unbatted.isEmpty()) {
            return;
        }

        System.out.println("Available Batsmen:");
        for (int i = 0; i < unbatted.size(); i++) {
            System.out.printf("  %d. %s\n", i + 1, unbatted.get(i));
        }

        while (true) {
            String choice = prompt("Select next batsman (1-" + unbatted.size() + "): ").trim();
            try {
                int idx = Integer.parseInt(choice) - 1;
                if (idx >= 0 && idx < unbatted.size()) {
                    String selected = unbatted.get(idx);
                    engine.setNextBatsman(selected);
                    System.out.println("[✓] " + selected + " is the new batsman at the crease.\n");
                    break;
                }
            } catch (Exception ignored) {}
            System.out.println("[!] Invalid choice. Select from the numbered list.");
        }
    }

    private void promptSelectNextBowler(Innings inn) {
        System.out.println("\n[!] OVER COMPLETED! Select bowler for the next over.");
        String prevBowler = inn.getPreviousBowler();
        List<String> available = new ArrayList<>();

        for (Player p : inn.getBowlingTeam().getPlayers()) {
            if (prevBowler == null || !p.getName().equalsIgnoreCase(prevBowler)) {
                available.add(p.getName());
            }
        }

        System.out.println("Available Bowlers (Note: " + prevBowler + " cannot bowl consecutive overs):");
        for (int i = 0; i < available.size(); i++) {
            System.out.printf("  %d. %s\n", i + 1, available.get(i));
        }

        while (true) {
            String choice = prompt("Select next bowler (1-" + available.size() + "): ").trim();
            try {
                int idx = Integer.parseInt(choice) - 1;
                if (idx >= 0 && idx < available.size()) {
                    String selected = available.get(idx);
                    engine.setNextBowler(selected);
                    System.out.println("[✓] " + selected + " will bowl the next over.\n");
                    break;
                }
            } catch (Exception ignored) {}
            System.out.println("[!] Invalid choice. Select from the numbered list.");
        }
    }

    private void promptStartSecondInnings() {
        Match match = engine.getMatch();
        if (match == null || match.getFirstInnings() == null) return;

        Innings inn1 = match.getFirstInnings();
        int target = inn1.getTotalRuns() + 1;

        System.out.println("\n========================================");
        System.out.println("         FIRST INNINGS CONCLUDED");
        System.out.println("========================================");
        System.out.printf(" %s scored %d/%d in %s overs.\n",
                inn1.getBattingTeam().getName(), inn1.getTotalRuns(),
                inn1.getWickets(), inn1.getCurrentOversFormatted());
        System.out.printf(" Target for %s: %d runs from %d overs (RRR: %.2f)\n",
                inn1.getBowlingTeam().getName(), target,
                match.getAllocatedOvers(), (target * 6.0) / (match.getAllocatedOvers() * 6.0));
        System.out.println("========================================\n");

        String ans = prompt("Would you like to start the 2nd Innings now? (y/n) [y]: ").trim();
        if (ans.isEmpty() || ans.equalsIgnoreCase("y")) {
            Team battingTeam = inn1.getBowlingTeam();
            Team bowlingTeam = inn1.getBattingTeam();

            System.out.println("\n--- START SECOND INNINGS ---");
            System.out.printf("Chasing Team: %s  |  Defending Team: %s\n\n", battingTeam.getName(), bowlingTeam.getName());

            String striker = selectPlayerFromTeam(battingTeam, "Select Opening Striker (Batter 1):", Collections.emptySet());
            String nonStriker = selectPlayerFromTeam(battingTeam, "Select Opening Non-Striker (Batter 2):", Collections.singleton(striker));
            String bowler = selectPlayerFromTeam(bowlingTeam, "Select Opening Bowler:", Collections.emptySet());

            engine.startSecondInnings(striker, nonStriker, bowler);
            System.out.println("\n[✓] Second innings started successfully!\n");
            handleEnterBallByBallScore();
        } else {
            System.out.println("You can start the second innings anytime from option 4.");
            pause();
        }
    }

    // Option 5: View Live Score
    private void handleViewLiveScore() {
        Match match = engine.getMatch();
        if (match == null) {
            System.out.println("\n[!] No match available.");
        } else {
            System.out.println();
            System.out.println(ScorecardFormatter.formatLiveBanner(match));
        }
        pause();
    }

    // Option 6: View Batting Scorecard
    private void handleViewBattingScorecard() {
        Match match = engine.getMatch();
        if (match == null) {
            System.out.println("\n[!] No match available.");
            pause();
            return;
        }

        Innings targetInn = selectInnings(match);
        if (targetInn != null) {
            System.out.println();
            System.out.println(ScorecardFormatter.formatBattingScorecard(targetInn));
        }
        pause();
    }

    // Option 7: View Bowling Scorecard
    private void handleViewBowlingScorecard() {
        Match match = engine.getMatch();
        if (match == null) {
            System.out.println("\n[!] No match available.");
            pause();
            return;
        }

        Innings targetInn = selectInnings(match);
        if (targetInn != null) {
            System.out.println();
            System.out.println(ScorecardFormatter.formatBowlingScorecard(targetInn));
        }
        pause();
    }

    // Option 8: View Fall of Wickets
    private void handleViewFallOfWickets() {
        Match match = engine.getMatch();
        if (match == null) {
            System.out.println("\n[!] No match available.");
            pause();
            return;
        }

        Innings targetInn = selectInnings(match);
        if (targetInn != null) {
            System.out.println();
            System.out.println(ScorecardFormatter.formatFallOfWickets(targetInn));
        }
        pause();
    }

    // Option 9: View Partnership Statistics
    private void handleViewPartnerships() {
        Match match = engine.getMatch();
        if (match == null) {
            System.out.println("\n[!] No match available.");
            pause();
            return;
        }

        Innings targetInn = selectInnings(match);
        if (targetInn != null) {
            System.out.println();
            System.out.println(ScorecardFormatter.formatPartnerships(targetInn));
        }
        pause();
    }

    // Option 10: View Match Summary
    private void handleViewMatchSummary() {
        Match match = engine.getMatch();
        if (match == null) {
            System.out.println("\n[!] No match available.");
        } else {
            System.out.println();
            System.out.println(ScorecardFormatter.formatMatchSummary(match));
        }
        pause();
    }

    // Option 11: Save Match Data
    private void handleSaveMatch() {
        Match match = engine.getMatch();
        if (match == null) {
            System.out.println("\n[!] No match available to save.");
            pause();
            return;
        }

        System.out.println("\n--- SAVE MATCH DATA ---");
        System.out.println("1. Save as JSON");
        System.out.println("2. Save as CSV");
        System.out.println("3. Save both JSON & CSV");
        String format = prompt("Select format (1-3) [Default: 3]: ").trim();

        String defaultFilename = match.getTeam1().getName().replaceAll("\\s+", "_") + "_vs_"
                + match.getTeam2().getName().replaceAll("\\s+", "_") + "_" + match.getMatchId();

        File sampleDir = new File("sample_data");
        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }

        try {
            if (format.equals("1") || format.equals("3") || format.isEmpty()) {
                File jsonFile = new File(sampleDir, defaultFilename + ".json");
                MatchStorage.saveToJson(match, jsonFile);
                System.out.println("[✓] Saved JSON to: " + jsonFile.getAbsolutePath());
            }
            if (format.equals("2") || format.equals("3") || format.isEmpty()) {
                File csvFile = new File(sampleDir, defaultFilename + ".csv");
                MatchStorage.saveToCsv(match, csvFile);
                System.out.println("[✓] Saved CSV to: " + csvFile.getAbsolutePath());
            }
            System.out.println("\nMatch data saved successfully!");
        } catch (Exception e) {
            System.out.println("[!] Error saving match: " + e.getMessage());
        }
        pause();
    }

    // Option 12: Load Previous Match
    private void handleLoadMatch() {
        System.out.println("\n--- LOAD PREVIOUS MATCH ---");
        File sampleDir = new File("sample_data");
        File[] jsonFiles = sampleDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        if (jsonFiles != null && jsonFiles.length > 0) {
            System.out.println("Available Matches in sample_data:");
            for (int i = 0; i < jsonFiles.length; i++) {
                System.out.printf("  %d. %s\n", i + 1, jsonFiles[i].getName());
            }
            System.out.println("  0. Specify custom file path");

            String choice = prompt("Select match (0-" + jsonFiles.length + "): ").trim();
            File targetFile = null;
            try {
                int idx = Integer.parseInt(choice);
                if (idx >= 1 && idx <= jsonFiles.length) {
                    targetFile = jsonFiles[idx - 1];
                }
            } catch (Exception ignored) {}

            if (targetFile == null) {
                String path = promptNonEmpty("Enter absolute or relative file path to JSON: ");
                targetFile = new File(path);
            }

            try {
                Match loaded = MatchStorage.loadFromJson(targetFile);
                if (loaded != null) {
                    engine.setMatch(loaded);
                    System.out.println("\n[✓] Match loaded successfully from: " + targetFile.getName());
                    System.out.println(ScorecardFormatter.formatLiveBanner(loaded));
                } else {
                    System.out.println("\n[!] Failed to parse match data from file.");
                }
            } catch (Exception e) {
                System.out.println("\n[!] Error loading match: " + e.getMessage());
            }
        } else {
            String path = promptNonEmpty("Enter file path to match JSON: ");
            File targetFile = new File(path);
            try {
                Match loaded = MatchStorage.loadFromJson(targetFile);
                if (loaded != null) {
                    engine.setMatch(loaded);
                    System.out.println("\n[✓] Match loaded successfully!");
                    System.out.println(ScorecardFormatter.formatLiveBanner(loaded));
                } else {
                    System.out.println("\n[!] Failed to parse match data.");
                }
            } catch (Exception e) {
                System.out.println("\n[!] Error loading match: " + e.getMessage());
            }
        }
        pause();
    }

    // Option 13: Undo Last Delivery
    private void handleUndoLastDelivery() {
        if (engine.undoLastDelivery()) {
            System.out.println("\n[✓] Last delivery successfully undone!");
            System.out.println(ScorecardFormatter.formatLiveBanner(engine.getMatch()));
        } else {
            System.out.println("\n[!] Cannot undo: no deliveries to undo or start of match reached.");
        }
        pause();
    }

    // Helper functions
    private boolean confirmExit() {
        Match match = engine.getMatch();
        if (match != null && match.getStatus() != MatchStatus.COMPLETED && match.getStatus() != MatchStatus.NOT_STARTED) {
            String ans = prompt("An unfinished match is currently active. Are you sure you want to exit? (y/n): ");
            return ans.equalsIgnoreCase("y");
        }
        return true;
    }

    private Innings selectInnings(Match match) {
        if (match.getSecondInnings() == null) {
            return match.getFirstInnings();
        }
        System.out.println("\nSelect Innings:");
        System.out.println("1. 1st Innings (" + match.getFirstInnings().getBattingTeam().getName() + ")");
        System.out.println("2. 2nd Innings (" + match.getSecondInnings().getBattingTeam().getName() + ")");
        String choice = prompt("Enter choice (1 or 2) [Default: 2]: ").trim();
        return choice.equals("1") ? match.getFirstInnings() : match.getSecondInnings();
    }

    private String selectPlayerFromTeam(Team team, String heading, Set<String> excludedNames) {
        System.out.println(heading);
        List<String> available = new ArrayList<>();
        for (Player p : team.getPlayers()) {
            if (!excludedNames.contains(p.getName())) {
                available.add(p.getName());
            }
        }
        for (int i = 0; i < available.size(); i++) {
            System.out.printf("  %d. %s\n", i + 1, available.get(i));
        }
        while (true) {
            String choice = prompt("Select player (1-" + available.size() + "): ").trim();
            try {
                int idx = Integer.parseInt(choice) - 1;
                if (idx >= 0 && idx < available.size()) {
                    return available.get(idx);
                }
            } catch (Exception ignored) {}
            System.out.println("[!] Invalid selection. Please enter a valid number.");
        }
    }

    private String prompt(String text) {
        System.out.print(text);
        if (!scanner.hasNextLine()) {
            return "14";
        }
        try {
            return scanner.nextLine();
        } catch (NoSuchElementException e) {
            return "14";
        }
    }

    private String promptNonEmpty(String text) {
        while (true) {
            if (!scanner.hasNextLine()) {
                return "";
            }
            String res = prompt(text).trim();
            if (!res.isEmpty()) {
                return res;
            }
            System.out.println("[!] Input cannot be empty. Please try again.");
        }
    }

    private int promptInt(String text, int min, int max) {
        while (true) {
            if (!scanner.hasNextLine()) {
                return min;
            }
            String input = prompt(text).trim();
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) {
                    return val;
                }
            } catch (Exception ignored) {}
            System.out.printf("[!] Please enter an integer between %d and %d.\n", min, max);
        }
    }

    private void pause() {
        System.out.print("\nPress Enter to return to menu...");
        if (!scanner.hasNextLine()) {
            return;
        }
        try {
            scanner.nextLine();
        } catch (Exception ignored) {}
    }
}
