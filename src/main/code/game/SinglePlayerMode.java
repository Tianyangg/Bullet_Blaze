package main.code.game;

import javafx.util.Pair;
import java.util.ArrayList;
import main.code.engine.IGameLogic;
import main.code.game.audio.AudioHandler;
import main.code.renderer.Window;
import main.code.game.movement.KeyboardInput;
import main.code.game.movement.MouseInput;
import main.code.game.movement.Physics;
import main.code.renderer.Renderer;
import main.code.utils.AIPlayer;
import main.code.utils.Player;
import main.code.utils.Map;
import main.code.utils.Bullet;
import main.code.utils.Box;

import static main.code.utils.GameConstants.*;

/** Implements IGameLogic for Single Player game
 *
 * @author Thomas Murphy
 */
public class SinglePlayerMode implements IGameLogic {

    private final Renderer renderer;
    private Player player;
    private ArrayList<AIPlayer> gameAI;
    private GameState gameState;
    private Map map;
    private ArrayList<Pair<Byte, Short>> chosenWeapons;
    private byte difficulty;
    private boolean[] inputs;
    private static Physics makeMovement;
    private static KeyboardInput listenKeyboard;
    private static MouseInput listenMouse;
    private long lastFired;
    private long lastDropped;
    private String gameExitMode;
    private long endETA;
    private boolean isShooting = false;
    private boolean dropWeapon = false;
    private static AudioHandler audioHandler;
    private double initialSFXVolume;
    private boolean roundOver;
    private boolean gameOver;


    /** Constructor for creating a single player game
     * @param difficulty - The difficulty setting chosen by the user in the UI
     * @param chosenMap - The map chosen by the user in the UI
     * @param chosenWeapons - The list of weapons chosen by the user in the UI
     */
    public SinglePlayerMode(byte difficulty, Map chosenMap, ArrayList<Pair<Byte, Short>> chosenWeapons) {

        this.renderer = new Renderer();
        this.player = new Player((byte) 0);
        this.gameAI = new ArrayList<>();
        this.difficulty = difficulty;
        this.map = chosenMap;
        this.chosenWeapons = chosenWeapons;
    }

    /** Initialises the game logic by setting up all required components
     * @param window - The window which the game will be rendered within
     * @throws Exception - Exception thrown from renderer.init()
     */
    @Override
    public void init(Window window) throws Exception {

        this.endETA = 0;
        gameExitMode = "";
        makeMovement = new Physics(BOX_HEIGHT);

        listenKeyboard = new KeyboardInput();
        listenMouse = new MouseInput(window);
        listenMouse.init();

        audioHandler = new AudioHandler();
        initialSFXVolume = audioHandler.getSoundEffectVolume();

        roundOver = false;
        gameOver = true;

        player.init();
        //Sets array of booleans to identify movement of player
        inputs = new boolean[3];
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = false;
        }

        //Initialises AIPlayers. Number of AIPlayers created is dependent
        //On the difficulty setting the user has chosen
        this.gameAI = addAIs(gameAI, this.difficulty);
        for (AIPlayer ai : gameAI) {
            ai.init();
            ai.setCurrentWeapon(new Pair<>(AI_WEAPON_ID, DEFAULT_PISTOL_AMMO));
        }

        //Sets player and AI spawn points based on spawn points in map
        map.setSpawns(player,gameAI);

        //Puts all Player objects in a list to be passed to the gamestate.
        ArrayList<Player> allPlayers = new ArrayList<>();
        allPlayers.add(player);
        allPlayers.addAll(gameAI);

        //Setup all the boxes in the map.
        ArrayList<Box> boxes = map.setupBoxes(chosenWeapons);

        //Initialise gamestate.
        this.gameState = new GameState(allPlayers, map, boxes, false);

