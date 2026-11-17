package cricstats.engine;

import cricstats.models.*;

import java.util.*;

public class CricketScoringEngine {
    private Match match;
    private final Deque<Match> undoStack = new ArrayDeque<>();

    public CricketScoringEngine() {
    }

    public CricketScoringEngine(Match match) {
        this.match = match;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
        this.undoStack.clear();
    }

    // 1. Create Match
    public Match createMatch(String team1Name, String team2Name, int allocatedOvers) {
        Team t1 = new Team(team1Name);
        Team t2 = new Team(team2Name);
        this.match = new Match(t1, t2, allocatedOvers);
        this.undoStack.clear();
        return this.match;
    }

    // 2. Setup Teams and Players
    public boolean addPlayerToTeam(int teamNumber, String playerName) {
        if (match == null) return false;
        Team team = (teamNumber == 1) ? match.getTeam1() : match.getTeam2();
        if (team == null) return false;
        return team.addPlayer(playerName);
    }

    // 3. Batting order setup
    public boolean setupBattingOrder(int teamNumber, List<String> orderedPlayerNames) {
        if (match == null) return false;
        Team team = (teamNumber == 1) ? match.getTeam1() : match.getTeam2();
        if (team == null) return false;

        if (orderedPlayerNames.size() != team.getPlayers().size()) {
            return false;
        }

        // Validate all players belong to team
        for (String pName : orderedPlayerNames) {
            if (!team.containsPlayer(pName)) {
                return false;
            }
        }

        team.setBattingOrder(new ArrayList<>(orderedPlayerNames));
        return true;
    }

    // Initialize 1st Innings
    public boolean startFirstInnings(String strikerName, String nonStrikerName, String openingBowlerName) {
        if (match == null) return false;

        Team battingTeam;
        Team bowlingTeam;

        // Determine batting team based on toss
        if ("BAT".equalsIgnoreCase(match.getTossDecision())) {
            battingTeam = match.getTossWinner().equalsIgnoreCase(match.getTeam1().getName()) ? match.getTeam1() : match.getTeam2();
            bowlingTeam = battingTeam == match.getTeam1() ? match.getTeam2() : match.getTeam1();
        } else {
            bowlingTeam = match.getTossWinner().equalsIgnoreCase(match.getTeam1().getName()) ? match.getTeam1() : match.getTeam2();
            battingTeam = bowlingTeam == match.getTeam1() ? match.getTeam2() : match.getTeam1();
        }

        Innings inn1 = new Innings(1, battingTeam, bowlingTeam, match.getAllocatedOvers(), -1);
        inn1.setCurrentStriker(strikerName);
        inn1.setCurrentNonStriker(nonStrikerName);
        inn1.setCurrentBowler(openingBowlerName);
        inn1.setNextBattingIndex(2); // positions 0 and 1 are opening batsmen

        // Mark opening batsmen as having batted
        Player pStriker = battingTeam.getPlayer(strikerName);
        if (pStriker != null) pStriker.setHasBatted(true);
        Player pNonStriker = battingTeam.getPlayer(nonStrikerName);
        if (pNonStriker != null) pNonStriker.setHasBatted(true);

        inn1.setCurrentPartnership(new Partnership(1, strikerName, nonStrikerName));
        inn1.setCurrentOver(new Over(1, openingBowlerName));

        match.setFirstInnings(inn1);
        match.setCurrentInningsNumber(1);
        match.setStatus(MatchStatus.INNINGS_1_IN_PROGRESS);

        this.undoStack.clear();
        return true;
    }

