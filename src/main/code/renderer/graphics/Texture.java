package main.code.renderer.graphics;

import de.matthiasmann.twl.utils.PNGDecoder;
import org.lwjgl.system.MemoryStack;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

/**
 * A class that handles textures
 *
 * @author Matei Vicovan-Hantascu
 */
public class Texture {

    private int textureID;
    private int width;
    private int height;

    /**
     * Creates the texture using the given path
     * @param path
     *          the path to the file containing the texture
     */
    public Texture(String path) {

        ByteBuffer image;
        int width, height;

        try (MemoryStack stack = MemoryStack.stackPush()) {

            /* Prepare image buffers */
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            /* Load image */
            image = stbi_load(path, w, h, comp, 4);

            if (image == null) {
                throw new RuntimeException("Failed to load a texture file!"
                        + System.lineSeparator() + stbi_failure_reason());
            }

            /* Get width and height of image */
            width = w.get();
            height = h.get();

        }
        this.width = width;
        this.height = height;
        this.textureID = glGenTextures();

        createTexture(image);

    }

    /**
     * Creates the texture using an input stream
     * @param is
     *          the input stream used to get the texture data
     * @throws Exception
     */
    public Texture(InputStream is) throws Exception{
        PNGDecoder decoder = new PNGDecoder(is);
        this.width = decoder.getWidth();
        this.height = decoder.getHeight();

        //Load texture contents into a byte buffer
        ByteBuffer buf = ByteBuffer.allocate(4 * decoder.getWidth() * decoder.getHeight());
        decoder.decode(buf, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
        buf.flip();

        //Create a new OpenGL texture
        this.textureID = glGenTextures();
        createTexture(buf);
    }

    /**
     * Build the texture
     * @param buffer
     *          buffer containing texture data
     */
    private void createTexture(ByteBuffer buffer) {
        // Bind the texture
        glBindTexture(GL_TEXTURE_2D, textureID);

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // Upload the texture data
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        // Generate Mipmap
        glGenerateMipmap(GL_TEXTURE_2D);
    }

    /**
     * Deletes the texture
     */
    public void delete(){
        glDeleteTextures(textureID);
    }

    /**
     * @return
     *          id of the texture
     */
    public int getId(){
        return textureID;
    }

    /**
     * @return
     *      the height of the texture
     */
    public int getHeight(){
        return height;
    }


    /**
     * @return
     *      the width of the texture
     */
    public int getWidth(){
        return width;
    }

}
