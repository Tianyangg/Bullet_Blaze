package main.code.game;

import javafx.util.Pair;
import java.util.ArrayList;
import main.code.game.audio.AudioHandler;
import main.code.utils.Player;
import main.code.utils.Bullet;
import main.code.utils.Map;
import main.code.utils.Box;
import main.code.utils.AIPlayer;


/**This class will capture the state of the game at a given point in time.
 * It will capture all players and AI players to determine whether the game
 * has ended (player dies with AI players still remaining) or the round has
 * ended (player still remaining with all AI players dead). It will also capture
 * the state of an online game
 *
 * @author Thomas Murphy
 */
public class GameState {

    private Map map;
    private boolean isPaused;
    private boolean isOver;
    public ArrayList<Player> players;
    private ArrayList<Box> boxes;
    private boolean isOnlineGame;
    private int menuInfo;
    private boolean alertBox;
    private boolean music;
    private boolean soundFx;
    private int endETA;
    private AudioHandler audioHandler;


    /** Constructor to initialise the gameState
     * @param players - List of all players in the game (both Player and AIPlayer)
     * @param map - The map used in the game
     * @param boxes - List of the boxes to be placed on the map
     * @param isOnlineGame - Indicates if this is an online gameState - for rendering purposes
     */
    public GameState(ArrayList<Player> players, Map map, ArrayList<Box> boxes, boolean isOnlineGame){
        this.players = players;
        this.map = map;
        this.boxes = boxes;
        this.isPaused = false;
        this.isOnlineGame = isOnlineGame;
        menuInfo = 0;
        alertBox = false;
        music = true;
        soundFx = true;
        this.endETA = -1;
        audioHandler = new AudioHandler();
    }

    /**Default constructor for gameState
     */
    public GameState() {
        this.players = new ArrayList<>();
        this.map = new Map();
        this.boxes = new ArrayList<>();
        this.isPaused = false;
        menuInfo = 0;
        alertBox = false;
        music = true;
        soundFx = true;
    }


    /** Returns all players in the gameState
     * @return List of all players in gameState
     */
    public ArrayList<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    /** Returns the map
     * @return map stored in gameState
     */
    public Map getMap() {
        return map;
    }

    /** Sets the players in the gameState to a new list of players
     * @param players - The new list of players
     */
    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    /** Sets the map in the gameState to a new map
     * @param map - The new map
     */
    public void setMap(Map map) {
        this.map = map;
    }

    /** Returns the list of boxes in the gameState
     * @return The list of boxes
     */
    public ArrayList<Box> getBoxes() { return boxes; }

    /** Sets the list of boxes in the gameState by comparing the
     *  existing list with the updated one. Creates a new arrayList
     *  which has all the new changes
     * @param boxes - the updated list of boxes
     */
    public void setBoxes(ArrayList<Box> boxes) {
        ArrayList<Box> aux = new ArrayList<>();
        if(this.boxes.size() != 0) {
            for (int i = 0; i < this.boxes.size(); i++) {
                Box toCompare = this.boxes.get(i);
                for (int j = 0; j < boxes.size(); j++) {
                    Box with = boxes.get(j);
                    if (with.getXPos() == toCompare.getXPos() && with.getYPos() == toCompare.getYPos()) {
                        if (!with.isDestroyed() && !toCompare.isDestroyed()) {
                            aux.add(with);
                        } else if (with.isDestroyed()) {
                            aux.add(with);
                        } else if (toCompare.isDestroyed()) {
                            aux.add(toCompare);
                        }
                    } else aux.add(with);

                }
            }
            this.boxes = new ArrayList<>(aux);
        }else this.boxes = boxes;
    }

    /** Sets the list of boxes to the updated list
     * @param boxes - the updated list of boxes
     */
    public void setNewRoundBoxes(ArrayList<Box> boxes){
        this.boxes = boxes;
    }

    /** Determines if the single player game is in a game over state
     * @return boolean identifying if gameState is in a game over state
     */
    public boolean isGameOver(){
        if(this.isOver){
            return true;
        }else {
            boolean isGameOver = false;
            Pair<Integer, Integer> results = calcGameStatus();
            if ((results.getKey() == 0) && (results.getValue() >= 1)) {
                isGameOver = true;
            }
            return isGameOver;
        }
    }

    /** Sets the isOver field to state if the game is over
     * @param value - boolean to identify if game is over
     */
    public void setIsOver(boolean value){
        this.isOver = value;
    }

    /** Determines if the single player game is in a round over state
     * @return boolean identifying if gameState is in a round over state
     */
    public boolean isRoundOver(){
        boolean isRoundOver = false;
        Pair<Integer,Integer> results = calcGameStatus();
        if((results.getKey() >=1) && (results.getValue() <= 0)){
            isRoundOver = true;
        }
        return isRoundOver;
    }


