# CricStats – Ball-by-Ball Cricket Scoring and Analytics Engine

A fully functional, menu-driven console application built in pure **Java** for scoring local, club, and college-level cricket matches. CricStats provides professional ball-by-ball scoring, real-time live match summaries, automatic batting and bowling analytics, partnership tracking, fall-of-wickets tables, robust undo history, JSON/CSV persistence, and an automated Player of the Match points calculator.

---

## 1. Key Features

### Feature 1: Team Setup and Batting Order
- Interactive team creation for Team 1 and Team 2.
- Configurable team sizes (2 to 11 players per team).
- Case-insensitive player validation preventing duplicate names or empty entries.
- Customizable batting order.
- Opening batsmen (striker and non-striker) and opening bowler selection.
- Toss simulation with choice of Batting or Bowling first.

### Feature 2: Ball-by-Ball Scoring & Cricket Rules
- **Deliveries supported**:
  - `0, 1, 2, 3, 4, 6` runs scored off the bat.
  - **Wide Balls (`W`)**: 1 penalty run + additional runs taken (byes/overthrows); illegal delivery; batsman faces 0 balls; strike rotates on odd runs taken.
  - **No-Balls (`NB`)**: 1 penalty run + runs off the bat or extras; illegal delivery; batsman faces 1 ball.
  - **Byes (`B`) & Leg-Byes (`LB`)**: Legal deliveries; batsman faces 1 ball; runs credited to team extras, **not** charged to bowler.
- **Dismissals supported**:
  - **Bowled (`b Bowler`)**: Credited to bowler.
  - **Caught (`c Fielder b Bowler`)**: Credited to bowler and catcher.
  - **LBW (`lbw b Bowler`)**: Credited to bowler.
  - **Stumped (`st Keeper b Bowler`)**: Credited to bowler and wicketkeeper.
  - **Hit Wicket (`hit wicket b Bowler`)**: Credited to bowler.
  - **Run-Out (`run out (Fielder)`)**: Bowler is **not** credited with a wicket; fielder credited with run-out; allows specifying striker or non-striker and completed runs before dismissal.
- **Strike Rotation**:
  - Automatically rotates strike on odd completed runs (1, 3 runs).
  - Automatically swaps strike at the end of each over (6 legal balls).
  - Manual strike swap command (`S`) supported at any time.
- **Over Management & Maidens**:
  - Overs tracked in standard cricket notation (e.g., `3.4` overs).
  - Bowler cannot bowl consecutive overs.
  - Maiden over automatically awarded if bowler concedes 0 runs in 6 legal balls.
- **Innings & Match Progression**:
  - Automatic innings termination when all-out (`wickets == teamSize - 1`) or allocated overs completed.
  - Automatic 2nd innings target calculation (`1st Innings Total + 1`).
  - Immediate match conclusion as soon as chasing team reaches the target.
  - Full Undo delivery support (`U` / Option 13) that seamlessly restores score, balls, wickets, player stats, maidens, partnerships, and strike.

### Feature 3: Automatic Batting & Bowling Statistics
- **Batting**: Runs, Balls Faced, Fours (4s), Sixes (6s), Strike Rate ($\frac{\text{Runs}}{\text{Balls Faced}} \times 100$), and Dismissal Details.
- **Bowling**: Overs Bowled (cricket format), Maiden Overs, Runs Conceded (excluding byes/leg-byes), Wickets Taken, Economy Rate ($\frac{\text{Runs Conceded}}{\text{Overs Bowled}}$), Wides Conceded, and No-Balls Conceded.

### Feature 4: Real-Time Run Rate & Required Run Rate
- Continuously displays:
  - Current Score (`Runs / Wickets`)
  - Overs Completed (`e.g., 4.2 / 20`)
  - Current Run Rate ($\text{CRR} = \frac{\text{Total Runs}}{\text{Overs Bowled}}$)
  - 2nd Innings Target Score
  - Runs Required & Balls Remaining
  - Required Run Rate ($\text{RRR} = \frac{\text{Runs Required}}{\text{Overs Remaining}}$)

### Feature 5: Complete Scorecard & Fall of Wickets
- Formatted Batting Scorecard with extras breakdown and total line.
- Formatted Bowling Scorecard with maiden and economy rate details.
- Fall of Wickets table showing wicket number, score, dismissed batsman, over, and partnership.
- Final match result displaying winning team and victory margin (runs or wickets) or tie.

