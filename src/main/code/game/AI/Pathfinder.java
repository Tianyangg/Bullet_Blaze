package main.code.game.AI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import main.code.utils.Map;

import static main.code.utils.GameConstants.*; //PLAYER_HEIGHT, PLAYER_WIDTH,

/**
 * Finds paths from one Position to another along a given map
 * @author Jamie Moloney
 * @see Node
 * @see Position
 */
public class Pathfinder {

    //This Pathfinder was primarily made by primary knowledge, but helped along by this tutorial here:
    //https://gamedevelopment.tutsplus.com/tutorials/how-to-adapt-a-pathfinding-to-a-2d-grid-based-platformer-implementation--cms-24679
    //It's not just a carbon copy (the tutorial's in C#, after all), there's a fair few differences and tweaks to make it more applicable to the game, but it was a big help.

    private boolean printStuff = false;
    private Map map;
    private int jumpHeight, charHeight, charWidth; //Just in case we ever want to change a characters size for whatever reason
    private Position startPos, endPos;
    private int[][] moves = {{1,0}, {0,1}, {-1,0}, {0,-1}}; //Right, Up, Left, Down
    private ArrayList<Node> openList = new ArrayList<Node>();
    private HashSet<Position> visited = new HashSet<Position>();

    /**
     * Creates a Pathfinder for the given map where the AIPlayer can jump up to a given height
     * @param map           the map array that the AIPlayer will be on
     * @param jumpHeight    how high this AIPlayer can jump
     */
    public Pathfinder(Map map, int jumpHeight){
        this.map = map;
        this.jumpHeight = jumpHeight * 2;
        if(PLAYER_HEIGHT <= 0)
            charHeight = 1;
        else
            charHeight = ((PLAYER_HEIGHT + BOX_HEIGHT - 1) / BOX_HEIGHT); //Rounded up
        if(PLAYER_WIDTH <= 0)
            charWidth = 1;
        else
            charWidth = ((PLAYER_WIDTH + BOX_HEIGHT - 1) / BOX_HEIGHT);
    }

    /**
     * Returns true if the x and y coords given are clear and that there is ground or a box underneath it
     * @param x     the x coordinate to check
     * @param y     the y coordinate to check
     * @return      true if [x,y] is clear and [x,y+1] is not
     */
    private boolean onGround(int x, int y){
        return (map.getYInvertIndex(x,y) == 0
                && (map.getYInvertIndex(x,(y-1)) != 0));
    }

    /**
     * Returns true if the the AIPlayer can fit into the given position and all other positions they would take up
     * @param x     the x coordinate to check
     * @param y     the y coordinate to check
     * @return      true if the AIPlayer can fit into the Position and surrounding Positions
     */
    private boolean ableToBeIn(int x, int y){
        for(int i = 0; i < charWidth; i++){
            for(int j = 0; j < charHeight; j++){
                if(map.getYInvertIndex(x+i, y+j) != 0){
                     return false;
                }
            }
        }

        return true;
    };

    /**
     * Returns true if the AIPlayer fulfils both ableToBeIn(x,y) and onGround(x,y)
     * @param x     the x coordinate to check
     * @param y     the y coordinate to check
     * @return      true if the AIPlayer fulfils both ableToBeIn(x,y) and onGround(x,y)
     */
    private boolean validForPlayer(int x, int y){
        return ableToBeIn(x,y) && onGround(x,y);
    }

    /**
     * Prints the map given into the terminal
     * @param inp           the map to print
     * @param printGround   whether to print out Positions where onGround(x,y) is true
     */
    private void printMap(Map inp, boolean printGround) {
        int[][] grid = inp.getGrid();
        LinkedList<Position> grounded = new LinkedList<Position>();
        int y = grid.length;
        int x = grid[0].length;
        //System.out.println(x + ", " + y);
        for(int i = (y - 1); i >= 0; i--) {
            for (int j = 0; j < x; j++) {
                int res = inp.getYInvertIndex(j,i);
                if (res == 1)
                    System.out.print("=");
                else if (res == 2)
                    System.out.print("x");
                else if (printGround && onGround(j,i)) {
                    System.out.print("G");
                    grounded.add(new Position(j, i));
                }
                /*else if (ableToBeIn(j,i)){
                    System.out.print("V");
                }*/
                else
                    System.out.print(" ");
            }
            System.out.print("\n");
        }
        if(printGround) {
            System.out.println("Grounded positions:");
            for (Position pos : grounded) {
                //System.out.println(pos.toString());
            }
        }
    }

