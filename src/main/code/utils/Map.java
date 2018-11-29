package main.code.utils;

import javafx.util.Pair;
import main.code.renderer.Window;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

import static main.code.utils.GameConstants.*;

//Values for the map in the matrix:
    //      0 -- empty space (background)
    //      1 -- platform/wall
    //      2 -- box

/**
 * A class that implements a map for the game
 *
 * @author Jamie Moloney, Matei Vicovan-Hantascu, Thomas Murphy
 */
public class Map {

    private int[][] grid;
    private Point[] spawnPoints;
    private Point[] boxSpawnPoints;

    /**
     * Constructs a map object
     * @param grid
     *          a matrix representing the platforms in game
     * @param spawn
     *          an array with all the available spawn points for players
     * @param boxes
     *          an array with the positions of the boxes
     */
    public Map(int[][] grid, Point[] spawn, Point[] boxes){
        this.grid = grid;
        this.spawnPoints = spawn;
        this.boxSpawnPoints = boxes;
    }

    /**
     * Constructs a map with no values
     */
    public Map() {
        this.grid = new int [0][0];
        this.spawnPoints = new Point[0];
        this.boxSpawnPoints = new Point[0];
    }

    /**
     * @return
     *          the spawn points for the current map
     */
    public Point[] getSpawns(){
        return spawnPoints;
    }

    /**
     * @return
     *          positions of the boxes for the current map
     */
    public Point[] getBoxSpawns() { return boxSpawnPoints; }

    /**
     * @return
     *         returns the grid that represents the platforms
     */
    public int[][] getGrid(){
        return this.grid;
    }


    /**
     * @param grid
     *          the grid used for the game
     */
    public void setMap(int[][] grid){
        this.grid = grid;
    }


    /**
     * @param i
     *         x position
     * @param j
     *         y positions
     * @param value
     *        value in map
     */
    public void changeAtIndexes(int i, int j, int value){
        this.grid[j][i] = value;
    }


    /**
     * @param i
     *          x position
     * @param j
     *          y position
     * @return
     *          the value from grid
     *          if outside of bounds return 1 (meaning platform)
     */
    public int getAtIndex(int i, int j){
        if(j < 0 || j >= grid.length || i < 0 || i >= grid[0].length)
            return 1;   //All values outside the grid are platform blocks.

        return this.grid[j][i];
    }

    /**
     * Since in the game, 0,0 is at top left, I need an inverted method.
     * @param i
     *          x position
     * @param j
     *          y position
     * @return
     *          the value in the map
     *          if outside of bounds return 1 (meaning platform)
     */
    public int getYInvertIndex(int i, int j) {
        j = this.grid.length - 1 - j;
        if(j < 0 || j >= grid.length || i < 0 || i >= grid[0].length)
            return 1;

        //System.out.println("i: " + i + " j: " + j);
        return this.grid[j][i];

    }

    /**
     * @return
     *          width of the map in pixels
     */
    public int getXSize(){
        return BOX_HEIGHT * grid[0].length;
    }

    /**
     * @return
     *          height of the map in pixels
     */
    public int getYSize(){
        return BOX_HEIGHT * grid.length;
    }


    /**
     * Shuffles a given list of weapons and then puts each one randomly into a box
     * @param chosenWeapons
     *          the weapons that needs to be added in the boxes
     * @return
     *          a list of boxes containing the weapons
     */
    public ArrayList<Box> setupBoxes(ArrayList<Pair<Byte,Short>> chosenWeapons){
        ArrayList<Box> newBoxes = new ArrayList<Box>();
        Point[] boxSpawns = getBoxSpawns();
        shuffle(boxSpawns);

        for(Point p : boxSpawns){
            Box box = new Box();
            box.init();
            box.setCoord((short)(p.x * BOX_HEIGHT), (short) (p.y * BOX_HEIGHT));
            grid[p.y][p.x] = 2;
            newBoxes.add(box);
        }


        //put weapons in each box - it can be null
        //They will appear random in the game as the spawn points have been
        //shuffled.
        //Always ensure player has >=60% chance of finding a weapon in a box
        //COULD CHANGE % DEPENDENT ON GAME DIFFICULTY. I.E. THE HARDER THE
        //DIFFICULTY, THE LESS CHANCE OF FINDING A WEAPON.
        int numWeaponsNeeded;
        if((((float)chosenWeapons.size()/(float)boxSpawns.length)*100) <= 60){
            numWeaponsNeeded = chosenWeapons.size();

            while((numWeaponsNeeded/boxSpawns.length)*100 <= 60){
                numWeaponsNeeded++;
            }
        }else{
            numWeaponsNeeded = chosenWeapons.size();
        }

        Random rand = new Random();
        for(int i = 0; i < newBoxes.size(); i++){
            if(i < numWeaponsNeeded){
                int weapon = rand.nextInt(chosenWeapons.size());
                newBoxes.get(i).setContentsOfBox(chosenWeapons.get(weapon));
            }else{
                newBoxes.get(i).setContentsOfBox(null);
            }
        }
        return newBoxes;
    }

