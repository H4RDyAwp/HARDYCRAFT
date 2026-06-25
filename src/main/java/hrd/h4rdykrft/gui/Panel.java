package hrd.h4rdykrft.gui;

import hrd.h4rdykrft.render.Shader;
import org.lwjgl.opengl.GL33;
import static org.lwjgl.opengl.GL33.*;

public class Panel extends UIElement {
    private int vao, vbo;
    public float[] color = {1.0f, 0.5f, 1.0f, 1.0f}; // RGBA

    public Panel(float x, float y, float width, float height) {
        super(x, y, width, height);
        setupMesh();
    }

    private void setupMesh() {
        float[] vertices = {
                0, 0,
                width, 0,
                width, height,
                0, height
        };

        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    @Override
    public void render(Shader shader) {
        if (!visible) return;
        shader.setUniform("offset", x, y);

        // КРИТИЧЕСКОЕ ИСПРАВЛЕНИЕ: Используем "color" и отключаем текстовый режим "isText"
        shader.setUniform("color", color[0], color[1], color[2], color[3]);
        shader.setUniform("isText", 0);

        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
        glBindVertexArray(0);
    }
}