        //Initialise renderer
        renderer.init(window, gameState, player.getId());

    }

    /** Listens for player input - both keyboard and mouse
     * @param window - The window which the game will be rendered within
     */
    @Override
    public void input(Window window) {

        if(!roundOver) {
            listenKeyboard.movementInput(window, inputs, player, gameState);

            isShooting = listenKeyboard.isPlayerShooting(window, isShooting);

            dropWeapon = listenKeyboard.isPlayerDroppingWeapon(window, dropWeapon);

            if (!gameState.isAlertBox()) {
                gameState.setMenutInfo(listenMouse.checkMenuButtons());
            } else {
                gameState.setMenutInfo(listenMouse.checkAllertBox());
            }
        }
    }

    /** Determines which update method to be called based on current state of gamestate
     * @param window - The window which the game will be rendered within
     */
    @Override
    public void update(Window window) {

        if (this.gameState.isGameOver() || this.gameState.isRoundOver()) {
            this.gameState.setPaused(false);
        }

        if (!this.gameState.isPaused()) {
            if (this.gameState.isRoundOver()) {
                updateRoundOver();
            } else if (this.gameState.isGameOver()) {
                if(gameOver){
                    audioHandler.updateSFXVolume(audioHandler.getSoundEffectVolume());
                    audioHandler.gameOverSound.setCycleCount(1);
                    audioHandler.gameOverSound.play();
                    gameOver = false;
                }
                updateGameOver(window);
            } else {
                updateInput(window);
            }
        } else {
            updatePausedGame(window);
        }
    }

    /** Updates window based on the menu options the player selects
     *  when the game is paused.
     * @param window - The window which the game will be rendered within
     */
    private void updatePausedGame(Window window) {
        int mouse = this.gameState.getMenuInfo();

        if (this.gameState.isAlertBox()) {
            if (mouse / 10 == 1 && mouse % 10 == 2) {
                //No triggered
                this.gameState.setAlertBox(false);
                this.gameState.setPaused(false);
                window.setMenuPressed(false);
                window.setMenuReleased(true);
                gameExitMode = "";
            }

            if (mouse / 10 == 2 && mouse % 10 == 2) {
                //Yes triggered
                this.gameState.setAlertBox(false);
                this.gameState.setPaused(false);
                this.gameState.setIsOver(true);
                window.setMenuPressed(false);
                window.setMenuReleased(true);
            }

        } else {
            if (mouse / 10 == 1 && mouse % 10 == 2) {
                //Resume triggered
                this.gameState.setPaused(false);
                window.setMenuPressed(false);
                window.setMenuReleased(true);
            }

            if (mouse / 10 == 2 && mouse % 10 == 2) {
                //Music on/off
                if (gameState.isMusic()) {
                    audioHandler.backgroundMusic.stop();
                    audioHandler.setIsBackgroundOn(false);
                    gameState.setMusic(gameState.isMusic());
                } else {
                    audioHandler.backgroundMusic.setVolume(audioHandler.getBackgroundVolume());
                    audioHandler.backgroundMusic.play();
                    audioHandler.setIsBackgroundOn(true);
                    gameState.setMusic(gameState.isMusic());

                }
            }
            if (mouse / 10 == 3 && mouse % 10 == 2) {
                //Sound effects on/off
                if (gameState.isSoundFx()) {
                    audioHandler.updateSFXVolume(0.0);
                    audioHandler.setIsEffectOn(false);
                    gameState.setSoundFx(gameState.isSoundFx());
                } else {
                    audioHandler.updateSFXVolume(initialSFXVolume);
                    gameState.setSoundFx(gameState.isSoundFx());
                    audioHandler.setIsEffectOn(true);
                }
            }

            if (mouse / 10 == 4 && mouse % 10 == 2) {
                //Main Menu triggered
                this.gameState.setAlertBox(true);
                gameExitMode = "back";
            }

            if (mouse / 10 == 5 && mouse % 10 == 2) {
                //Exit game triggered
                this.gameState.setAlertBox(true);
                gameExitMode = "exit";
            }
        }
    }

    /** Updates window to display the game over overlay
     * @param window - The window which the game will be rendered within
     */
    private void updateGameOver(Window window) {
        //Go about closing the game.
        if (endETA == 0) {
            endETA = (System.currentTimeMillis() / 1000);
        } else {
            int time = (int) (System.currentTimeMillis() / 1000);
            if (time - endETA <= 6) {
                gameState.setEndETA((int) (time - endETA));
            } else {
                System.out.println("GameOver");
                System.out.println("Your final score was: " + player.getScore());

                window.close();
            }
        }
    }

    /** Updates window to display the round over overlay and sets up
     *  game for the next round
     */
    private void updateRoundOver() {
        roundOver = true;

        if (endETA == 0) {
            endETA = System.currentTimeMillis() / 1000;
        } else {
            int time = (int) (System.currentTimeMillis() / 1000);
            if (time - endETA <= 6) {
                gameState.setEndETA((int) (time - endETA));
            } else {

                System.out.println("Round over");

                //Caps the number of AI enemies in the single player game to 20
                //Made it a number divisible by 2 and 3
                ArrayList<Player> allNewRoundPlayers = new ArrayList<>();
                if (gameAI.size() <= (24 - difficulty)) {
                    this.gameAI = addAIs(gameAI, difficulty);
                }
                allNewRoundPlayers.add(player);
                allNewRoundPlayers.addAll(gameAI);

                //Updates gamestate with new AIplayers
                this.gameState.setPlayers(allNewRoundPlayers);

                //Reset the boxes and update the gamestate
                ArrayList<Box> newBoxes = map.setupBoxes(chosenWeapons);
                this.gameState.setNewRoundBoxes(newBoxes);

                //Re-initialises all AIPlayers
                for (Player p : gameAI) {
                    p.init();
                }

                //resets the spawns
                map.setSpawns(player,gameAI);

                try {
                    renderer.initPlayers(gameState.getPlayers(), gameState.isOnlineGame());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                endETA = 0;
                gameState.setEndETA(0);

                audioHandler.prepareYourself.setVolume(audioHandler.getSoundEffectVolume());
                audioHandler.playSoundEffect(audioHandler.prepareYourself);
                roundOver = false;
            }
        }
    }

    /** Updates Player and AIPlayers based on inputs received
     * @param window - The window which the game will be rendered within
     */
    private void updateInput(Window window) {

        //Updates player coordinates based on input
        player = makeMovement.takeAction(player, map.getGrid(), inputs);
        //Resets input
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = false;
        }

        //Drops weapon
        if (dropWeapon) {
            //Check to ensure you are not able to remove default weapon
            if (this.player.getWeapons().indexOf(this.player.getCurrentWeapon()) != 0) {
                if (!(System.currentTimeMillis() - lastDropped < 1000)) {
                    lastDropped = System.currentTimeMillis();
                    System.out.println("Dropping weapon");
                    int weaponPos = this.player.getWeapons().indexOf(this.player.getCurrentWeapon());
                    this.player.removeWeapon(weaponPos);
                }
            }
            dropWeapon = false;
        }
        //Shoots weapon
        if (isShooting) {
            //Checks if player has a weapon (i.e. currentWeapon is not null)
            if (this.player.getCurrentWeapon().getKey() != NO_WEAPON_ID) {
                //checks if the player can fire (i.e. enough time has passed so that the player cannot constantly shoot).
                byte currentWeaponID = this.player.getCurrentWeapon().getKey();
                double currentFiringInterval;
                switch (currentWeaponID) {
                    case PISTOL_ID:
                        currentFiringInterval = DEFAULT_FIRING_INTERVAL;
                        break;
                    case MACHINEGUN_ID:
                        currentFiringInterval = MACHINEGUN_FIRING_INTERVAL;
                        break;
                    case SHOTGUN_ID:
                        currentFiringInterval = SHOTGUN_FIRING_INTERVAL;
                        break;
                    case SNIPER_ID:
                        currentFiringInterval = SNIPER_FIRING_INTERVAL;
                        break;
                    case UZI_ID:
                        currentFiringInterval = UZI_FIRING_INTERVAL;
                        break;
                    default:
                        currentFiringInterval = DEFAULT_FIRING_INTERVAL;
                }

                if (!(System.currentTimeMillis() - lastFired < currentFiringInterval)) {
                    //Only fires bullet if the currently selected weapon has enough ammo
                    if (this.player.getCurrentWeapon().getValue() != 0) {
                        lastFired = System.currentTimeMillis();
                        System.out.println("Bullet Fired");
                        int weaponPosition = this.player.getWeapons().indexOf(this.player.getCurrentWeapon());
                        this.player.shootBullet();
                        Pair<Byte, Short> updatedWeapon = new Pair<>(player.getCurrentWeapon().getKey(), (short) (player.getCurrentWeapon().getValue() - 1));
                        this.player.setCurrentWeapon(updatedWeapon);
                        this.player.getWeapons().set(weaponPosition, updatedWeapon);
                        System.out.println("Remaining ammo: " + player.getCurrentWeapon().getValue());
                    }
                }
            }
            isShooting = false;
        }

        //Gives all AIPlayers updated player coordinates so that they
        //can pathfind.
        for (AIPlayer ai : gameAI) {
            ai.update(player.getXPos(), player.getYPos(), window.getHeight());
        }

        //Controls the movement of the bullets from both player and
        //AI.
        //Also handles collisions during bullet movement
        updateBullets(window);
    }

    /** Calls render to render all updates onto window
     * @param window - The window which the game will be rendered within
     */
    @Override
    public void render(Window window) {
        //Call renderer with window and gameState
        renderer.render(window, gameState);
    }

    /**Performs necessary cleanup for when the game has ended
     */
    @Override
    public void cleanup() {
        renderer.cleanup();
    }

    /** Returns the gameExitMode string - what the user wishes to do
     *  upon ending the game
     * @return - the gameExitMode string
     */
    @Override
    public String getGameExitMode() {
        return this.gameExitMode;
    }

    /** Adds more AIPlayers into the gameState
     * @param gameAI - The existing list of AIPlayers in the game
     * @param difficulty - The chosen difficulty setting - how many AIPlayers should be
     *                   added per round.
     * @return - The updated ArrayList of AIPlayers
     */
    private ArrayList<AIPlayer> addAIs(ArrayList<AIPlayer> gameAI, int difficulty) {
        for (int i = 0; i < difficulty; i++) {
            gameAI.add((new AIPlayer((byte) (gameAI.size() + 1), map)));
        }
        return gameAI;
    }

    /** Updates bullet movement for all bullets fired by player and AIPlayer
     * @param window - The window which the game will be rendered within
     */
    private void updateBullets(Window window) {

        for (Player p : this.gameState.getPlayers()) {
            ArrayList<Bullet> bullets = p.getBulletsFired();
            for (int i = 0; i < bullets.size(); i++) {
                if (bullets.get(i).isDisplayed()) {
                    bullets.get(i).moveBullet(window, gameState);
                } else {
                    bullets.remove(i);
                }
            }
        }
    }
}
