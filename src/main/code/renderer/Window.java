package main.code.renderer;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * A class containing the window and its information
 * @author Matei Vicovan-Hantascu
 */
public class Window {

    private final String title;
    private boolean fullScreen;
    private int width;
    private int height;
    private boolean vSync;
    private boolean resized;
    private long windowHandle;
    private double xpos;
    private double ypos;
    private boolean menuPressed;
    private boolean menuReleased;


    /**
     * Create the window with the given resolution
     * @param title
     *          the title
     * @param width
     *          the width
     * @param height
     *          the height
     * @param vSync
     *          use vSync to render
     */
    public Window(String title, int width, int height, boolean vSync) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.vSync = vSync;
        this.resized = false;
        this.fullScreen = false;
        menuPressed = false;
        menuReleased = true;
    }


    /**
     * Create the window using full screen mode with the resolution of the screen
     * @param title
     *      the title
     * @param vSync
     *      use vSync to render
     */
    public Window(String title, boolean vSync){
        this.title = title;
        this.vSync = vSync;
        this.resized = false;
        this.fullScreen = true;
        this.height = 1;

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        this.width = gd.getDisplayMode().getWidth();
        this.height = gd.getDisplayMode().getHeight();
        menuPressed = false;
        menuReleased = true;
    }


    /**
     * Initialise the window
     */
    public void init() {

        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }


        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        // Create the window
        if(fullScreen)
            windowHandle = glfwCreateWindow(width, height, title, glfwGetPrimaryMonitor(), NULL);
        else
            windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);

        if (windowHandle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup resize callback
        glfwSetFramebufferSizeCallback(windowHandle, (window, width, height) -> {
            this.width = width;
            this.height = height;
            this.setResized(true);
        });

        /*
        glfwSetCursorPosCallback(windowHandle, posCallBack = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {


            }
        });
        */

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(
                windowHandle,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(windowHandle);

        if (isvSync()) {
            // Enable v-sync
            glfwSwapInterval(1);
        }

        // Make the window visible
        glfwShowWindow(windowHandle);
        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }


    /**
     * Set the clear color
     * @param r
     *          red level color
     * @param g
     *          green level color
     * @param b
     *          blue level color
     * @param alpha
     *          alpha level
     */
    public void setClearColor(float r, float g, float b, float alpha) {
        glClearColor(r, g, b, alpha);
    }


    /**
     * Checks if a key is pressed
     * @param keyCode
     *          the key to be checked
     * @return
     *          boolean
     */
    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(windowHandle, keyCode) == GLFW_PRESS;
    }


    /**
     * Checks a single press for the menu button
     * @param keyCode
     *      the key to be checked
     * @return
     *      boolean
     */
    public boolean isMenuKeySinglePressed(int keyCode){

        if(glfwGetKey(windowHandle, keyCode) == GLFW_PRESS && menuReleased){
            menuPressed = true;
            menuReleased = false;
        }else if(glfwGetKey(windowHandle, keyCode) == GLFW_PRESS && !menuReleased){
            menuPressed = false;
            menuReleased = false;
        }else if(glfwGetKey(windowHandle, keyCode) == GLFW_RELEASE){
            menuReleased = true;
        }

        return menuPressed;
    }


    /**
     * @param value
     *      the given value
     */
    public void setMenuPressed(boolean value){
        this.menuPressed = value;
    }


    /**
     * @param value
     *      the given value
     */
    public void setMenuReleased(boolean value){
        this.menuReleased = value;
    }


    /**
     * Checks if the window should close
     * @return
     *      a boolean saying whether or not the window should be closed
     */
    public boolean windowShouldClose() {
        return glfwWindowShouldClose(windowHandle);
    }


    /**
     * A method to destroy the window
     * used when a game ends
     */
    public void destroy(){
        glfwDestroyWindow(windowHandle);
    }


    /**
     * @return
     *      the title of the window
     */
    public String getTitle() {
        return title;
    }


    /**
     * @return
     *      the width of the window
     */
    public int getWidth() {
        return width;
    }


    /**
     * @return
     *      the height of th window
     */
    public int getHeight() {
        return height;
    }


    /**
     * @return
     *      a boolean checking whether or not the window is resized
     */
    public boolean isResized() {
        return resized;
    }

    public void setResized(boolean resized) {
        this.resized = resized;
    }

    /**
     * @return
     *      vSync used to sync the window
     */
    public boolean isvSync() {
        return vSync;
    }

    public void setvSync(boolean vSync) {
        this.vSync = vSync;
    }

    /**
     * Updates the window
     */
    public void update() {
        glfwSwapBuffers(windowHandle);
        glfwPollEvents();
    }

    /**
     * Closes the window
     */
    public void close(){
        glfwSetWindowShouldClose(windowHandle,true);
    }

    /**
     * @return
     *      the window id
     */
    public long getWindowHandle() {
        return windowHandle;
    }
}