### Stretch Feature A: Partnership Statistics
- Tracks partnership runs and balls for every wicket.
- Displays current live partnership and historical partnerships table.
- Resets partnership immediately upon dismissal.

### Stretch Feature B: Configurable Player of the Match Points
- Automatically evaluates all players across both teams using a points formula:
  - **1 point** per run scored.
  - **1 bonus point** per four.
  - **2 bonus points** per six.
  - **20 points** per bowler wicket.
  - **1 point** per maiden over.
  - **5 bonus points** for a half-century ($50+$ runs).
  - **10 bonus points** for a century ($100+$ runs).
  - **5 points** per catch, run-out, or stumping.
- Automatically selects the highest-scoring player and outputs a detailed performance breakdown.

---

## 2. Project Structure

```
e:/CricStats/
├── bin/                                # Compiled Java bytecode classes
├── cricstats/                          # Main Application Package
│   ├── models/                         # Domain Entities
│   │   ├── Delivery.java               # Single delivery details & stats
│   │   ├── Dismissal.java              # Dismissal metadata & description
│   │   ├── DismissalType.java          # Enum of dismissal modes
│   │   ├── ExtraType.java              # NONE, WIDE, NO_BALL, BYE, LEG_BYE
│   │   ├── FallOfWicket.java           # Wicket milestone record
│   │   ├── Innings.java                # Innings state, overs & stats
│   │   ├── Match.java                  # Match state, teams & result
│   │   ├── MatchStatus.java            # Match lifecycle enum
│   │   ├── Over.java                   # Over container & maiden calculation
│   │   ├── Partnership.java            # Partnership tracker
│   │   ├── Player.java                 # Player batting, bowling & fielding stats
│   │   ├── PlayerOfMatchConfig.java    # Configurable POM weights
│   │   └── Team.java                   # Team container & batting order
│   ├── engine/
│   │   └── CricketScoringEngine.java   # Scoring logic, validation & undo stack
│   ├── storage/
│   │   ├── JsonHelper.java             # Pure Java zero-dependency JSON parser & writer
│   │   └── MatchStorage.java           # JSON & CSV serialization / deserialization
│   ├── ui/
│   │   ├── ConsoleUI.java              # 14-option menu & interactive scoring loop
│   │   └── ScorecardFormatter.java     # ASCII table formatters & live banners
│   └── CricStatsApp.java               # Application entry point
├── sample_data/                        # Sample match data files
│   ├── sample_match.json               # Full match state in JSON format
│   └── sample_match.csv                # Full match scorecard & delivery log in CSV
├── tests/                              # Automated Test Suite
│   ├── CricketScoringTest.java         # 12 compulsory test cases
│   └── GenerateSampleMatchData.java    # Realistic match generator
├── Main.java                           # Root launcher delegating to CricStatsApp
├── run.bat                             # One-click Windows runner script
├── test.bat                            # One-click Windows test runner script
└── README.md                           # Documentation
```

---

## 3. Technology Requirements Met

- **Language**: Java 25 (Compatible with Java 11, 17, 21, 25+ LTS).
- **Zero Third-Party Dependencies**: Uses purely Java Standard Library (`java.util`, `java.io`, `java.nio`).
- **Persistence**: Built-in JSON serializer/deserializer and CSV export.
- **In-Memory State**: Real-time memory state with undo/snapshot history.

---

## 4. Setup and Execution

### Prerequisites
- JDK 11 or higher installed (`javac` and `java` available on PATH).

### Option A: Using Windows Batch Scripts
- To run the application:
  ```cmd
  run.bat
  ```
- To run the test suite:
  ```cmd
  test.bat
  ```

### Option B: Using Command Line (PowerShell / Terminal)
1. **Compile all sources**:
   ```powershell
   javac -d bin cricstats/models/*.java cricstats/engine/*.java cricstats/storage/*.java cricstats/ui/*.java cricstats/*.java tests/*.java
   ```
2. **Launch CricStats**:
   ```powershell
   java -cp bin cricstats.CricStatsApp
   ```
3. **Run the Automated Test Suite**:
   ```powershell
   java -cp bin tests.CricketScoringTest
   ```

---

## 5. Main Console Menu

Upon launch, CricStats displays the menu:

