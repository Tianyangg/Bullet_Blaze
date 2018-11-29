package main.code.utils;

import javafx.util.Pair;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;

import main.code.game.audio.AudioHandler;
import main.code.renderer.Window;

import static main.code.utils.GameConstants.*;

/** Class to represent the player. Contains information
 *  used to move the player, manage the players weapons
 *  inventory system and shooting weapons
 *
 * @author Thomas Murphy
 */
public class Player {
    private byte id;
    private short xPos;
    private short yPos;
    private byte hp;
    public String name="";
    private ArrayList<Pair< Byte,Short>> weapons;
    private LinkedList<Byte> weaponEntryTracker;
    private boolean isJumping = false;
    private boolean isFalling = false;
    private short initial_y;
    private int stateOfMovement;
    private Pair<Byte, Short> currentWeapon;
    private ArrayList<Bullet> bulletsFired;
    private short shootingDirection;
    private short energy;
    private boolean sprint;
    private int score;
    private byte character_id;
    private byte roundsWon;
    public boolean ready;
    private AudioHandler audioHandler;

    /** Default constructor for player
     */
    public Player(){
        default_init();
    }

    /** Constructor for player
     * @param id - given id of the player
     */
    public Player(byte id){
        default_init();
        this.id= id;
    }

    /** Initialises all defaults for the player
     */
    private void default_init(){
        id = 0;
        xPos = 0;
        yPos = 0;
        hp = 100;
        name="";
        character_id = 1;
        weapons = new ArrayList<>();
        weaponEntryTracker = new LinkedList<>();
        isJumping = false;
        isFalling = false;
        initial_y= -1;
        stateOfMovement = 0;
        for(int i = 0; i < 3; i++){
            this.weapons.add(new Pair<>(NO_WEAPON_ID, (short) 0));
        }
        this.weapons.set(0,new Pair<>(PISTOL_ID,DEFAULT_PISTOL_AMMO));
        currentWeapon = this.weapons.get(0);
        bulletsFired = new ArrayList<>();
        shootingDirection = 1;
        energy = 0;
        sprint = false;
        score = 0;
        roundsWon = 0;
        ready = false;
        audioHandler = new AudioHandler();
    }

    /** Constructor used by networking to rebuild player object after it has been sent via network
     * @param id - id of the player
     * @param xPos - x coordinate of the player
     * @param yPos - y coordinate of the player
     * @param hp - Health value of the player
     * @param name - Name of the player
     * @param weapons - List of weapons in the players inventory
     * @param bullets - List of bullets that the player has fired
     * @param trakedWeapons - List of identifiers to track entries in players inventory
     * @param jumpState - Boolean to indicate whether the player is jumping
     * @param fallState - Boolean to indicate whether the player is falling
     * @param initial_y - The initial y coordinate of player before jumping
     * @param currentWeapon - The players currently used weapon
     * @param currWeaponammo - The ammo of the currently used weapon
     * @param shootingDirection - The direction that the player is facing
     * @param sprint - The spring value of the player
     * @param score - The current score of the player
     * @param roundsWon - The number of rounds the player has won
     * @param ready - Boolean to indicate if the player is ready
     * @param stateOfMovement - The state of movement of the player - used for animation
     * @param charId - The id of the players character model
     */
    public Player(byte id, short xPos, short yPos, byte hp, String name, ArrayList<Pair<Byte, Short>> weapons, ArrayList<Bullet> bullets, LinkedList<Byte> trakedWeapons, boolean jumpState, boolean fallState, short initial_y, byte currentWeapon,short currWeaponammo, short shootingDirection, boolean sprint, int score, byte roundsWon, boolean ready,int stateOfMovement,byte charId){
        this.id =id;
        this.score = score;
        this.ready = ready;
        this.xPos = xPos;
        this.yPos = yPos;
        this.hp = hp;
        this.weapons = weapons;
        this.name = name;
        character_id = 1;
        this.weaponEntryTracker = trakedWeapons;
        this.isJumping=jumpState;
        this.isFalling=fallState;
        this.bulletsFired = bullets;
        this.currentWeapon = new Pair<>(currentWeapon,currWeaponammo);
        this.initial_y = initial_y;
        this.shootingDirection = shootingDirection;
        this.sprint = sprint;
        this.roundsWon = roundsWon;
        this.stateOfMovement = stateOfMovement;
        this.character_id = charId;
        audioHandler = new AudioHandler();
    }

    /** Initialises player health, bullets and movement
     *  before each round of the game starts
     */
    public void init(){
        hp = DEFAULT_MAX_HEALTH;
        bulletsFired = new ArrayList<>();
        initial_y = -1;

        //Sets the byte to identify the state of movement
        //For animation purposes in the renderer
        //0 = stationary
        //1 = jumping
        //2 = moving left
        //3 = jumping left
        //4 = right
        //5 = Jumping right
        //6 = crouching
        //8 = crouching left
        //10 = crouching right
        stateOfMovement = 0;
    }

