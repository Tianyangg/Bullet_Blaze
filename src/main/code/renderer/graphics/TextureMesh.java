package main.code.renderer.graphics;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * A class that creates and handles the mesh for a given texture
 *
 * @author Matei Vicovan-Hantascu
 */
public class TextureMesh {

    private int vaoId;
    private int posVboId;
    private int textureVboId;
    private int indexVboId;
    private int vertexCount;
    private Texture texture;


    /**
     * Build the mesh
     * @param boxRatio
     *          the ratio between width and height of screen
     * @param textureX
     *          x position of texture on sprite
     * @param textureY
     *          y position of texture on sprite
     * @param textureXSize
     *          width of the texture on sprite
     * @param textureYSize
     *          height of the texture on sprite
     * @param texture
     *          the given sprite
     */
    public TextureMesh(float boxRatio, float textureX, float textureY, float textureXSize, float textureYSize, Texture texture){
        float box_size_x = 0.1f * boxRatio;
        float box_size_y = 0.1f;

        float[] positions = new float[]{
                //V0 - left top
                -1, 1,
                //V1 - left bottom
                -1, 1 -  box_size_y,
                //V2 - right bottom
                -1 + box_size_x, 1 - box_size_y,
                //V3 - right top
                -1 + box_size_x, 1,
        };
        float[] textureCoords = new float[]{
                textureX, textureY,
                textureX, textureY + textureYSize,
                textureX + textureXSize, textureY + textureYSize,
                textureX + textureXSize, textureY
        };

        int[] indices = new int[]{
                0, 1, 3, 3, 1, 2
        };

        set(positions, textureCoords, indices, texture);
    }

    /**
     * Builds the texture
     * @param x
     *          x position on screen
     * @param y
     *          y position on screen
     * @param textureX
     *          x position of texture on sprite
     * @param textureY
     *          y position of texture on sprite
     * @param textureXSize
     *          width of the texture on sprite
     * @param textureYSize
     *          height of the texture on sprite
     * @param texture
     *          the given sprite
     */
    public TextureMesh(float x, float y, float textureX, float textureY, float textureXSize, float textureYSize, Texture texture){

        float[] positions = new float[]{
                -1, 1,
                -1, 1 - y,
                -1 + x, 1 - y,
                -1 + x, 1
        };

        float[] textureCoords = new float[]{
                textureX, textureY,
                textureX, textureY + textureYSize,
                textureX + textureXSize, textureY + textureYSize,
                textureX + textureXSize, textureY
        };

        int[] indices = new int[]{
                0, 1, 3, 3, 1, 2
        };

        set(positions, textureCoords, indices, texture);
    }

    /**
     * Builds the texture
     * @param pos
     *          array of positions on screen
     * @param textCoords
     *          array of position on sprite
     * @param indices
     *          array of indices
     * @param texture
     *          the given sprite
     */
    public TextureMesh(float[] pos, float[] textCoords, int[] indices, Texture texture){
        set(pos, textCoords, indices, texture);
    }

    /**
     * Sets and builds the mesh
     * @param positions
     *          array of positions on screen
     * @param textureCoords
     *          array of position on sprite
     * @param indices
     *          array of indices
     * @param texture
     *          the given sprite
     */
    private void set(float[] positions, float[] textureCoords, int[] indices, Texture texture) {
        FloatBuffer posBuffer = null;
        FloatBuffer textCoordsBuffer = null;
        IntBuffer indicesBuffer = null;

        try {

            this.texture = texture;

            vertexCount = indices.length;

            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            // Position VBO
            posVboId = glGenBuffers();
            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, posVboId);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);

            // Texture coordinates VBO
            textureVboId = glGenBuffers();
            textCoordsBuffer = MemoryUtil.memAllocFloat(textureCoords.length);
            textCoordsBuffer.put(textureCoords).flip();
            glBindBuffer(GL_ARRAY_BUFFER, textureVboId);
            glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

            // Index VBO
            indexVboId = glGenBuffers();
            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexVboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            glBindVertexArray(0);

        } finally {
            if (posBuffer != null) {
                MemoryUtil.memFree(posBuffer);
            }
            if (textCoordsBuffer != null) {
                MemoryUtil.memFree(textCoordsBuffer);
            }
            if (indicesBuffer != null) {
                MemoryUtil.memFree(indicesBuffer);
            }

        }
    }


    /**
     * @return
     *          id of the vertex array
     */
    public int getVaoId() {
        return vaoId;
    }


    /**
     * @return
     *          number of vertexes
     */
    public int getVertexCount() {
        return vertexCount;
    }


    /**
     * Render the texture on screen
     */
    public void render() {

        // Activate firs texture bank
        glActiveTexture(GL_TEXTURE0);

        // Bind the texture
        glBindTexture(GL_TEXTURE_2D, texture.getId());

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
    public void cleanUp() {
        glDisableVertexAttribArray(0);

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(posVboId);
        glDeleteBuffers(textureVboId);
        glDeleteBuffers(indexVboId);

        // Delete the texture
        texture.delete();

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    /**
     * Dispose the buffers
     */
    public void deleteBuffers() {

        glDisableVertexAttribArray(0);
        // Delete the VBOs

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glDeleteBuffers(posVboId);
        glDeleteBuffers(textureVboId);
        glDeleteBuffers(indexVboId);

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);

    }
}
