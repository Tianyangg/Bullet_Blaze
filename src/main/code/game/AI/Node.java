package main.code.game.AI;

import java.util.LinkedList;

/**
 * A node in the Pathfinder search tree
 * @author Jamie Moloney
 */
public class Node {
    private Position pos;
    private LinkedList<Position> instructions;
    private int heuristic;
    private int level;

    /**
     * Creates a Node with the Position, instructions, heuristic and level values set to the values given
     * @param pos           the position to be held by the node
     * @param instructions  the instructions to get from the start position to this position
     * @param heuristic     the heuristic value calculated for this node
     * @param level         the level on the search tree this node is on
     */
    public Node(Position pos, LinkedList<Position> instructions, int heuristic, int level){
        this.pos = pos;
        this.instructions = instructions;
        this.heuristic = heuristic;
        this.level = level;
    }

    /**
     * Returns the position held by the node
     * @return the position held by the node
     */
    public Position getPos(){
        return pos;
    }

    /**
     * Returns the instruction list held by the node
     * @return the instruction list held by the node
     */
    public LinkedList<Position> getInst(){
        return instructions;
    }

    /**
     * Returns the heuristic held by the node
     * @return the heuristic held by the node
     */
    public int getHeuristic(){
        return heuristic;
    }

    /**
     * Returns the level held by the node
     * @return the level held by the node
     */
    public int getLevel(){
        return level;
    }

}
