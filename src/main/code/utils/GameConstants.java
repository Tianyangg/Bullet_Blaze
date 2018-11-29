package main.code.utils;

/** Class containing all constants required
 *  by the system. Split into types of constants
 *
 * @author Thomas Murphy
 */
public abstract class GameConstants {

    //Name of the game
    public static final String NAME_OF_GAME = "Bullet Blaze";

    //Movement constants:
    public static final byte BULLET_SPEED = 20;
    public static final byte PLAYER_SPEED = 10;
    public static final byte PLAYER_JUMP_SPEED = 12;
    public static final int PLAYER_SPRINT_SPEED = 15;
    public static final int PLAYER_JUMP_HEIGHT = 2;
    public static final int MAX_ENERGY = 1000;
    public static final int MIN_ENERGY = 400;
    public static final int ENERGY_DRAIN = -15;

    //Player constants
    public static final int PLAYER_HEIGHT = 102;
    public static final int PLAYER_WIDTH = 43;
    public static final byte DEFAULT_MAX_HEALTH = 100;
    public static final int PLAYER_RANGE = 20;
    public static final int PLAYER_ORIGIN_Y = 15;
    public static final int PLAYER_ORIGIN_X = 40;

    //Server constants
    public static final short SERVER_TCP_PORT = 9182;
    public static final short SERVER_UDP_PORT = 9183;
    public static final int MAX_PACK_SIZE = 4000;
    public static final long MAX_IDLE_MILISEC = 10000;
    public static String serverIP = "35.205.148.96";

    //Default weapon stats
    public static final short DEFAULT_PISTOL_AMMO = -1;
    public static final double DEFAULT_RANGE = 15;
    public static final byte DEFAULT_DMG = 10;
    public static final long DEFAULT_FIRING_INTERVAL = 1000;

    //Machine gun weapon stats
    public static final byte MACHINEGUN_DMG = 15;
    public static final byte MACHINEGUN_RANGE = 20;
    public static final short MACHINEGUN_AMMO = 30;
    public static final long MACHINEGUN_FIRING_INTERVAL = 500;

    //Shotgun weapon stats
    public static final byte SHOTGUN_DMG = 20;
    public static final byte SHOTGUN_RANGE = 10;
    public static final short SHOTGUN_AMMO = 15;
    public static final long SHOTGUN_FIRING_INTERVAL = 1500;

    //Sniper weapon stats
    public static final byte SNIPER_DMG = 30;
    public static final byte SNIPER_RANGE = 30;
    public static final short SNIPER_AMM0 = 10;
    public static final long SNIPER_FIRING_INTERVAL = 2000;

    //Uzi weapon stats
    public static final byte UZI_DMG = 12;
    public static final byte UZI_RANGE = 15;
    public static final short UZI_AMMO = 20;
    public static final long UZI_FIRING_INTERVAL = 500;

    //AI weapon stats
    public static final byte AI_DMG = 5;

    //Weapon Id values
    public static final byte PISTOL_ID = 1;
    public static final byte MACHINEGUN_ID = 2;
    public static final byte SHOTGUN_ID = 3;
    public static final byte SNIPER_ID = 4;
    public static final byte UZI_ID = 5;
    public static final byte NO_WEAPON_ID = -1;
    public static final byte AI_WEAPON_ID = 6;

    //Character Id values
    public static final byte SIMPLE_PLAYER = 1;
    public static final byte ZOMBIE = 5;

    //Game Engine constants
    public static final int TARGET_FPS = 75;
    public static final int TARGET_UPS = 60;

    //Renderer constants
    public static final int WINDOWS_HEIGHT = 1200;
    public static final int BOX_HEIGHT = 60;
    public static final int PLATFORM_SPRITE_WIDTH = 8;
    public static final float PLATFORM_SPRITE_X_POS = (1.0f)/(PLATFORM_SPRITE_WIDTH * 1.0f);
    public static final float MIN_PLAYER_X = 0.2f;
    public static final float MIN_PLAYER_Y = 0.4f;
    public static final float MAX_PLAYER_X = 1.8f;
    public static final float MAX_PLAYER_Y = 1.6f;
    public static final String TEXTURE_PATH = "src/main/resources/textures/";
    public static final int GRASS = 0;
}
