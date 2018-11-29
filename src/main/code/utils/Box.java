package main.code.utils;

import javafx.util.Pair;

/** This class represents the destructible boxes placed around the map.
 * The player can break the boxes by shooting them.
 * The box can contain a weapon so that when the player
 * breaks the box the weapon will be added to their inventory
 *
 * @author Thomas Murphy
 */
public class Box {
    private short xPos;
    private short yPos;
    private byte hp;
    private Pair<Byte,Short>  contentsOfBox;
    private boolean isDestroyed;

    /** Default constructor for the box
     */
    public Box() {
        init();
    }

    /** Constructor used by networking to rebuild box object after it has been sent
     * @param xPos - x coordinate of box
     * @param yPos - y coordinate of box
     * @param hp - the health value of the box
     * @param content - the contents of the box
     * @param isDestroyed - boolean to indicate if the box has been destroyed
     */
    public Box(short xPos, short yPos, byte hp, Pair<Byte, Short> content, boolean isDestroyed) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.hp = hp;
        this.contentsOfBox = content;
        this.isDestroyed = isDestroyed;
    }

    /** Method to initialise a box
     */
    public void init(){
        this.xPos = 0;
        this.yPos = 0;
        this.hp = 20;
        this.contentsOfBox = null;
        this.isDestroyed = false;
    }

    /** Returns the x coordinate of the box
     * @return x coordinate of the box
     */
    public short getXPos() {
        return xPos;
    }

    /** Returns the y coordinate of the box
     * @return y coordinate of the box
     */
    public short getYPos() {
        return yPos;
    }

    /** Sets the coordinates of the box
     * @param xPos - x coordinate of the box
     * @param yPos - y coordinate of the box
     */
    public void setCoord(short xPos , short yPos ){
        this.xPos = xPos;
        this.yPos = yPos;
    }

    /** Returns the current health value of the box
     * @return health of the box
     */
    public byte getHp() {
        return hp;
    }

    /** Sets the current health value of the box
     * @param hp - updated health value
     */
    public void setHp(byte hp) {
        this.hp = hp;
    }

    /** Returns the content of the box
     * @return the content of the box - weapon or null
     */
    public Pair<Byte, Short> getContentsOfBox() {
        return contentsOfBox;
    }

    /** Sets the contents of the box
     * @param contentsOfBox - new contents of box - weapon or null
     */
    public void setContentsOfBox(Pair<Byte, Short> contentsOfBox) {
        this.contentsOfBox = contentsOfBox;
    }

    /** Returns whether the box is destroyed or not
     * @return boolean to indicate if the box is destroyed
     */
    public boolean isDestroyed() {
        return isDestroyed;
    }

    /** Sets whether the box has been destroyed
     * @param destroyed - boolean to indicate if the box has been destroyed
     */
    public void setDestroyed(boolean destroyed) {
        isDestroyed = destroyed;
    }

    /** Returns current state of box
     * @return String consisting of values of all fields of the box
     */
    public String toString(){
        String s = "";
        s+= "xPos: " + Short.toString(xPos) + " yPos: " + Short.toString(yPos) + " Hp: " + Byte.toString(hp)
                + " Content: Key = " + Byte.toString(contentsOfBox.getKey()) +" Value = " + Short.toString(contentsOfBox.getValue())
                + Boolean.toString(isDestroyed) + '\n';
        return s;
    }

}
