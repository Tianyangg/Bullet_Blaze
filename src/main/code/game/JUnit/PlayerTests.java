package main.code.game.JUnit;

import javafx.util.Pair;
import main.code.utils.Box;
import main.code.utils.Map;
import main.code.utils.MapReader;
import main.code.utils.Player;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

import static main.code.utils.GameConstants.*;
import static org.junit.Assert.*;

public class PlayerTests {

    private static Map map;
    private static ArrayList<Pair<Byte,Short>> chosenWeapons;
    private static ArrayList<Box> boxes;
    private Player player;

    @BeforeClass
    public static void getResources(){
        map = (new MapReader().readMap("src/main/resources/map/map1"));
        chosenWeapons = new ArrayList<>();
        chosenWeapons.add(new Pair<>(NO_WEAPON_ID,(short)0));
        boxes = map.setupBoxes(chosenWeapons);
    }

    @Before
    public void createPlayer(){
        player = new Player((byte) 0);
        player.init();
    }

    /** Test to see if shootBullet Method creates the correct Bullet object
     *  based on players currentWeapon
     */
    @Test
    public void testShootBullet(){
        //First shoot with default pistol
        player.shootBullet();

        assertTrue((player.getBulletsFired().get(0).getDmg() == DEFAULT_DMG) && (player.getBulletsFired().get(0).getRange() == DEFAULT_RANGE));

        //Shoot bullet with different weapon
        player.setCurrentWeapon(new Pair<>(SNIPER_ID,SNIPER_AMM0));

        player.shootBullet();

        assertTrue((player.getBulletsFired().get(1).getDmg() == SNIPER_DMG) && (player.getBulletsFired().get(1).getRange() == SNIPER_RANGE));
    }

    /** Tests to see if addWeapon method adds weapons correctly
     */
    @Test
    public void testAddWeapon(){
        Pair weapon = new Pair<>(SNIPER_ID,SNIPER_AMM0);
        player.addWeapon(weapon);

        assertEquals(player.getWeapons().get(1), weapon);

        Pair nextWeapon = new Pair<>(SHOTGUN_ID,SHOTGUN_AMMO);
        player.addWeapon(nextWeapon);

        assertEquals(player.getWeapons().get(2), nextWeapon);
    }

    /** Test to see if addWeapon updates existing weapon if same
     *  weapon is added
     */
    @Test
    public void testUpdateExistingWeapons(){
        Pair weapon = new Pair<>(SNIPER_ID,SNIPER_AMM0);
        player.addWeapon(weapon);
        Pair nextWeapon = new Pair<>(SNIPER_ID,SNIPER_AMM0);
        player.addWeapon(nextWeapon);

        assertTrue((player.getWeapons().get(1).getValue()) == (2*SNIPER_AMM0));
    }

    /** Test to see if oldest weapon is removed if another weapon is added
     */
    @Test
    public void testRemoveOldestWeapon(){
        Pair weapon = new Pair<>(SNIPER_ID,SNIPER_AMM0);
        player.addWeapon(weapon);
        Pair nextWeapon = new Pair<>(SHOTGUN_ID,SHOTGUN_AMMO);
        player.addWeapon(nextWeapon);

        Pair testWeapon = new Pair<>(MACHINEGUN_ID,MACHINEGUN_AMMO);
        player.addWeapon(testWeapon);

        assertEquals(player.getWeapons().get(1),testWeapon);
    }

    /** Test to see if dropWeapon method removes the correct weapon
     *  from inventory
     */
    @Test
    public void testRemoveWeapon(){
        Pair weapon = new Pair<>(SNIPER_ID,SNIPER_AMM0);
        player.addWeapon(weapon);
        Pair nextWeapon = new Pair<>(SHOTGUN_ID,SHOTGUN_AMMO);
        player.addWeapon(nextWeapon);

        player.removeWeapon(1);

        Pair res = new Pair<Byte,Short>(NO_WEAPON_ID, (short) 0);
        assertEquals(player.getWeapons().get(1),res);
    }

    /** Test to see if drop weapon correctly sets currentWeapon if
     * current weapon is dropped
     */
    @Test
    public void testRemoveCurrentWeapon(){
        Pair weapon = new Pair<>(SNIPER_ID,SNIPER_AMM0);
        player.addWeapon(weapon);
        Pair nextWeapon = new Pair<>(SHOTGUN_ID,SHOTGUN_AMMO);
        player.addWeapon(nextWeapon);
        player.setCurrentWeapon(nextWeapon);

        player.removeWeapon(2);

        assertEquals(player.getCurrentWeapon(), weapon);
    }

    /** Test to see if position 0 and 2 are filled in inventory and
     * current weapon is 2, when dropped, current weapon is then 0
     */
    @Test
    public void testRemoveCurrentWeapon2(){
        Pair weapon = new Pair<>(SNIPER_ID,SNIPER_AMM0);
        player.addWeapon(weapon);
        Pair nextWeapon = new Pair<>(SHOTGUN_ID,SHOTGUN_AMMO);
        player.addWeapon(nextWeapon);
        player.setCurrentWeapon(nextWeapon);

        player.removeWeapon(1);
        player.removeWeapon(2);

        assertEquals(player.getCurrentWeapon(), player.getWeapons().get(0));
    }
}
