package main.code.renderer.graphics;

import org.joml.Matrix4f;

/**
 * A class used to compute transformation matrices for rendering on screen
 *
 * @author Matei Vicovan-Hantascu
 */
public class Transformation {
    private final Matrix4f projectionMatrix;
    private final Matrix4f modelMatrix;
    private final Matrix4f modelProjectioon;

    /**
     *Create the transformation
     */
    public Transformation(){
        projectionMatrix = new Matrix4f();
        modelMatrix = new Matrix4f();
        modelProjectioon = new Matrix4f();
    }


    /**
     * Compute the model matrix
     * @param x
     *          position in x coordinate
     * @param y
     *          position on y coordinate
     * @param scale
     *          scaler
     * @return
     *          matrix to transpose the texture on screen
     */
    public Matrix4f getModelMatrix(float x, float y, float scale){
        modelMatrix.identity();
        modelMatrix.translate(x, y, 0).
                rotateX((float)Math.toRadians(0)).
                rotateY((float)Math.toRadians(0)).
                rotateZ((float)Math.toRadians(0)).
                scale(scale);
        return modelMatrix;
    }
}