    /**
     * Compares two paths and returns which path is smaller
     * @param start1    the start position of the first path
     * @param goal1     the goal position of the first path
     * @param start2    the start position of the second path
     * @param goal2     the goal position of the second path
     * @return          the smaller path
     */
    public LinkedList<Position> returnSmallerPath(Position start1, Position goal1, Position start2, Position goal2){
        if(printStuff){
            System.out.println("Comparing (" + start1.toString() + " -> " + goal1.toString() + ") to (" + start2.toString() + goal2.toString() + ")");
        }

        LinkedList<Position> path1 = getPath(start1, goal1, false);
        LinkedList<Position> path2 = getPath(start2, goal2, false);

        if(path1.size() == 0 && path2.size() == 0) {
            System.out.println("Neither path is available");
            return new LinkedList<Position>();
        }

        if(path1.size() < path2.size() || path2.size() == 0){
            path1 = filter(path1);
            return path1;
        }
        else if(path1.size() > path2.size() || path1.size() == 0){
            path2 = filter(path2);
            return path2;
        }
        else if(path1.size() == path2.size()){
            path1 = filter(path1);
            return path1;
        }

        return new LinkedList<Position>();
    }

    /**
     * Returns the filtered path between position [x1,y1] and [x2,y2]
     * @param x1    the x position of the start position
     * @param y1    the y position of the start position
     * @param x2    the x position of the goal position
     * @param y2    the y position of the goal position
     * @return      the calculated path
     */
    public LinkedList<Position> getPath(int x1, int y1, int x2, int y2){
        //Just calls the other getPath function with the Positions made.
        return getPath(new Position(x1, y1), new Position(x2, y2), true);
    }

    /**
     * Returns the path between position [x1,y1] and [x2,y2]. The path is filtered if reduce is true.
     * @param x1        the x position of the start position
     * @param y1        the y position of the start position
     * @param x2        the x position of the goal position
     * @param y2        the y position of the goal position
     * @param reduce    whether to filter the path
     * @return          the calculated path
     */
    public LinkedList<Position> getPath(int x1, int y1, int x2, int y2, boolean reduce){
        //Just calls the other getPath function with the Positions made.
        return getPath(new Position(x1, y1), new Position(x2, y2), reduce);
    }

    /**
     * Returns the filtered path between position startPos and endPos
     * @param startPos  the start position
     * @param endPos    the goal position
     * @return          the filtered path
     */
    public LinkedList<Position> getPath(Position startPos, Position endPos){
        //Calls the other getPath function with reduce set to true
        return getPath(startPos, endPos, true);
    }

