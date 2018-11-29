package main.code.game.JUnit;

import javafx.scene.Scene;
import main.code.game.AI.BotState;
import main.code.game.AI.Position;
import main.code.game.audio.AudioHandler;
import main.code.renderer.Window;
import main.code.utils.AIPlayer;
import main.code.utils.Map;
import main.code.utils.MapReader;
import main.code.utils.Player;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedList;

import static main.code.utils.GameConstants.*;
import static org.junit.Assert.*;

public class AITests {
    DummyAIPlayer ai;
    static Window window;
    static Map map;

    private int gridToXPos(int grid){   //This supposes you want the bottom right side of the player to be in this grid coordinate.
        int ret = (grid * BOX_HEIGHT) - PLAYER_WIDTH - PLAYER_ORIGIN_X;
        System.out.println(grid + " ->" + ret);
        return ret;
    }

    private int gridToYPos(int grid){
        return (grid * BOX_HEIGHT) - PLAYER_HEIGHT - PLAYER_ORIGIN_Y;
    }

    private int xPosToGrid(int xPos){
        return (xPos + PLAYER_WIDTH + PLAYER_ORIGIN_X) / BOX_HEIGHT;
    }

    @BeforeClass
    public static void getResources(){   //We only need to read the map once
        map = (new MapReader().readMap("src/main/resources/Maps"));
        window = new Window("AI Test", false);
    }

    @Before
    public void createAI(){
        ai = new DummyAIPlayer((byte)1, map, 5, window);
        ai.init();
    }

    @Test
    public void AIMoveAlongPath(){
        ai.setCoord((short)gridToXPos(5),(short)gridToYPos(9));

        /*
            Due to the randomness involved in moving the AI, the AI doesn't always move right if they are close enough to the Player
            Instead they move back and forth if they can still shoot the Player.
            So we will artificially give the AI a path to follow to test their movement.
        */

        LinkedList<Position> path = new LinkedList<Position>();
        path.add(new Position(5,9));
        path.add(new Position(10,9));

        ai.setPath(path);
        for (int i = 0; i < 10; i++) {
            ai.updateMovement();
        }

        assertTrue((xPosToGrid(ai.getXPos()) > 5 && ai.getMovement()[2]));
    }

    @Test
    public void AIJump(){
        ai.setCoord((short)gridToXPos(5),(short)gridToYPos(9));

        LinkedList<Position> path = new LinkedList<Position>();
        path.add(new Position(5,9));
        path.add(new Position(5,5));

        ai.setPath(path);
        ai.updateMovement();

        assertTrue(ai.getInitial_y() == gridToYPos(9)); //This is true when a jump was started when the AI was at y-coord 9.
    }

    @Test
    public void AIFindPath(){
        ai.setCoord((short)gridToXPos(62),(short)gridToYPos(11));

        ai.update(gridToXPos(62), gridToYPos(17), 0);

        LinkedList<Position> path = ai.getPath();

        for(Position pos : path){
            System.out.println(pos.toString());
        }

        assertTrue(path.size() != 0 && path.getLast().getY() == 17);
    }

    @Test
    public void AIIdle(){
        ai.setCoord((short)gridToXPos(9),(short)gridToYPos(9));

        for(int i = 0; i < 50; i++) {
            ai.update(gridToXPos(50), gridToYPos(50), 0);
        }

        assertTrue(xPosToGrid(ai.getXPos()) != 5);
    }

    @Test
    public void AIAimAtPlayer(){
        ai.setCoord((short)gridToXPos(5),(short)gridToYPos(9));

        ai.update(gridToXPos(8),gridToYPos(9),0);


        assertTrue(ai.getShootingDirection() == 1);

        createAI();
        ai.setCoord((short)gridToXPos(5),(short)gridToYPos(9));

        ai.update(gridToXPos(3),gridToYPos(9),0);

        assertTrue(ai.getShootingDirection() == -1);
    }

    @Test
    public void AIShootAtPlayer(){
        ai.setCoord((short)gridToXPos(5),(short)gridToYPos(9));

        ai.update(gridToXPos(11),gridToYPos(9),0);

        assertTrue(ai.getBulletsFired().size() > 0);
    }
}