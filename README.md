# Chess AI for DataScienceProject 2023/24
This project contains the Chess-Bot (in multiple different versions), 
a software for letting the bot play automatically against itself 
as well as a PGN-Writer which saves the played games in files.
# Contents
This project is a self made chess-engine. The Chess-Logic can be found in the utils-class `src/program/ChessRules.java`
There is the class `src/program/Game.java` which plays a game with to given players as constructor-parameters.
Subclasses of the class `Player` are `Humanplayer` as well as all bot-versions.

Each bot extends the abstract class `src/program/players/ais/v2/AI_MinmaxAbstract.java`.
This class offers the minmax-method implemented with alpha-beta-pruning. 
There is one abstract method in this class - The which is the analyzeBoardMethod(), since the analyse-function is the part of the bot which differs from version to version.
The latest bot version is `src/program/players/ais/v2/AI2_v3.java`
# Setup
If you want to run/test the bot on your local machine you first need to clone this repository.
Even though this project was developed in intellij, it should work with other IDEs to.
It does not use any Build-System like maven or gradle.

The Main-Method is configured start a terminal-game where the white player is a `HumanPlayer`
and the black player is the latest bot-version

If you want to see the bot play automatically against each itself (like we did to gather our data)
you need to go into the class `Main.java` and in the switch-head in the main-method change the number to a number between 0 and 4.
Running the main-method now should launch multiple games at the same time on different threads. when the games are finished the new files should be under src/data.

Hope you enjoy! :D