    /**
     * Returns the path between position startPos and endPos. The path is filtered if reduce is true.
     * @param startPos  the start position
     * @param endPos    the goal position
     * @param reduce    whether to filter the path
     * @return          the calculated path
     */
    public LinkedList<Position> getPath(Position startPos, Position endPos, boolean reduce){
        int height = map.getGrid().length - 1;

        this.startPos = startPos;
        this.endPos = endPos;

        startPos.setY(height - startPos.getY());
        endPos.setY(height - endPos.getY());

        if(printStuff)
            System.out.println(startPos.toString() + " -> " + endPos.toString());

        if(map.getYInvertIndex(endPos.getX(), endPos.getY()) != 0)
            return new LinkedList<Position>();

        //If we start trying to move in the air, set jumpHeight to the max so we have to fall down.
        if(startPos.getY() > 0) {
            if (map.getYInvertIndex(startPos.getX(), startPos.getY() - 1) == 0)
                startPos = new Position(startPos.getX(), startPos.getY(), jumpHeight);
        }

        if(printStuff)
            System.out.println(startPos.toString() + " has jumpValue " + startPos.getJump());

        //Adds the start Node to the openList
        //A heuristic for the first node doesn't matter since it's the only one.
        LinkedList<Position> inst = new LinkedList<Position>();
        inst.add(startPos);
        openList.add(new Node(startPos, inst, 0, 0));
        visited.add(startPos);
        int count = 0;

        Node node;

        while(!openList.isEmpty() && count++ < 5000){
            //Gets the first node in the list
            node = openList.remove(0);
            if(printStuff)
                System.out.println("---- Considering node " + node.getPos().toString() + " ----(" + count + ")  ");

            if(node.getPos().equals(endPos)){
                openList.clear();
                visited.clear();

                if(node.getInst().size() == 1)
                    return new LinkedList<Position>();

                if(reduce){
                    if (printStuff)
                        System.out.println("Path is " + node.getInst().size() + " long.\n" + node.getInst().getFirst().toString() + " -> " + node.getInst().getLast().toString());
                    LinkedList<Position> tmp1 = filter(node.getInst());
                    if (printStuff)
                        System.out.println("Reduced to " + tmp1.size());

                    flipY(tmp1);
                    return tmp1;
                }
                else {
                    LinkedList<Position> ret =  node.getInst();
                    flipY(ret);
                    return ret;
                }
            }
            else
                getSuccessors(node);
        }

        openList.clear(); //This should be empty already, but what's the harm?
        visited.clear();
        return new LinkedList<Position>();
    }

    /**
     * Flip the y-coordinate of the given list
     * @param list  the list to flip
     */
    private void flipY(LinkedList<Position> list) {
        for (Position pos : list) {
            pos.setY((map.getGrid().length - 1) - pos.getY());
            if (printStuff)
                System.out.println(pos.toString());
        }
    }

    /**
     * Put valid child nodes of the given node into the openList
     * @param node  the given node
     */
    private void getSuccessors(Node node){
        Position pos1 = node.getPos();
        Position pos2;
        for(int[] mve : moves){
            pos2 = pos1.move(mve);
            if(printStuff)
                System.out.println("Considering " + pos2.toString());

            int x1 = pos1.getX(), x2 = pos2.getX(), y1 = pos1.getY(), y2 = pos2.getY(), j1 = pos1.getJump();

            if(x2 >= 0 && x2 < map.getGrid()[0].length && y2 >= 0 && y2 < map.getGrid().length) {
                if (ableToBeIn(x2,y2)) {
                    if(printStuff)
                        System.out.println(pos2.toString() + " is valid.");

                    boolean onGround = false, underCeiling = false;


                    if (onGround(x2,y2))
                        onGround = true;

                    if (map.getYInvertIndex(x2, y2 + charHeight) != 0)
                        underCeiling = true;

                    //Working out JumpValue
                    if (onGround) {
                        if(printStuff)
                            System.out.println(pos2.toString() + " is on the ground.");
                        pos2.setJump(0);
                    } else if (underCeiling) {
                        if(printStuff)
                            System.out.println(pos2.toString() + " is under a ceiling");
                        if (x1 != x2)
                            pos2.setJump(Math.max(j1 + 1, jumpHeight + 1));
                        else
                            pos2.setJump(Math.max(j1 + 2, jumpHeight));
                    } else if (y2 > y1) {//Going up
                        if(printStuff)
                            System.out.println(pos2.toString() + " is jumping up");
                        if (j1 % 2 == 0)
                            pos2.setJump(j1 + 2);
                        else
                            pos2.setJump(j1 + 1);
                    } else if (y1 > y2) {//Going down
                        if(printStuff)
                            System.out.println(pos2.toString() + " is falling down");
                        if (j1 % 2 == 0)
                            pos2.setJump(Math.max(j1 + 2, jumpHeight));
                        else
                            pos2.setJump(Math.max(j1 + 1, jumpHeight));
                    } else if (x1 != x2) {
                        pos2.setJump(j1 + 1);
                    }

                    int j2 = pos2.getJump();

                    if (j1 != 0 && j1 % 2 != 0 && x1 != x2) {
                        if(printStuff)
                            System.out.println(pos2.toString() + " is trying to move horizontally twice in the air");
                        continue;
                    }
                    if (j2 > jumpHeight && y2 > y1) {
                        if(printStuff)
                            System.out.println(pos2.toString() + " is jumping too high");
                        continue;
                    }
                    if (j2 >= (jumpHeight * 2) && x1 != x2) {
                        if(printStuff)
                            System.out.println(pos2.toString() + "has been falling too long to move horizontally");
                        continue;
                    }

                    if (!visited.contains(pos2)) {
                        visited.add(pos2);

                        LinkedList<Position> newInst = new LinkedList<Position>();
                        newInst.addAll(node.getInst());
                        newInst.add(pos2);
                        int newLevel = node.getLevel() + 1;
                        addToOpenList(pos2, newInst, newLevel);
                    }
                    else
                        if(printStuff)
                            System.out.println(pos2.toString() + " with jump " + pos2.getJump() + " has already been searched.");
                }
            }
        }
    }

