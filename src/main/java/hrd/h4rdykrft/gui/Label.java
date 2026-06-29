package hrd.h4rdykrft.gui;

import hrd.h4rdykrft.render.Shader;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.system.MemoryStack;
import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBTruetype.stbtt_GetBakedQuad;

public class Label extends UIElement {
    public String text = "";
    public Font font;
    public float[] color = {1.0f, 1.0f, 1.0f, 1.0f};

    private final int vao, vbo;
    private int vertexCount = 0; // Храним количество вершин на GPU

    public Label(Font font, float x, float y) {
        super(x, y, 0, 0);
        this.font = font;

        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        // Настраиваем указатели один раз при инициализации
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);

        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);

        glBindVertexArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void setText(String text) {
        if (text == null) text = "";
        if (this.text.equals(text)) return; // Если текст не изменился, ничего не делаем

        this.text = text;

        if (text.isEmpty()) {
            vertexCount = 0;
            return;
        }

        // Обновляем буфер на GPU только при изменении текста
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer xBuf = stack.floats(0.0f);

            // 24.0f — базовая линия. Убедитесь, что это значение подходит вашему размеру шрифта
            FloatBuffer yBuf = stack.floats(24.0f);
            STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);

            // 6 вершин на символ, 4 float на вершину (X, Y, U, V)
            FloatBuffer verticesBuffer = stack.mallocFloat(text.length() * 6 * 4);

            int charsWritten = 0;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c < 32 || c > 126) continue;

                // ИСПРАВЛЕНИЕ: Передаем false, так как Y-ось вашей матрицы направлена ВНИЗ
                stbtt_GetBakedQuad(font.charData, Font.BITMAP_W, Font.BITMAP_H, c - 32, xBuf, yBuf, q, false);

                // Треугольник 1
                verticesBuffer.put(q.x0()).put(q.y0()).put(q.s0()).put(q.t0());
                verticesBuffer.put(q.x0()).put(q.y1()).put(q.s0()).put(q.t1());
                verticesBuffer.put(q.x1()).put(q.y1()).put(q.s1()).put(q.t1());

                // Треугольник 2
                verticesBuffer.put(q.x0()).put(q.y0()).put(q.s0()).put(q.t0());
                verticesBuffer.put(q.x1()).put(q.y1()).put(q.s1()).put(q.t1());
                verticesBuffer.put(q.x1()).put(q.y0()).put(q.s1()).put(q.t0());

                charsWritten++;
            }

            verticesBuffer.flip();
            vertexCount = charsWritten * 6; // Точное количество вершин

            if (vertexCount > 0) {
                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                // Используем GL_STATIC_DRAW, так как данные внутри буфера меняются редко
                glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
                glBindBuffer(GL_ARRAY_BUFFER, 0);
            }
        }
    }

    @Override
    public void render(Shader shader) {
        // Если объект невидим, текста нет или он не отрендерился в буфер — выходим
        if (!visible || vertexCount == 0) return;

        shader.setUniform("offset", x, y);
        shader.setUniform("color", color[0], color[1], color[2], color[3]);
        shader.setUniform("renderMode", 1);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, font.textureId);
        shader.setUniform("uiTexture", 0);

        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount); // Отрисовка за 1 дешевый вызов
        glBindVertexArray(0);

        glBindTexture(GL_TEXTURE_2D, 0);
        shader.setUniform("renderMode", 0);
    }

    // Не забывайте освобождать буферы при удалении UI элемента/сцены!
    public void cleanup() {
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }
}