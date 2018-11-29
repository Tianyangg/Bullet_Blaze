package main.code.renderer.graphics;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;


/**
 * Handles the use of text
 * Builds the meshes and renders them
 *
 *@author Matei Vicovan-Hantascu
 */
public class TextItem {

    private static final int VERTICES_PER_QUAD = 4;

    private final Vector2f pos;
    private final float boxRatio;

    private float charScaller;
    private Vector3f colour;
    private String text;
    private TextureMesh mesh;
    private Texture texture;

    private float width;


    /**
     * Generates a text handler
     * @param text
     *          given text
     * @param texture
     *          the sprite with characters
     * @param boxRatio
     *          ratio between screen width and height
     * @param charScaller
     *          size of the character
     * @throws Exception
     */
    public TextItem(String text, Texture texture, float boxRatio, float charScaller) throws Exception{
        this.pos = new Vector2f(0f, 0f);
        this.colour = new Vector3f(1f, 1f, 1f);
        this.texture = texture;
        this.boxRatio = boxRatio;
        this.text = text;
        this.charScaller = charScaller;
        this.mesh = buildMesh(0f);
    }


    /**
     * Builds the mesh for the current text
     * @param size
     *          size of the character
     * @return
     *          mesh of current text
     */
    private TextureMesh buildMesh(float size){
        List<Float> positions = new ArrayList();
        List<Float> textCoords = new ArrayList();
        List<Integer> indices = new ArrayList();
        char[] characters = text.toCharArray();
        int numChars = characters.length;

        float startx = 0;
        for(int i = 0; i < numChars; i++){
            //Build a character tile composed by two triangles

            //Compute the position of the character from the sprite
            //Characters must be on ASCII order
            int pos = (characters[i] - 32);
            float pos_y = (pos / 16)/8f;
            float pos_x = (pos % 16)/16f;

            //Left Top vertex
            positions.add(-1f + startx * boxRatio); //x
            positions.add(1f);   //y
            textCoords.add(pos_x);
            textCoords.add(pos_y);
            indices.add(i*VERTICES_PER_QUAD);


            //Left Bottom vertex
            positions.add(-1f + startx * boxRatio); //x
            positions.add(1f - size);
            textCoords.add(pos_x);
            textCoords.add(pos_y + 1f/8f);
            indices.add(i*VERTICES_PER_QUAD + 1);

            //Right Bottom vertex
            positions.add(-1f + (startx + size) * boxRatio); //x
            positions.add(1f - size); //y
            textCoords.add(pos_x + 1f/16f);
            textCoords.add(pos_y + 1f/8f);
            indices.add(i*VERTICES_PER_QUAD + 2);

            //Right Top vertex
            positions.add(-1f + (startx + size) * boxRatio);
            positions.add(1f);
            textCoords.add(pos_x + 1f/16f);
            textCoords.add(pos_y);
            indices.add(i*VERTICES_PER_QUAD + 3);

            //Add indices for left top and bot right vertices
            indices.add(i*VERTICES_PER_QUAD );
            indices.add(i*VERTICES_PER_QUAD + 2);

            startx += size / charScaller;

        }

        this.width = startx;

        float[] posArr = new float[positions.size()];
        for(int i = 0; i < positions.size(); i ++){
            posArr[i] = positions.get(i);
        }

        float[] textCoordsArr = new float[textCoords.size()];
        for(int i = 0; i < textCoords.size(); i ++){
            textCoordsArr[i] = textCoords.get(i);
        }

        int[] indicesArr = new int[indices.size()];
        for(int i = 0; i < indices.size(); i ++){
            indicesArr[i] = indices.get(i);
        }

        TextureMesh newMesh = new TextureMesh(posArr, textCoordsArr, indicesArr, texture);
        return newMesh;

    }


    /**
     * @return
     *          the current text to be rendered
     */
    public String getText(){
        return this.text;
    }


    /**
     * @return
     *          the mesh used to render
     */
    public TextureMesh getMesh(){
        return this.mesh;
    }


    /**
     * @return
     *          width of the mesh
     */
    public float getWidth(){
        return width;
    }


    /**
     * Changes the current mesh
     * @param mesh
     *          the given mesh
     */
    public void setMesh(TextureMesh mesh){
        this.mesh = mesh;
    }


    /**
     * Replaces the text and builds a new mesh
     * @param text
     *          the given text
     * @param size
     *          the size of a character
     */
    public void setText(String text, float size) {
        this.text = text;
        mesh.deleteBuffers();
        this.getMesh().deleteBuffers();
        this.setMesh(buildMesh(size));
    }

    public void cleanup(){
        mesh.cleanUp();
    }
}
