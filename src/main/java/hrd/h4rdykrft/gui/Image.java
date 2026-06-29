package hrd.h4rdykrft.gui;

import hrd.h4rdykrft.render.Shader;
import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.opengl.GL33.*;

public class Image extends UIElement {
    private int vao, vbo;
    private int textureId;

    public Image(float x, float y, float width, float height, String texturePath) {
        super(x, y, width, height);
        setupMesh();
        loadTexture(texturePath);
    }

    private void setupMesh() {
        // 2D Coordinates (X, Y) followed by Texture Coordinates (U, V)
        float[] vertices = {
                // X,   Y,      U,   V
                0,   0,      0.0f, 0.0f, // Top-Left
                width,   0,      1.0f, 0.0f, // Top-Right
                width, height,   1.0f, 1.0f, // Bottom-Right
                0, height,   0.0f, 1.0f  // Bottom-Left
        };

        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        int stride = 4 * Float.BYTES;

        // Attribute 0: Position (X, Y)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);

        // Attribute 1: Texture Coordinates (U, V)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void loadTexture(String path) {
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);

        // Set texture parameters for wrapping and filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // Flip image vertically on load if your OpenGL coordinates expect 0,0 at bottom-left

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            ByteBuffer data = STBImage.stbi_load(path, width, height, channels, 4);
            if (data != null) {
                // Upload image data to OpenGL
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
                glGenerateMipmap(GL_TEXTURE_2D);
                STBImage.stbi_image_free(data);
            } else {
                throw new RuntimeException("Failed to load texture file: " + path + " -> " + STBImage.stbi_failure_reason());
            }
        }
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    @Override
    public void render(Shader shader) {
        if (!visible) return;

        shader.setUniform("offset", x, y);

        // Включаем режим текстуры (2)
        shader.setUniform("renderMode", 2);

        // Передаем белый цвет, чтобы текстура отображалась без цветовых искажений
        shader.setUniform("color", 1.0f, 1.0f, 1.0f, 1.0f);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
        // Если ваш класс Shader не привязывает сэмплер автоматически, раскомментируйте:
        // shader.setUniform("uiTexture", 0);

        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }
}
