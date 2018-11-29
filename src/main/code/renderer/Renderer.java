package main.code.renderer;

import javafx.util.Pair;
import main.code.game.GameState;
import main.code.renderer.graphics.*;
import main.code.utils.Box;
import main.code.utils.Bullet;
import main.code.utils.Player;
import org.joml.Matrix4f;


import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

import static main.code.utils.GameConstants.*;
import static org.lwjgl.opengl.GL11.*;


/**
 * Implements the renderer
 *
 * @author Matei Vicovan-Hantascu
 */
public class Renderer {

    //Used to reduce the space between letters
    private static final float CHAR_SCALLER = 2.2f;


    private ShaderProgram shaderProgram;
    private ShaderProgram textureShaderProgram;
    private ShaderProgram hudShaderProgram;
    private final Transformation transformation;

    private int clientId = -1;

    private HashMap<String, TextureMesh> textureMap;
    private HashMap<Integer, PlayerAnimation> animationMap;
    private TextItem hudItem;
    private Texture background;
    private float boxRatio;
    private float boxSize = 0;
    private float windowsRatio;

    public Renderer() {
        transformation = new Transformation();
        textureMap = new HashMap<>();
        animationMap = new HashMap<>();
    }

    /**
     * Initialise the renderer
     * @param window
     *          window used to be rendered
     * @param gameState
     *          given game state with all the information
     * @param id
     *          id of the current client
     * @throws Exception
     */
    public void init(Window window, GameState gameState, int id) throws Exception {
        this.boxRatio = (window.getHeight() * 1.0f) /  (window.getWidth() * 1.0f);
        this.windowsRatio = (window.getHeight() * 1.0f) / (WINDOWS_HEIGHT * 1.0f);
        this.boxSize = windowsRatio * BOX_HEIGHT;
        this.clientId = id;
        PlayerAnimation.init(boxRatio);
        ArrayList<Player> players = gameState.getPlayers();

        initShaders();
        initPlayers(players, gameState.isOnlineGame());
        initGuns();
        initBackground();
        initBullet();
        initHuds();
        initMenu();
        initUI();
        window.setClearColor(255.0f, 255.0f, 255.0f, 0.0f);
    }


    /**
     * Initialising shaders
     * @throws Exception
     */
    //Initialises the shaders
    private void initShaders() throws Exception{
        //Create shader
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(UtilLoader.loadResource("/vertex.vs"));
        shaderProgram.createFragmentShader(UtilLoader.loadResource("/fragment.fs"));
        shaderProgram.link();
        shaderProgram.createUniform("modelMatrix");


        //Create texture shader
        textureShaderProgram = new ShaderProgram();
        textureShaderProgram.createVertexShader(UtilLoader.loadResource("/texture_vertex.vs"));
        textureShaderProgram.createFragmentShader(UtilLoader.loadResource("/texture_frag.fs"));
        textureShaderProgram.link();
        textureShaderProgram.createUniform("textureMatrix");
        textureShaderProgram.createUniform("texture_sampler");


        //Create hud shader
        hudShaderProgram = new ShaderProgram();
        hudShaderProgram.createVertexShader(UtilLoader.loadResource("/hud_vertex.vs"));
        hudShaderProgram.createFragmentShader(UtilLoader.loadResource("/hud_frag.fs"));
        hudShaderProgram.link();
        hudShaderProgram.createUniform("hudMatrix");
        hudShaderProgram.createUniform("texture_sampler");
        hudShaderProgram.createUniform("colour");

    }

    /**
     * initialising the meshes of the given players
     *
     * @param players
     *          given players
     *
     * @throws Exception
     */
    public void initPlayers(ArrayList<Player> players, boolean isOnline) throws  Exception{
        animationMap.clear();
        for(Player player: players) {
            if(!isOnline) {
                if (player.getId() == this.clientId) {
                    animationMap.put((int) player.getId(), new PlayerAnimation((int) SIMPLE_PLAYER, boxRatio));
                } else {
                    animationMap.put((int) player.getId(), new PlayerAnimation((int) ZOMBIE, boxRatio));
                }
            }else{
                animationMap.put((int)player.getId(), new PlayerAnimation(player.get_charId(), boxRatio));
            }
        }
    }

    /**
     * initialising the meshes of the guns
     */
    private void initGuns() {
        Texture texture = new Texture(TEXTURE_PATH + "guns.png");;
        TextureMesh mesh;

        float i = 0.125f;
        float j = 0.5f;

        mesh = new TextureMesh(0.17f * boxRatio, 0.17f, i * 0f, j * 0f, i, j, texture);
        textureMap.put("pistolRight", mesh);

        mesh = new TextureMesh(0.17f * boxRatio, 0.17f, i * 1f, j * 0f, i, j, texture);
        textureMap.put("pistolLeft", mesh);

        mesh = new TextureMesh(0.17f * boxRatio, 0.17f, i * 2f, j * 0f, i, j, texture);
        textureMap.put("uziRight", mesh);

        mesh = new TextureMesh(0.17f * boxRatio, 0.17f, i * 3f, j * 0f, i, j, texture);
        textureMap.put("uziLeft", mesh);

        mesh = new TextureMesh(0.2f * boxRatio, 0.2f, i * 4f, j * 0f, i, j, texture);
        textureMap.put("shotgunRight", mesh);

        mesh = new TextureMesh(0.2f * boxRatio, 0.2f, i * 5f, j * 0f, i, j, texture);
        textureMap.put("shotgunLeft", mesh);

        mesh = new TextureMesh(0.2f * boxRatio, 0.2f, i * 6f, j * 0f, i, j, texture);
        textureMap.put("rifleRight", mesh);

        mesh = new TextureMesh(0.2f * boxRatio, 0.2f, i * 7f, j * 0f, i, j, texture);
        textureMap.put("rifleLeft", mesh);

        mesh = new TextureMesh(0.23f * boxRatio, 0.23f, i * 0f, j * 1f, i, j, texture);
        textureMap.put("sniperRight", mesh);

        mesh = new TextureMesh(0.23f * boxRatio, 0.23f, i * 1f, j * 1f, i, j, texture);
        textureMap.put("sniperLeft", mesh);
    }


    /**
     * initialising the mesh for text
     * @throws Exception
     */
    private void initHuds() throws Exception{
        Texture texture = new Texture(TEXTURE_PATH + "chars.png");
        hudItem = new TextItem("", texture, boxRatio, CHAR_SCALLER);
    }

