package main.code.game.AI;

import java.util.Objects;

/**
 * A holder for x positions, y positions and a jump value
 * @author Jamie Moloney
 */
public class Position {
    private int x, y, jump;

    /**
     * Creates a Position with the x and y coordinates given and a jump value of 0
     * @param x the given x coordinate
     * @param y the given y coordinate
     */
    public Position(int x, int y){
        this.x = x;
        this.y = y;
        this.jump = 0;
    }

    /**
     * Creates a Position with the x and y coordinates and jump value given
     * @param x     the given x coordinate
     * @param y     the given y coordinate
     * @param jump  the given jump value
     */
    public Position(int x, int y, int jump){
        this.x = x;
        this.y = y;
        this.jump = jump;
    }

    /**
     * Returns the x coordinate held by the Position
     * @return the x coordinate
     */
    public int getX(){
        return x;
    }

    /**
     * Returns the y coordinate held by the Position
     * @return the y coordinate
     */
    public int getY(){
        return y;
    }

    /**
     * Sets the y coordinate of the Position to the given y coordinate
     * @param y the given y coordinate
     */
    public void setY(int y){
        this.y = y;
    }

    /**
     * Returns the jump value held by the Position
     * @return the jump value
     */
    public int getJump(){
        return jump;
    }

    /**
     * Sets the jump value of the Position to the given jump value
     * @param jump the given jump value
     */
    public void setJump(int jump){
        this.jump = jump;
    }

    /**
     * Compares this Position with another given object to check if they are equivalent
     * @param o the given object
     * @return  true if the object is equivalent to this Position
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Position pos2 = (Position) o;
        return this.x == pos2.getX() &&
                this.y == pos2.getY() &&
                this.jump == pos2.getJump();
    }

    /**
     * Returns a hash code for this Position
     * @return  the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY(), getJump());
    }
    //Needed ^

    /**
     * Compares this Position's and the given Position's x and y coordinates and returns true if they are the same
     * @param pos2  the given Position
     * @return      true if the x and y coordinates are the same
     */
    public boolean equals(Position pos2){
        return equals(pos2, false);
    }

    /**
     * Compares this Position's and the given Position's x and y coordinates and returns true if they are the same. If considerJump is true, the jump value is also compared
     * @param pos2          the given Position
     * @param considerJump  whether to consider the jump value
     * @return              true if the x and y coordinates - and optionally jump value - are the same
     */
    public boolean equals(Position pos2, boolean considerJump){
        if(this.x == pos2.getX() && this.y == pos2.getY()){
            if(!considerJump || this.jump == pos2.getJump())
                return true;
        }

        return false;
    }

    /**
     * Converts the Position to a String
     * @return  the Position as a string
     */
    public String toString(){
        return "(" + this.x + ", " + this.y + ", " + this.jump + ")";
    }

    /**
     * Returns the new Position given by the given movement
     * @param mve   the given movement
     * @return      the new Position given
     */
    public Position move(int[] mve){
        return new Position(this.x + mve[0], this.y + mve[1]);
    }
}