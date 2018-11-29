package main.code.game.JUnit;

import javafx.util.Pair;
import main.code.game.GameState;
import main.code.utils.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

import static main.code.utils.GameConstants.*;
import static org.junit.Assert.*;

public class GameStateTests {

    private static Map map;
    private static ArrayList<Box> boxes;
    private static ArrayList<Pair<Byte, Short>> chosenWeapons;
    private static Player player;
    private static ArrayList<Player> players;
    private GameState gameState;

    @BeforeClass
    public static void getResources(){
        map = (new MapReader().readMap("src/main/resources/map/map1"));
        chosenWeapons = new ArrayList<>();
        chosenWeapons.add(new Pair<>(NO_WEAPON_ID,(short)0));
        boxes = map.setupBoxes(chosenWeapons);
        player = new Player((byte) 0);
        players = new ArrayList<>();
        players.add(player);
    }

    @Before
    public void createGameState(){
        gameState = new GameState(players, map, boxes,false);
    }

    /** Test to determine if the gameState identifies a roundOver state
     */
    @Test
    public void testRoundOver(){
        //Adds a dead AI into the list of players
        Player aiPlayer = new AIPlayer((byte) 1,map);
        aiPlayer.setHp((byte) 0);
        ArrayList<Player> newPlayers = gameState.getPlayers();
        newPlayers.add(aiPlayer);
        gameState.setPlayers(newPlayers);

        boolean res = gameState.isRoundOver();

        assertTrue(res == true);

        //Now checks if gamestate does not falsely identify roundOver
        aiPlayer.setHp((byte) 1);

        res = gameState.isRoundOver();

        assertTrue(res == false);
    }

    /** Test to determine if the gameState identifies a gameOver state
     */
    @Test
    public void testGameOver(){
        //Adds an alive AI into the list and sets player hp to 0
        Player aiPlayer = new AIPlayer((byte) 1,map);
        player.setHp((byte) 0);
        ArrayList<Player> newPlayers = gameState.getPlayers();
        newPlayers.add(aiPlayer);
        gameState.setPlayers(newPlayers);

        boolean res = gameState.isGameOver();

        assertTrue(res == true);

        //Now checks if gamestate does not falsely identify roundOver
        player.setHp((byte) 1);

        res = gameState.isGameOver();

        assertTrue(res == false);
    }

    /** Test to determine if the gameState identifies an onlineRoundOver state
     */
    @Test
    public void testOnlineRoundOver(){
        //Adds an alive player
        Player newPlayer = new Player((byte) 1);
        player.setHp((byte) 0);
        ArrayList<Player> newPlayers = gameState.getPlayers();
        newPlayers.add(newPlayer);
        gameState.setPlayers(newPlayers);

        boolean res = gameState.isOnlineRoundOver();

        assertTrue(res == true);

        //Now checks if gamestate does not falsely identify roundOver
        player.setHp((byte) 1);

        res = gameState.isOnlineRoundOver();

        assertTrue(res == false);
    }

    /** Test to determine if the gameState identifies an onlineGameOver state
     */
    @Test
    public void testOnlineGameOver(){
        //Sets player rounds won to 3
        Player newPlayer = new Player((byte) 1);
        player.setRoundsWon((byte) 3);
        ArrayList<Player> newPlayers = gameState.getPlayers();
        newPlayers.add(newPlayer);
        gameState.setPlayers(newPlayers);

        boolean res = gameState.isOnlineGameOver();

        assertTrue(res == true);

        //Now checks if gamestate does not falsely identify roundOver
        player.setRoundsWon((byte) 2);

        res = gameState.isOnlineGameOver();

        assertTrue(res == false);
    }
}
