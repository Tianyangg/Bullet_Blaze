package main.code.utils;

import javafx.util.Pair;
import main.code.game.AI.*;
import main.code.game.JUnit.DummyPhysics;
import main.code.game.movement.Physics;
import static main.code.utils.GameConstants.*; //PLAYER_JUMP_HEIGHT
import main.code.renderer.Window;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/**
 * Extends the Player class with framework for AI decision making and houses a Pathfinder.
 * @author Jamie Moloney
 * @see BotState
 * @see Pathfinder
 * @see Player
 */
public class AIPlayer extends Player {

    private boolean allowedToShoot = true, printStuff = false;
    private Pathfinder pf;
    private BotState currentState = BotState.Idle;
    private Map map;
    private int[][] grid;
    private int xSize, ySize, updatesSincePathfind = 1000, updatesSinceLastHit = 1000, updatesSinceLastSeen = 1000, pathProg = 0, goalX = 0, goalY = 0, boxSize = 0, charHeight = 1, charWidth = 1;
    private int prevX = -1, prevY = -1, updatesSinceMovement = 0;
    private DummyPhysics moveHandle;
    private boolean[] movement;
    private LinkedList<Position> path = new LinkedList<Position>();
    private long lastFired = 0;
    private Window window;
    private Random random = new Random();

    /**
     * Creates an AIPlayer with the values specified.
     * @param id            the AIPlayer's ID
     * @param map           the map array that the AIPlayer will be on
     */
    public AIPlayer(byte id, Map map){
        super(id);
        pf = new Pathfinder(map, (PLAYER_JUMP_HEIGHT + 1));
        this.map = map;
        this.grid = map.getGrid();
        this.xSize = grid[0].length;
        this.ySize = grid.length;
        this.boxSize = BOX_HEIGHT;
        moveHandle = new DummyPhysics(BOX_HEIGHT);
        movement = new boolean[4];
        resetMovement();
        charHeight = PLAYER_HEIGHT;
        charWidth = PLAYER_WIDTH;
    }

    /**
     * Resets the movement array used for Physics
     */
    private void resetMovement(){
        for (int i = 0; i < movement.length; i++){
            movement[i] = false;
        }
    }

    /**
     * Returns true if the x and y coords given are clear and that there is ground or a box underneath it
     * @param x     the x coordinate to check
     * @param y     the y coordinate to check
     * @return      true if [x,y] is clear and [x,y+1] is not
     */
    private boolean onGround(int x, int y){
        return (map.getAtIndex(x,y) == 0
                && map.getAtIndex(x,(y+1)) != 0);
    }

    /**
     * Sets the Player's hp stat to the value given. If that value is lower than their current health, resets
     * @param hp    the new value of the AIPlayer's HP
     */
    public void setHp(byte hp){
        if(getHp() > hp)
            updatesSinceLastHit = 0;

        super.setHp(hp);
    }