    // Initialize 2nd Innings
    public boolean startSecondInnings(String strikerName, String nonStrikerName, String openingBowlerName) {
        if (match == null || match.getFirstInnings() == null) return false;

        Team battingTeam = match.getFirstInnings().getBowlingTeam();
        Team bowlingTeam = match.getFirstInnings().getBattingTeam();
        int target = match.getFirstInnings().getTotalRuns() + 1;

        Innings inn2 = new Innings(2, battingTeam, bowlingTeam, match.getAllocatedOvers(), target);
        inn2.setCurrentStriker(strikerName);
        inn2.setCurrentNonStriker(nonStrikerName);
        inn2.setCurrentBowler(openingBowlerName);
        inn2.setNextBattingIndex(2);

        Player pStriker = battingTeam.getPlayer(strikerName);
        if (pStriker != null) pStriker.setHasBatted(true);
        Player pNonStriker = battingTeam.getPlayer(nonStrikerName);
        if (pNonStriker != null) pNonStriker.setHasBatted(true);

        inn2.setCurrentPartnership(new Partnership(1, strikerName, nonStrikerName));
        inn2.setCurrentOver(new Over(1, openingBowlerName));

        match.setSecondInnings(inn2);
        match.setCurrentInningsNumber(2);
        match.setStatus(MatchStatus.INNINGS_2_IN_PROGRESS);

        this.undoStack.clear();
        return true;
    }

    // Save state for undo
    public void pushSnapshot() {
        if (match != null) {
            undoStack.push(new Match(match));
        }
    }

