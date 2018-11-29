package main.code.game.movement;

import main.code.game.GameState;
import main.code.renderer.Window;
import main.code.utils.Player;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Implements the keyboard input
 *
 * @author Maria Antonia Badarau
 */

public class KeyboardInput {

    /**
     * Checks the user keyboard input
     * <p>
     * If the escape key is pressed the game is paused
     * <p>
     * If E or D are pressed the direction of the shooting is changed to right and left
     * <p>
     * If the up arrow key is pressed then the player is jumping
     * If the left arrow key is pressed then the player is moving left
     * If the right arrow is pressed then the player is moving right
     * <p>
     * If the 1 key is pressed then the player selects the 1st weapon
     * If the 2 key is pressed then the player selects the 2nd weapon
     * If the 3 key is pressed then the player selects the 3rd weapon
     *
     * @param window
     * @param movement  index 0 is for left
     *                  index 1 is for shift
     *                  index 2 is for right
     * @param player    receives the current player
     * @param gameState
     */

    public void movementInput(Window window, boolean[] movement, Player player, GameState gameState) {


        if (window.isMenuKeySinglePressed(GLFW_KEY_ESCAPE)) {
            if (!gameState.isPaused()) {
                gameState.setPaused(true);
            } else {
                gameState.setPaused(false);
            }
        }

        if (!gameState.isPaused()) {

            if (window.isKeyPressed(GLFW_KEY_E)) {
                player.setShootingDirection((short) 1);
            } else if (window.isKeyPressed(GLFW_KEY_Q)) {
                player.setShootingDirection((short) -1);
            }
            if (window.isKeyPressed(GLFW_KEY_UP) && player.getInitial_y() == player.getYPos()) {
                //inputs[1] = true;
                player.setJumping(true);
            }

            if (window.isKeyPressed(GLFW_KEY_LEFT)) {
                movement[0] = true;
            } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
                movement[2] = true;
            }

            if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
                movement[1] = true;
            }

            //keys to change current weapon
            if (window.isKeyPressed(GLFW_KEY_1)) {
                player.setCurrentWeapon(player.getWeapons().get(0));
            } else if (window.isKeyPressed(GLFW_KEY_2)) {
                player.setCurrentWeapon(player.getWeapons().get(1));
            } else if (window.isKeyPressed(GLFW_KEY_3)) {
                player.setCurrentWeapon(player.getWeapons().get(2));
            }
        }
    }

    /**
     * Determines if the player is shooting
     * Player is shooting when the space key is pressed
     *
     * @param window
     * @param isShooting
     * @return
     */
    public boolean isPlayerShooting(Window window, boolean isShooting) {

        if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            isShooting = true;
        }

        return isShooting;

    }

    /**
     * Determines if the player is dropping the current weapon
     * The player is dropping the current weapon if the D key is pressed
     *
     * @param window
     * @param dropWeapon
     * @return
     */

    public boolean isPlayerDroppingWeapon(Window window, boolean dropWeapon) {

        if (window.isKeyPressed(GLFW_KEY_D)) {
            dropWeapon = true;
        }

        return dropWeapon;
    }


}