```
========================================
CRICSTATS
BALL-BY-BALL CRICKET SCORING ENGINE
========================================
1. Create New Match
2. Set Up Teams and Players
3. Start First Innings
4. Enter Ball-by-Ball Score
5. View Live Score
6. View Batting Scorecard
7. View Bowling Scorecard
8. View Fall of Wickets
9. View Partnership Statistics
10. View Match Summary
11. Save Match Data
12. Load Previous Match
13. Undo Last Delivery
14. Exit
========================================
Enter your choice: 
```

---

## 6. Interactive Delivery Commands (Option 4)

When scoring a match in Option 4:

| Command | Action | Description |
| :--- | :--- | :--- |
| `0` - `6` | Runs off bat | Adds runs to batsman & team; legal ball increments; strike rotates on odd runs (1, 3). |
| `W` | Wide Ball | 1 penalty extra + optional bye/overthrow runs; illegal delivery; batsman faces 0 balls. |
| `NB` | No-Ball | 1 penalty extra + prompt for bat runs or bye extras; illegal delivery; batsman faces 1 ball. |
| `B` | Byes | Legal delivery; runs to team extras; batsman faces 1 ball; 0 runs conceded by bowler. |
| `LB` | Leg Byes | Legal delivery; runs to team extras; batsman faces 1 ball; 0 runs conceded by bowler. |
| `OUT` | Wicket | Prompts for dismissal type (Bowled, Caught, LBW, Stumped, Hit Wicket, Run-Out). |
| `U` | Undo | Reverts previous delivery, restoring all score, balls, player figures, and strike. |
| `S` | Swap Strike | Manually rotates striker and non-striker. |
| `M` | Menu | Saves current state and returns to Main Menu. |

---

## 7. Automated Test Suite (12 Compulsory Tests)

Run `java -cp bin tests.CricketScoringTest` to verify all 12 rules:

1. **Normal Scoring**: Verifies 0, 1, 2, 3, 4, 6 runs off bat, batsman balls faced, strike rate, boundaries, and strike rotation.
2. **Wide Deliveries**: Verifies 1 extra run, non-legal ball count, bowler charged, batsman balls faced unaffected, and strike rotation on odd runs taken.
3. **No-Ball Deliveries**: Verifies 1 penalty run, non-legal ball count, bat runs credited to batsman and bowler, balls faced incremented.
4. **Byes and Leg-Byes**: Verifies legal delivery count, striker balls faced incremented, team extras updated, and **0 runs charged to bowler**.
5. **Bowled and Caught Dismissals**: Verifies bowler credited with wicket, fielder credited with catch, and fall-of-wickets milestone created.
6. **Run-Outs**: Verifies bowler **is not credited** with wicket, fielder credited with run-out, and specified batsman dismissed.
7. **Over Completion & Maidens**: Verifies 6 legal balls trigger over completion, maiden awarded when bowler concedes 0 runs, strike swapped, and consecutive overs by same bowler prevented.
8. **All-Out Situations**: Verifies innings terminates when `wickets == teamSize - 1`.
9. **Second Innings Target Calculation**: Verifies target is strictly `1st Innings Total + 1`, and checks runs required, balls remaining, and RRR.
10. **Successful Run Chase**: Verifies match concludes immediately when chasing team score reaches target, declaring victory by wickets remaining.
11. **Undo Last Delivery**: Verifies complete state restoration of score, wickets, legal balls, maidens, player figures, strike, and partnerships.
12. **Saving and Loading Match Data**: Verifies full round-trip JSON serialization and CSV export.

---

## 8. Sample Console Output

### Live Match Banner
```
=========================================================================================
 Royal Challengers vs Super Kings  |  INNINGS 2  |  COMPLETED
-----------------------------------------------------------------------------------------
 BATTING: Super Kings       SCORE: 60/4   OVERS: 4.3 / 5   CRR: 13.33
 TARGET:  80                NEED:  20 runs from 3 balls   RRR: 40.00
-----------------------------------------------------------------------------------------
 *Striker:     MS Dhoni          13 runs (  8b,  0x4,  1x6)  SR: 162.50
  Non-Striker: Ravindra Jadeja   13 runs (  6b,  1x4,  1x6)  SR: 216.67
  Bowler:      Mohammed Siraj   2.3 ov, 0 mdns, 26 runs, 2 wkts  Econ: 10.40
  This Over:   1 2 W
  Partnership: 24 runs (13 balls) [ MS Dhoni: 11, Ravindra Jadeja: 13, Ext: 0 ]
=========================================================================================
```

