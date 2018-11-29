package main.code.game.JUnit;

import javafx.util.Pair;
import main.code.game.GameState;
import main.code.renderer.Window;
import main.code.utils.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

import static main.code.utils.GameConstants.*;
import static org.junit.Assert.*;

public class BulletTests {

    private static Map map;
    private static ArrayList<Pair<Byte,Short>> chosenWeapons;
    private static ArrayList<Box> boxes;
    private static ArrayList<Player> players;
    private static Player player, player2;
    private static Window window;
    private static GameState gamestate;
    private Bullet bullet;

    @BeforeClass
    public static void getResources(){
        map = (new MapReader().readMap("src/main/resources/map/map1"));
        chosenWeapons = new ArrayList<>();
        chosenWeapons.add(new Pair<>(NO_WEAPON_ID,(short)0));
        boxes = map.setupBoxes(chosenWeapons);
        player = new Player((byte) 0);
        player2 = new Player((byte) 1);
        player2.setCoord((short)10,(short)0);
        players = new ArrayList<>();
        players.add(player);
        players.add(player2);
        window = new Window("Test", 800,600,true);
        gamestate = new GameState(players,map,new ArrayList<>(),false);
    }

    @Before
    public void createBullet(){
        bullet = new Bullet((byte)0,(byte)0,(byte)0,(byte)0,DEFAULT_DMG,DEFAULT_RANGE,(byte)1,PISTOL_ID);
    }

    /** Test to see if bullet detects collsion with player
     */
    @Test
    public void testPlayerCollision(){
        player2.setCoord((short)10,(short)0);
        bullet.moveBullet(window,gamestate);

        assertEquals(player2.getHp(), (byte)90);
    }

    /** Test to see if player detects collision with player
     *  in other direction
     */
    @Test
    public void testPlayerCollision2(){
        player2.setCoord((short)10,(short)0);
        player.setCoord((byte)20,(byte)0);
        Bullet testBullet = new Bullet((byte) 20, (byte) 0, (byte) 20, (byte) 0,DEFAULT_DMG, DEFAULT_RANGE, (byte)-1,PISTOL_ID);

        testBullet.moveBullet(window,gamestate);

        assertEquals(player2.getHp(), (byte)80);
    }

    /** Test to see if bullet does not detect any collision
     */
    @Test
    public void testNoCollision(){
        player.setCoord((byte)0,(byte)0);
        player2.setCoord((byte)10,(byte)10);

        bullet = new Bullet((byte)0,(byte)0,(byte)0,(byte)0,DEFAULT_DMG,DEFAULT_RANGE,(byte)1,PISTOL_ID);

        bullet.moveBullet(window,gamestate);

        assertEquals(bullet.getxPos(), BULLET_SPEED);
    }

    /** Test to see if bullet collides with box
     */
    @Test
    public void testBoxCollision(){
        Box box = new Box();
        box.setCoord((short)5,(short)0);
        ArrayList<Box> test = new ArrayList<>();
        test.add(box);
        gamestate.setBoxes(test);
        bullet = new Bullet((byte)0,(byte)0,(byte)0,(byte)0,DEFAULT_DMG,DEFAULT_RANGE,(byte)1,PISTOL_ID);

        bullet.moveBullet(window,gamestate);

        assertEquals(box.getHp(), (byte) 10);

    }

    /** Test to see if bullet detects collision with environment
     */
    @Test
    public void testEnvironmentCollision(){
        bullet = new Bullet((byte)0,(byte)0,(byte)0,(byte)0,DEFAULT_DMG,DEFAULT_RANGE,(byte)-1,PISTOL_ID);

        bullet.moveBullet(window,gamestate);

        assertTrue(bullet.isDisplayed() == false);
    }

}
