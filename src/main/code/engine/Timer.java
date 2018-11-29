package main.code.engine;

/**
 * A class that helps the engine to synchronise the frame rate
 *
 * @author Matei Vicovan-Hantascu
 */
public class Timer {
    private float lastLoopTime;

    /**
     * Initialise the class with the time when the game started
     */
    public void init(){
        lastLoopTime = getTime();
    }

    /**
     * @return
     *          the current time when the method has been called
     */
    public float getTime(){
        return System.nanoTime()/ 1000_000_000.0f;
    }


    /**
     * @return
     *          the difference between the current time and the last time this method has been called
     */
    public float getElapsedTime(){
        float time = getTime();
        float elapsedTime = (time - lastLoopTime);
        lastLoopTime = time;
        return elapsedTime;
    }

    /**
     * @return
     *          the last loop time
     */
    public double getLastLoopTime(){
        return lastLoopTime;
    }
}
