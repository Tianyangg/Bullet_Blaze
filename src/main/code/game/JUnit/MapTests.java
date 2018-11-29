package main.code.game.JUnit;

import javafx.util.Pair;
import main.code.utils.*;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import static main.code.utils.GameConstants.BOX_HEIGHT;
import static main.code.utils.GameConstants.NO_WEAPON_ID;
import static org.junit.Assert.assertTrue;

public class MapTests {

    private static Map map;
    private static ArrayList<Pair<Byte,Short>>chosenWeapons;

    @Before
    public void createMap(){
        map = (new MapReader().readMap("src/main/resources/map/map1"));
        chosenWeapons = new ArrayList<>();
        chosenWeapons.add(new Pair<>(NO_WEAPON_ID,(short)0));
    }

    /** Test to see if all boxes are given coordinates found in map
     */
    @Test
    public void setupBoxes(){
        ArrayList<Box> boxes;
        boxes = map.setupBoxes(chosenWeapons);

        boolean posRes = true;
        Point[] points = map.getBoxSpawns();
        for(Box b : boxes){
            Point point = new Point(b.getXPos()/BOX_HEIGHT,b.getYPos()/BOX_HEIGHT);
            if(!(Arrays.asList(points).contains(point))){
                posRes = false;
            }
        }

        assertTrue(posRes == true);
    }

    /** Test to see if all players are given coordinates found in map
     */
    @Test
    public void setPlayerSpawns(){
        Player player = new Player((byte)0);

        ArrayList<AIPlayer> gameAI = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            gameAI.add((new AIPlayer((byte) (gameAI.size() + 1), map)));
        }

        map.setSpawns(player, gameAI);

        Point[] points = map.getSpawns();

        Point playerPos = new Point(player.getXPos()/BOX_HEIGHT,player.getYPos()/BOX_HEIGHT);

        assertTrue(Arrays.asList(points).contains(playerPos));

        boolean aiPosRes = true;
        for(AIPlayer ai : gameAI){
            Point aiPos = new Point(ai.getXPos()/BOX_HEIGHT,ai.getYPos()/BOX_HEIGHT);
            if(!(Arrays.asList(points).contains(aiPos))){
                aiPosRes = false;
            }
        }

        assertTrue(aiPosRes == true);
    }
}
