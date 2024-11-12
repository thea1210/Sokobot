package solver;

import java.util.*;

public class SokoBot {

  private final List<int[]> targetPositions = new ArrayList<>();//list of all target positions

//CREATES NODES TO COMPARE
  private class Node implements Comparable<Node> {
    int positionX, positionY; // player's position in array
    char[][] itemsData;
    int cost, heuristic; // g(n) cost of moves and h(n) estimate cost of moves
    String path; // moves taken or to take

    Node(int positionX, int positionY, char[][] itemsData, int cost, int heuristic, String path) {
      this.positionX = positionX;
      this.positionY = positionY;
      this.itemsData = copyArray(itemsData); // copy crate positions for each state
      this.cost = cost;
      this.heuristic = heuristic;
      this.path = path;
    }

    // COMPARES NODES CALCULATED F(N) TO ANOTHER
    @Override
    public int compareTo(Node other) {
      return (this.cost + this.heuristic) - (other.cost + other.heuristic);
    }
  }

  // SOLVES FOR HEURISTIC VALUES
  private int[][] precomputeDistances(char[][] itemsData, List<int[]> targetPositions) {
    int[][] distances = new int[itemsData.length][itemsData[0].length];

    for (int i = 0; i < itemsData.length; i++) {
      for (int j = 0; j < itemsData[i].length; j++) {
        distances[i][j] = Integer.MAX_VALUE; // set distance as max for proper comparison

        if (itemsData[i][j] == '$') { // if it's a crate, loop through all targets and find distance from targets
          for (int[] target : targetPositions) {

            // calculate Manhattan distance and keep the minimum distance from target positions
            int distance = calculateDistance(target[0], target[1], i, j);
            distances[i][j] = Math.min(distances[i][j], distance);
          }
        }
      }
    }
    return distances; //return h(n)
  }

  // CALCULATE HEURISTIC VALUER FOR EACH (NEW) STATE FROM GOAL STATE
  private int heuristic(char[][] itemsData, char[][] mapData, int[][] crateDistances, int positionX, int positionY) {
    int totalDistance = 0;

    for (int i = 0; i < itemsData.length; i++) {
      for (int j = 0; j < itemsData[i].length; j++) {
        if (itemsData[i][j] == '$') {
          totalDistance += crateDistances[i][j];

          // penalize crates that are trapped in corners or surrounded by walls (dead end)
          if (isTrapped(i, j, mapData, itemsData)) {
            totalDistance += 100;
          }

          // consider the distance between the player and the crate
          totalDistance += 2 * calculateDistance(positionX, positionY, i, j);
        }
      }
    }

      // evaluate the proximity of the player to the nearest target
      int nearestTargetDistance = Integer.MAX_VALUE;
      
      for (int[] target : targetPositions) {
        nearestTargetDistance = Math.min(nearestTargetDistance,
                calculateDistance(positionX, positionY, target[0], target[1]));
      }
      
      totalDistance += 4 * nearestTargetDistance; //player and target distance weight

      return totalDistance;
    }

  // COPIES 2D ARRAY FOR ITEMDATA FOR STATE COMPARISONS FOR NEW STATES/ NODES
  private char[][] copyArray(char[][] original) {
    char[][] copy = new char[original.length][];
    for (int i = 0; i < original.length; i++) {
        copy[i] = Arrays.copyOf(original[i], original[i].length);
    }
    return copy;
}

  //MAIN METHOD
  public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {
    int positionX = -1, positionY = -1; // initial position XY is not an actual position within map

    // get target positions
    for (int i = 0; i < mapData.length; i++) {
      for (int j = 0; j < mapData[i].length; j++) {

        if (mapData[i][j] == '.') {
          targetPositions.add(new int[]{i, j}); // add target position to list
        }

        if (itemsData[i][j] == '@') { // if player position found -> set positionX and Y
          positionX = i;
          positionY = j;
        }
      }
    }

    // compute distances of the crates to the targets
    int[][] crateDistances = precomputeDistances(itemsData, targetPositions);

    //openSet to know what nodes to explore next & closetSet for states/nodes already visited
    PriorityQueue<Node> openSet = new PriorityQueue<>();
    Set<String> closedSet = new HashSet<>();

    //makes a new node start to know all current positions: player, crate, etc.
    Node start = new Node(positionX, positionY, itemsData, 0, heuristic(itemsData, mapData, crateDistances, positionX, positionY), "");
    openSet.add(start);

    while (!openSet.isEmpty()) { // while not all nodes are explored
      Node current = openSet.poll();

      if (isGoalState(current.itemsData, mapData)) {// check if goal state is achieved to return path solution
        return current.path;
      }

      // check possible moves by checking connected edges or adjacentNodes
      for (Node adjacentNode : generateAdjacentNodes(current, mapData, crateDistances)) {

        // prune dead end moves
        if (!needsPrune(adjacentNode, mapData)) {
          String stateKey = getStateKey(adjacentNode); // make a new state key / name after every move is made

          if (!closedSet.contains(stateKey)) { // add state key name to visited states
            closedSet.add(stateKey);
            openSet.add(adjacentNode);
          }
        }
        
      }
    }

    return ""; // if no solution is found it does nothing
  }

  // CHECKS IF ALL CRATES ARE ON GOAL STATE
  private boolean isGoalState(char[][] itemsData, char[][] mapData) {
    for (int i = 0; i < itemsData.length; i++) {
      for (int j = 0; j < itemsData[i].length; j++) {

        if (itemsData[i][j] == '$' && mapData[i][j] != '.') {
          return false; // crate is not on target
        }
      }
    }

    return true; // all crates are on target
  }

