package main.code.renderer.graphics;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

/**
 * Class that creates and handles a shader program
 *
 * @author Matei Vicovan-Hantascu
 */
public class ShaderProgram {

    private final int programId;
    private int vertexShaderId;
    private int fragmentShaderId;
    private final Map<String, Integer> uniforms;


    /**
     * Creates a shader program object
     * @throws Exception
     */
    public ShaderProgram() throws Exception{
        programId = glCreateProgram();
        if(programId == 0)
            throw new   Exception("Could not create Shader");
        uniforms = new HashMap<>();
    }


    /**
     * Creates a vertex shader program
     * @param shaderCode
     *          the given shader code used to create
     * @throws Exception
     */
    public void createVertexShader(String shaderCode) throws Exception{
        vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
    }


    /**
     * Creates a fragment shader program
     * @param shaderCode
     *          the given shader code used to create
     * @throws Exception
     */
    public void createFragmentShader(String shaderCode) throws Exception{
        fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
    }


    /**
     * Creates a shader
     * @param shaderCode
     *          the shader code
     * @param shaderType
     *          the shader type
     * @return
     * @throws Exception
     */
    protected int createShader(String shaderCode, int shaderType)throws Exception{
        int shaderId = glCreateShader(shaderType);

        if (shaderId == 0)
            throw new Exception("Error creating shader. Type: " + shaderType);

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if(glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0)
            throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));

        glAttachShader(programId, shaderId);
        return shaderId;
    }


    /**
     * Create a uniform
     * @param uniformName
     *          name of the uniform
     * @throws Exception
     */
    //Create uniforms:
    public void createUniform(String uniformName) throws Exception {
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if (uniformLocation < 0) {
            throw new Exception("Could not find uniform:" + uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }

    /**
     * Sets the uniform
     * @param uniformName
     *          name of the uniform
     * @param value
     *          value of the uniform
     */
    public void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Dump the matrix into a float buffer
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
        }
    }


    /**
     * Sets the uniform
     * @param uniformName
     *          name of the uniform
     * @param value
     *          value of the uniform
     */
    public void setUniform(String uniformName, int value){
        glUniform1i(uniforms.get(uniformName), value);
    }


    /**
     * Sets a uniform used for colours
     * @param uniformName
     *          name of the uniform
     * @param r
     *          red value of the color
     * @param g
     *          green value of the color
     * @param b
     *          blue value of the color
     * @param o
     *          opacity of the item
     */
    public void setUniform(String uniformName, int r, int g, int b, int o){
        glUniform4f(uniforms.get(uniformName), r, g, b, o);
    }


    /**
     * Links the shader to the program
     * @throws Exception
     */
    public void link() throws Exception{
        glLinkProgram(programId);
        if(glGetProgrami(programId, GL_LINK_STATUS) == 0){
            throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(programId,1024));
        }

        if(vertexShaderId != 0){
            glDetachShader(programId, vertexShaderId);
        }

        if(fragmentShaderId != 0){
            glDetachShader(programId, fragmentShaderId);
        }

        glValidateProgram(programId);
        if(glGetProgrami(programId, GL_VALIDATE_STATUS) == 0){
            System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
        }
    }


    /**
     * Binds openGL to the program
     */
    public void bind(){
        glUseProgram(programId);
    }


    /**
     * Unbinds openGL from the program
     */
    public void unbind(){
        glUseProgram(0);
    }


    /**
     * @return
     *          id of the program
     */
    public int getProgramId(){
        return programId;
    }


    /**
     * Dispose the program
     */
    public void cleanup(){
        unbind();
        if(programId != 0){
            glDeleteProgram(programId);
        }
    }


}