    /**
     * initialising the mesh for the UI
     * @throws Exception
     */
    private void initUI() throws Exception{
        Texture texture;
        TextureMesh mesh;

        texture = new Texture(TEXTURE_PATH + "ui/enemyUI.png");
        mesh = new TextureMesh(0.6f * boxRatio, 0.15f, 0f, 0f, 1f, 1f, texture);
        textureMap.put("enemyUI", mesh);

        texture = new Texture(TEXTURE_PATH + "ui/characterUI.png");
        mesh = new TextureMesh(0.8f * boxRatio, 0.2f, 0f, 0f, 1f, 1f, texture);
        textureMap.put("characterUI", mesh);

        texture = new Texture(TEXTURE_PATH + "ui/faces.png");
        mesh = new TextureMesh(0.15f * boxRatio, 0.15f, 0f, 0f, 0.125f, 1f, texture);
        textureMap.put("facePlayer1", mesh);

        mesh = new TextureMesh(0.15f * boxRatio, 0.15f, 0.125f, 0f, 0.125f, 1f, texture);
        textureMap.put("facePlayer2", mesh);

        mesh = new TextureMesh(0.15f * boxRatio, 0.15f, 0.25f, 0f, 0.125f, 1f, texture);
        textureMap.put("facePlayer3", mesh);

        mesh = new TextureMesh(0.15f * boxRatio, 0.15f, 0.375f, 0f, 0.125f, 1f, texture);
        textureMap.put("facePlayer4", mesh);

        mesh = new TextureMesh(0.15f * boxRatio, 0.15f, 0.5f, 0f, 0.125f, 1f, texture);
        textureMap.put("zombieFace", mesh);

        texture = new Texture(TEXTURE_PATH + "ui/uielements.png");
        mesh = new TextureMesh(0.04f * boxRatio, 0.04f, 0f, 0f, 0.5f, 0.5f, texture);
        textureMap.put("target", mesh);

        mesh = new TextureMesh(0.04f * boxRatio, 0.04f, 0.5f, 0f, 0.5f, 0.5f, texture);
        textureMap.put("lightning", mesh);

        mesh = new TextureMesh(0.04f * boxRatio, 0.04f, 0f, 0.5f, 0.5f, 0.5f, texture);
        textureMap.put("heart", mesh);

        mesh = new TextureMesh(0.04f * boxRatio, 0.04f, 0.5f, 0.5f, 0.5f, 0.5f, texture);
        textureMap.put("clock", mesh);

        texture = new Texture(TEXTURE_PATH + "ui/xDead.png");
        mesh = new TextureMesh(0.17f * boxRatio, 0.17f, 0f, 0f, 1f, 1f, texture);
        textureMap.put("xDead", mesh);

        texture = new Texture(TEXTURE_PATH + "ui/weaponbar.png");
        mesh = new TextureMesh(0.2f * boxRatio, 0.2f, 0f, 0f, 0.5f, 1f, texture );
        textureMap.put("weaponBar1", mesh);

        mesh = new TextureMesh(0.2f * boxRatio, 0.2f, 0.5f, 0f, 0.5f, 1f, texture );
        textureMap.put("weaponBar2", mesh);

        texture = new Texture(TEXTURE_PATH + "ui/uiguns.png");
        mesh = new TextureMesh(0.15f * boxRatio, 0.15f, 0f, 0f, 0.125f, 1f, texture);
        textureMap.put("pistolUi", mesh);

        mesh = new TextureMesh(0.15f * boxRatio, 0.15f, 0.125f, 0f, 0.125f, 1f, texture);
        textureMap.put("uziUi", mesh);

        mesh = new TextureMesh(0.15f * boxRatio, 0.15f, 0.25f, 0f, 0.125f, 1f, texture);
        textureMap.put("shotgunUi", mesh);

        mesh = new TextureMesh(0.15f * boxRatio, 0.15f, 0.375f, 0f, 0.125f, 1f, texture);
        textureMap.put("rifleUi", mesh);

        mesh = new TextureMesh(0.15f * boxRatio, 0.15f, 0.5f, 0f, 0.125f, 1f, texture);
        textureMap.put("sniperUi", mesh);
    }

    /**
     * initialise the mesh for the menu
     */
    private void initMenu() {
        Texture texture;
        TextureMesh mesh;

        float buttonWidth = 0.4f * boxRatio;
        float buttonHeight = 0.1f;

        texture = new Texture(TEXTURE_PATH + "menu/SimpleButton.png");
        mesh = new TextureMesh(buttonWidth, buttonHeight, 0f, 0f, 1f, 1f, texture);
        textureMap.put("simpleButton", mesh);

        mesh = new TextureMesh(0.1f * boxRatio, 0.1f, 0f, 0f, 1f, 1f, texture);
        textureMap.put("alertBox", mesh);

        texture = new Texture(TEXTURE_PATH + "menu/ButtonMouseOver.png");
        mesh = new TextureMesh(buttonWidth, buttonHeight, 0f, 0f, 1f, 1f, texture);
        textureMap.put("buttonMouseOver", mesh);

        texture = new Texture(TEXTURE_PATH + "menu/ClickedButton.png");
        mesh = new TextureMesh(buttonWidth, buttonHeight, 0f, 0f, 1f, 1f, texture);
        textureMap.put("clickedButton", mesh);

        texture = new Texture(TEXTURE_PATH + "menu/MenuBorder.png");
        mesh = new TextureMesh(buttonWidth + 0.2f * boxRatio, buttonHeight + 0.85f, 0f, 0f, 1f, 1f, texture);
        textureMap.put("menuBorder", mesh);

        mesh = new TextureMesh(1f * boxRatio, 0.5f, 0f, 0f, 1f, 1f, texture);
        textureMap.put("alertBox", mesh);
    }

    /**
     * initialise the mesh for the background (background texture, platform meshes, box mssh)
     */
    //Initialise the meshes of the background
    private void initBackground(){

        background = new Texture(TEXTURE_PATH  + "background.png");

        Texture platforms = new Texture(TEXTURE_PATH  + "tilesGrass.png");
        TextureMesh mesh;

        int TEXTURE_TYPE = GRASS;

        mesh = new TextureMesh(boxRatio, PLATFORM_SPRITE_X_POS * 0f, 0f, PLATFORM_SPRITE_X_POS, 0.5f, platforms);
        textureMap.put("platformTop", mesh);

        mesh = new TextureMesh(boxRatio, PLATFORM_SPRITE_X_POS * 1f, 0f, PLATFORM_SPRITE_X_POS, 0.5f, platforms);
        textureMap.put("platformBottom", mesh);

        mesh = new TextureMesh(boxRatio, PLATFORM_SPRITE_X_POS * 2f, 0f, PLATFORM_SPRITE_X_POS, 0.5f, platforms);
        textureMap.put("platformLeft", mesh);

        mesh = new TextureMesh(boxRatio, PLATFORM_SPRITE_X_POS * 3f, 0f, PLATFORM_SPRITE_X_POS, 0.5f, platforms);
        textureMap.put("platformRight", mesh);

        mesh = new TextureMesh(boxRatio, PLATFORM_SPRITE_X_POS * 4f, 0f, PLATFORM_SPRITE_X_POS, 0.5f, platforms);
        textureMap.put("platformTopLeft", mesh);

        mesh = new TextureMesh(boxRatio, PLATFORM_SPRITE_X_POS * 5f, 0f, PLATFORM_SPRITE_X_POS, 0.5f, platforms);
        textureMap.put("platformTopRight", mesh);

        mesh = new TextureMesh(boxRatio, PLATFORM_SPRITE_X_POS * 6f, 0f, PLATFORM_SPRITE_X_POS, 0.5f, platforms);
        textureMap.put("platformBottomLeft", mesh);

        mesh = new TextureMesh(boxRatio, PLATFORM_SPRITE_X_POS * 7f, 0f, PLATFORM_SPRITE_X_POS, 0.5f, platforms);
        textureMap.put("platformBottomRight", mesh);

        mesh = new TextureMesh(boxRatio, PLATFORM_SPRITE_X_POS * 0f, 0.5f, PLATFORM_SPRITE_X_POS, 0.5f, platforms);
        textureMap.put("platformTopLeftCorner", mesh);

        mesh = new TextureMesh(boxRatio, PLATFORM_SPRITE_X_POS * 1f, 0.5f, PLATFORM_SPRITE_X_POS, 0.5f, platforms);
        textureMap.put("platformTopRightCorner", mesh);

        mesh = new TextureMesh(boxRatio, PLATFORM_SPRITE_X_POS * 2f, 0.5f, PLATFORM_SPRITE_X_POS, 0.5f, platforms);
        textureMap.put("platformBottomLeftCorner", mesh);

        mesh = new TextureMesh(boxRatio, PLATFORM_SPRITE_X_POS * 3f, 0.5f, PLATFORM_SPRITE_X_POS, 0.5f, platforms);
        textureMap.put("platformBottomRightCorner", mesh);

        mesh = new TextureMesh(boxRatio, PLATFORM_SPRITE_X_POS * 4f, 0.5f, PLATFORM_SPRITE_X_POS, 0.5f, platforms);
        textureMap.put("platformFullLeft", mesh);

        mesh = new TextureMesh(boxRatio, PLATFORM_SPRITE_X_POS * 5f, 0.5f, PLATFORM_SPRITE_X_POS, 0.5f, platforms);
        textureMap.put("platformFullRight", mesh);

        mesh = new TextureMesh(boxRatio, PLATFORM_SPRITE_X_POS * 6f, 0.5f, PLATFORM_SPRITE_X_POS, 0.5f, platforms);
        textureMap.put("platform", mesh);

        platforms = new Texture(TEXTURE_PATH + "box.png");
        mesh = new TextureMesh(boxRatio, 0f, 0f, 1f, 1f, platforms);
        textureMap.put("box", mesh);

    }

    /**
     * initialising the mesh for bullets
     */
    private void initBullet(){

        Texture texture = new Texture(TEXTURE_PATH + "bullet.png");
        TextureMesh mesh = new TextureMesh(0.04f * boxRatio, 0.025f, 0f, 0f, 1f, 1f, texture);
        textureMap.put("bullet", mesh);

    }


