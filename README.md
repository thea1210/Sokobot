# Description
This repository showcases a SOKOBOT, an artificial intelligence algorithm for a bot that plays Sokoban. Sokoban is a classic Japanese puzzle game. 
It was created by Horiyuki Imabayashi and was first published by Thinking Rabbit in 1981. The name Sokoban (倉庫番) translates to “warehouse keeper”. 
The game has been ported to numerous platforms, inspiring countless unofficial clones [Example](https://www.mathsisfun.com/games/sokoban.html). Sokoban is a grid-based puzzle game where players organize crates in a warehouse.

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
Try running the program in Sokobot mode for the same map. Press the SPACE BAR to start the bot.