### Complete Match Scorecard & Result
```
=========================================================================================
 CRICSTATS COMPLETE MATCH SUMMARY
 Match ID: MATCH-291DE264  |  Date: 2026-11-15 17:07
 Teams:    Royal Challengers vs Super Kings
 Format:   5 Overs per innings
 Toss:     Royal Challengers won the toss and elected to BAT first
=========================================================================================

=== FIRST INNINGS ===
=========================================================================================
 BATTING SCORECARD - ROYAL CHALLENGERS
-----------------------------------------------------------------------------------------
 Batter                 Dismissal                        R     B   4s   6s      SR
-----------------------------------------------------------------------------------------
 Virat Kohli            b Shivam Dube                   15     7    1    1  214.29
 Faf du Plessis         c MS Dhoni b Shivam Dube        17     7    2    1  242.86
 Glenn Maxwell          st MS Dhoni b Ravindra Jadeja   16     7    0    2  228.57
 *Dinesh Karthik        not out                         22     8    2    2  275.00
 Mohammed Siraj         not out                          6     2    1    0  300.00
-----------------------------------------------------------------------------------------
 Extras: 3   (wd 1, nb 1, b 1, lb 0, pen 0)
 TOTAL:  79  / 3 (5.0 Overs, RR: 15.80)
=========================================================================================

=========================================================================================
 BOWLING SCORECARD - SUPER KINGS
-----------------------------------------------------------------------------------------
 Bowler                      O    M     R    W    Econ   WD   NB
-----------------------------------------------------------------------------------------
 Shivam Dube               2.0    0    26    2   13.00    1    0
 *Ravindra Jadeja          3.0    0    52    1   17.33    0    1
=========================================================================================

=========================================================================================
 FALL OF WICKETS - ROYAL CHALLENGERS
-----------------------------------------------------------------------------------------
 Wkt #  Score        Dismissed Batter         Over       Partnership     
-----------------------------------------------------------------------------------------
 1      19/1         Faf du Plessis           1.2        19 r (8 b)      
 2      50/2         Virat Kohli              3.1        31 r (12 b)     
 3      72/3         Glenn Maxwell            4.3        22 r (8 b)      
=========================================================================================

=== SECOND INNINGS ===
=========================================================================================
 BATTING SCORECARD - SUPER KINGS
-----------------------------------------------------------------------------------------
 Batter                 Dismissal                        R     B   4s   6s      SR
-----------------------------------------------------------------------------------------
 Ruturaj Gaikwad        run out (Dinesh Karthik)        25     9    4    1  277.78
 Devon Conway           b Mohammed Siraj                 6     2    0    1  300.00
 Shivam Dube            c Virat Kohli b Glenn Maxwell    2     2    0    0  100.00
 *MS Dhoni              not out                         13     8    0    1  162.50
 Ravindra Jadeja        b Mohammed Siraj                13     6    1    1  216.67
-----------------------------------------------------------------------------------------
 Extras: 1   (wd 1, nb 0, b 0, lb 0, pen 0)
 TOTAL:  60  / 4 (4.3 Overs, RR: 13.33)
=========================================================================================

=========================================================================================
 BOWLING SCORECARD - ROYAL CHALLENGERS
-----------------------------------------------------------------------------------------
 Bowler                      O    M     R    W    Econ   WD   NB
-----------------------------------------------------------------------------------------
 Glenn Maxwell             2.0    0    34    1   17.00    1    0
 *Mohammed Siraj           2.3    0    26    2   10.40    0    0
=========================================================================================

=========================================================================================
 FALL OF WICKETS - SUPER KINGS
-----------------------------------------------------------------------------------------
 Wkt #  Score        Dismissed Batter         Over       Partnership     
-----------------------------------------------------------------------------------------
 1      15/1         Devon Conway             0.5        15 r (5 b)      
 2      28/2         Shivam Dube              1.4        13 r (5 b)      
 3      36/3         Ruturaj Gaikwad          2.2        8 r (4 b)       
 4      60/4         Ravindra Jadeja          4.3        24 r (13 b)     
=========================================================================================

=========================================================================================
 RESULT: Royal Challengers won by 19 runs!
 PLAYER OF THE MATCH: Mohammed Siraj (Royal Challengers)
 Performance: 6 runs (2b, 1x4, 0x6) | 2/26 in 2.3 ov [47 pts]
=========================================================================================
```