    /**
     * Updates the AI's behaviour - creates a new path if required and updates the AI's movement
     * @param PX        the human Player's x coordinate
     * @param PY        the human Player's y coordinate
     * @param winSize   the width of the window, in pixels
     */
    public void update(int PX, int PY, int winSize){
        if(isAlive()) {
            resetMovement();

            PX = PX + PLAYER_ORIGIN_X + charWidth;
            PY = PY + PLAYER_ORIGIN_Y + charHeight;
            int PgX = (PX / boxSize);
            int PgY = (PY / boxSize);
            int AX = super.getXPos() + PLAYER_ORIGIN_X + charWidth;
            int AY = super.getYPos() + PLAYER_ORIGIN_Y + charHeight;
            int AgX = (AX / boxSize);
            int AgY = (AY / boxSize);

            if (printStuff) {
                System.out.println("Currently at position (" + AX + ", " + AY + ") in pixels, (" + AgX + ", " + AgY + ") in the grid.");
                System.out.println("Player apparently at (" + PX + ", " + PY + ") in pixels, (" + PgX + ", " + PgY + ") in the grid.");
            }

            double currentFiringInterval = DEFAULT_FIRING_INTERVAL, currentRange = DEFAULT_RANGE;

            //If the AI is close enough to 'see' the player...
            int dist = (int) Math.sqrt(Math.pow((PgX - AgX), 2) + Math.pow((PgY - AgY), 2));

            if (dist < 9
                    || (updatesSinceLastSeen < (TARGET_UPS * 3.5) && dist < 14)
                    || (updatesSinceLastHit < (TARGET_UPS * 2) && dist < 18)
            ){
                if (printStuff)
                    System.out.println("AI" + getId() + " can 'see' the player.");
                updatesSinceLastSeen = 0;
                //Getting info on current gun
                if (PgX > AgX)
                    setShootingDirection((short) 1);
                else
                    setShootingDirection((short) -1);

                if (allowedToShoot && !(System.currentTimeMillis() - lastFired < (currentFiringInterval * 1.25))) {
                    //Only fires bullet if the currently selected weapon has enough ammo
                    if (super.getCurrentWeapon().getValue() != 0 && Math.abs(PgY - AgY) < 3 && Math.abs(PgX - AgX) < currentRange) {
                        lastFired = System.currentTimeMillis();
                        if (printStuff)
                            System.out.println("Bullet Fired");

                        super.shootBullet();
                        super.setCurrentWeapon(new Pair<Byte, Short>(super.getCurrentWeapon().getKey(), (short) (super.getCurrentWeapon().getValue() - 1)));

                        if (printStuff)
                            System.out.println("Remaining ammo: " + super.getCurrentWeapon().getValue());
                    }
                }


                //Path Updating
                if (updatesSincePathfind > ((2 * TARGET_UPS) / 3) && (Math.abs(PgY - goalY) > 2 || Math.abs(PgX - goalX) > 2)
                        || (updatesSincePathfind > (TARGET_UPS / 2) && path.isEmpty())
                        || updatesSinceMovement > (TARGET_UPS)) {

                    //First, find all of the possible positions to put the AI so they can shoot at the player
                    ArrayList<Position> lefts = new ArrayList<Position>();
                    ArrayList<Position> rights = new ArrayList<Position>();

                    boolean found = false;

                    if (!onGround(PgX, PgY)) {
                        int maxDist = 5;
                        for (int ty = PgY + 1; ty - PgY <= maxDist && !found; ty++) {
                            if (onGround(PgX, ty)) {
                                goalX = PgX;
                                goalY = ty;
                                found = true;
                            }
                        }
                    }

                    if (!found) {
                        goalX = PgX;
                        goalY = PgY;
                        found = true;
                    }

                    //Finding a close coord in grid coords
                    int maxDist = (int) (currentRange * 0.8), minDist = (int) (currentRange * 0.1);

                    for (int tx = (goalX - maxDist); tx <= (goalX + maxDist); tx++) {
                        if (Math.abs(tx - goalX) >= minDist) {
                            if (onGround(tx, goalY)) {
                                if (goalX > tx)
                                    lefts.add(new Position(tx, goalY));
                                else
                                    rights.add(new Position(tx, goalY));

                                if (printStuff)
                                    System.out.println("Position (" + tx + ", " + goalY + ") is possible.");
                            }
                            else if (printStuff)
                                System.out.println("Position (" + tx + ", " + goalY + ") isn't on the ground.");
                        }
                    }

                    Position start = new Position(AgX, AgY);

                    if (!lefts.isEmpty() && !rights.isEmpty()) {
                        Position left, right;

                        int noLefts = lefts.size(), noRights = rights.size();

                        if (noLefts > 1)
                            left = lefts.get((random.nextInt(noLefts - 1) + random.nextInt(noLefts - 1)) / 2);
                        else
                            left = lefts.get(0);

                        if (noRights > 1)
                            right = rights.get((random.nextInt(noRights - 1) + random.nextInt(noRights - 1)) / 2);
                        else
                            right = rights.get(0);

                        LinkedList<Position> tempPath = pf.returnSmallerPath(start, left, start, right);

                        if (tempPath.size() > 1) {
                            if (printStuff)
                                System.out.println("Going from " + tempPath.get(0).toString() + " to " + tempPath.getLast().toString());
                            path = tempPath;
                            pathProg = 1;
                            updatesSincePathfind = 0;
                            changeState(BotState.MoveTo);
                        }
                    } else if (!lefts.isEmpty()) {
                        Position left;

                        int noLefts = lefts.size();

                        if (noLefts > 1)
                            left = lefts.get(random.nextInt(noLefts - 1));
                        else
                            left = lefts.get(0);

                        LinkedList<Position> tempPath = pf.getPath(start, left);

                        if (tempPath.size() > 1) {
                            path = tempPath;
                            pathProg = 1;
                            updatesSincePathfind = 0;
                            changeState(BotState.MoveTo);
                        }
                    } else if (!rights.isEmpty()) {
                        Position right;

                        int noRights = rights.size();

                        if (noRights > 1)
                            right = rights.get(random.nextInt(noRights - 1));
                        else
                            right = rights.get(0);

                        LinkedList<Position> tempPath = pf.getPath(start, right);

                        if (tempPath.size() > 1) {
                            path = tempPath;
                            pathProg = 1;
                            updatesSincePathfind = 0;
                            changeState(BotState.MoveTo);
                        }
                    }
                    else if (printStuff)
                        System.out.println("No possible endPos");
                }
            } else { //If not, just walk around
                updatesSinceLastSeen++;
                if (updatesSincePathfind > (TARGET_UPS * 2)) {
                    if (!onGround(AgX, AgY)) {
                        boolean cont = true;
                        for (int i = (AgY + 1); cont; i++) {
                            if (onGround(AgX, i)) {
                                cont = false;
                                path = pf.getPath(AgX, AgY, AgX, i);
                                pathProg = 1;
                                updatesSincePathfind = 0;
                                changeState(BotState.MoveTo);
                            }
                        }
                    } else {
                        if (printStuff)
                            System.out.println("Walking around!");

                        ArrayList<Position> possible = new ArrayList<Position>();

                        int maxDistX = 7, minDistX = 0, maxDistY = 2, minDistY = 0;

                        for (int tx = AgX - maxDistX; tx <= AgX + maxDistX; tx++) {
                            if (Math.abs(tx - AgX) >= minDistX) {
                                for (int ty = AgY - maxDistY; ty <= AgY + maxDistY; ty++) {
                                    if (Math.abs(ty - AgY) >= minDistY && onGround(tx, ty))
                                        possible.add(new Position(tx, ty));
                                }
                            }
                        }

                        if (!possible.isEmpty()) {
                            int pathNo = random.nextInt(possible.size() - 1);
                            Position goalPos = possible.get(pathNo);
                            LinkedList<Position> tempPath = pf.getPath(AgX, AgY, goalPos.getX(), goalPos.getY());
                            if (tempPath.size() > 1) {
                                path = tempPath;
                                pathProg = 1;
                                updatesSincePathfind = 0;
                                changeState(BotState.MoveTo);
                            }
                        }
                    }
                }
            }

            updateMovement();
        }
    }