    /**
     * Calculate the given position's heurisitic and add it, as a node, into the openList
     * @param pos2      the given position
     * @param newInst   the instructions to get to the given position from the start position
     * @param newLevel  the level which the new node will be on
     */
    private void addToOpenList(Position pos2, LinkedList<Position> newInst, int newLevel) {
        int heuristic = (newLevel / 2)
                + ((pos2.getJump() + 2) / 3)    //JumpLevel divided by 3, rounded up
                + 2 * Math.abs(pos2.getY() - endPos.getY()) //How far away in the y-axis we are
                ;

        for(int i = 0; i < openList.size(); i++){
            if(heuristic <= openList.get(i).getHeuristic()) { //< or <= makes a difference. Are newer possibilities more or less important?
                openList.add(i, new Node(pos2, newInst, heuristic, newLevel));
                return;
            }
        }
        openList.add(new Node(pos2, newInst, heuristic, newLevel));
    }

    /**
     * Return a new LinkedList with a filtered down list of key Positions along the given path
     * @param inst  the given path
     * @return      a new LinkedList with a filtered down list of key Positions
     */
    private LinkedList<Position> filter(LinkedList<Position> inst) {
        if(inst.size() == 0)
            return inst;

        LinkedList<Position> temp = new LinkedList<Position>();

        Position prevPos = inst.get(inst.size() - 1);
        Position nextPos;
        Position pos;

        for(int i = (inst.size() - 1); i > 0; i--){
            pos = inst.get(i);
            nextPos = inst.get(i - 1);

            if(temp.isEmpty() //The end node must be added
                || (pos.getJump() == 0 && prevPos.getJump() != 0) //Where a jump/fall is to be performed
                || (pos.getJump() == 2) //The first in-air space for the jump
                || (pos.getJump() == 0 && nextPos.getJump() != 0) //A landing point
                || (prevPos.getY() < pos.getY() && nextPos.getY() < pos.getY()) //The highest point of a jump
                || (((pos.getX() > 0 && map.getYInvertIndex(pos.getX() - 1, pos.getY()) != 0) || (pos.getX() < (map.getGrid()[0].length) && map.getYInvertIndex(pos.getX() + 1, pos.getY()) != 0))
                        && pos.getY() != temp.getLast().getY()  //Manoeuvring around obstacles.
                        /*&& pos.getX() != temp.getLast().getX()*/) //We need this for precision.
                )
                temp.add(pos);

            prevPos = pos;
        }

        temp.add(inst.get(0)); //Add the start node

        //We need to work out temp in reverse order so we can work out the manoeuvring positions,
        //but we want to return a linkedlist in the right order, so...
        LinkedList<Position> ret = new LinkedList<Position>();

        for(int i = (temp.size() - 1); i >= 0; i--)
            ret.add(temp.pollLast());

        return ret;
    }

    /**
     * Returns the map used by the Pathfinder
     * @return  the map
     */
    public Map getMap(){
        return map;
    }
}
