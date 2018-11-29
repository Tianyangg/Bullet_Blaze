package main.code.utils;

import javafx.util.Pair;
import main.code.renderer.Window;
import main.code.game.GameState;

import static main.code.utils.GameConstants.*;

/**Class to represent a bullet fired from a weapon.
 * Contains all the information needed to check for
 * collisions between itself and players, boxes and the
 * map.
 *
 * @author Thomas Murphy
 */
@SuppressWarnings("Duplicates")
public class Bullet{

    private short xPos;
    private short yPos;
    public short originX;
    private byte sourcePlayerId;
    private byte dmg;
    private boolean isDisplayed;
    private double range;
    public short direction;
    private byte weaponID;


    /**Default constructor for bullet
     */
    public Bullet(){
        xPos = 0;
        yPos = 0;
        sourcePlayerId = 0;
        isDisplayed = true;
        this.direction = 1;
    }

    /** Constructor used by networking to rebuild bullet object when it should be displayed
     * @param xPos - x coordinate of the bullet
     * @param yPos - y coordinate of the bullet
     * @param playerOrigin - The x coordinate where the bullet was fired
     * @param sourcePlayerId - The id of the player who shot the bullet
     * @param dmg - The damage value of the bullet
     * @param range - The range of the bullet
     * @param direction - The direction that the bullet is travelling
     * @param weaponID - The id of the weapon that the bullet was fired from
     */
    public Bullet(short xPos, short yPos, short playerOrigin, byte sourcePlayerId, byte dmg, double range, short direction, byte weaponID){
        this.xPos = xPos;
        this.yPos = yPos;
        this.originX = playerOrigin;
        this.sourcePlayerId = sourcePlayerId;
        this.dmg = dmg;
        this.range = range;
        isDisplayed = true;
        this.direction = direction;
        this.weaponID = weaponID;
    }

    /** Constructor used by networking to rebuild object after being sent through network
     * @param xPos - x coordinate of the bullet
     * @param yPos - y coordinate of the bullet
     * @param playerOrigin - The x coordinate where the bullet was fired
     * @param sourcePlayerId - The id of the player who shot the bullet
     * @param dmg - The damage value of the bullet
     * @param isDisplayed - Boolean to indicate whether the bullet should be displayed
     * @param range - The range of the bullet
     * @param direction - The direction that bullet is travelling
     * @param weaponID - The id of the weapon that the bullet was fired from
     */
    public Bullet(short xPos, short yPos, short playerOrigin, byte sourcePlayerId, byte dmg, boolean isDisplayed, double range, short direction, byte weaponID){
        this.xPos = xPos;
        this.yPos = yPos;
        this.originX = playerOrigin;
        this.sourcePlayerId = sourcePlayerId;
        this.dmg = dmg;
        this.range = range;
        this.isDisplayed = isDisplayed;
        this.direction = direction;
        this.weaponID = weaponID;
    }

    /** Sets the damage value of the bullet
     * @param dmg - The updated damage value
     */
    public void setDmg(byte dmg){
        this.dmg = dmg;
    }

    /** Returns the damage value of the bullet
     * @return The damage value of the bullet
     */
    public byte getDmg(){
        return dmg;
    }

    /** Returns the id of the player that shot the bullet
     * @return The id of the player that shot the bullet
     */
    public byte getSourcePlayerId() {
        return sourcePlayerId;
    }

    /** Returns the y coordinate of the bullet
     * @return The y coordinate of the bullet
     */
    public short getyPos() {
        return yPos;
    }

    /** Returns the x coordinate of the bullet
     * @return The x coordinate of the bullet
     */
    public short getxPos() {
        return xPos;
    }

    /** Returns whether the bullet should be displayed
     * @return boolean to indicate whether the bullet should be displayed
     */
    public boolean isDisplayed(){
        return this.isDisplayed;
    }

    /** Sets whether the bullet should be displayed
     * @param isDisplayed - boolean to indicate whether the bullet should be displayed
     */
    private void setDisplayed(boolean isDisplayed){
        this.isDisplayed = isDisplayed;
    }