    /** Returns the id of the players character model
     * @return id of the players character model
     */
    public byte get_charId(){
        return character_id;
    }

    /** Sets the id of the players character model
     * @param id - new id for the players character model
     */
    public void set_charId(byte id){
        character_id = id;
    }

    /** Sets the direction of where the player is facing
     * @param x - new direction of where the player is facing
     */
    public void setShootingDirection(short x){
        this.shootingDirection = x;
    }

    /** Returns the direction of where the player is facing
     * @return value to indicate which direction the player is facing
     */
    public short getShootingDirection() {
        return shootingDirection;
    }

    /** Sets the health value of the player
     * @param hp - updated health value of the player
     */
    public void setHp(byte hp){
        this.hp = hp;
    }

    /** Sets the coordinates of the player
     * @param xPos - updated x coordinate of the player
     * @param yPos - updated y coordinate of the player
     */
    public void setCoord(short xPos , short yPos ){
        this.xPos = xPos;
        this.yPos = yPos;
    }

    /** Sets the sprint state of the player
     * @param s - boolean to indicate the sprint state of the player
     */
    public void setSprint(boolean s){
        this.sprint = s;
    }

    /** Returns the sprint state of the player
     * @return boolean to indicate if the player is sprinting
     */
    public boolean isSprinting(){
        return this.sprint;
    }

    /** Adds a weapon to the players inventory
     * @param weapon - new weapon to be added
     */
    public void addWeapon(Pair<Byte,Short> weapon){
        //Total number of weapons player can carry is 5 (inc. default weapon)
        //If player has <3 weapons, simply add weapon.
        //Otherwise, first remove oldest weapon in inventory then add at that
        //position.
        //oldest weapon tracked by linked list of weapon entries.
        if(this.weapons.size() < 3) {
            this.weapons.add(weapon);
            this.weaponEntryTracker.add((byte) this.weapons.indexOf(weapon));
        }else{
            //Checks for duplicates, if duplicate weapons
            //found, new weapons ammo is just added to the current
            //weapon already in inventory
            for(Pair<Byte,Short> w : this.weapons){
                if(Objects.equals(w.getKey(), weapon.getKey())){
                    if(weaponEntryTracker.size() == 1){
                        weaponEntryTracker = new LinkedList<>();
                    }
                    else{
                        for(int tracker : weaponEntryTracker){
                            if(tracker == this.weapons.indexOf(w)){
                                weaponEntryTracker.remove((Integer) tracker);
                            }
                        }
                    }
                    Pair<Byte,Short> newWeapon = new Pair<>(weapon.getKey(), (short) (w.getValue()+weapon.getValue()));
                    if(this.currentWeapon == w){
                        this.setCurrentWeapon(newWeapon);
                    }
                    this.weapons.set(this.weapons.indexOf(w),newWeapon);
                    weaponEntryTracker.add((byte) this.weapons.indexOf(newWeapon));
                    return;
                }
            }
            //check for any no weapon entries - indicates
            //player dropped weapon.
            for(Pair<Byte,Short> w : this.weapons){
                if(w.getKey() == NO_WEAPON_ID){
                    this.weapons.set(this.weapons.indexOf(w),weapon);
                    this.weaponEntryTracker.add((byte) this.weapons.indexOf(weapon));
                    return;
                }
            }
            //If no null entries are found, remove oldest weapon
            int oldestWeapon = this.weaponEntryTracker.poll();
            byte oldestWeaponKey = this.weapons.get(oldestWeapon).getKey();
            this.weapons.set(oldestWeapon, weapon);
            this.weaponEntryTracker.add((byte) this.weapons.indexOf(weapon));
            if(oldestWeaponKey == currentWeapon.getKey()){
                setCurrentWeapon(weapon);
            }

        }
    }

    /** Removes weapon found at given position in the players inventory
     * @param weaponPosition - position in inventory of the weapon to be removed
     */
    public void removeWeapon(int weaponPosition){
        //Set the weapon entry to null and
        //remove the index from the entry tracker
        weapons.set(weaponPosition,new Pair<>(NO_WEAPON_ID, (short) 0));
        weaponEntryTracker.remove((Integer) weaponPosition);
        switch(weaponPosition) {
            case 1:
                System.out.println("Position is 1");
                setCurrentWeapon(getWeapons().get(0));
                break;
            case 2:
                System.out.println("Position is 2");
                if (getWeapons().get(1).getKey() != NO_WEAPON_ID) {
                    System.out.println("Weapon at 1");
                    setCurrentWeapon(getWeapons().get(1));
                } else {
                    System.out.println("Weapon at 2");
                    setCurrentWeapon(getWeapons().get(0));
                }
                break;
        }
    }

