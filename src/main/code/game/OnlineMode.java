package main.code.game;

import javafx.util.Pair;
import java.util.ArrayList;
import main.code.Networking.Client;
import main.code.engine.IGameLogic;
import main.code.game.audio.AudioHandler;
import main.code.game.movement.KeyboardInput;
import main.code.game.movement.MouseInput;
import main.code.game.movement.Physics;
import main.code.renderer.Renderer;
import main.code.renderer.Window;
import main.code.utils.Player;
import main.code.utils.Map;
import main.code.utils.Box;
import main.code.utils.Bullet;

import static main.code.utils.GameConstants.*;

/**Implements IGameLogic for online game
 *
 * @author Thomas Murphy
 */
public class OnlineMode implements IGameLogic {

    private final Renderer renderer;
    private final Client client;
    private Player player;
    private Map map;
    private boolean[] inputs;
    private static Physics makeMovement;
    private static KeyboardInput listenKeyboard;
    private static MouseInput listenMouse;
    private static String gameExitMode;
    private long lastFired;
    private long lastDropped;
    private boolean isShooting = false;
    private boolean dropWeapon = false;
    private long endETA;
    private boolean gameOver;
    private AudioHandler audioHandler = new AudioHandler();
    private double initialSFXVolume = audioHandler.getSoundEffectVolume();
    private boolean isWin = false;
    private int updated = 0;

    /** Constructor for creating an online game
     * @param client - Client object used to send player data over network
     * @param map - map chosen by user who created the online game
     */
    public OnlineMode(Client client, Map map) {
        renderer = new Renderer();
        this.client = client;
        client.startSending();
        player = client.getPlayer();
        this.map = map;
    }

    /** Initialises the game by setting up the required components
     * @param window - The window which the game will be rendered within
     * @throws Exception - Exception thrown from renderer.init()
     */
    @Override
    public void init(Window window) throws Exception {
        gameExitMode = "";
        makeMovement = new Physics(BOX_HEIGHT);

        listenMouse = new MouseInput(window);
        listenMouse.init();
        listenKeyboard = new KeyboardInput();

        player.init();
        //Sets array of booleans to identify movement of player
        inputs = new boolean[3];
        for(int i = 0; i < inputs.length; i++){
            inputs[i] = false;
        }

        renderer.init(window, client.gameState, player.getId());

    }

    /** Listens for player input from both keyboard and mouse
     * @param window - The window which the game will be rendered within
     */
    @Override
    public void input(Window window) {

        listenKeyboard.movementInput(window, inputs, player, client.gameState);

        isShooting = listenKeyboard.isPlayerShooting(window, isShooting);

        dropWeapon = listenKeyboard.isPlayerDroppingWeapon(window, dropWeapon);

        if (!client.gameState.isAlertBox()) {
            client.gameState.setMenutInfo(listenMouse.checkMenuButtons());
        } else {
            client.gameState.setMenutInfo(listenMouse.checkAllertBox());
        }
    }

    /** Determines which update method to be called based on the state
     *  of the servers gameState
     * @param window - The window which the game will be rendered within
     */
    @Override
    public void update(Window window) {

        if (this.client.roundover && !isWin) {
            updateOnlineRoundOver();
        } else if (this.client.gameover) {
            if(gameOver){
                audioHandler.updateSFXVolume(audioHandler.getSoundEffectVolume());
                audioHandler.gameOverSound.setCycleCount(1);
                audioHandler.gameOverSound.play();
                gameOver = false;
            }
            updateOnlineGameOver(window);
        } else {
            updateInput(window);
        }
    }

    /** Updates clients player and boxes based on input received
     * @param window - The window which the game will be rendered within
     */
    private void updateInput(Window window){
        //Handles mouse input when the pause menu is displayed
        //This will not pause the game, other players can still move and
        //shoot this clients player
        int mouse = this.client.gameState.getMenuInfo();
        if (this.client.gameState.isAlertBox()) {
            if (mouse / 10 == 1 && mouse % 10 == 2) {
                //No triggered
                this.client.gameState.setAlertBox(false);
                this.client.gameState.setPaused(false);
                window.setMenuPressed(false);
                window.setMenuReleased(true);
                gameExitMode = "";
            }

            if (mouse / 10 == 2 && mouse % 10 == 2) {
                //Yes triggered
                this.client.gameState.setAlertBox(false);
                this.client.gameState.setPaused(false);
                this.client.gameState.setIsOver(true);
                window.setMenuPressed(false);
                window.setMenuReleased(true);
                window.close();
            }
        } else {
            if (mouse / 10 == 1 && mouse % 10 == 2) {
                //Resume triggered
                this.client.gameState.setPaused(false);
                window.setMenuPressed(false);
                window.setMenuReleased(true);
            }

            if (mouse / 10 == 2 && mouse % 10 == 2) {
                //Music on/off
                //Music on/off
                if (client.gameState.isMusic()) {
                    audioHandler.backgroundMusic.stop();
                    audioHandler.setIsBackgroundOn(false);
                    client.gameState.setMusic(client.gameState.isMusic());
                } else {
                    audioHandler.backgroundMusic.setVolume(audioHandler.getBackgroundVolume());
                    audioHandler.backgroundMusic.play();
                    audioHandler.setIsBackgroundOn(true);
                    client.gameState.setMusic(client.gameState.isMusic());
                }
            }
            if (mouse / 10 == 3 && mouse % 10 == 2) {
                //Sound effects on/off
                if (client.gameState.isSoundFx()) {
                    audioHandler.updateSFXVolume(0.0);
                    audioHandler.setIsEffectOn(false);
                    client.gameState.setSoundFx(client.gameState.isSoundFx());
                } else {
                    audioHandler.updateSFXVolume(initialSFXVolume);
                    client.gameState.setSoundFx(client.gameState.isSoundFx());
                    audioHandler.setIsEffectOn(true);
                }
            }

            if (mouse / 10 == 4 && mouse % 10 == 2) {
                //Main Menu triggered
                this.client.gameState.setAlertBox(true);
                gameExitMode = "back";
            }

            if (mouse / 10 == 5 && mouse % 10 == 2) {
                this.client.gameState.setAlertBox(true);
                gameExitMode = "exit";
            }
        }

        player = makeMovement.takeAction(player, map.getGrid(), inputs);
        //Resets input
        for(int i = 0; i < inputs.length; i++){
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
        if(isShooting){
            //Checks if player has a weapon (i.e. currentWeapon is not null)
            if(this.player.getCurrentWeapon().getKey() != NO_WEAPON_ID) {
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
                        Pair<Byte,Short> updatedWeapon = new Pair<>(player.getCurrentWeapon().getKey(), (short) (player.getCurrentWeapon().getValue() - 1));
                        this.player.setCurrentWeapon(updatedWeapon);
                        this.player.getWeapons().set(weaponPosition,updatedWeapon);
                        System.out.println("Remaining ammo: " + player.getCurrentWeapon().getValue());
                    }
                }
            }
            isShooting = false;
        }