    /** Returns the range of the bullet
     * @return range value of the bullet
     */
    public double getRange(){
        return this.range;
    }

    /** Sets the range of the bullet
     * @param range - updated range value of the bullet
     */
    public void setRange(double range) {
        this.range = range;
    }

    /** Returns the id of the weapon that shot the bullet
     * @return id of the weapon that shot the bullet
     */
    public byte getWeaponID() { return weaponID; }

    /** Moves the bullets by the given bullet speed. Each pixel the bullet moves
     *  is checked for collisions between the bullet and the players, boxes or the
     *  environment. If no collisions are found, then the bullet can be continue to move
     * @param window - The window which the game will be rendered within
     * @param gameState - current gameState of the game which contains the players, boxes
     *                  and map to check against for collisions
     */
    public void moveBullet(Window window, GameState gameState){

        int[][] gridMap = gameState.getMap().getGrid();

        for(int i = 0; i < BULLET_SPEED; i++){
            int nextXPos = this.xPos+direction;

            //Changes the damage of certain weapons
            //based on distance travelled
            //for sniper - damage increases the more it travels
            //for shotgun - damage decreases the more it travels
            if(weaponID == SNIPER_ID || weaponID == SHOTGUN_ID){
                int distTravelled = nextXPos - (this.originX+BOX_HEIGHT);
                if(distTravelled < 0){
                    distTravelled = distTravelled * -1;
                }
                switch (weaponID){
                    case SNIPER_ID:
                        //Caps damage change - can only increase by 50%
                        if(this.dmg <= (SNIPER_DMG + (SNIPER_DMG/2))){
                            if((distTravelled%75) == 0) {
                                dmg += 1;
                            }
                        }
                        break;
                    case SHOTGUN_ID:
                        if(this.dmg >= (SHOTGUN_DMG/2)){
                            if((distTravelled%45) == 0) {
                                dmg -= 1;
                            }
                        }
                        break;
                }
            }


            Player playerWhoShot = null;
            for(Player p : gameState.getPlayers()){
                if(p.getId()==sourcePlayerId){
                    playerWhoShot = p;
                }
            }

            //Only checks collisions for players that didnt fire that bullet
            //meaning, you cannot get hurt from your own weapon. Also only checks
            //for bullets from an AIPlayer with the player, meaning, AIPlayers cannot
            //hurt each other
            for(Player p : gameState.getPlayers()){
                if(p.getId() != sourcePlayerId){
                    if(playerWhoShot instanceof AIPlayer){
                        if(!(p instanceof AIPlayer)) {
                            if (p.isAlive()) {
                                //Check for collision between left hand side of player and right hand side of bullet
                                //(for when bullet is travelling to the right)
                                if ((((nextXPos + BOX_HEIGHT / 900) >= p.getXPos()) && (nextXPos <= p.getXPos())) &&
                                        ((this.yPos >= p.getYPos()) && (this.yPos <= (p.getYPos() + PLAYER_HEIGHT)))) {
                                    System.out.println("Bullet collided with - " + p.getId());
                                    p.setHp((byte) (p.getHp() - this.getDmg()));
                                    System.out.println("Health of AI - " + p.getId() + " = " + p.getHp());
                                    if (p.getHp() <= 0) {
                                        System.out.println("You killed - " + p.getId() + " - score +100");
                                        playerWhoShot.setScore((playerWhoShot.getScore() + 100));
                                    }
                                    this.setDisplayed(false);
                                    return;
                                }
                                //Check collision between left hand side of bullet and right hand side of player
                                else if (((nextXPos <= (p.getXPos() + PLAYER_WIDTH)) && ((nextXPos + BOX_HEIGHT / 900) >= (p.getXPos() + PLAYER_WIDTH))) &&
                                        ((this.yPos >= p.getYPos()) && (this.yPos <= (p.getYPos() + PLAYER_HEIGHT)))) {
                                    System.out.println("Bullet collided with - " + p.getId());
                                    p.setHp((byte) (p.getHp() - this.getDmg()));
                                    System.out.println("Health of AI - " + p.getId() + " = " + p.getHp());
                                    if (p.getHp() <= 0) {
                                        System.out.println("You killed - " + p.getId() + " - score +100");
                                        playerWhoShot.setScore(playerWhoShot.getScore() + 100);
                                    }
                                    this.setDisplayed(false);
                                    return;
                                }
                            }
                        }
                    }
                    else {
                        if (p.isAlive()) {
                            //Check for collision between left hand side of player and right hand side of bullet
                            //(for when bullet is travelling to the right)
                            if ((((nextXPos + BOX_HEIGHT / 900) >= p.getXPos()) && (nextXPos <= p.getXPos())) &&
                                    ((this.yPos >= p.getYPos()) && (this.yPos <= (p.getYPos() + PLAYER_HEIGHT)))) {
                                System.out.println("Bullet collided with - " + p.getId());
                                p.setHp((byte) (p.getHp() - this.getDmg()));
                                System.out.println("Health of AI - " + p.getId() + " = " + p.getHp());
                                if (p.getHp() <= 0) {
                                    System.out.println("You killed - " + p.getId() + " - score +100");
                                    playerWhoShot.setScore((playerWhoShot.getScore() + 100));
                                }
                                this.setDisplayed(false);
                                return;
                            }
                            //Check collision between left hand side of bullet and right hand side of player
                            else if (((nextXPos <= (p.getXPos() + PLAYER_WIDTH)) && ((nextXPos + BOX_HEIGHT / 900) >= (p.getXPos() + PLAYER_WIDTH))) &&
                                    ((this.yPos >= p.getYPos()) && (this.yPos <= (p.getYPos() + PLAYER_HEIGHT)))) {
                                System.out.println("Bullet collided with - " + p.getId());
                                p.setHp((byte) (p.getHp() - this.getDmg()));
                                System.out.println("Health of AI - " + p.getId() + " = " + p.getHp());
                                if (p.getHp() <= 0) {
                                    System.out.println("You killed - " + p.getId() + " - score +100");
                                    playerWhoShot.setScore(playerWhoShot.getScore() + 100);
                                }
                                this.setDisplayed(false);
                                return;
                            }
                        }
                    }
                }
            }

            //Checks collision with boxes
            //If the box gets destroyed, it checks if there is
            //a weapon inside. If so, the weapon is added to whoever
            //shot the bullet that destroyed it. If there is no weapon,
            //the player receives a health boost
            for(Box b : gameState.getBoxes()){
                if(!b.isDestroyed()){
                    //Check for collision between left hand side of box and right hand side of bullet
                    //(for when bullet is travelling to the right)
                    if ((((nextXPos + BOX_HEIGHT/900) >= b.getXPos()) && (nextXPos <= b.getXPos())) &&
                            ((this.yPos >= b.getYPos()) && (this.yPos <= (b.getYPos()+BOX_HEIGHT)))) {
                        System.out.println("Bullet collided with box");
                        b.setHp((byte) (b.getHp() - this.getDmg()));
                        System.out.println("Health of box = " + b.getHp());
                        this.setDisplayed(false);

                        if(b.getHp() <= 0){
                            System.out.println("Box destroyed");
                            Pair<Byte,Short> weapon = b.getContentsOfBox();
                            gridMap[b.getYPos()/BOX_HEIGHT][b.getXPos()/BOX_HEIGHT] = 0;
                            Map updatedMap = new Map(gridMap,gameState.getMap().getSpawns(),gameState.getMap().getBoxSpawns());
                            gameState.setMap(updatedMap);

                            if(weapon != null){
                                if(weapon.getKey() != NO_WEAPON_ID) {
                                    System.out.println("There is a weapon inside");
                                    Player boxDestroyer = null;
                                    for (Player p : gameState.getPlayers()) {
                                        if (p.getId() == this.getSourcePlayerId()) {
                                            boxDestroyer = p;
                                        }
                                    }
                                    if(weapon.getKey() != NO_WEAPON_ID) {
                                        boxDestroyer.addWeapon(weapon);
                                    }
                                    System.out.println("Number of weapons player - " + boxDestroyer.getId() + " has = " + boxDestroyer.getWeapons().size());
                                }

                            }else{
                                System.out.println("There is no weapon inside.");
                                byte newHp = (byte) (playerWhoShot.getHp()+25);
                                if(newHp >= 100){
                                    playerWhoShot.setHp((byte) 100);
                                }
                                else {
                                    playerWhoShot.setHp((byte) (playerWhoShot.getHp() + 25));
                                }
                            }
                            b.setDestroyed(true);
                        }
                        return;
                    }
                    //Check collision between left hand side of bullet and right hand side of box
                    else if(((nextXPos <= (b.getXPos()+BOX_HEIGHT)) && ((nextXPos + BOX_HEIGHT/900) >= (b.getXPos()+BOX_HEIGHT))) &&
                            ((this.yPos >= b.getYPos()) && (this.yPos <= (b.getYPos()+BOX_HEIGHT)))){
                        System.out.println("Bullet collided with box");
                        b.setHp((byte) (b.getHp() - this.getDmg()));
                        System.out.println("Health of box = " + b.getHp());
                        this.setDisplayed(false);

                        if(b.getHp() <= 0) {
                            System.out.println("Box destroyed");
                            Pair<Byte, Short> weapon = b.getContentsOfBox();
                            gridMap[b.getYPos()/BOX_HEIGHT][b.getXPos()/BOX_HEIGHT] = 0;
                            Map updatedMap = new Map(gridMap,gameState.getMap().getSpawns(),gameState.getMap().getBoxSpawns());
                            gameState.setMap(updatedMap);

                            if (weapon != null) {
                                System.out.println("There is a weapon inside");
                                Player boxDestroyer = null;
                                for (Player p : gameState.getPlayers()) {
                                    if (p.getId() == this.getSourcePlayerId()) {
                                        boxDestroyer = p;
                                    }
                                }
                                if(weapon.getKey() != NO_WEAPON_ID) {
                                    boxDestroyer.addWeapon(weapon);
                                }
                                System.out.println("Number of weapons player - " + boxDestroyer.getId() + " has = " + boxDestroyer.getWeapons().size());

                            } else {
                                System.out.println("There is no weapon inside.");
                                byte newHp = (byte) (playerWhoShot.getHp()+25);
                                if(newHp >= 100){
                                    playerWhoShot.setHp((byte) 100);
                                }
                                else {
                                    playerWhoShot.setHp((byte) (playerWhoShot.getHp() + 25));
                                }
                            }
                            b.setDestroyed(true);
                        }
                        return;
                    }
                }
            }

            //Collision check with environment
            //This is done via the grid coordinates
            float nextXBoxPos = ((xPos + direction)*1.0f)/BOX_HEIGHT;
            float nextYBoxPos = (yPos * 1.0f)/BOX_HEIGHT;

            if(direction == 1){
                if ( !((nextXBoxPos < gridMap[0].length) && (gridMap[(int) nextYBoxPos][(int) nextXBoxPos] == 0)) ) {
                    System.out.println("Bullet collided with wall");
                    this.setDisplayed(false);
                    return;
                }
            }else{
                if ( !((nextXBoxPos <= 1 && nextXBoxPos > 0) || ( nextXBoxPos > 1 && gridMap[(int)nextYBoxPos][(int)nextXBoxPos] == 0)) ) {
                    System.out.println("Bullet collided with wall");
                    this.setDisplayed(false);
                    return;
                }
            }


            //If there are no collisions, then the bullet can move
            this.xPos += (direction);
        }
        if((direction == 1 && (this.xPos > (this.range * window.getHeight()/20) + originX)) || (direction == -1 && (this.xPos < (originX - (this.range * window.getHeight()/20))))){
            this.isDisplayed = false;
        }
    }

    /** Returns current state of bullet
     * @return String consisting of values of all fields of the bullet
     */
    public String toString(){
        return "xpos: " + Integer.toString(xPos) + " " +"ypos: "+ Integer.toString(yPos) + " " +"sourcePlayerId: "+ String.valueOf(sourcePlayerId) + " originx: "+
                Short.toString(originX) + " dmg: " + String.valueOf(dmg) + " range: "+Double.toString(range) + " direction: "+String.valueOf(direction);
    }
}
