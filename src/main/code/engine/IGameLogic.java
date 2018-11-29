package main.code.engine;

import main.code.renderer.Window;

/** Interface which outlines the structure of
 * the game logic
 *
 * @author Thomas Murphy
 */
public interface IGameLogic {
    /** Will have the code to initialise the game logic
     * @param window - The window which the game will be rendered within
     * @throws Exception - Exception thrown from renderer.init()
     */
    void init(Window window) throws Exception;

    /** Will handle player input
     * @param window - The window which the game will be rendered within
     */
    void input(Window window);

    /** Will handle updating the game based on player inputs
     * @param window - The window which the game will be rendered within
     */
    void update(Window window);

    /** Will handle the rendering of the changes onto the window
     * @param window - The window which the game will be rendered within
     */
    void render(Window window);

    /** Will return the state at which the game exits so the UI can be
     *  set up accordingly
     * @return string to indicate what state the game would have closed
     */
    String getGameExitMode();

    /** Will handle all the cleanup for when the game closes
     */
    void cleanup();
}
