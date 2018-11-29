package main.code.renderer.graphics;

import org.lwjgl.system.CallbackI;

import java.util.HashMap;

import static main.code.utils.GameConstants.SIMPLE_PLAYER;
import static main.code.utils.GameConstants.TEXTURE_PATH;


/**
 * A class that handles the rendering of the player and its animation
 *
 * @author Matei Vicovan-Hantascu
 */
public class PlayerAnimation {
    private final int type;
    private int lastMovementState;
    private int animationIncr;
    private int maxAnimationIncr = 15;
    private static HashMap<String, TextureMesh> textureMap = new HashMap<>();
    private float boxRatio;


    /**
     * Builds the textures for the player
     * @param type
     *          type of character
     * @param boxRatio
     *          ratio between height and width of the screen
     */
    public PlayerAnimation(int type, float boxRatio){
        this.boxRatio = boxRatio;
        this.type = type;
        lastMovementState = 0;
        animationIncr = 0;
    }


    /**
     * A method to initialise all the textures for the given ratio
     * @param boxRatio
     *          ratio between height and width of the screen
     */
    public static void init(float boxRatio) {

        Texture texture;
        TextureMesh mesh;

        float i = 0.125f;
        float j = 0.5f;

        for(int k = 1; k <= 5; k++) {
            String textureType = getType(k);

            texture = new Texture(TEXTURE_PATH + "poses/" + textureType + "_poses.png");

            mesh = new TextureMesh(0.2f * boxRatio, 0.2f, i * 0f, j * 0f, i, j, texture);
            textureMap.put(textureType + "StandRight", mesh);

            mesh = new TextureMesh(0.2f * boxRatio, 0.2f, i * 1f, j * 0f, i, j, texture);
            textureMap.put(textureType + "StandLeft", mesh);

            mesh = new TextureMesh(0.2f * boxRatio, 0.2f, i * 2f, j * 0f, i, j, texture);
            textureMap.put(textureType + "WalkRight1", mesh);

            mesh = new TextureMesh(0.2f * boxRatio, 0.2f, i * 3f, j * 0f, i, j, texture);
            textureMap.put(textureType + "WalkLeft1", mesh);

            mesh = new TextureMesh(0.2f * boxRatio, 0.2f, i * 4f, j * 0f, i, j, texture);
            textureMap.put(textureType + "WalkRight2", mesh);

            mesh = new TextureMesh(0.2f * boxRatio, 0.2f, i * 5f, j * 0f, i, j, texture);
            textureMap.put(textureType + "WalkLeft2", mesh);

            mesh = new TextureMesh(0.2f * boxRatio, 0.2f, i * 6f, j * 0f, i, j, texture);
            textureMap.put(textureType + "JumpRight", mesh);

            mesh = new TextureMesh(0.2f * boxRatio, 0.2f, i * 7f, j * 0f, i, j, texture);
            textureMap.put(textureType + "JumpLeft", mesh);

            mesh = new TextureMesh(0.2f * boxRatio, 0.2f, i * 0f, j * 1f, i, j, texture);
            textureMap.put(textureType + "FallRight", mesh);

            mesh = new TextureMesh(0.2f * boxRatio, 0.2f, i * 1f, j * 1f, i, j, texture);
            textureMap.put(textureType + "FallLeft", mesh);

            mesh = new TextureMesh(0.2f * boxRatio, 0.2f, i * 2f, j * 1f, i, j, texture);
            textureMap.put(textureType + "RightLeftArm", mesh);

            mesh = new TextureMesh(0.2f * boxRatio, 0.2f, i * 3f, j * 1f, i, j, texture);
            textureMap.put(textureType + "RightRightArm", mesh);

            mesh = new TextureMesh(0.2f * boxRatio, 0.2f, i * 4f, j * 1f, i, j, texture);
            textureMap.put(textureType + "LeftLeftArm", mesh);

            mesh = new TextureMesh(0.2f * boxRatio, 0.2f, i * 5f, j * 1f, i, j, texture);
            textureMap.put(textureType + "LeftRightArm", mesh);

            mesh = new TextureMesh(0.2f * boxRatio, 0.2f, i * 6f, j * 1f, i, j, texture);
            textureMap.put(textureType + "RightHand", mesh);

            mesh = new TextureMesh(0.2f * boxRatio, 0.2f, i * 7f, j * 1f, i, j, texture);
            textureMap.put(textureType + "LeftHand", mesh);

        }
    }


    /**
     * Render the body of the player
     * @param state
     *          state of movement (standing/walking/jumping/falling)
     * @param orientation
     *          orientation of the player (left/right)
     */
    public void renderBody(int state, int orientation){
        String movement = getType(type);
        switch(state){
            case 0:
                movement += "Stand";

                this.lastMovementState = 0;
                this.animationIncr = 0;

                if(orientation < 0)
                    movement += "Left";
                else
                    movement += "Right";

                textureMap.get(movement).render();
                break;

            case 1:
                movement += "Walk";

                if(this.lastMovementState != 1) {
                    this.lastMovementState = 1;
                    this.animationIncr = 0;
                }

                if(orientation < 0)
                    movement += "Left";
                else
                    movement += "Right";

                if(this.animationIncr <= this.maxAnimationIncr/2)
                    movement += "1";
                else
                    movement += "2";

                this.animationIncr++;
                this.animationIncr = this.animationIncr % this.maxAnimationIncr;
                textureMap.get(movement).render();
                break;

            case 2:
                movement += "Jump";
                this.lastMovementState = 2;
                this.animationIncr = 0;

                if(orientation < 0)
                    movement += "Left";
                else
                    movement += "Right";

                textureMap.get(movement).render();
                break;

            case 3:
                movement += "Fall";
                this.lastMovementState = 3;
                this.animationIncr = 0;

                if(orientation < 0)
                    movement += "Left";
                else
                    movement += "Right";

                textureMap.get(movement).render();
                break;

        }
    }


    /**
     * Render the right arm of the player
     * @param orientation
     *          orientation of the player (right/left)
     */
    public void renderRightArm(int orientation){
        String texture = getType(type);
        if(orientation < 0) {
            texture += "LeftLeftArm";
        }else{
            texture += "RightRightArm";
        }

        textureMap.get(texture).render();
    }


    /**
     * Render the left arm of the player
     * @param orientation
     *          orientation of the player (right/left)
     */
    public void renderLeftArm(int orientation){
        String texture = getType(type);
        if(orientation < 0) {
            texture += "LeftRightArm";
        }else{
            texture += "RightLeftArm";
        }

        textureMap.get(texture).render();
    }


    /**
     * Render hand of the player
     * @param orientation
     *          orientation of the player (right/left)
     */
    public void renderHand(int orientation){
        String texture = getType(type);
        if(orientation < 0){
            texture += "LeftHand";
        }else{
            texture += "RightHand";
        }

        textureMap.get(texture).render();
    }


    /**
     * Changes the int type to a string type
     * @param type
     *          the type of the character
     * @return
     *          the type of the character as a string
     */
    private static String getType(int type){
        if(type < 5 && type > 0) {
            return "player" + type;
        }else{
            return "zombie";
        }
    }

    public static void cleanup(){
        for(TextureMesh mesh: textureMap.values()){
            mesh.cleanUp();
        }
    }
}