    /**
     * clear the screen
     * used to clear the previous rendering
     */
    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    /**
     * render the given state on the window
     * @param window
     *          window used to render on
     * @param gameState
     *          the given state
     */
    public void render(Window window, GameState gameState){
        clear();

        if(window.isResized()){
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        renderTexture(window, gameState);
        renderHuds(window, gameState);
    }


    /**
     * render the text for the given state
     * @param window
     *          window used to render on
     * @param gameState
     *          the given game state to be rendered
     */
    private void renderHuds(Window window, GameState gameState){


        //Fields used to render the text
        String textToRend = "";
        float xPos = 0f;
        Player client = null;
        int enemiesNo = 0;


        ArrayList<Player> players = gameState.getPlayers();
        for(Player player: players){
            if(player.getId() == this.clientId)
                client = player;
            else if(player.isAlive())
                enemiesNo++;
        }

        Matrix4f modelMatrix = null;

        hudShaderProgram.bind();

        //Render the name of the player
        //textToRend = client.getName();
        if(gameState.isOnlineGame())
            textToRend = client.getName();
        else
            textToRend = "Jimmy";
        hudItem.setText(textToRend, 0.065f);
        modelMatrix = transformation.getModelMatrix(0.2f * boxRatio, 0f, 1f);
        hudShaderProgram.setUniform("hudMatrix", modelMatrix);
        hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
        hudItem.getMesh().render();


        //Render stats of the player
        textToRend = Integer.toString((int)client.getHp());
        hudItem.setText(textToRend, 0.05f);
        modelMatrix = transformation.getModelMatrix(0.27f * boxRatio, -0.075f, 1f);
        hudShaderProgram.setUniform("hudMatrix", modelMatrix);
        hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
        hudItem.getMesh().render();

        textToRend = Integer.toString((int)client.getEnergy());
        hudItem.setText(textToRend, 0.05f);
        modelMatrix = transformation.getModelMatrix(0.27f * boxRatio, -0.135f, 1f);
        hudShaderProgram.setUniform("hudMatrix", modelMatrix);
        hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
        hudItem.getMesh().render();

        textToRend = Integer.toString(client.getScore());
        hudItem.setText(textToRend, 0.05f);
        modelMatrix = transformation.getModelMatrix(0.45f * boxRatio, -0.075f, 1f);
        hudShaderProgram.setUniform("hudMatrix", modelMatrix);
        hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
        hudItem.getMesh().render();



        //Render menu text
        if(gameState.isPaused()) {

            if(!gameState.isAlertBox()){
                textToRend = "Menu";
                hudItem.setText(textToRend, 0.1f);
                xPos = getCentered(getTextSize(textToRend, 0.1f));
                modelMatrix = transformation.getModelMatrix(xPos, -0.625f, 1f);
                hudShaderProgram.setUniform("hudMatrix", modelMatrix);
                hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
                hudItem.getMesh().render();

                textToRend = "Resume";
                hudItem.setText(textToRend, 0.06f);
                xPos = getCentered(getTextSize(textToRend, 0.06f));
                modelMatrix = transformation.getModelMatrix(xPos, -0.767f, 1f);
                hudShaderProgram.setUniform("hudMatrix", modelMatrix);
                hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
                hudItem.getMesh().render();


                if(gameState.isMusic()){
                    textToRend = "Music: On";
                }else{
                    textToRend = "Music: Off";
                }
                hudItem.setText(textToRend, 0.06f);
                xPos = getCentered(getTextSize(textToRend, 0.06f));
                modelMatrix = transformation.getModelMatrix(xPos, -0.917f, 1f);
                hudShaderProgram.setUniform("hudMatrix", modelMatrix);
                hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
                hudItem.getMesh().render();

                if(gameState.isSoundFx()) {
                    textToRend = "Sound Fx: On";
                }else{
                    textToRend = "Sound Fx: Off";
                }
                hudItem.setText(textToRend, 0.06f);
                xPos = getCentered(getTextSize(textToRend, 0.06f));
                modelMatrix = transformation.getModelMatrix(xPos, -1.067f, 1f);
                hudShaderProgram.setUniform("hudMatrix", modelMatrix);
                hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
                hudItem.getMesh().render();

                textToRend = "Main Menu";
                hudItem.setText(textToRend, 0.06f);
                xPos = getCentered(getTextSize(textToRend, 0.06f));
                modelMatrix = transformation.getModelMatrix(xPos, -1.217f, 1f);
                hudShaderProgram.setUniform("hudMatrix", modelMatrix);
                hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
                hudItem.getMesh().render();

                textToRend = "Exit";
                hudItem.setText(textToRend, 0.06f);
                xPos = getCentered(getTextSize(textToRend, 0.06f));
                modelMatrix = transformation.getModelMatrix(xPos, -1.367f, 1f);
                hudShaderProgram.setUniform("hudMatrix", modelMatrix);
                hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
                hudItem.getMesh().render();

            }else{
                textToRend = "Are you sure you";
                hudItem.setText(textToRend, 0.1f);
                xPos = getCentered(getTextSize(textToRend, 0.1f));
                modelMatrix = transformation.getModelMatrix(xPos, -0.65f, 1f);
                hudShaderProgram.setUniform("hudMatrix", modelMatrix);
                hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
                hudItem.getMesh().render();

                textToRend = "wish to exit?";
                hudItem.setText(textToRend, 0.1f);
                xPos = getCentered(getTextSize(textToRend, 0.1f));
                modelMatrix = transformation.getModelMatrix(xPos, -0.75f, 1f);
                hudShaderProgram.setUniform("hudMatrix", modelMatrix);
                hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
                hudItem.getMesh().render();

                textToRend = "No";
                hudItem.setText(textToRend, 0.08f);
                xPos = getCentered(getTextSize(textToRend, 0.08f));
                modelMatrix = transformation.getModelMatrix(xPos - 0.22f * boxRatio, -0.92f, 1f);
                hudShaderProgram.setUniform("hudMatrix", modelMatrix);
                hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
                hudItem.getMesh().render();

                textToRend = "Yes";
                hudItem.setText(textToRend, 0.08f);
                xPos = getCentered(getTextSize(textToRend, 0.08f));
                modelMatrix = transformation.getModelMatrix(xPos + 0.22f * boxRatio, -0.92f, 1f);
                hudShaderProgram.setUniform("hudMatrix", modelMatrix);
                hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
                hudItem.getMesh().render();
            }
        }



        //Render the box text for round over under the single player mode
        if(gameState.isRoundOver() && !gameState.isOnlineGame()){


            textToRend = "Well done!";
            hudItem.setText(textToRend, 0.1f);
            xPos = getCentered(getTextSize(textToRend, 0.1f));
            modelMatrix = transformation.getModelMatrix(xPos, -0.65f, 1f);
            hudShaderProgram.setUniform("hudMatrix", modelMatrix);
            hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
            hudItem.getMesh().render();

            textToRend = "Your current score is: " + client.getScore();
            hudItem.setText(textToRend, 0.07f);
            xPos = getCentered(getTextSize(textToRend, 0.07f));
            modelMatrix = transformation.getModelMatrix(xPos, -0.8f, 1f);
            hudShaderProgram.setUniform("hudMatrix", modelMatrix);
            hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
            hudItem.getMesh().render();

            textToRend = "Next round starts in: ";
            if(gameState.getEndETA() == -1){
                textToRend += "  ";
            }
            else if(gameState.getEndETA() > 5) {
                textToRend += "  ";
            }else{
                textToRend += gameState.getEndETA();
            }

            hudItem.setText(textToRend, 0.07f);
            xPos = getCentered(getTextSize(textToRend, 0.07f));
            modelMatrix = transformation.getModelMatrix(xPos, -0.9f, 1f);
            hudShaderProgram.setUniform("hudMatrix", modelMatrix);
            hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
            hudItem.getMesh().render();

        }



        //Render the box text for the game over under the single player mode
        if(gameState.isGameOver() && !gameState.isOnlineGame()){
            textToRend = "You died...";
            hudItem.setText(textToRend, 0.1f);
            xPos = getCentered(getTextSize(textToRend, 0.1f));
            modelMatrix = transformation.getModelMatrix(xPos, -0.65f, 1f);
            hudShaderProgram.setUniform("hudMatrix", modelMatrix);
            hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
            hudItem.getMesh().render();

            textToRend = "Your final score is: " + client.getScore();
            hudItem.setText(textToRend, 0.07f);
            xPos = getCentered(getTextSize(textToRend, 0.07f));
            modelMatrix = transformation.getModelMatrix(xPos, -0.8f, 1f);
            hudShaderProgram.setUniform("hudMatrix", modelMatrix);
            hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
            hudItem.getMesh().render();

            textToRend = "You will be redirected to the menu in: ";
            if(gameState.getEndETA() == -1){
                textToRend += "  ";
            }
            else if(gameState.getEndETA() > 5) {
                textToRend += "  ";
            }else{
                textToRend += gameState.getEndETA();
            }

            hudItem.setText(textToRend, 0.05f);
            xPos = getCentered(getTextSize(textToRend, 0.05f));
            modelMatrix = transformation.getModelMatrix(xPos, -0.9f, 1f);
            hudShaderProgram.setUniform("hudMatrix", modelMatrix);
            hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
            hudItem.getMesh().render();
        }



        //Render the box text for the round over under the multi player mode
        if(gameState.isOnlineRoundOver() && gameState.isOnlineGame() && !gameState.isOnlineGameOver()){
            textToRend = "Round over!";
            hudItem.setText(textToRend, 0.08f);
            xPos = getCentered(getTextSize(textToRend, 0.08f));
            modelMatrix = transformation.getModelMatrix(xPos, -0.625f, 1f);
            hudShaderProgram.setUniform("hudMatrix", modelMatrix);
            hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
            hudItem.getMesh().render();

            textToRend = "Next round starts in: ";
            if(gameState.getEndETA() == -1){
                textToRend += " ";
            }
            else if(gameState.getEndETA() > 5) {
                textToRend += " ";
            }else{
                textToRend += gameState.getEndETA();
            }

            hudItem.setText(textToRend, 0.07f);
            xPos = getCentered(getTextSize(textToRend, 0.07f));
            modelMatrix = transformation.getModelMatrix(xPos, -0.725f, 1f);
            hudShaderProgram.setUniform("hudMatrix", modelMatrix);
            hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
            hudItem.getMesh().render();

            float yPos = -0.625f;
            int i = 0;
            for(Player player: players){
                textToRend = player.getName();
                textToRend += ": " + Byte.toString(player.getRoundsWon()) + "/3";
                hudItem.setText(textToRend, 0.06f);
                xPos = getCentered(getTextSize(textToRend, 0.06f));
                modelMatrix = transformation.getModelMatrix(xPos, -0.8f - 0.075f * i, 1f);
                hudShaderProgram.setUniform("hudMatrix", modelMatrix);
                hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
                hudItem.getMesh().render();
                i++;
            }
        }



        //Render the box text for the game over under the multi player mode
        if(gameState.isOnlineGameOver() && gameState.isOnlineGame()){
            if((int)client.getRoundsWon() == 3)
                textToRend = "YOU WON!";
            else
                textToRend = "YOU LOST!";
            hudItem.setText(textToRend, 0.08f);
            xPos = getCentered(getTextSize(textToRend, 0.08f));
            modelMatrix = transformation.getModelMatrix(xPos, -0.625f, 1f);
            hudShaderProgram.setUniform("hudMatrix", modelMatrix);
            hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
            hudItem.getMesh().render();

            textToRend = "You will be redirected to the menu in: ";
            if(gameState.getEndETA() == -1){
                textToRend += " ";
            }
            else if(gameState.getEndETA() > 5) {
                textToRend += " ";
            }else{
                textToRend += gameState.getEndETA();
            }

            hudItem.setText(textToRend, 0.05f);
            xPos = getCentered(getTextSize(textToRend, 0.05f));
            modelMatrix = transformation.getModelMatrix(xPos, -0.725f, 1f);
            hudShaderProgram.setUniform("hudMatrix", modelMatrix);
            hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
            hudItem.getMesh().render();

            float yPos = -0.625f;
            int i = 0;
            for(Player player: players){
                textToRend = player.getName();
                textToRend += ": " + Byte.toString(player.getRoundsWon()) + "/3";
                hudItem.setText(textToRend, 0.06f);
                xPos = getCentered(getTextSize(textToRend, 0.06f));
                modelMatrix = transformation.getModelMatrix(xPos, -0.8f - 0.075f * i, 1f);
                hudShaderProgram.setUniform("hudMatrix", modelMatrix);
                hudShaderProgram.setUniform("colour", Color.WHITE.getRed(), Color.WHITE.getGreen(), Color.WHITE.getBlue(), 1);
                hudItem.getMesh().render();
                i++;
            }
        }



        //Render number of bullets for each weapon
        textToRend = "Inf";
        xPos = getCentered(0.6f);
        hudItem.setText(textToRend, 0.04f);
        modelMatrix = transformation.getModelMatrix(xPos + 0.065f * boxRatio , -1.96f, 1f);
        hudShaderProgram.setUniform("hudMatrix", modelMatrix);
        hudShaderProgram.setUniform("colour", Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue(), 1);
        hudItem.getMesh().render();


        ArrayList<Pair<Byte, Short>> weapons = client.getWeapons();

        if((int)weapons.get(1).getKey() != NO_WEAPON_ID){
            textToRend = Short.toString(weapons.get(1).getValue());
            xPos = getCentered(0.6f) + 0.16f * boxRatio;
            hudItem.setText(textToRend, 0.04f);
            modelMatrix = transformation.getModelMatrix(xPos + 0.06f * boxRatio , -1.96f, 1f);
            hudShaderProgram.setUniform("hudMatrix", modelMatrix);
            hudShaderProgram.setUniform("colour", Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue(), 1);
            hudItem.getMesh().render();

        }

        if((int)weapons.get(2).getKey() != NO_WEAPON_ID){
            textToRend = Short.toString(weapons.get(2).getValue());
            xPos = getCentered(0.6f) + 0.32f * boxRatio;
            hudItem.setText(textToRend, 0.04f);
            modelMatrix = transformation.getModelMatrix(xPos + 0.06f * boxRatio , -1.96f, 1f);
            hudShaderProgram.setUniform("hudMatrix", modelMatrix);
            hudShaderProgram.setUniform("colour", Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue(), 1);
            hudItem.getMesh().render();
        }



        //Render enemies stats
        if(gameState.isOnlineGame()){
            float yPos = -0.005f;
            xPos = 2.0f - 0.4f * boxRatio;
            for(Player player: players){
                if(player.getId() != clientId){
                    textToRend = player.getName();
                    hudItem.setText(textToRend, 0.04f);
                    modelMatrix = transformation.getModelMatrix(xPos, yPos, 1f);
                    hudShaderProgram.setUniform("hudMatrix", modelMatrix);
                    hudShaderProgram.setUniform("colour", Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue(), 1);
                    hudItem.getMesh().render();
                    yPos -= 0.16f;
                }
            }
        }else{
            textToRend = "No. : " + Integer.toString(enemiesNo);
            xPos = 2.0f - 0.4f * boxRatio;
            hudItem.setText(textToRend, 0.04f);
            modelMatrix = transformation.getModelMatrix(xPos, -0.005f, 1f);
            hudShaderProgram.setUniform("hudMatrix", modelMatrix);
            hudShaderProgram.setUniform("colour", Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue(), 1);
            hudItem.getMesh().render();
        }

        hudShaderProgram.unbind();

    }


    /**
     * render the texture of the given game state
     * @param window
     *          window used to render on
     * @param gameState
     *          the given game state to render
     */
    private void renderTexture(Window window, GameState gameState){

        //Fields used to render texture
        Player client = null;

        ArrayList<Player> players = gameState.getPlayers();
        int[][] grid = gameState.getMap().getGrid();

        for(Player player: players){
            if(player.getId() == this.clientId)
                client = player;
        }


        //Computing the position of the client on the screen
        //(the closer it is to the edge of the world, closer it will be rendered to the respective edge of the screen)
        float screenMaxScalerX = MAX_PLAYER_X - MIN_PLAYER_X;
        float screenMaxScalerY = MAX_PLAYER_Y - MIN_PLAYER_Y;

        float playerOnScaleScreenX = MIN_PLAYER_X + screenMaxScalerX * ((1.0f * client.getXPos()) / (1.0f * gameState.getMap().getXSize()));
        float playerOnScaleScreenY = MIN_PLAYER_Y + screenMaxScalerY * ((1.0f * client.getYPos()) / (1.0f * gameState.getMap().getYSize()));

        //Compute an approximation of the position of the player on the grid representation
        //It is used to get the background, bullets and players thar are in range for rendering
        float playerGrid_x =  client.getXPos() / BOX_HEIGHT;
        float playerGrid_y =  client.getYPos() / BOX_HEIGHT;


        //Compute the position of the origin of the screen based on the origin of the original map
        float screenOrigin_x = client.getXPos() * windowsRatio - playerOnScaleScreenX * boxSize * 10f / boxRatio;
        float screenOrigin_y = client.getYPos() * windowsRatio - playerOnScaleScreenY* boxSize * 10f;


        //Render each object in range sight of client player
        textureShaderProgram.bind();


        //Render the sky-background
        TextureMesh mesh = new TextureMesh(
                2f,
                2f,
                screenOrigin_x/4096f,
                screenOrigin_y/4096f,
                window.getWidth()/4096f,
                window.getHeight()/4096f,
                background
        );
        Matrix4f matrix = transformation.getModelMatrix(0f, 0f, 1f);
        textureShaderProgram.setUniform("textureMatrix", matrix);
        mesh.render();
        mesh.deleteBuffers();

        //Render the platforms
        renderGrid(client.getXPos(), client.getYPos(), screenOrigin_x, screenOrigin_y, playerOnScaleScreenX, playerOnScaleScreenY, grid);

        //Render boxes
        renderBoxes(playerGrid_x, playerGrid_y, screenOrigin_x, screenOrigin_y, gameState.getBoxes());

        //Render bullets
        renderBullets(playerGrid_x, playerGrid_y, screenOrigin_x, screenOrigin_y, players);


        boolean over = false;
        if(gameState.isOnlineGame()){
            over = gameState.isOnlineGameOver() || gameState.isOnlineRoundOver();
        }else{
            over = gameState.isGameOver() || gameState.isRoundOver();
        }

        //Render players
        renderPlayers(playerGrid_x, playerGrid_y, screenOrigin_x, screenOrigin_y, players, over);

        //render the client if alive
        if(client.isAlive()) {
            //Render the meshes for this player

            renderPlayer(playerOnScaleScreenX, playerOnScaleScreenY, client, over);
        }

        //render the UI
        renderUI(players, gameState.isOnlineGame(), gameState.isRoundOver(), gameState.isGameOver(), gameState.isOnlineGameOver(), gameState.isOnlineRoundOver());


        //render the menu
        if(gameState.isPaused()) {
            renderMenu(gameState.getMenuInfo(), gameState.isAlertBox());
        }

        textureShaderProgram.unbind();
    }

    /**
     * render boxes that in range of view for the given game state
     * @param playerGrid_x
     *          the x position of the player used to compute the box in view
     * @param playerGrid_y
     *          the y position of the player used to compute the box in view
     * @param screenOrigin_x
     *          the x position of the screen with reference to the origin of the map
     * @param screenOrigin_y
     *          the x position of the screen with reference to the origin of the map
     * @param boxes
     *          the given boxes of the game state
     */
    private void renderBoxes(float playerGrid_x, float playerGrid_y, float screenOrigin_x, float screenOrigin_y, ArrayList<Box> boxes) {
        for(Box box: boxes){
            if((Math.abs(playerGrid_x - box.getXPos()/BOX_HEIGHT) <= PLAYER_RANGE || Math.abs(playerGrid_y - box.getYPos()/BOX_HEIGHT) <= PLAYER_RANGE) && !box.isDestroyed()){
                Matrix4f matrix = getModel(box.getXPos() * windowsRatio, box.getYPos() * windowsRatio, screenOrigin_x, screenOrigin_y,1f);

                    textureShaderProgram.setUniform("textureMatrix", matrix);

                    //Render the mesh for this player
                    textureMap.get("box").render();
                }
            }

    }

    /**
     * render UI of the given state
     * this includes:
     *          - weapon bar
     *          - stats block (health, energy, score)
     *          - enemy block
     *          - menu (if it is the case)
     * @param players
     *          the players containing details to render on screen
     * @param isOnline
     *          checking if the game is Online
     * @param roundOver
     *          checking if the singlep layeyr round is over
     * @param gameOver
     *          checking if the single player game is over
     * @param onlineGameOver
     *          checking if the multi player round is over
     * @param onlineRoundOver
     *          checking if the single player round is over
     */
    private void renderUI(ArrayList<Player> players, boolean isOnline, boolean roundOver, boolean gameOver, boolean onlineGameOver, boolean onlineRoundOver){
        Matrix4f matrix;

        Player client = null;
        for(Player player: players){
            if(player.getId() == this.clientId)
                client = player;
        }

        //Render client block stats(top left corner)
        matrix = transformation.getModelMatrix(0f, 0f, 1f);
        textureShaderProgram.setUniform("textureMatrix", matrix);
        textureMap.get("characterUI").render();

        matrix = transformation.getModelMatrix(0.025f * boxRatio, -0.025f, 1f);
        textureShaderProgram.setUniform("textureMatrix", matrix);
        textureMap.get("facePlayer" + client.get_charId()).render();

        matrix = transformation.getModelMatrix(0.215f * boxRatio, -0.08f, 1f);
        textureShaderProgram.setUniform("textureMatrix", matrix);
        textureMap.get("heart").render();

        matrix = transformation.getModelMatrix(0.215f * boxRatio, -0.14f, 1f);
        textureShaderProgram.setUniform("textureMatrix", matrix);
        textureMap.get("lightning").render();

        matrix = transformation.getModelMatrix(0.405f * boxRatio, -0.08f, 1f);
        textureShaderProgram.setUniform("textureMatrix", matrix);
        textureMap.get("target").render();


        //Render the block of enemies (top right corner)
        float xPos = 2.0f - 0.6f * boxRatio;
        float xPos2 = (xPos + 0.47f * boxRatio);
        int i = 0;

        if(isOnline) {
            for (Player player : players) {
                float yPos = - 0.03f - 0.16f * i;
                if (player.getId() != clientId) {
                    matrix = transformation.getModelMatrix(xPos, -0.16f * i, 1f);
                    textureShaderProgram.setUniform("textureMatrix", matrix);
                    textureMap.get("enemyUI").render();

                    matrix = transformation.getModelMatrix(xPos2 * 0.8f + (-1 + xPos2) / 5f, yPos * 0.8f + (1 - yPos)/5f - 0.06f * i, 0.8f);
                    textureShaderProgram.setUniform("textureMatrix", matrix);
                    textureMap.get("facePlayer" + player.get_charId()).render();

                    if (!player.isAlive()) {
                        matrix = transformation.getModelMatrix(xPos2 * 0.8f + (-1 + xPos2) / 5f, yPos * 0.8f + (1 - yPos)/5f - 0.06f * i, 0.8f);
                        textureShaderProgram.setUniform("textureMatrix", matrix);
                        textureMap.get("xDead").render();
                    }
                    i++;
                }
            }

        }else{

            float yPos = -0.03f;
            matrix = transformation.getModelMatrix(xPos, 0f, 1f);
            textureShaderProgram.setUniform("textureMatrix", matrix);
            textureMap.get("enemyUI").render();
            matrix = transformation.getModelMatrix( xPos2 * 0.8f + (-1 + xPos2)/5f , yPos * 0.8f + (1 - yPos)/5f, 0.8f);
            textureShaderProgram.setUniform("textureMatrix", matrix);
            textureMap.get("zombieFace").render();

        }

        //Render weapon bar
        //Including the weapon from the invetory
        float weaponBarX = getCentered(0.6f);
        ArrayList<Pair<Byte, Short>> weapons = client.getWeapons();

        for(i = 0; i < 3; i++){
            if(weapons.get(i).getKey() == NO_WEAPON_ID){
                matrix = transformation.getModelMatrix(weaponBarX + 0.16f * i * boxRatio, -1.8f, 1f);
                textureShaderProgram.setUniform("textureMatrix", matrix);
                textureMap.get("weaponBar1").render();
            }else {
                int weaponId = (int) weapons.get(i).getKey();

                matrix = transformation.getModelMatrix(weaponBarX + 0.16f * i * boxRatio, -1.8f, 1f);
                textureShaderProgram.setUniform("textureMatrix", matrix);
                if (weaponId == (int) client.getCurrentWeapon().getKey()) {
                    textureMap.get("weaponBar2").render();
                } else {
                    textureMap.get("weaponBar1").render();
                }

                matrix = transformation.getModelMatrix(weaponBarX + 0.025f * boxRatio + 0.16f * i * boxRatio, -1.81f, 1f);
                textureShaderProgram.setUniform("textureMatrix", matrix);
                textureMap.get(idToString(weaponId) + "Ui").render();
            }
        }


        //Render the game over box
        if ((roundOver || gameOver) && !isOnline) {
            float borderX = getCentered(1f);
            matrix = transformation.getModelMatrix(borderX, -0.6f, 1f);
            textureShaderProgram.setUniform("textureMatrix", matrix);
            textureMap.get("alertBox").render();
        }

        if ((onlineRoundOver || onlineGameOver) && isOnline) {
            float borderX = getCentered(1f);
            matrix = transformation.getModelMatrix(borderX, -0.6f, 1f);
            textureShaderProgram.setUniform("textureMatrix", matrix);
            textureMap.get("alertBox").render();
        }
    }

    /**
     * renders the menu of the given state
     * @param menuInfo
     *          info to render on window
     * @param alertBox
     *          checks if the alert box is triggered
     */
    private void renderMenu(int menuInfo, boolean alertBox){
        Matrix4f matrix;

        if(!alertBox){
            float borderX = getCentered(0.6f);
            float buttonX = getCentered(0.4f);

            float buttonSizeY = 0.15f;
            float buttonStartY = -0.75f;

            matrix = transformation.getModelMatrix(borderX, -0.6f, 1f);
            textureShaderProgram.setUniform("textureMatrix", matrix);
            textureMap.get("menuBorder").render();

            for (int i = 1; i < 6; i++) {

                matrix = transformation.getModelMatrix(buttonX, buttonStartY - (i - 1) * buttonSizeY, 1f);
                textureShaderProgram.setUniform("textureMatrix", matrix);

                if (menuInfo / 10 == i && menuInfo % 10 == 1) {
                    textureMap.get("buttonMouseOver").render();
                } else if (menuInfo / 10 == i && menuInfo % 10 == 2) {
                    textureMap.get("clickedButton").render();
                } else {
                    textureMap.get("simpleButton").render();
                }
            }
        }else{
            float borderX = getCentered(1f);
            float buttonX = getCentered(0.95f);

            float buttonStartY = -0.9f * 0.8f;

            matrix = transformation.getModelMatrix(borderX, -0.6f, 1f);
            textureShaderProgram.setUniform("textureMatrix", matrix);
            textureMap.get("alertBox").render();

            matrix = transformation.getModelMatrix(buttonX * 0.8f, buttonStartY, 0.8f);
            textureShaderProgram.setUniform("textureMatrix", matrix);


            if (menuInfo / 10 == 1 && menuInfo % 10 == 1) {
                textureMap.get("buttonMouseOver").render();
            } else if (menuInfo / 10 == 1 && menuInfo % 10 == 2) {
                textureMap.get("clickedButton").render();
            } else {
                textureMap.get("simpleButton").render();
            }


            matrix = transformation.getModelMatrix((buttonX + 0.55f * boxRatio) * 0.8f, buttonStartY, 0.8f);
            textureShaderProgram.setUniform("textureMatrix", matrix);
            if (menuInfo / 10 == 2 && menuInfo % 10 == 1) {
                textureMap.get("buttonMouseOver").render();
            } else if (menuInfo / 10 == 2 && menuInfo % 10 == 2) {
                textureMap.get("clickedButton").render();
            } else {
                textureMap.get("simpleButton").render();
            }

        }
    }

    /**
     * render the enemies on the window
     * @param playerGrid_x
     *          x position to check the player in view with reference to the client
     * @param playerGrid_y
     *          y position to check the player in view with reference to the client
     * @param screenOrigin_x
     *          the x position of the screen with reference to the origin of the map
     * @param screenOrigin_y
     *          the y position of the screen with reference to the origin of the map
     * @param players
     *          the given players
     */
    private void renderPlayers(float playerGrid_x, float playerGrid_y, float screenOrigin_x, float screenOrigin_y, ArrayList<Player> players, boolean over) {
        for(Player player: players){
            if(player.getId() != clientId && player.isAlive()){
                if((Math.abs(playerGrid_x - player.getXPos()/BOX_HEIGHT) <= PLAYER_RANGE || Math.abs(playerGrid_y - player.getYPos()/BOX_HEIGHT) <= PLAYER_RANGE)){
                    Point.Float point = getModel(player.getXPos()*windowsRatio, player.getYPos()*windowsRatio, screenOrigin_x, screenOrigin_y);
                    renderPlayer(point.x, point.y, player, over);
                }
            }
        }
    }


    /**
     * render the individual player
     * @param x
     *      x position on screen of the player
     * @param y
     *      y position on screen of the player
     * @param player
     *      the given player
     */
    private void renderPlayer(float x, float y, Player player, boolean over){

        textureShaderProgram.unbind();

        shaderProgram.bind();
        float[] colours;
        if(player.getId() == clientId){
            colours = new float[]{
                    0f, 1f, 0f,
                    1f, 0f, 1f,
                    0f, 1f, 0f,
                    0f, 0f, 0f
            };
        }else {
            colours = new float[]{
                    1f, 0f, 1f,
                    0f, 1f, 0f,
                    1f, 0f, 0f,
                    0f, 0f, 0f
            };
        }

        float healthBarSizeX = ((((PLAYER_WIDTH * 1.0f)/(BOX_HEIGHT * 1.0f)) / 10f) * player.getHp() / 100f) * boxRatio;
        Mesh healthBar = new Mesh(colours, healthBarSizeX, 0.015f);
        Matrix4f matrix = transformation.getModelMatrix(x + 0.065f * boxRatio, -y - 0.025f, 1f);
        shaderProgram.setUniform("modelMatrix", matrix);
        healthBar.render();
        healthBar.cleanup();
        shaderProgram.unbind();

        textureShaderProgram.bind();

        Matrix4f modelMatrix;

        if(player.getShootingDirection() > 0)
            modelMatrix = transformation.getModelMatrix(x + 0.035f * boxRatio, -y - 0.035f, 1f);
        else
            modelMatrix = transformation.getModelMatrix(x - 0.035f * boxRatio, -y - 0.04f, 1f);
        textureShaderProgram.setUniform("textureMatrix", modelMatrix);
        if(player.getCurrentWeapon().getKey() == PISTOL_ID || player.getCurrentWeapon().getKey() == UZI_ID || player.getCurrentWeapon().getKey() == AI_WEAPON_ID) {
            animationMap.get((int) player.getId()).renderRightArm(player.getShootingDirection());
        }

        modelMatrix = transformation.getModelMatrix(x, -y, 1f);
        textureShaderProgram.setUniform("textureMatrix", modelMatrix);
        if(over) {
            animationMap.get((int) player.getId()).renderBody(0, player.getShootingDirection());
        }else{
            animationMap.get((int) player.getId()).renderBody(player.getStateOfMovement(), player.getShootingDirection());
        }
        if(player.getCurrentWeapon().getKey() != PISTOL_ID && player.getCurrentWeapon().getKey() != UZI_ID && player.getCurrentWeapon().getKey() != AI_WEAPON_ID) {
            if (player.getShootingDirection() > 0)
                modelMatrix = transformation.getModelMatrix(x + 0.07f * boxRatio, -y - 0.045f, 1f);
            else
                modelMatrix = transformation.getModelMatrix(x - 0.07f * boxRatio, -y - 0.045f, 1f);
            textureShaderProgram.setUniform("textureMatrix", modelMatrix);
            animationMap.get((int) player.getId()).renderHand(player.getShootingDirection());
        }

        renderGun(x, y, player);

        if(player.getShootingDirection() > 0)
            modelMatrix = transformation.getModelMatrix(x - 0.008f * boxRatio, -y - 0.04f, 1f);
        else
            modelMatrix = transformation.getModelMatrix(x + 0.008f * boxRatio, -y - 0.04f, 1f);
        textureShaderProgram.setUniform("textureMatrix", modelMatrix);
        animationMap.get((int)player.getId()).renderLeftArm(player.getShootingDirection());



        if (player.getShootingDirection() > 0)
            modelMatrix = transformation.getModelMatrix(x + 0.01f * boxRatio, -y - 0.05f, 1f);
        else
            modelMatrix = transformation.getModelMatrix(x - 0.01f * boxRatio, -y - 0.05f, 1f);
        textureShaderProgram.setUniform("textureMatrix", modelMatrix);
        animationMap.get((int) player.getId()).renderHand(player.getShootingDirection());

    }

    /**
     * render the gun wield by the given player
     * @param x
     *      x positon on screen of the player
     * @param y
     *      y position on screen of the player
     * @param player
     *      the given player
     */
    private void renderGun(float x, float y, Player player){
        int type = (int)player.getCurrentWeapon().getKey();

        Matrix4f modelMatrix;

        switch(type){
            case AI_WEAPON_ID:
                if(player.getShootingDirection() > 0) {
                    modelMatrix = transformation.getModelMatrix(x + 0.045f * boxRatio, -y - 0.06f, 1f);
                    textureShaderProgram.setUniform("textureMatrix", modelMatrix);
                    textureMap.get("pistolRight").render();
                }else {
                    modelMatrix = transformation.getModelMatrix(x - 0.015f * boxRatio, -y - 0.06f, 1f);
                    textureShaderProgram.setUniform("textureMatrix", modelMatrix);
                    textureMap.get("pistolLeft").render();
                }
                break;
            case PISTOL_ID:
                if(player.getShootingDirection() > 0) {
                    modelMatrix = transformation.getModelMatrix(x + 0.045f * boxRatio, -y - 0.06f, 1f);
                    textureShaderProgram.setUniform("textureMatrix", modelMatrix);
                    textureMap.get("pistolRight").render();
                }else {
                    modelMatrix = transformation.getModelMatrix(x - 0.015f * boxRatio, -y - 0.06f, 1f);
                    textureShaderProgram.setUniform("textureMatrix", modelMatrix);
                    textureMap.get("pistolLeft").render();
                }
                break;

            case SHOTGUN_ID:
                if(player.getShootingDirection() > 0) {
                    modelMatrix = transformation.getModelMatrix(x + 0.05f * boxRatio, -y - 0.04f, 1f);
                    textureShaderProgram.setUniform("textureMatrix", modelMatrix);
                    textureMap.get("shotgunRight").render();
                }else {
                    modelMatrix = transformation.getModelMatrix(x - 0.04f * boxRatio, -y - 0.04f, 1f);
                    textureShaderProgram.setUniform("textureMatrix", modelMatrix);
                    textureMap.get("shotgunLeft").render();
                }
                break;
            case UZI_ID:
                if(player.getShootingDirection() > 0) {
                    modelMatrix = transformation.getModelMatrix(x + 0.045f * boxRatio, -y - 0.065f, 1f);
                    textureShaderProgram.setUniform("textureMatrix", modelMatrix);
                    textureMap.get("uziRight").render();
                }else {
                    modelMatrix = transformation.getModelMatrix(x - 0.01f * boxRatio, -y - 0.065f, 1f);
                    textureShaderProgram.setUniform("textureMatrix", modelMatrix);
                    textureMap.get("uziLeft").render();
                }
                break;
            case MACHINEGUN_ID:
                if(player.getShootingDirection() > 0) {
                    modelMatrix = transformation.getModelMatrix(x + 0.0375f * boxRatio, -y - 0.04f, 1f);
                    textureShaderProgram.setUniform("textureMatrix", modelMatrix);
                    textureMap.get("rifleRight").render();
                }else {
                    modelMatrix = transformation.getModelMatrix(x - 0.0375f * boxRatio, -y - 0.04f, 1f);
                    textureShaderProgram.setUniform("textureMatrix", modelMatrix);
                    textureMap.get("rifleLeft").render();
                }
                break;
            case SNIPER_ID:
                if(player.getShootingDirection() > 0) {
                    modelMatrix = transformation.getModelMatrix(x + 0.05f * boxRatio, -y - 0.02f, 1f);
                    textureShaderProgram.setUniform("textureMatrix", modelMatrix);
                    textureMap.get("sniperRight").render();
                }else {
                    modelMatrix = transformation.getModelMatrix(x - 0.08f * boxRatio, -y - 0.02f, 1f);
                    textureShaderProgram.setUniform("textureMatrix", modelMatrix);
                    textureMap.get("sniperLeft").render();
                }
                break;
        }

    }

    /**
     * render the given grid on the screen
     * @param playerX
     *          x position on map of the current client to get the grid in view
     * @param playerY
     *          y position on map of the current client to get the grid in view
     * @param screenOrigin_x
     *          the x position of the screen with reference to the origin of the map
     * @param screenOrigin_y
     *          the x position of the screen with reference to the origin of the map
     * @param playerOnScaleScreenX
     *          x position on screen of the current client to get the grid in view
     * @param playerOnScaleScreenY
     *          y position on screen of the current client to get the grid in view
     * @param grid
     *          the given grid
     */
    private void renderGrid(float playerX, float playerY, float screenOrigin_x, float screenOrigin_y, float playerOnScaleScreenX, float playerOnScaleScreenY, int[][] grid){

        float playerGrid_x =  playerX / BOX_HEIGHT;
        float playerGrid_y =  playerY / BOX_HEIGHT;

        int boxesOnX = (int) (20 / boxRatio) + 1;
        int boxesOnY = 20;

        int minX = (int) (playerOnScaleScreenX * boxesOnX)/2;
        int maxX = boxesOnX - minX;
        int minY = (int) (playerOnScaleScreenY * boxesOnY)/2;
        int maxY = boxesOnY - minY;

        for(int i = (int) (playerGrid_y - minY - 2); i <= playerGrid_y + maxY + 2; i++){
            for (int j = (int) (playerGrid_x - minX - 2); j <= playerGrid_x + maxX + 2; j++) {

                //Render the item based on the grid value
                //If it is outside of the grid render a platform box

                if (i < 0 || j < 0 || i >= grid.length || j >= grid[0].length) {

                    float boxPixel_x = boxSize * j;
                    float boxPixel_y = boxSize * i;

                    //Get the model matrix
                    Matrix4f matrix = getModel(boxPixel_x, boxPixel_y, screenOrigin_x, screenOrigin_y, 1f);

                    textureShaderProgram.setUniform("textureMatrix", matrix);


                    if (i < 0) {
                        //do nothing
                    } else if (i == grid.length && j >= 0 && j <= grid[0].length - 1) {
                        textureMap.get("platform").render();
                        textureMap.get("platformTop").render();
                    } else if (i > grid.length) {
                        textureMap.get("platform").render();
                    } else if (j - 1 < 0 || j + 1 > grid[0].length) {
                        textureMap.get("platform").render();
                    } else if (grid[i - 1][j] == 1) {
                        textureMap.get("platform").render();
                    } else {
                        textureMap.get("platformTop").render();
                    }
                }

                else if(grid[i][j] == 1) {

                    float boxPixel_x = boxSize * j;
                    float boxPixel_y = boxSize * i;

                    //Get the model matrix
                    Matrix4f matrix = getModel(boxPixel_x, boxPixel_y, screenOrigin_x, screenOrigin_y,  boxRatio);

                    textureShaderProgram.setUniform("textureMatrix", matrix);

                    //Render the mesh for this player

                    textureMap.get("platform").render();

                    if(i - 1 >= 0 && i + 1 < grid.length && j - 1 >= 0 && j + 1 < grid[0].length) {

                        if ((grid[i - 1][j] == 0 || grid[i - 1][j] == 2) && (grid[i + 1][j] == 0 || grid[i + 1][j] == 2) && (grid[i][j - 1] == 0 || grid[i][j - 1] == 3)) {
                            textureMap.get("platformFullLeft").render();
                        }

                        if ((grid[i - 1][j] == 0 || grid[i - 1][j] == 2) && (grid[i + 1][j] == 0 || grid[i + 1][j] == 2) && (grid[i][j + 1] == 0 || grid[i][j + 1] == 3)) {
                            textureMap.get("platformFullRight").render();
                        }

                        if ((grid[i - 1][j] == 0 || grid[i - 1][j] == 2) && (grid[i][j + 1] == 0 || grid[i][j + 1] == 2) && grid[i][j - 1] == 1 && grid[i + 1][j] == 1) {
                            textureMap.get("platformTopRight").render();
                        }

                        if ((grid[i - 1][j] == 0 || grid[i - 1][j] == 2) && (grid[i][j - 1] == 0 || grid[i][j - 1] == 2) && grid[i][j + 1] == 1 && grid[i + 1][j] == 1) {
                            textureMap.get("platformTopLeft").render();
                        }

                        if ((grid[i + 1][j] == 0 || grid[i + 1][j] == 2) && (grid[i][j - 1] == 0 || grid[i][j - 1] == 2) && grid[i][j + 1] == 1 && grid[i - 1][j] == 1) {
                            textureMap.get("platformBottomLeft").render();
                        }

                        if ((grid[i + 1][j] == 0 || grid[i + 1][j] == 2) && (grid[i][j + 1] == 0 || grid[i][j + 1] == 2) && grid[i][j - 1] == 1 && grid[i - 1][j] == 1) {
                            textureMap.get("platformBottomRight").render();
                        }

                        if (grid[i + 1][j] == 1 && (grid[i][j + 1] == 0 || grid[i][j + 1] == 2) && grid[i - 1][j] == 1) {
                            textureMap.get("platformRight").render();
                        }

                        if (grid[i + 1][j] == 1 && (grid[i][j - 1] == 0 || grid[i][j - 1] == 2) && grid[i - 1][j] == 1) {
                            textureMap.get("platformLeft").render();
                        }

                        if ((grid[i - 1][j] == 0 || grid[i - 1][j] == 2) && grid[i][j - 1] == 1 && grid[i][j + 1] == 1) {
                            textureMap.get("platformTop").render();
                        }

                        if ((grid[i + 1][j] == 0 || grid[i + 1][j] == 2) && grid[i][j - 1] == 1 && grid[i][j + 1] == 1) {
                            textureMap.get("platformBottom").render();
                        }

                        if (grid[i - 1][j] == 1 && grid[i][j - 1] == 1) {
                            textureMap.get("platformTopLeftCorner").render();
                        }

                        if (grid[i - 1][j] == 1 && grid[i][j + 1] == 1) {
                            textureMap.get("platformTopRightCorner").render();
                        }

                        if (grid[i + 1][j] == 1 && grid[i][j - 1] == 1) {
                            textureMap.get("platformBottomLeftCorner").render();
                        }

                        if (grid[i + 1][j] == 1 && grid[i][j + 1] == 1) {
                            textureMap.get("platformBottomRightCorner").render();
                        }
                    }else{
                        if(i == 0 && j == 0){
                            //do nothing
                        }else if(i == grid.length && j == 0) {
                            //do nothing

                        }else if(i == 0){
                            //do nothing

                        }else if(j == 0){
                            if (grid[i + 1][j] == 1 && (grid[i][j + 1] == 0 || grid[i][j + 1] == 2)) {
                                textureMap.get("platformRight").render();
                            }

                            if ((grid[i - 1][j] == 0 || grid[i - 1][j] == 2) && grid[i][j + 1] == 1) {
                                textureMap.get("platformTop").render();
                            }

                            if ((grid[i + 1][j] == 0 || grid[i + 1][j] == 2) && grid[i][j + 1] == 1) {
                                textureMap.get("platformBottom").render();
                            }
                        }else if(j == grid[0].length - 1){

                            if (grid[i + 1][j] == 1 && (grid[i][j - 1] == 0 || grid[i][j - 1] == 2) && grid[i - 1][j] == 1) {
                                textureMap.get("platformLeft").render();
                            }

                            if ((grid[i - 1][j] == 0 || grid[i - 1][j] == 2) && grid[i][j - 1] == 1) {
                                textureMap.get("platformTop").render();
                            }

                            if ((grid[i + 1][j] == 0 || grid[i + 1][j] == 2) && grid[i][j - 1] == 1) {
                                textureMap.get("platformBottom").render();
                            }
                        }
                    }
                    /*
                    if(i - 1 < 0 ) {
                        textureMap.get("platformEmpty").render();
                    }else if(grid[i-1][j] == 1) {
                        textureMap.get("platformEmpty").render();
                    }else {
                        textureMap.get("platformTop").render();
                    }*/
                }

            }
        }

    }

    /**
     * render the bullets on the screen
     * @param playerGrid_x
     *      x position on map of the current client to get the grid in view
     * @param playerGrid_y
     *      y position on map of the current client to get the grid in view
     * @param screenOrigin_x
     *      the x position of the screen with reference to the origin of the map
     * @param screenOrigin_y
     *      the y position of the screen with reference to the origin of the map
     * @param players
     *      the given players containing all the bullets
     */
    private void renderBullets(float playerGrid_x, float playerGrid_y, float screenOrigin_x, float screenOrigin_y, ArrayList<Player> players) {
        for(Player player: players){
            for(Bullet bullet: player.getBulletsFired()){
                if((Math.abs(playerGrid_x - bullet.getxPos()/BOX_HEIGHT) <= PLAYER_RANGE || Math.abs(playerGrid_y - bullet.getyPos()/BOX_HEIGHT) <= PLAYER_RANGE)){
                    Matrix4f matrix = getModel(bullet.getxPos()*windowsRatio, (bullet.getyPos() - 2)*windowsRatio, screenOrigin_x, screenOrigin_y,  boxRatio);

                    textureShaderProgram.setUniform("textureMatrix", matrix);

                    //Render the mesh for this bullet
                    textureMap.get("bullet").render();
                }
            }
        }
    }


    /**
     * clean the renderer and all of its resources
     */
    public void cleanup(){
        if (shaderProgram != null){
            shaderProgram.cleanup();
        }

        if(textureShaderProgram != null){
            textureShaderProgram.cleanup();
        }

        for(TextureMesh mesh: textureMap.values()){
            mesh.cleanUp();
        }

        PlayerAnimation.cleanup();
        hudItem.cleanup();


    }

    /**
     * compute the model matrix
     * @param boxPixel_x
     *          x position on map
     * @param boxPixel_y
     *          y position on map
     * @param screenOrigin_x
     *          the x position of the screen with reference to the origin of the map
     * @param screenOrigin_y
     *          the y position of the screen with reference to the origin of the map
     * @param ratio
     *          the given ratio of the item
     * @return
     *          the model matrix
     */
    //Generates a model matrix based on the i and j location of the box
    private Matrix4f getModel(float boxPixel_x, float boxPixel_y, float screenOrigin_x, float screenOrigin_y, float ratio){

        float boxToScreenOrigin_x = boxPixel_x - screenOrigin_x;
        float boxToScreenOrigin_y = boxPixel_y - screenOrigin_y;

        float boxGridToScreen_x =  (boxRatio * boxToScreenOrigin_x) / (boxSize*10);
        float boxGridToScreen_y = boxToScreenOrigin_y / (boxSize*10);

        Matrix4f modelMatrix = transformation.getModelMatrix(
                boxGridToScreen_x,
                -boxGridToScreen_y,
                1.0f
        );

        return modelMatrix;
    }

    /**
     * compute the position of the object
     * @param x
     *          x position on map
     * @param y
     *          y position on map
     * @param screenOrigin_x
     *          the x position of the screen with reference to the origin of the map
     * @param screenOrigin_y
     *          the y position of the screen with reference to the origin of the map
     * @return
     *          points on screen on which the object has to be render
     */
    private Point.Float getModel(float x, float y, float screenOrigin_x, float screenOrigin_y){
        float boxToScreenOrigin_x = x - screenOrigin_x;
        float boxToScreenOrigin_y = y - screenOrigin_y;

        float boxGridToScreen_x =  (boxRatio * boxToScreenOrigin_x) / (boxSize*10);
        float boxGridToScreen_y = boxToScreenOrigin_y / (boxSize*10);

        return (new Point.Float(boxGridToScreen_x, boxGridToScreen_y));
    }

    /**
     * converts the id of the gun to a string
     * @param id
     *          the given id of the gun
     * @return
     *          return the type of the gun as a string
     */
    private String idToString(int id){
        switch(id){
            case AI_WEAPON_ID:
                return "pistol";
            case PISTOL_ID:
                return "pistol";
            case UZI_ID:
                return "uzi";
            case SHOTGUN_ID:
                return "shotgun";
            case MACHINEGUN_ID:
                return "rifle";
            case SNIPER_ID:
                return "sniper";

                default:
                    return "pistol";
        }
    }

    /**
     * get the size of a string on the x coordinate considering its length and size of char
     * @param text
     *          the given text
     * @param size
     *          the char size
     * @return
     *          the width of the text on screen
     */
    private float getTextSize(String text, float size){
        return (text.length() + 1) * size / CHAR_SCALLER;
    }

    /**
     * computes the position of the item which needs to be used such that it would be centered
     * @param x
     *          width of the item
     * @return
     *          the position on screen
     */
    private float getCentered(float x){
        return (2.0f - x * boxRatio) / 2f;
    }
}