    /**
     * Updates the AI's movements according to their set path
     */
    public void updateMovement() {
        switch (currentState) {
            case Idle:
                anotherUpdate();
                moveHandle.takeAction(this, grid, movement);
                break;

            case MoveTo:
                anotherUpdate();

                //targ is in grid coords
                int targX = path.get(pathProg).getX(), targY = path.get(pathProg).getY();

                //cur is in pixel coords
                int curX = super.getXPos() + PLAYER_ORIGIN_X, curY = super.getYPos() + PLAYER_ORIGIN_Y;
                int curRX = curX + charWidth, curBY = curY + charHeight;

                //curg is in grid coords
                int curgX = curX / boxSize, curgY = curY / boxSize;
                int curgRX = curRX / boxSize, curgBY = curBY / boxSize;

                //nexg and lasg will be in grid coords
                int nexgX, nexgY, lasgX, lasgY;

                if (curX != prevX || curY != prevY){
                    curX = prevX;
                    curY = prevY;
                    updatesSinceMovement = 0;
                }
                else {
                    updatesSinceMovement++;
                    if(printStuff)
                        System.out.println("No movement");
                }


                if (pathProg > 0) {
                    lasgX = path.get(pathProg - 1).getX();
                    lasgY = path.get(pathProg - 1).getY();
                } else {
                    lasgX = targX;
                    lasgY = targY;
                }

                if (pathProg < (path.size() - 1)) {
                    nexgX = path.get(pathProg + 1).getX();
                    nexgY = path.get(pathProg + 1).getY();
                } else {
                    nexgX = targX;
                    nexgY = targY;
                }

                if (printStuff) {
                    System.out.println("Trying to get from (" + curgX + ", " + curgY + ") to (" + targX + ", " + targY + ").");
                    System.out.println("The bottom right pixel is (" + curgRX + ", " + curgBY + ").");
                }

                int tmpStateOfMovement = 0;

                if (targX < curgRX && targX <= lasgX) { //Needs to move left
                    if (printStuff)
                        System.out.println("==========Moving left");
                    movement[0] = true;
                    //tmpStateOfMovement += 2;
                } else if (targX > curgX && targX >= lasgX) { //Needs to move right
                    if (printStuff)
                        System.out.println("==========Moving right");
                    movement[2] = true;
                    //tmpStateOfMovement += 4;
                }

                if ((curgBY > targY)
                        && super.getInitial_y() == super.getYPos() && !super.isFalling()) { //Needs to jump
                    if (printStuff)
                        System.out.println("==========Moving up");
                    movement[1] = true;
                    super.setJumping(true);
                    //tmpStateOfMovement += 1;
                }
                //Falling down is handled by physics.

                //super.setStateOfMovement(tmpStateOfMovement);

                moveHandle.takeAction(this, grid, movement);

                curX = super.getXPos() + PLAYER_ORIGIN_X;
                curY = super.getYPos() + PLAYER_ORIGIN_Y;
                curgX = curX / boxSize;
                curgY = curY / boxSize;
                curRX = curX + charWidth;
                curBY = curY + charHeight;
                curgRX = curRX / boxSize;
                curgBY = curBY / boxSize;

                if (printStuff) {
                    System.out.println("Last target was: " + lasgX + ", " + lasgY + "\nCurrently at:    " + curgX + ", " + curgY + "\nTarget is at:    " + targX + ", " + targY + "\nNext Target at:  " + nexgX + ", " + nexgY);
                    System.out.println("curgX: " + curgX + " -- curgY: " + curgY + " -- curgRX: " + curgRX + " -- curgBY: " + curgBY + " -- pathProg: " + pathProg + " -- path size: " + path.size());
                }

                if (((targX == 0 && curgX == 0)
                        || (targX == xSize && curgRX == targX)
                        || (lasgX == targX && (curgX == targX || curgRX == targX))
                        || (lasgX > targX && ((curgRX >= targX && nexgX > targX) || (curgRX <= targX && nexgX < targX) || (curgRX == targX && nexgX == targX))) //Moving left
                        || (lasgX < targX && ((curgX >= targX && nexgX > targX) || (curgX <= targX && nexgX < targX) || (curgX == targX && nexgX == targX)))) //Moving right
                        &&
                        ((targY < nexgY && targY <= curgY)
                        || (targY > nexgY && targY >= curgBY)
                        || (targY == nexgY && (targY == curgY || targY == curgBY))
                        || (targY == 0 && curgY == 0)
                        || (targY == ySize && curgBY == targY))) {
                    if (printStuff)
                        System.out.println("++++++++++++++++++At or past target+++++++++++++++++++++");
                    pathProg++;
                }
               /* else if(printStuff){
                    System.out.println("(targX == 0 && curgX == 0) = " + (targX == 0 && curgX == 0));
                    System.out.println("(targX == xSize && curgRX == targX) = " + (targX == xSize && curgRX == targX));
                    System.out.println("(lasgX == targX && (curgX == targX || curgRX == targX)) = " + (lasgX == targX && (curgX == targX || curgRX == targX)));
                    System.out.println("(lasgX > targX && ((curgRX >= targX && nexgX > targX) || (curgRX <= targX && nexgX < targX) || (curgRX == targX && nexgX == targX))) = " + (lasgX > targX && ((curgRX >= targX && nexgX > targX) || (curgRX <= targX && nexgX < targX) || (curgRX == targX && nexgX == targX))));
                    System.out.println("(lasgX < targX && ((curgX >= targX && nexgX > targX) || (curgX <= targX && nexgX < targX) || (curgX == targX && nexgX == targX)))) = " + (lasgX < targX && ((curgX >= targX && nexgX > targX) || (curgX <= targX && nexgX < targX) || (curgX == targX && nexgX == targX))));
                    System.out.println("\n(targY < nexgY && targY <= curgY) = " + (targY < nexgY && targY <= curgY));
                    System.out.println("(targY > nexgY && targY >= curgBY) = " + (targY > nexgY && targY >= curgBY));
                    System.out.println("(targY == nexgY && (targY == curgY || targY == curBY) = " + (targY == nexgY && (targY == curgY || targY == curBY)));
                    System.out.println("(targY == 0 && curgY == 0) = " + (targY == 0 && curgY == 0));
                    System.out.println("(targY == ySize && curgBY == targY)" + (targY == ySize && curgBY == targY));
                }*/

                if (pathProg >= path.size()) {
                    changeState(BotState.Idle);
                    path.clear();
                    if(printStuff){
                        System.out.println("+++++++++++++++++++++++++++++++++Path finished+++++++++++++++++++++++++++++++++++");
                    }
                }

                break;
        }
    }

    /**
     * Increases values updatesSinceLastHit and updatesSincePathfind by 1
     */
    private void anotherUpdate() {
        updatesSinceLastHit++;
        updatesSincePathfind++;
    }

    /**
     * Changes the AIPlayer's state, so it knows when to move
     * @param newState  the state which the AIPlayer will be changed to
     */
    public void changeState(BotState newState){
        this.currentState = newState;
    }

    /**
     * Returns the path currently being followed
     * @return  the path
     */
    public LinkedList<Position> getPath(){
        return path;
    }

    /**
     * Returns the update values
     * @return  an array of updatesSincePathfind, updatesSinceLastSeen, and updatesSinceLastHit
     */
    public int[] getUpdates(){
        int[] ret = new int[3];
        ret[0] = updatesSincePathfind;
        ret[1] = updatesSinceLastSeen;
        ret[2] = updatesSinceLastHit;
        return ret;
    }

    /**
     * Sets the given path as the AIPlayer's path
     * @param newPath   the new path
     */
    public void setPath(LinkedList<Position> newPath){
        this.path = newPath;
        changeState(BotState.MoveTo);
    }
}
