package main.code.game.JUnit;

import main.code.utils.Map;
import main.code.utils.MapReader;
import main.code.utils.Player;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import static main.code.utils.GameConstants.*;

public class PhysicsTests {

    static Map map;
    static int[][] grid;
    static DummyPhysics movement;
    static Player player;
    static boolean[] actions;

    @BeforeClass
    public static void getMap(){   //We only need to read the map once
        map = (new MapReader().readMap("src/main/resources/Maps"));
        grid = map.getGrid();
        movement = new DummyPhysics(BOX_HEIGHT);
        player = new Player();
        actions = new boolean[3];
    }

    @Test
    public void NoInput(){
        actions[0] = false;
        actions[1] = false;
        actions[2] = false;
        player.setCoord((short)(10 * BOX_HEIGHT + PLAYER_ORIGIN_X), (short)(8 * BOX_HEIGHT + PLAYER_ORIGIN_Y));
        player.setInitial_y((short)(7 * BOX_HEIGHT + PLAYER_ORIGIN_Y));
        assertTrue(player.getXPos() == movement.takeAction(player, grid, actions).getXPos());
        assertTrue(player.getYPos() == movement.takeAction(player, grid, actions).getYPos());
    }

    @Test
    public void MoveLeft(){
        actions[0] = true;
        actions[1] = false;
        actions[2] = false;
        player.setCoord((short)(19 * BOX_HEIGHT + PLAYER_ORIGIN_X), (short)(7 * BOX_HEIGHT + PLAYER_ORIGIN_Y));
        player.setInitial_y((short)(7 * BOX_HEIGHT + PLAYER_ORIGIN_Y));
        assertTrue(player.getXPos() - PLAYER_SPEED == movement.takeAction(player, grid, actions).getXPos());
    }

    @Test
    public void MoveRight(){
        actions[0] = false;
        actions[1] = false;
        actions[2] = true;
        player.setCoord((short)(19 * BOX_HEIGHT + PLAYER_ORIGIN_X), (short)(7 * BOX_HEIGHT + PLAYER_ORIGIN_Y));
        player.setInitial_y((short)(7 * BOX_HEIGHT + PLAYER_ORIGIN_Y));
        assertTrue(player.getXPos() + PLAYER_SPEED == movement.takeAction(player, grid, actions).getXPos());
    }

    @Test
    public void SprintLeft(){
        actions[0] = true;
        actions[1] = true;
        actions[2] = false;
        player.setCoord((short)(19 * BOX_HEIGHT + PLAYER_ORIGIN_X), (short)(7 * BOX_HEIGHT + PLAYER_ORIGIN_Y));
        player.setInitial_y((short)(7 * BOX_HEIGHT + PLAYER_ORIGIN_Y));
        player.addEnergy(500);
        assertTrue(player.getXPos() - PLAYER_SPRINT_SPEED == movement.takeAction(player, grid, actions).getXPos());
    }

    @Test
    public void SprintRight(){
        actions[0] = false;
        actions[1] = true;
        actions[2] = true;
        player.setCoord((short)(19 * BOX_HEIGHT + PLAYER_ORIGIN_X), (short)(7 * BOX_HEIGHT + PLAYER_ORIGIN_Y));
        player.setInitial_y((short)(7 * BOX_HEIGHT + PLAYER_ORIGIN_Y));
        player.addEnergy(500);
        assertTrue(player.getXPos() + PLAYER_SPRINT_SPEED == movement.takeAction(player, grid, actions).getXPos());
    }

    @Test
    public void Jump(){
        actions[0] = false;
        actions[1] = false;
        actions[2] = false;
        player.setJumping(true);
        player.setCoord((short)(19 * BOX_HEIGHT + PLAYER_ORIGIN_X), (short)(7 * BOX_HEIGHT + PLAYER_ORIGIN_Y));
        player.setInitial_y((short)(7 * BOX_HEIGHT + PLAYER_ORIGIN_Y));
        assertTrue(player.getYPos() - PLAYER_JUMP_SPEED == movement.takeAction(player, grid, actions).getYPos());
    }

    @Test
    public void Falling(){
        actions[0] = false;
        actions[1] = false;
        actions[2] = false;
        player.setJumping(false);
        player.setFalling(true);
        player.setCoord((short)(19 * BOX_HEIGHT + PLAYER_ORIGIN_X), (short)(6 * BOX_HEIGHT + PLAYER_ORIGIN_Y));
        player.setInitial_y((short)(8 * BOX_HEIGHT + PLAYER_ORIGIN_Y));
        assertTrue(player.getYPos() + PLAYER_JUMP_SPEED == movement.takeAction(player, grid, actions).getYPos());
    }

    @Test
    public void CollisionLeft(){
        actions[0] = true;
        actions[1] = false;
        actions[2] = false;
        player.setJumping(false);
        player.setFalling(false);
        player.setCoord((short)(5 * BOX_HEIGHT - PLAYER_ORIGIN_X + 5), (short)(7 * BOX_HEIGHT + PLAYER_ORIGIN_Y));
        player.setInitial_y((short)(7 * BOX_HEIGHT + PLAYER_ORIGIN_Y));
        assertTrue(movement.takeAction(player, grid, actions).getXPos() == 5 * BOX_HEIGHT - PLAYER_ORIGIN_X);
    }

    @Test
    public void JumpColision(){
        actions[0] = false;
        actions[1] = false;
        actions[2] = false;
        player.setJumping(true);
        player.setFalling(false);
        player.setCoord((short)(28 * BOX_HEIGHT), (short)(8 * BOX_HEIGHT - PLAYER_ORIGIN_Y + 7));
        player.setInitial_y((short)(8 * BOX_HEIGHT + PLAYER_ORIGIN_Y));
        assertTrue(movement.takeAction(player,grid,actions).getYPos() == 8 * BOX_HEIGHT - PLAYER_ORIGIN_Y);
    }

}