    /** Returns the health value of the player
     * @return The current health value of the player
     */
    public byte getHp(){
        return hp;
    }

    /** Returns the id of the player
     * @return The current id of the player
     */
    public byte getId(){
        return id;
    }

    /** Sets the id of the player
     * @param id - The new id of the player
     */
    public void setId(byte id){this.id = id;};

    /** Returns the x coordinate of the player
     * @return The current x coordinate of the player
     */
    public short getXPos(){
        return xPos;
    }

    /** Returns the y coordinate of the player
     * @return The current y coordinate of the player
     */
    public short getYPos(){
        return yPos;
    }

    /** Returns the list of weapons in the players inventory
     * @return current list of weapons in the players inventory
     */
    public ArrayList<Pair<Byte, Short>> getWeapons() {
        return weapons;
    }

    /** Returns the players the current weapon
     * @return Current weapon of the player
     */
    public Pair<Byte,Short> getCurrentWeapon(){
        return this.currentWeapon;
    }

    /** Sets the players current weapon
     * @param weapon - new current weapon of the player
     */
    public void setCurrentWeapon(Pair<Byte,Short> weapon){
        currentWeapon = weapon;
    }

    /** Returns the list of bullets fired by the player
     * @return List of bullets fired by the player
     */
    public ArrayList<Bullet> getBulletsFired(){
        return this.bulletsFired;
    }

    /** Shoots a bullet.
     * Adds a new bullet to the arraylist of bullets that the player has fired in game.
     * Sets coordinates based on players current position and the damage based on the players
     * currently selected weapon.
     */
    public void shootBullet( ){
        bulletsFired.add(new Bullet((short)(this.xPos + BOX_HEIGHT), (short)((this.getYPos() + (BOX_HEIGHT+15))), this.xPos, this.id, (byte) 0, (byte) 0, shootingDirection, currentWeapon.getKey()));

        byte weaponFired = currentWeapon.getKey();
        switch (weaponFired){
            case PISTOL_ID: bulletsFired.get(bulletsFired.size()-1).setDmg(DEFAULT_DMG);
                bulletsFired.get(bulletsFired.size()-1).setRange(DEFAULT_RANGE);
                audioHandler.updateSFXVolume(audioHandler.getSoundEffectVolume());
                audioHandler.playSoundEffect(audioHandler.pistolSound);
                break;
            case MACHINEGUN_ID: bulletsFired.get(bulletsFired.size()-1).setDmg(MACHINEGUN_DMG);
                bulletsFired.get(bulletsFired.size()-1).setRange(MACHINEGUN_RANGE);
                audioHandler.updateSFXVolume(audioHandler.getSoundEffectVolume());
                audioHandler.playSoundEffect(audioHandler.machineGunSound);
                break;
            case SHOTGUN_ID: bulletsFired.get(bulletsFired.size()-1).setDmg(SHOTGUN_DMG);
                bulletsFired.get(bulletsFired.size()-1).setRange(SHOTGUN_RANGE);
                audioHandler.updateSFXVolume(audioHandler.getSoundEffectVolume());
                audioHandler.playSoundEffect(audioHandler.shotgunSound);
                break;
            case SNIPER_ID: bulletsFired.get(bulletsFired.size()-1).setDmg(SNIPER_DMG);
                bulletsFired.get(bulletsFired.size()-1).setRange(SNIPER_RANGE);
                audioHandler.updateSFXVolume(audioHandler.getSoundEffectVolume());
                audioHandler.playSoundEffect(audioHandler.machineGunSound);
                break;
            case UZI_ID: bulletsFired.get(bulletsFired.size()-1).setDmg(UZI_DMG);
                bulletsFired.get(bulletsFired.size()-1).setRange(UZI_RANGE);
                audioHandler.updateSFXVolume(audioHandler.getSoundEffectVolume());
                audioHandler.playSoundEffect(audioHandler.machineGunSound);
                break;
            case AI_WEAPON_ID: bulletsFired.get(bulletsFired.size()-1).setDmg(AI_DMG);
                bulletsFired.get(bulletsFired.size()-1).setRange(DEFAULT_RANGE);
                System.out.println("Bullet sound " + audioHandler.getSoundEffectVolume());
                audioHandler.updateSFXVolume(audioHandler.getSoundEffectVolume());
                audioHandler.playSoundEffect(audioHandler.pistolSound);
                break;
            default: bulletsFired.get(bulletsFired.size()-1).setDmg(DEFAULT_DMG);
                bulletsFired.get(bulletsFired.size()-1).setRange(DEFAULT_RANGE);

        }
    }

