package main.code.game.movement;

import main.code.renderer.Window;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Implements the keyboard input
 *
 * @author Maria Antonia Badarau
 */

public class MouseInput {

    public Window window;

    private Vector2d currentPos;
    private float mouseX;
    private float mouseY;

    private float boxRatio;

    private boolean inWindow = false;

    private boolean leftButtonPressed = false;
    private boolean released = true;
    private boolean pressed = false;


    public MouseInput(Window window) {
        currentPos = new Vector2d(0, 0);
        this.window = window;
        boxRatio = (window.getHeight() * 1.0f) / (window.getWidth() * 1.0f);
    }

    /**
     * Sets the callbacks for the cursor
     * Sends a callback when the cursor is moved
     * Sends a callback when the cursor enters the window
     * Sends a callback when the left click of the mouse is pressed
     */
    public void init() {

        // sends a callback when the mouse is moved
        glfwSetCursorPosCallback(window.getWindowHandle(), (window, xpos, ypos) -> {
            currentPos.x = xpos;
            currentPos.y = ypos;
        });
        // sends a callback when the mouse enters the window
        glfwSetCursorEnterCallback(window.getWindowHandle(), (windowHandle, entered) -> {
            inWindow = entered;
        });
        // sends a callback when one action occurs
        glfwSetMouseButtonCallback(window.getWindowHandle(), (windowHandle, button, action, mode) -> {
            // checks is the left mouse button is pressed
            leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
        });
    }

    /**
     * Checks if the left button of the mouse is pressed or released
     *
     * @return pressed
     */
    public boolean isLeftButtonPressed() {
        // checks if the left button of the mouse is pressed

        if (leftButtonPressed && released) {
            released = false;
            pressed = true;
        } else if (leftButtonPressed && !released) {
            pressed = false;
            released = false;
        } else {
            released = true;
            pressed = false;
        }

        return pressed;
    }


    /**
     * Checks the position of the cursor when it comes to the in-game menu buttons
     * Computes the values of the cursor's coordinates
     * Checks if the cursor is over the button and if the left button of the button if not pressed
     * Checks if the cursor is not over the button
     * <p>
     * To determine which of the above is happening, it saves the codes into int checkButton
     * codes for buttons:
     * 1 for 1st button
     * 2 for 2nd button
     * 3 for 3rd button
     * 4 for 4th button
     * 5 for 5th button
     * codes for actions:
     * 1 for cursor being over the button but left mouse button not pressed
     * 2 for cursor being over the button and left mouse button pressed
     * <p>
     * eg: if checkButton = 11 => cursor being over the button but left mouse button not pressed
     * if checkButton = 52 =>  for cursor being over the button and left mouse button pressed
     *
     * @return checkButton
     */

    public int checkMenuButtons() {

        // computing the coordinates of the cursor

        mouseX = (float) ((currentPos.x * 1.0f - window.getWidth() / 2f) / (window.getWidth() / 2f));
        mouseY = (float) ((currentPos.y * 1.0f - window.getHeight() / 2f) / (window.getHeight() / 2f));

        float checkX = ((2f - 0.4f * boxRatio) / 2) - 1;
        int checkButton = 0;

        if (checkX < mouseX && mouseX < (checkX + 0.4f * boxRatio) && mouseY > -0.24f && mouseY < -0.157f) {
            checkButton = 11;
        }

        if (checkX < mouseX && mouseX < (checkX + 0.4f * boxRatio) && mouseY > -0.24f && mouseY < -0.157f && isLeftButtonPressed()) {
            checkButton = 12;
        }


        // coordinates of the 2nd button

        if (checkX < mouseX && mouseX < (checkX + 0.4f * boxRatio) && mouseY > -0.098f && mouseY < -0.0098f) {
            checkButton = 21;
        }

        if (checkX < mouseX && mouseX < (checkX + 0.4f * boxRatio) && mouseY > -0.098f && mouseY < -0.0098f && isLeftButtonPressed()) {
            checkButton = 22;
        }


        // coordinates of the 3rd button

        if (checkX < mouseX && mouseX < (checkX + 0.4f * boxRatio) && mouseY > 0.05f && mouseY < 0.144f) {
            checkButton = 31;
        }

        if (checkX < mouseX && mouseX < (checkX + 0.4f * boxRatio) && mouseY > 0.05f && mouseY < 0.144f && isLeftButtonPressed()) {
            checkButton = 32;
        }

        // coordinates of the 1st button

        if (checkX < mouseX && mouseX < (checkX + 0.4f * boxRatio) && mouseY > 0.2f && mouseY < 0.29f) {
            checkButton = 41;
        }

        if (checkX < mouseX && mouseX < (checkX + 0.4f * boxRatio) && mouseY > 0.2f && mouseY < 0.29f && isLeftButtonPressed()) {
            checkButton = 42;
        }

        if (checkX < mouseX && mouseX < (checkX + 0.4f * boxRatio) && mouseY > 0.35f && mouseY < 0.44f) {
            checkButton = 51;
        }

        if (checkX < mouseX && mouseX < (checkX + 0.4f * boxRatio) && mouseY > 0.35f && mouseY < 0.44f && isLeftButtonPressed()) {
            checkButton = 52;
        }

        return checkButton;
    }

    /**
     * Checks the position of the cursor when it comes to the alertBox buttons
     * Computes the values of the cursor's coordinates
     * Checks if the cursor is over the button and if the left button of the button if not pressed
     * Checks if the cursor is not over the button
     * <p>
     * To determine which of the above is happening, it saves the codes into int checkButton
     * codes for buttons:
     * 1 for 1st button
     * 2 for 2nd button
     * <p>
     * codes for actions:
     * 1 for cursor being over the button but left mouse button not pressed
     * 2 for cursor being over the button and left mouse button pressed
     * <p>
     *
     * @return checkButton
     */

    public int checkAllertBox() {
        mouseX = (float) ((currentPos.x * 1.0f - window.getWidth() / 2f) / (window.getWidth() / 2f));
        mouseY = (float) ((currentPos.y * 1.0f - window.getHeight() / 2f) / (window.getHeight() / 2f));

        float checkLeftButtonOnLeft = -0.475f * boxRatio * 0.8f;
        float checkLeftButtonOnRight = -0.075f * boxRatio * 0.8f;

        float checkRightButtonOnLeft = 0.075f * boxRatio * 0.8f;
        float checkRightButtonOnRight = 0.475f * boxRatio * 0.8f;

        int checkButton = 0;

        if (checkLeftButtonOnLeft < mouseX && mouseX < checkLeftButtonOnRight && mouseY > -0.079f && mouseY < -0.003f) {
            checkButton = 11;
        }

        if (checkLeftButtonOnLeft < mouseX && mouseX < checkLeftButtonOnRight && mouseY > -0.079f && mouseY < -0.003f && isLeftButtonPressed()) {
            checkButton = 12;
        }

        if (checkRightButtonOnLeft < mouseX && mouseX < checkRightButtonOnRight && mouseY > -0.079f && mouseY < -0.003f) {
            checkButton = 21;
        }

        if (checkRightButtonOnLeft < mouseX && mouseX < checkRightButtonOnRight && mouseY > -0.079f && mouseY < -0.003f && isLeftButtonPressed()) {
            checkButton = 22;
        }

        return checkButton;
    }

}