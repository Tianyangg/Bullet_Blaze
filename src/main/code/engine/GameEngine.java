package main.code.engine;

import javafx.scene.media.AudioClip;
import main.code.UI.BulletMenumain;
import main.code.game.audio.AudioHandler;
import main.code.renderer.Window;
import main.code.game.movement.MouseInput;
import main.code.utils.Bullet;

import static main.code.utils.GameConstants.TARGET_FPS;
import static main.code.utils.GameConstants.TARGET_UPS;

/**
 * A class that implements a generic engine for any given game logic
 *
 * @author Thomas Murphy, Matei Vicovan-Hantascu, Antonia Badarau
 */
public class GameEngine implements Runnable {

    private final Thread gameLoopThread;
    private final Window window;
    private final IGameLogic gameLogic;
    private final Timer timer;
    private final MouseInput mouse;
    private boolean running;
    private static AudioHandler audioHandler = new AudioHandler();
    private BulletMenumain bulletMenumain = new BulletMenumain();


    /**
     * Constructs an engine for a given game logic
     * @param windowTitle
     *              title of the window on which the game is rendered
     * @param width
     *              width of the title
     * @param height
     *              height of the title
     * @param vSync
     *              boolean stating if the engine is suppose to use a synchronization or not
     * @param gameLogic
     *              the given game logic
     * @throws Exception
     */
    public GameEngine(String windowTitle, int width, int height, boolean vSync, IGameLogic gameLogic) throws Exception {
        gameLoopThread = new Thread(this,  "Game_LOOP_THREAD");
        this.window = new Window(windowTitle, vSync);
        this.gameLogic = gameLogic;
        this.timer = new Timer();
        this.mouse = new MouseInput(window);
        if (bulletMenumain.getIsPlaying()) {
            startMusic();
        }
    }


    /**
     * A method that allows the game to play music from outside of the engine
     * (for example from the UI)
     */
    public void startMusic() {
        audioHandler.backgroundMusic.setVolume(audioHandler.getBackgroundVolume());
        audioHandler.playBackgroundMusicInGame(audioHandler.backgroundMusic);
        System.out.println("IS background on? " + audioHandler.isBackgroundOn());
    }


    /**
     * This method starts the thread for the game engine
     * If the game is played on Mac we just run it in the main thread
     * The method will wait for the end of the game so it can retrieve the message of return:
     *                  - back - the application should return to the menu (JavaFX)
     *                  - exit - the application should close
     * @return
     *              the message representing the type of exit
     */
    public String begin() {

        String osName = System.getProperty("os.name");

        if (osName.contains("Mac")) {
            gameLoopThread.run();
        } else {
            gameLoopThread.start();
            try {
                gameLoopThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println(this.gameLogic.getGameExitMode());
        return this.gameLogic.getGameExitMode();
    }

    /**
     * The run method that is called to start the thread
     */
    @Override
    public void run() {
        try {
            init();
            gameLoop();
        } catch (InterruptedException e) {
            running = false;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            gameLogic.cleanup();
        }

    }

    /**
     * @return
     *          if the game is on
     */
    public boolean isGameRunning() {
        return this.gameLoopThread.isAlive();
    }


    /**
     * Initializes the window, timer and game logic of the constructed engine object
     * @throws Exception
     */
    protected void init() throws Exception {
        window.init();
        timer.init();
        gameLogic.init(window);
    }


    /**
     * The game loop that takes care of input, updates and rendering
     */
    protected void gameLoop() {
        float elapsedTime;
        float accumulator = 0f;
        float interval = 1f / TARGET_UPS;

        running = true;

        while (running && !window.windowShouldClose()) {
            elapsedTime = timer.getElapsedTime();
            accumulator += elapsedTime;

            while (accumulator >= interval) {
                input();
                update(interval);
                accumulator -= interval;
            }
            render();

            if (!window.isvSync()) {
                sync();
            }
        }

        window.destroy();
    }

    /**
     * A method used to keep the frame rate at a current speed
     */
    private void sync() {

        float loopSlot = 1f / TARGET_FPS;
        double endTime = timer.getLastLoopTime() + loopSlot;

        while (timer.getTime() < endTime) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ie) {
            }
        }
    }

    /**
     * A method that retrieves the input for the given game logic
     */
    protected void input() {
        gameLogic.input(window);
    }

    /**
     * A method that updates the game logic
     * @param interval
     *              the amount of time passed since the last update
     */
    protected void update(double interval) {
        gameLogic.update(window);
    }

    /**
     * A method that calls the renderer of the game logic and updates the window
     */
    protected void render() {
        gameLogic.render(window);
        window.update();
    }

    /**
     * A method that allows to stop the thread from outside
     */
    public void stop() {
        gameLoopThread.interrupt();
    }
}
