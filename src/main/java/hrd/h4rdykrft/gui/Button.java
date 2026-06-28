package hrd.h4rdykrft.gui;

import hrd.h4rdykrft.render.Shader;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL33.*;

public class Button extends UIElement {
    private int vao, vbo;

    // Массивы цветов для разных состояний (RGBA)
    public float[] normalColor = {0.6f, 0.6f, 0.6f, 1.0f};
    public float[] hoverColor  = {0.4f, 0.4f, 0.4f, 1.0f}; // Темнее при наведении
    public float[] pressColor  = {0.2f, 0.2f, 0.2f, 1.0f}; // Еще темнее при нажатии

    private boolean isHovered = false;
    private boolean isPressed = false;

    public Label label;
    public Runnable onClick;

    public Button(float x, float y, float width, float height, Font font, String text, Runnable onClick) {
        super(x,y,width,height);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.onClick = onClick;

        // Встроенный текст
        this.label = new Label(font, x, y);
        this.label.setText(text);
        this.label.color = new float[]{1.0f, 1.0f, 1.0f, 1.0f};

        initGL();
    }

    private void initGL() {
        // Вершины для отрисовки фона кнопки (как у Panel)
        float[] vertices = {
                0.0f, height, 0.0f, 1.0f,
                width, height, 1.0f, 1.0f,
                0.0f, 0.0f,   0.0f, 0.0f,
                width, 0.0f,   1.0f, 0.0f
        };

        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    // Метод проверки взаимодействия с мышью
    public void handleInput(float mouseX, float mouseY, boolean mousePressed) {
        if (!visible) return;

        // Проверяем, находится ли курсор над кнопкой
        isHovered = (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height);

        if (isHovered) {
            if (mousePressed) {
                isPressed = true;
            } else if (isPressed) {
                // Если мышь отпустили, находясь над кнопкой — засчитываем клик
                isPressed = false;
                if (onClick != null) {
                    onClick.run();
                }
            }
        } else {
            // Сбрасываем состояние, если курсор ушел с кнопки
            isPressed = false;
        }
    }

    public void render(Shader shader) {
        if (!visible) return;

        // Динамически выбираем цвет в зависимости от состояния
        float[] currentColor = isPressed ? pressColor : (isHovered ? hoverColor : normalColor);

        // Передаем цвет в шейдер
        shader.setUniform("color", currentColor[0], currentColor[1], currentColor[2], currentColor[3]);
        shader.setUniform("renderMode", 0); // Отключаем текстурирование текста для фона
        shader.setUniform("offset", x, y);

        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        glBindVertexArray(0);
        // Отрисовываем текст поверх кнопки
        if (label != null && label.font != null) {
            // Приблизительное центрирование текста (зависит от размера шрифта)
            label.x = this.x + (this.width / 2.0f) - (label.font.getStringSize(label.text).width() / 2);
            label.y = this.y + (this.height / 2.0f) - label.font.getStringSize(label.text).height() / 2 +2;
            label.render(shader);
        }
    }
}