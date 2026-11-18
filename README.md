# capstone-CP081-vitasta
CricStats: Ball-by-Ball Cricket Scoring and  Analytics Engine
CricStats is a Java-based, menu-driven cricket scoring and analytics application designed to manage local, club, college-level and practice cricket matches.

It provides ball-by-ball scoring, automatic batting and bowling statistics, live score updates, partnership tracking, fall-of-wickets records, match summaries and Player of the Match calculations.

The application also supports saving and loading match data using JSON and exporting match statistics in CSV format.
🚀 Key Features
1. Team Management
Create two cricket teams.
Add 2–11 players to each team.
Prevent duplicate and empty player names.
Customize batting order.
Select opening batsmen and bowlers.
Manage toss winner and batting/bowling decisions.

3. Ball-by-Ball Scoring
Record runs: 0, 1, 2, 3, 4 and 6.
Handle wide balls and no-balls.
Calculate byes and leg-byes.
Support multiple dismissal types:
Bowled
Caught
LBW
Stumped
Hit Wicket
Run Out
Automatic strike rotation.
Manual strike swapping.
Automatic over completion.

4. Automatic Batting Statistics
The application calculates:
Total runs scored.
Balls faced.
Fours and sixes.
Strike rate.
Dismissal information.
Strike Rate Formula:
Strike Rate = (Runs Scored / Balls Faced) × 100

5. Automatic Bowling Statistics
The application tracks:
Overs bowled.
Maiden overs.
Runs conceded.
Wickets taken.
Economy rate.
Wide balls.
No-balls.

Economy Rate Formula:
Economy Rate = Runs Conceded / Overs Bowled

6. Live Match Analytics
Current score and wickets.
Overs completed.
Current Run Rate (CRR).
Target score.
Runs required.
Balls remaining.
Required Run Rate (RRR).

7. Complete Scorecard
Batting scorecard.
Bowling scorecard.
Extras breakdown.
Fall of Wickets (FOW).
Partnership statistics.
Final match summary.
Winning margin by runs or wickets, or a tie.
7. Advanced Features
Undo the last delivery with complete match-state restoration.
Automatic innings progression.
Automatic second-innings target calculation.
Maiden over detection.
Prevention of consecutive overs by the same bowler.
Partnership tracking for every wicket.
Automatic Player of the Match selection.

8. Player of the Match Calculation
The application uses a points-based performance evaluation system.
