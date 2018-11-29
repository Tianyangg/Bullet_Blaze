package main.code.renderer.graphics;


import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * A class that handles simple meshes without texture, just colours
 *
 * @author Matei Vicovan-Hantascu
 */
public class Mesh {

    private int vaoId;
    private int posVboId;
    private int vertexCount;
    private int idxVboId;
    private int colourVboId;


    /**
     * Builds the mesh
     * @param ratioBox
     *          ration between the width and height of the screen
     * @param colours
     *          colours used to render the object
     * @param scale
     *          size of the object
     */
    public Mesh(float ratioBox, float[] colours, float scale){

        float box_size_x = scale * ratioBox;
        float box_size_y = scale;

        float[] positions = new float[]{
                //V0 - left top
                -1f, 1f,
                //V1 - left bottom
                -1f, 1f -  box_size_y,
                //V2 - right bottom
                -1f + box_size_x, 1f - box_size_y,
                //V3 - right top
                -1f + box_size_x, 1f,
        };

        int[] indices = new int[]{
                0, 1, 3, 3, 1, 2
        };

        set(positions, colours, indices);
    }


    /**
     * Builds the mesh
     * @param colours
     *          the colours to render the item with
     * @param xSize
     *          the width of the item
     * @param ySize
     *          the height of the item
     */
    public Mesh(float[] colours, float xSize, float ySize){

        float[] positions = new float[]{
                -1f, 1f,
                -1f, 1f + ySize,
                -1f + xSize, 1f + ySize,
                -1f + xSize, 1f
        };

        int[] indices = new int[]{
                0, 1, 3, 3, 1, 2
        };

        set(positions, colours, indices);
    }


    /**
     * Sets and builds the mesh
     * @param positions
     *          array of position on screen
     * @param colours
     *          array of colours
     * @param indices
     *          array of indices
     */
    public void set(float[] positions, float[] colours, int[] indices){
        FloatBuffer posBuffer = null;
        IntBuffer indicesBuffer = null;
        FloatBuffer colourBuffer = null;
        try{

            vertexCount = indices.length;

            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            //Position VBO
            posVboId = glGenBuffers();
            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, posVboId);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);

            //Colour VBO
            colourVboId = glGenBuffers();
            colourBuffer = MemoryUtil.memAllocFloat(colours.length);
            colourBuffer.put(colours).flip();
            glBindBuffer(GL_ARRAY_BUFFER, colourVboId);
            glBufferData(GL_ARRAY_BUFFER, colourBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

            //Index VBO
            idxVboId = glGenBuffers();
            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            //glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        } finally {
            if(posBuffer != null)
                MemoryUtil.memFree(posBuffer);
            if(indicesBuffer != null)
                MemoryUtil.memFree(indicesBuffer);
            if(indicesBuffer != null)
                MemoryUtil.memFree(colourBuffer);
        }
    }


    /**
     * @return
     *      id of the vertexes array
     */
    public int getVaoId(){
        return vaoId;
    }


    /**
     * @return
     *      number of vertexes
     */
    public int getVertexCount(){
        return vertexCount;
    }


    /**
     *      Render the item
     */
    public void render(){
        // Draw the mesh
        glBindVertexArray(getVaoId());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);

        // Restore state
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);
    }


    /**
     * Clean the buffers
     */
    public void cleanup() {
        glDisableVertexAttribArray(0);

        //Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(posVboId);
        glDeleteBuffers(colourVboId);
        glDeleteBuffers(idxVboId);

        //Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

}