    // Undo last delivery
    public boolean undoLastDelivery() {
        if (undoStack.isEmpty()) {
            return false;
        }

        // Restore previous state
        Match previousState = undoStack.pop();
        if (previousState != null) {
            this.match = new Match(previousState);
            return true;
        }
        return false;
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    // Record a delivery with complete cricket rules
    public DeliveryResult recordDelivery(int runsBat, ExtraType extraType, int extraRuns,
                                         DismissalType dismissalType, String dismissedPlayerName,
                                         String fielderName) {
        if (match == null) {
            return new DeliveryResult(false, "No active match found.", false, false, false);
        }

        Innings inn = match.getCurrentInnings();
        if (inn == null || inn.isCompleted()) {
            return new DeliveryResult(false, "Innings is already completed or not started.", false, false, false);
        }

        Player striker = inn.getBattingTeam().getPlayer(inn.getCurrentStriker());
        Player nonStriker = inn.getBattingTeam().getPlayer(inn.getCurrentNonStriker());
        Player bowler = inn.getBowlingTeam().getPlayer(inn.getCurrentBowler());

        if (striker == null || nonStriker == null || bowler == null) {
            return new DeliveryResult(false, "Active players (striker/non-striker/bowler) are not set properly.", false, false, false);
        }

        // Push snapshot before mutating state
        pushSnapshot();

        Delivery delivery = new Delivery();
        delivery.setDeliveryIndex(inn.getDeliveries().size() + 1);
        delivery.setStriker(striker.getName());
        delivery.setNonStriker(nonStriker.getName());
        delivery.setBowler(bowler.getName());
        delivery.setOverNumber(inn.getCompletedOvers().size() + 1);

        boolean isLegal = true;
        int bowlerRunsConceded = 0;
        int totalBallRuns = 0;
        boolean countBatsmanBall = true;
        boolean rotateStrike = false;

        // Process Extras
        if (extraType == ExtraType.WIDE) {
            isLegal = false;
            countBatsmanBall = false;
            int totalWide = extraRuns <= 0 ? 1 : extraRuns; // 1 standard wide + extra runs
            inn.setWideRuns(inn.getWideRuns() + totalWide);
            bowlerRunsConceded += totalWide;
            bowler.addWidesConceded(totalWide);
            totalBallRuns += totalWide;
            delivery.setExtraType(ExtraType.WIDE);
            delivery.setExtraRuns(totalWide);

            // On wide, if batsmen ran odd runs (e.g. 1 bye/run on wide), strike rotates
            if ((totalWide - 1) % 2 != 0) {
                rotateStrike = true;
            }
        } else if (extraType == ExtraType.NO_BALL) {
            isLegal = false;
            countBatsmanBall = true;
            int nbPenalty = 1;
            inn.setNoBallRuns(inn.getNoBallRuns() + nbPenalty);
            bowler.addNoBallsConceded(nbPenalty);
            bowlerRunsConceded += nbPenalty;
            totalBallRuns += nbPenalty;

            // Bat runs or byes off no-ball
            if (runsBat > 0) {
                striker.addRuns(runsBat);
                bowlerRunsConceded += runsBat;
                totalBallRuns += runsBat;
                delivery.setRunsBat(runsBat);
                if (runsBat % 2 != 0) {
                    rotateStrike = true;
                }
            } else if (extraRuns > 0) {
                // Byes off no-ball
                inn.setByeRuns(inn.getByeRuns() + extraRuns);
                totalBallRuns += extraRuns;
                if (extraRuns % 2 != 0) {
                    rotateStrike = true;
                }
            }
            delivery.setExtraType(ExtraType.NO_BALL);
            delivery.setExtraRuns(nbPenalty + (runsBat == 0 ? extraRuns : 0));
        } else if (extraType == ExtraType.BYE) {
            isLegal = true;
            countBatsmanBall = true;
            int byes = extraRuns > 0 ? extraRuns : runsBat;
            inn.setByeRuns(inn.getByeRuns() + byes);
            totalBallRuns += byes;
            delivery.setExtraType(ExtraType.BYE);
            delivery.setExtraRuns(byes);
            // Byes are NOT charged to bowler
            if (byes % 2 != 0) {
                rotateStrike = true;
            }
        } else if (extraType == ExtraType.LEG_BYE) {
            isLegal = true;
            countBatsmanBall = true;
            int legByes = extraRuns > 0 ? extraRuns : runsBat;
            inn.setLegByeRuns(inn.getLegByeRuns() + legByes);
            totalBallRuns += legByes;
            delivery.setExtraType(ExtraType.LEG_BYE);
            delivery.setExtraRuns(legByes);
            // Leg byes are NOT charged to bowler
            if (legByes % 2 != 0) {
                rotateStrike = true;
            }
        } else {
            // Normal ball
            isLegal = true;
            countBatsmanBall = true;
            striker.addRuns(runsBat);
            bowlerRunsConceded += runsBat;
            totalBallRuns += runsBat;
            delivery.setRunsBat(runsBat);
            delivery.setExtraType(ExtraType.NONE);
            delivery.setExtraRuns(0);
            if (runsBat % 2 != 0) {
                rotateStrike = true;
            }
        }

        // Batsman balls faced
        if (countBatsmanBall) {
            striker.incrementBallsFaced();
        }

        // Legal ball counters
        delivery.setLegal(isLegal);
        if (isLegal) {
            inn.setLegalBalls(inn.getLegalBalls() + 1);
            bowler.incrementLegalBallsBowled();
            int ballInOver = (inn.getLegalBalls() - 1) % 6 + 1;
            delivery.setLegalBallNumber(ballInOver);
            int overNum = (inn.getLegalBalls() - 1) / 6;
            delivery.setDisplayOverBall(overNum + "." + ballInOver);
        } else {
            int overNum = inn.getLegalBalls() / 6;
            int ballInOver = inn.getLegalBalls() % 6;
            delivery.setDisplayOverBall(overNum + "." + ballInOver + " (" + (extraType == ExtraType.WIDE ? "wd" : "nb") + ")");
        }

        // Update total runs and bowler runs
        delivery.setRunsConcededBowler(bowlerRunsConceded);
        delivery.setTotalRuns(totalBallRuns);
        bowler.addRunsConceded(bowlerRunsConceded);
        inn.setTotalRuns(inn.getTotalRuns() + totalBallRuns);

        // Update partnership
        if (inn.getCurrentPartnership() != null) {
            inn.getCurrentPartnership().addDelivery(striker.getName(),
                    delivery.getRunsBat(),
                    delivery.getTotalRuns() - delivery.getRunsBat(),
                    countBatsmanBall);
        }

        // Check Dismissal / Wicket
        boolean wicketFell = false;
        boolean needNewBatsman = false;
        if (dismissalType != null) {
            wicketFell = true;
            Dismissal dismissal = new Dismissal();
            dismissal.setType(dismissalType);
            dismissal.setBowler(bowler.getName());
            dismissal.setFielder(fielderName);

            String outBatsmanName = (dismissedPlayerName != null && !dismissedPlayerName.trim().isEmpty())
                    ? dismissedPlayerName.trim()
                    : striker.getName();

            dismissal.setDismissedPlayer(outBatsmanName);
            delivery.setDismissal(dismissal);

            Player outPlayer = inn.getBattingTeam().getPlayer(outBatsmanName);
            if (outPlayer != null) {
                outPlayer.setDismissal(dismissal);
            }

            // Credit bowler if bowler wicket
            if (dismissal.isBowlerWicket()) {
                bowler.incrementWickets();
            }

            // Credit fielder
            if (fielderName != null && !fielderName.trim().isEmpty()) {
                Player fielder = inn.getBowlingTeam().getPlayer(fielderName);
                if (fielder != null) {
                    if (dismissalType == DismissalType.CAUGHT) {
                        fielder.incrementCatches();
                    } else if (dismissalType == DismissalType.STUMPED) {
                        fielder.incrementStumpings();
                    } else if (dismissalType == DismissalType.RUN_OUT) {
                        fielder.incrementRunOuts();
                    }
                }
            }

            inn.setWickets(inn.getWickets() + 1);

            // Record Fall of Wicket
            int partRuns = inn.getCurrentPartnership() != null ? inn.getCurrentPartnership().getRuns() : 0;
            int partBalls = inn.getCurrentPartnership() != null ? inn.getCurrentPartnership().getBalls() : 0;
            FallOfWicket fow = new FallOfWicket(inn.getWickets(), outBatsmanName, inn.getTotalRuns(),
                    inn.getCurrentOversFormatted(), partRuns, partBalls);
            inn.getFallOfWickets().add(fow);

            if (inn.getCurrentPartnership() != null) {
                inn.getCurrentPartnership().setCurrent(false);
                inn.getPastPartnerships().add(inn.getCurrentPartnership());
            }

            // If not all out, we need a new batsman
            if (!inn.isAllOut()) {
                needNewBatsman = true;
            }
        }

        // Apply strike rotation for odd runs
        delivery.setStrikeRotated(rotateStrike);
        if (rotateStrike) {
            inn.swapStrike();
        }

        // Add delivery to current over and innings deliveries list
        if (inn.getCurrentOver() == null) {
            inn.setCurrentOver(new Over(inn.getCompletedOvers().size() + 1, bowler.getName()));
        }
        inn.getCurrentOver().addDelivery(delivery);
        inn.getDeliveries().add(delivery);

        // Check 2nd innings target chase immediate win
        if (inn.getInningsNumber() == 2 && inn.isTargetReached()) {
            inn.setCompleted(true);
            inn.setCompletionReason("Target Reached");
            match.evaluateMatchResult();
            return new DeliveryResult(true, "Target reached! Match finished.", wicketFell, false, true);
        }

        // Check if All Out
        if (inn.isAllOut()) {
            inn.setCompleted(true);
            inn.setCompletionReason("All Out");
            handleInningsEnd(inn);
            return new DeliveryResult(true, "Team is All Out! Innings finished.", wicketFell, false, inn.isCompleted());
        }

        // Check Over Completion
        boolean overComplete = false;
        if (inn.getCurrentOver().getLegalBalls() >= 6) {
            overComplete = true;
            Over finishedOver = inn.getCurrentOver();
            inn.getCompletedOvers().add(finishedOver);

            // Check maiden
            if (finishedOver.isMaiden()) {
                bowler.incrementMaidens();
            }

            // Swap strike at end of over
            inn.swapStrike();

            // Set previous bowler
            inn.setPreviousBowler(bowler.getName());

            // Check if allocated overs completed
            if (inn.isAllocatedOversCompleted()) {
                inn.setCompleted(true);
                inn.setCompletionReason("Overs Completed");
                handleInningsEnd(inn);
                return new DeliveryResult(true, "Allocated overs completed! Innings finished.", wicketFell, true, true);
            } else {
                // Initialize next over
                inn.setCurrentOver(new Over(inn.getCompletedOvers().size() + 1, ""));
            }
        }

        return new DeliveryResult(true, "Delivery recorded successfully.", wicketFell, overComplete, inn.isCompleted(), needNewBatsman);
    }

    private void handleInningsEnd(Innings inn) {
        if (inn.getInningsNumber() == 1) {
            match.setStatus(MatchStatus.INNINGS_BREAK);
        } else {
            match.setStatus(MatchStatus.COMPLETED);
            match.evaluateMatchResult();
        }
    }

    // Set next incoming batsman
    public boolean setNextBatsman(String newBatsmanName) {
        Innings inn = match.getCurrentInnings();
        if (inn == null) return false;

        Player newBatsman = inn.getBattingTeam().getPlayer(newBatsmanName);
        if (newBatsman == null || newBatsman.isOut() || newBatsman.isHasBatted()) {
            return false;
        }

        newBatsman.setHasBatted(true);

        // Determine which slot is empty (striker or non-striker)
        Player s = inn.getBattingTeam().getPlayer(inn.getCurrentStriker());
        Player ns = inn.getBattingTeam().getPlayer(inn.getCurrentNonStriker());

        if (s != null && s.isOut()) {
            inn.setCurrentStriker(newBatsman.getName());
        } else if (ns != null && ns.isOut()) {
            inn.setCurrentNonStriker(newBatsman.getName());
        } else {
            // Default replace striker
            inn.setCurrentStriker(newBatsman.getName());
        }

        // Start new partnership
        Partnership newPart = new Partnership(inn.getWickets() + 1, inn.getCurrentStriker(), inn.getCurrentNonStriker());
        inn.setCurrentPartnership(newPart);

        return true;
    }

    // Set next bowler for new over
    public boolean setNextBowler(String bowlerName) {
        Innings inn = match.getCurrentInnings();
        if (inn == null) return false;

        Player bowler = inn.getBowlingTeam().getPlayer(bowlerName);
        if (bowler == null) return false;

        // Rule: A bowler cannot bowl consecutive overs
        if (inn.getPreviousBowler() != null && inn.getPreviousBowler().equalsIgnoreCase(bowlerName)) {
            return false;
        }

        inn.setCurrentBowler(bowler.getName());
        if (inn.getCurrentOver() != null) {
            inn.getCurrentOver().setBowlerName(bowler.getName());
        }

        return true;
    }

    // Manual strike change
    public void manualSwapStrike() {
        Innings inn = match.getCurrentInnings();
        if (inn != null) {
            pushSnapshot();
            inn.swapStrike();
        }
    }

    // Delivery result helper class
    public static class DeliveryResult {
        public boolean success;
        public String message;
        public boolean wicketFell;
        public boolean overCompleted;
        public boolean inningsCompleted;
        public boolean needNewBatsman;

        public DeliveryResult(boolean success, String message, boolean wicketFell, boolean overCompleted, boolean inningsCompleted) {
            this(success, message, wicketFell, overCompleted, inningsCompleted, false);
        }

        public DeliveryResult(boolean success, String message, boolean wicketFell, boolean overCompleted, boolean inningsCompleted, boolean needNewBatsman) {
            this.success = success;
            this.message = message;
            this.wicketFell = wicketFell;
            this.overCompleted = overCompleted;
            this.inningsCompleted = inningsCompleted;
            this.needNewBatsman = needNewBatsman;
        }
    }
}