    /** Returns whether the player is alive
     * @return Boolean to indicate if the player is alive
     */
    public boolean isAlive(){
        if(this.hp <= 0){
            return false;
        }
        else{
            return true;
        }
    }

    /** Returns the score the of the player
     * @return value of the players current score
     */
    public int getScore(){ return this.score; }

    /** Sets the score of the player
     * @param score - the updated score of the player
     */
    public void setScore(int score) { this.score = score; }

    /** Returns the number of rounds the player has won
     * @return value of the players current number of rounds won
     */
    public byte getRoundsWon(){ return this.roundsWon; }

    /** Sets the number of the rounds the player has won
     * @param roundsWon - updated number of rounds won
     */
    public void setRoundsWon(byte roundsWon){ this.roundsWon=roundsWon; }

    /** Returns the players state of movement
     * @return The current state of movement of the player
     */
    public int getStateOfMovement(){ return stateOfMovement; }

    /** Sets the state of movement of the player
     * @param movementState - Updated players state of movement
     */
    public void setStateOfMovement(int movementState){stateOfMovement = movementState; }

    /** Returns the jump state of the player
     * @return Boolean to indicate if the player is jumping
     */
    public boolean isJumping() { return isJumping; }

    /** Sets the jump state of the player
     * @param jumping - Boolean to indicate if the player is jumping
     */
    public void setJumping(boolean jumping) { isJumping = jumping; }

    /** Returns the fall state of the player
     * @return Boolean to indicate if the player is falling
     */
    public boolean isFalling() { return isFalling; }

    /** Sets the fall state of the player
     * @param falling - Boolean to indicate if the player is falling
     */
    public void setFalling(boolean falling) { isFalling = falling; }

    /** Returns the initial y coordinate of the player before jumping
     * @return Initial y coordinate of the player before jumping
     */
    public short getInitial_y() {
        return initial_y;
    }

    /** Sets the initial y coordinate of the player before jumping
     * @param initial_y - updated initial y coordinate of the player before jumping
     */
    public void setInitial_y(short initial_y) {
        this.initial_y = initial_y;
    }

    /** Increases the players energy value by the given value
     * @param e - Value to increase the players energy by
     */
    public void addEnergy(int e){
        if(e + energy < 0)
            energy = 0;
        else if(e + energy > MAX_ENERGY)
            energy = MAX_ENERGY;
        else
            energy += e;
    }

    /** Returns the name of the player
     * @return Name of the player
     */
    public String getName(){
        return this.name;
    }

    /** Returns the energy value of the player
     * @return Energy value of the player
     */
    public int getEnergy(){ return energy; }

    /** Returns the weapon entry tracker list
     * @return Weapon entry tracker list
     */
    public LinkedList<Byte> getWeaponEntry(){
        return this.weaponEntryTracker;
    }

    /** Returns the current state of the player
     * @return String consisting of values of all fields of the player
     */
    public String toString(){
        String s = "id: " + Byte.toString(id) + " ";
        s+= "name: "+name+ " ";
        s+= "hp: " + Byte.toString(hp) + " ";
        s+= "xPos: " + Short.toString(xPos)+" ";
        s+= "yPos: " + Short.toString(yPos)+"\n";
        for (Pair<Byte,Short> pair : weapons){
            s+= "weaponId: " + Byte.toString(pair.getKey()) + " " + "ammo: " + Short.toString(pair.getValue())+'\n';
        }
        s+= "Bullets fired:"+'\n';
        for(Bullet b : bulletsFired){
            s+= b.toString()+'\n';
        }
        s+= "Weapons tracked:"+'\n';
        for(Byte b : weaponEntryTracker){
            s+= Byte.toString(b)+'\n';
        }
        s+= "initial y: " +Short.toString(initial_y)+'\n';
        s+= "current weapon: " + Byte.toString(currentWeapon.getKey()) + " ammo: " + Short.toString(currentWeapon.getValue())+'\n';
        s+= "shooting direction: " +Short.toString(shootingDirection)+'\n';
        return s;
    }

    /** Sets the list of bullets the player has fired to a new list
     * @param bullets - Updated list of bullets fired
     */
    public void setBulletsFired(ArrayList<Bullet> bullets) {
        if(!bulletsFired.isEmpty()) {
            //System.out.println(bullets.get(0).getxPos() != bulletsFired.get(0).getxPos());
            this.bulletsFired = bullets;
        }
    }

    /** returns the ready status of the player
     * @return String to indicate whether the player is ready
     */
    public String getStatus() {
        return ready ? "ready" : "not ready";
    }
}