  // CHECKS POSSIBLE MOVES THROUGH ADJACENT STATES
  private List<Node> generateAdjacentNodes(Node current, char[][] mapData, int[][] crateDistances) {
    List<Node> adjacentNodes = new ArrayList<>();

    int[] distX = {-1, 1, 0, 0}; // movements u, d, l, r for checking only
    int[] disty = {0, 0, -1, 1};
    char[] moves = {'u', 'd', 'l', 'r'};

    for (int i = 0; i < 4; i++) {
      int newX = current.positionX + distX[i]; //new position for bot to move to
      int newY = current.positionY + disty[i]; 

      // check if new position is a valid move
      if (isValidMove(newX, newY, current.positionX, current.positionY, mapData, current.itemsData)) {

        char[][] newItemsData = copyArray(current.itemsData);// copy itemsData before moving

        // check if player is pushing crate
        if (current.itemsData[newX][newY] == '$') {
          int crateNewX = newX + distX[i]; // list new XY coords of crate (as it is being pushed)
          int crateNewY = newY + disty[i];

          newItemsData[newX][newY] = ' ';
          newItemsData[crateNewX][crateNewY] = '$';// update the itemData w/ new crate positioning and blank spaces
        }

        newItemsData[current.positionX][current.positionY] = ' ';// update itemData w/ new player positioning and blank spaces
        newItemsData[newX][newY] = '@';

        // make a new node
        Node adjacentNode = new Node(newX, newY, newItemsData, current.cost + 1, heuristic(newItemsData, mapData, crateDistances, newX, newY), current.path + moves[i]);
        adjacentNodes.add(adjacentNode);
      }
    }

    return adjacentNodes;
  }

  //CHECK IF STATES WHERE CRATE IS MOVED NEEDS TO BE PRUNED: DEAD END
  private boolean needsPrune(Node node, char[][] mapData) {

    for (int i = 0; i < node.itemsData.length; i++) {
      for (int j = 0; j < node.itemsData[i].length; j++) {

        if (node.itemsData[i][j] == '$') { // check crates

          // if it is in a corner or wall
          if (inCorner(i, j, mapData, node.itemsData)) {
            return true; // crate will be stuck
          }
        }
      }
    }
    return false; // no dead end
  }


  // CHECK IF THE CRATE IS IN A CORNER
  private boolean inCorner(int x, int y, char[][] mapData, char[][] itemsData) {

    // check for crate in corner but not on target
    if (itemsData[x][y] == '$' && mapData[x][y] != '.') {
      boolean aboveWall = (mapData[x - 1][y] == '#');
      boolean belowWall = (mapData[x + 1][y] == '#');
      boolean leftWall = (mapData[x][y - 1] == '#');
      boolean rightWall = (mapData[x][y + 1] == '#');

      // if its in a corner
      if (aboveWall && leftWall || aboveWall && rightWall ||
              belowWall && leftWall || belowWall && rightWall) {
        return true;
      }

      // if its in a corner and crate blockages
      if ((leftWall && itemsData[x][y - 1] == '$') ||
              (rightWall && itemsData[x][y + 1] == '$') ||
              (aboveWall && itemsData[x - 1][y] == '$') ||
              (belowWall && itemsData[x + 1][y] == '$')) {
        return true;
      }
    }
    return false; // not a dead end position for the crate
  }


  // CHECK IF MOVING TO (x, y) IS VALID
  private boolean isValidMove(int x, int y, int positionX, int positionY, char[][] mapData, char[][] itemsData) {

    // if the new position is within map
    if (x < 0 || x >= mapData.length || y < 0 || y >= mapData[0].length || mapData[x][y] == '#') {
      return false; //not within
    }

    // if new position has a movable crate
    if (itemsData[x][y] == '$') {

      //get new crate position after moving
      int dx = x - positionX;
      int dy = y - positionY;
      int crateNewX = x + dx; // offset
      int crateNewY = y + dy;

      // invalid move
      if (crateNewX < 0 || crateNewX >= mapData.length || crateNewY < 0 || crateNewY >= mapData[0].length
      || mapData[crateNewX][crateNewY] == '#' || itemsData[crateNewX][crateNewY] == '$' ) {
        return false; //no
      }
    }

    return true; // is a valid move
  }
  
  //MAKES A NAME FOR EVERY NEW STATE (after every move): FOR CHECKING OF VISITED STATES
  //name format: X-Y, itemData as a string (player position and items)
  private String getStateKey(Node node) {
    StringBuilder sb = new StringBuilder();

    sb.append(node.positionX).append('-').append(node.positionY).append(' ');

    for (char[] row : node.itemsData) {
      sb.append(new String(row));
    }

    return sb.toString();
  }

  //CHECKS IF CRATE IS SURROUNDED BY WALLS OR OTHER CRATES
  private boolean isTrapped(int x, int y, char[][] mapData, char[][] itemsData) {

    if (mapData[x - 1][y] == '#' || mapData[x + 1][y] == '#' ||
            mapData[x][y - 1] == '#' || mapData[x][y + 1] == '#' ||
            itemsData[x - 1][y] == '$' || itemsData[x + 1][y] == '$' ||
            itemsData[x][y - 1] == '$' || itemsData[x][y + 1] == '$') {
      return true; //trapped
    }

      // Check if the crate is in a corner
    return (mapData[x - 1][y] == '#' && mapData[x][y - 1] == '#') ||
            (mapData[x - 1][y] == '#' && mapData[x][y + 1] == '#') ||
            (mapData[x + 1][y] == '#' && mapData[x][y - 1] == '#') ||
            (mapData[x + 1][y] == '#' && mapData[x][y + 1] == '#');
  }

  //CALCULATES MANHATTAN DISTANCE
  private int calculateDistance(int x1, int y1, int x2, int y2) {
    return Math.abs(x2 - x1) + Math.abs(y2 - y1);
  }
}