    /** Determines if the online game is in a round over state
     * @return boolean identifying if gameState is in a round over state
     */
    public boolean isOnlineRoundOver(){
        boolean isRoundOver = false;
        Pair<Integer,Integer> results = calcGameStatus();
        if((results.getKey() <= 1) ){
            isRoundOver = true;
        }
        return isRoundOver;
    }

    /** Determines if the online game is in a game over state
     * @return boolean identifying if gameState is in a game over state
     */
    public boolean isOnlineGameOver(){
        boolean isGameOver = false;
        for(Player p : players){
            if(p.getRoundsWon() >= 3){
                isGameOver = true;
                break;
            }
        }
        return isGameOver;
    }

    public int getMenuInfo(){
        return this.menuInfo;
    }

    public void setMenutInfo(int value){
        this.menuInfo = value;
    }


    /** Calculates the status of the gameState based on the
     *  number of players and AIPlayers that are currently alive
     * @return A pair containing the number of alive players and the
     *          number of alive AIPlayers
     */
    private Pair<Integer, Integer> calcGameStatus(){
        int alivePlayers = 0;
        int aliveAIs = 0;

        ArrayList<Player> players = this.players;
        for(Player player : players){
            if(player.getHp() > 0){
                if(player instanceof AIPlayer){
                    aliveAIs++;
                }
                else{
                    alivePlayers++;
                }
            }
        }

        return new Pair<>(alivePlayers,aliveAIs);
    }

    /** Adds the given player to the list of players
     * @param p - the new player to be added
     */
    public void addPlayer(Player p) {
        players.add(p);
    }

    /** Removes the given player from the list of players
     * @param p - the player to be removed
     */
    public void removePlayer(Player p) {
        players.remove(p);
    }

    /** Returns the pause state of the gameState
     * @return boolean to indicate if the gameState is paused
     */
    public boolean isPaused() {
        return isPaused;
    }

    /** Sets the pause status of the gameState
     * @param paused - boolean to indicate if the gameState is paused
     */
    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    /** Removes the player at the given position from the list of players
     * @param i - The position of the player to be removed.
     */
    public void removePlayer(int i) {
        players.remove(i);
    }

    /** Sets of the ready status of the player with the given id
     * @param id - The id of the player
     * @param value - boolean to determine if they are ready
     */
    public void setReady(byte id,boolean value) {
        for(Player p : players){
            if (p.getId() == id){
                p.ready = value;
            }
        }
    }

    /** Gets the player at the given position from the list of players
     * @param i - the position of the given player to be retrieved
     * @return the player found at the given position
     */
    public Player getPlayer(int i) {
        return players.get(i);
    }

    /** Returns the online state of the gameState
     * @return boolean to indicate if the gameState represents an online game
     */
    public boolean isOnlineGame() { return isOnlineGame; }

    /** Sets the online state of the gameState
     * @param onlineGame - boolean to indicate if the gameState represents an online game
     */
    public void setOnlineGame(boolean onlineGame) { isOnlineGame = onlineGame; }

    /** Returns whether an alert box should be displayed
     * @return boolean to indicate if an alert box should be displayed
     */
    public boolean isAlertBox() {
        return alertBox;
    }

    /** Sets whether an alert box should be displayed
     * @param alertBox - boolean to indicate if an alert box should be displayed
     */
    public void setAlertBox(boolean alertBox) {
        this.alertBox = alertBox;
    }

    /** Returns whether background music is being played
     * @return boolean to indicate if background music is being played
     */
    public boolean isMusic() {
        if (audioHandler.isBackgroundOn()){
            music = true;
        } else music = false;
        return music;
    }

    /** Sets whether background music is being played
     * @param music - boolean to indicate if background music is being played
     */
    public void setMusic(boolean music) {
        this.music = music;
    }

    /** Returns whether sound effect are being played
     * @return boolean to indicate if sound effects are being played
     */
    public boolean isSoundFx() {
        if (audioHandler.isEffectOn()){
            soundFx = true;
        } else soundFx = false;
        return soundFx;
    }

    /** Sets whether sound effects are being played
     * @param soundFx - boolean to indicate whether sound effects are being played
     */
    public void setSoundFx(boolean soundFx) {
        this.soundFx = soundFx;
    }

    /** Returns the current value of endETA
     * @return - endETA
     */
    public int getEndETA() {
        return endETA;
    }

    /** Sets the value of endETA to a new value
     * @param endETA - new value for endETA
     */
    public void setEndETA(int endETA) {
        this.endETA = 6 - endETA;
    }

    /** Sets the list of bullets of the player with the given id
     * @param playerID - id of player to have the bullets list changed
     * @param bullets - updated bullet list
     */
    public void setPlayerBullets(byte playerID, ArrayList<Bullet> bullets) {
        Player p;
        for(int i = 0; i < players.size();i++){
            p = players.get(i);
            if(p.getId() == playerID){
                p.setBulletsFired(bullets);
                players.set(i,p);
            }
        }
    }

    /** Sets the grid map identifier to indicate that
     *  a box has been placed there
     */
    public void setMapBoxes() {
        map.setBoxes(boxes);
    }
}
