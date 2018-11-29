package main.code.game.JUnit;

import main.code.game.AI.Pathfinder;
import main.code.game.AI.Position;
import main.code.utils.Map;
import main.code.utils.MapReader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.*;

public class PathfinderTests {

    static Map map;
    static int[][] grid;
    Pathfinder pf;
    LinkedList<Position> path;

    private boolean validPos(int x, int y){
        if(grid[y][x] == 0)
            return true;

        return false;
    }

    private boolean checkPath(LinkedList<Position> path){
        for (Position pos : path) {
            if(!validPos(pos.getX(),pos.getY()))
                return false;
        }
        return true;
    }

    @BeforeClass
    public static void getMap(){   //We only need to read the map once
        map = (new MapReader().readMap("src/main/resources/Maps"));
        grid = map.getGrid();
    }

    @Before
    public void createPF(){ //But we should create a fresh Pathfinder each time
        pf = new Pathfinder(map, 5);
    }

    @Test
    public void PFReturnEmpty(){
        path = pf.getPath(1,9,1,9);
        assertTrue(path.size() == 0);
    }

    @Test
    public void PFFindSimpPath(){
        path = pf.getPath(5,9,9,9, false);
        assertTrue(path.size() == 5 && checkPath(path));
    }

    @Test
    public void PFCanJump(){
        path = pf.getPath(20,9,24,6, false);
        assertTrue(checkPath(path) && path.size() > 0);
    }

    @Test
    public void PFComplexPath(){
        path = pf.getPath(62,11,78,19,false);
        for(Position pos : path){
            System.out.println(pos.toString());
        }
        System.out.println("");
        assertTrue(checkPath(path) && path.size() > 0);
    }

    @Test
    public void PFFilterStraightPath(){
        path = pf.getPath(5,9,9,9, true);
        assertTrue(path.size() == 2 && checkPath(path)); //Should just be start position and end position
    }

    @Test
    public void PFFilterJump(){
        path = pf.getPath(20, 9, 24, 6, true);
        assertTrue(path.size() < pf.getPath(20,9,24,6, false).size() && checkPath(path)); //Doesn't really matter exactly how many nodes are left, as long as they work.
    }

    @Test
    public void PFFilterComplexPath(){
        path = pf.getPath(62,11,78,19,true);
        assertTrue(path.size() < pf.getPath(62,11,78,19, false).size() && checkPath(path));
    }
}