    /** Sets up spawns in the single player game so that:
     * - spawns are random
     * - player is spawned at a separate place to the AI
     * - Ensures all remaining spawn points are used before
     *  allowing more AI to spawn at the same place
     * @param player - The given player
     * @param gameAI - The given list of AIplayers
     */
    public void setSpawns(Player player, ArrayList<AIPlayer> gameAI) {
        Point[] mapSpawns = getSpawns();

        //Shuffles the spawn points to stop player and enemies
        //always spawning at the same place
        shuffle(mapSpawns);

        player.setCoord((short) ((mapSpawns[0].x) * BOX_HEIGHT), (short) ((mapSpawns[0].y) * BOX_HEIGHT));

        //Randomly allocates AI spawns.
        //Makes sure that all spawn points in the map
        //have been used before allowing more AI to spawn
        //in the same location as others.
        Random rand = new Random();
        int lowBound = 1;
        int highBound = mapSpawns.length;
        byte[] hasSpawnBeenUsed = new byte[mapSpawns.length];
        hasSpawnBeenUsed[0] = 1;
        for (int i = 1; i < hasSpawnBeenUsed.length; i++) {
            hasSpawnBeenUsed[i] = 0;
        }
        for (int i = 0; i < gameAI.size(); i++) {
            int spawnPos = rand.nextInt((highBound - lowBound)) + lowBound;
            //Ensures no repeat spawns are done - at least until
            //all spawn points have been used.
            if (hasSpawnBeenUsed[spawnPos] == 1) {
                while (hasSpawnBeenUsed[spawnPos] == 1) {
                    spawnPos++;
                    if (spawnPos >= mapSpawns.length) {
                        spawnPos = 1;
                    }
                }
            }

            gameAI.get(i).setCoord((short) (mapSpawns[spawnPos].x * BOX_HEIGHT), (short) (mapSpawns[spawnPos].y * BOX_HEIGHT));
            hasSpawnBeenUsed[spawnPos] = 1;

            //Checks if all spawns have been used, if so,
            //reset flags to allow for repeat spawns.
            boolean allSpawnsUsed = true;
            for (int j = 1; j < hasSpawnBeenUsed.length; j++) {
                if (hasSpawnBeenUsed[j] == 0) allSpawnsUsed = false;
            }

            if (allSpawnsUsed) {
                hasSpawnBeenUsed[0] = 1;
                for (int k = 1; k < hasSpawnBeenUsed.length; k++) {
                    hasSpawnBeenUsed[k] = 0;
                }
            }
        }
    }

    /** Randomly selects a spawn point for the given player in an online game
     * @param player - the given player
     */
    public void setOnlineSpawns(Player player){
        Point[] mapSpawns = getSpawns();

        shuffle(mapSpawns);

        player.setCoord((short)((mapSpawns[0].x) *BOX_HEIGHT),(short)((mapSpawns[0].y) *BOX_HEIGHT));
    }

    /** Randomly shuffles the list of points.
     *  Used for setting box and player spawns.
     * @param points - the points to be shuffled
     */
    //Fisher-Yates Shuffle
    private void shuffle(Point[] points){
        Random rand = new Random();

        for(int i = points.length-1; i > 0; i--){
            int pos = rand.nextInt(i+1);
            Point temp = points[pos];
            points[pos] = points[i];
            points[i] = temp;
        }
    }

    /**
     * @return
     *          a string representation of this object
     */
    public String toString(){
        String s = "";
        s+="rows: " + grid.length + '\n';
        s+="cols: " + grid[0].length+'\n';
        s+="spawn poitns: "+ spawnPoints.length + '\n';
        s+= "BoxSpawns: " +'\n';
        for(Point aux : boxSpawnPoints){
            s+="X: " + Double.toString(aux.getX()) +" Y: " + Double.toString(aux.getY()) + '\n';
        }
        return s;
    }

    private Point[] setSpawns(){
        Point[] mapSpawns = getSpawns();

        //Shuffles the spawn points to stop player and enemies
        //always spawning at the same place
        shuffle(mapSpawns);

        return mapSpawns;
    }


    /**
     * Sets the boxes
     * @param boxes
     *          the given boxes
     */
    public void setBoxes(ArrayList<Box> boxes) {

        for(Box b :boxes){
            grid[b.getYPos()/GameConstants.BOX_HEIGHT][b.getXPos()/GameConstants.BOX_HEIGHT] = 2;
        }
    }
}
