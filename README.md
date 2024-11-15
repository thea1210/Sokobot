# Sokobot
This repository showcases a SOKOBOT, an artificial intelligence algorithm for a bot that plays Sokoban. This is an MCO1 project for CSINTSY.
Sokoban is a classic Japanese puzzle game where the player must push crates to target locations on a grid. 
The game has been ported to numerous platforms, inspiring countless unofficial clones for [Example](https://www.mathsisfun.com/games/sokoban.html). 
The game poses a challenge for automated solvers due to its large branching factor and dead-ends. This project uses the A* algorithm to solve the puzzles.
The project demonstrates the importance of state representation, good heuristic formulas, and strategic pruning in AI problem-solving. 

## How to Run
- Open terminal
- Navigate so that your current working directory is sokobot. This means you should see the maps and src folders.
- To compile the files, issue the following command:
  - In MacOS or Linux, javac src/*/*.java -d out/ -cp out
  - In Windows, javac src/gui/*.java src/main/*.java src/reader/*.java src/solver/*.java -d out/ -cp out
- Choose a map to use. The map files are in the maps folder.
- To run the program you need to indicate map filename found in the maps folder and the run mode.
  - Run modes may either be fp for Free Play mode or bot for Sokobot mode.
  - Map file must be found in the maps folder. Do not include the file extension.
    java -classpath out main.Driver <map-filename-only> <run-mode>
    Example, java -classpath out main.Driver testlevel fp

Game screen Free Play mode. You may use your arrow keys to control the character.
Press ESC to reset the initial configuration of the current map.
In Sokobot mode, press the SPACE BAR to start the bot.
