package hrd.h4rdykrft.render;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL33.*;

public class Shader {
    private final int programId;

    public Shader(String vertexPath, String fragmentPath) {
        String vertexCode = readFile(vertexPath);
        String fragmentCode = readFile(fragmentPath);

        int vertexShader = compileShader(GL_VERTEX_SHADER, vertexCode);
        int fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentCode);

        programId = glCreateProgram();
        glAttachShader(programId, vertexShader);
        glAttachShader(programId, fragmentShader);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw new RuntimeException("Shader linking failed: " + glGetProgramInfoLog(programId));
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void setUniform(String name, Matrix4f value) {
        int location = glGetUniformLocation(programId, name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            value.get(buffer);
            glUniformMatrix4fv(location, false, buffer);
        }
    }
    public void setUniform(String name, int value) {
        int location = glGetUniformLocation(programId, name);
        glUniform1i(location, value);
    }
    // Метод для установки vec2 (используется для offset)
    public void setUniform(String name, float x, float y) {
        glUniform2f(glGetUniformLocation(programId, name), x, y);
    }

    // Метод для установки vec4 (используется для uColor)
    public void setUniform(String name, float x, float y, float z, float w) {
        glUniform4f(glGetUniformLocation(programId, name), x, y, z, w);
    }


    private int compileShader(int type, String source) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        int success = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            System.err.println("Ошибка компиляции шейдера: " + glGetShaderInfoLog(shader));
        }
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException("Shader compilation failed: " + glGetShaderInfoLog(shader));
        }
        return shader;
    }

    private String readFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read shader file: " + path, e);
        }
    }
}