        //Determines if there has been change to the boxes on this client. If so,
        //The updated arraylist is sent to the server to update all clients
        ArrayList<Box> before = new ArrayList<>(client.gameState.getBoxes());
        updateBullets(window);
        ArrayList<Box> after = new ArrayList<>(client.gameState.getBoxes());
        for(int i = 0; i < before.size();i++)
            if(before.get(i).isDestroyed() != after.get(i).isDestroyed()){
                client.sendTcpBoxes();
                break;
            }
    }

    /**Updates window to display the round over overlay and set up online game for the
     * next round.
     */
    private void updateOnlineRoundOver() {
        if(updated==0) {
            for (Player p : client.gameState.getPlayers()) {
                if (p.isAlive()) {
                    System.out.println("Winner of the round was player - " + p.getId());
                    p.setRoundsWon((byte) (p.getRoundsWon() + 1));
                    System.out.println("Player " + p.getId() + " round tally = " + p.getRoundsWon());
                    if(p.getRoundsWon() >= 3){
                        isWin = true;
                        return;
                    }
                    updated++;
                    break;
                }
            }
        }
        if (endETA == 0) {
            endETA = System.currentTimeMillis() / 1000;
        } else {
            int time = (int) (System.currentTimeMillis() / 1000);
            if (time - endETA <= 6) {
                this.client.gameState.setEndETA((int) (time - endETA));
            } else {
                System.out.println("Round over");
                for (Player p : this.client.gameState.getPlayers()) {
                    p.init();
                }

                //resets the spawns
                map.setOnlineSpawns(player);
                endETA = 0;
                this.client.gameState.setEndETA(0);
                updated = 0;
            }
        }
        this.client.roundover = false;
    }

    /** Updates window to display game over overlay and go about closing the game
     *  and connection to the game
     * @param window - The window which the game will be rendered within
     */
    private void updateOnlineGameOver(Window window){
        //Go about closing the game.
        this.client.gameover = false;
        gameExitMode = "back";
        client.stopSending();
        client.disconnectFromGame();
        /*
        if(endETA == 0){
            endETA = (System.currentTimeMillis() / 1000);
        }else{
            int time = (int) (System.currentTimeMillis() / 1000);
            if(time - endETA <= 11) {
                this.client.gameState.setEndETA((int) (time - endETA));
            }else{
                System.out.println("GameOver");

                for(Player p : this.client.gameState.getPlayers()){
                    if(p.getRoundsWon() >= 3){
                        System.out.println("The winner of the game is player - " + p.getId());
                        break;
                    }
                }

                window.close();
            }
        }*/
        System.out.println("game over");
        for(Player p : this.client.gameState.getPlayers()){
            if(p.getRoundsWon() >= 3){
                System.out.println("The winner of the game is player - " + p.getId());
                break;
            }
        }
        window.close();
    }

    /** Calls renderer to render all updated onto the window
     * @param window - The window which the game will be rendered within
     */
    @Override
    public void render(Window window) {
        //Call renderer with window and gameState
        renderer.render(window, client.gameState);
    }

    /** Performs necessary cleanup for when the game has ended
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

    /** Updates bullet movement for all bullets fired by players in game
     * @param window - The window which the game will be rendered within
     */
    private void updateBullets(Window window){

        for(Player p : this.client.gameState.getPlayers()){
            ArrayList<Bullet> bullets = p.getBulletsFired();

            for(int i = 0; i < bullets.size(); i++){

                if(bullets.get(i).isDisplayed()){
                    bullets.get(i).moveBullet(window, client.gameState);
                }else{
                    bullets.remove(i);
                }
            }
            p.setBulletsFired(bullets);
        }

    }
}
