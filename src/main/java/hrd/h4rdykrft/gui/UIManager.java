package hrd.h4rdykrft.gui;

import hrd.h4rdykrft.render.Shader;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.opengl.GL33.*;

public class UIManager {
    private final List<UIElement> elements = new ArrayList<>();
    private final Shader uiShader;
    private final Matrix4f projection = new Matrix4f();

    public UIManager(Shader shader) {
        this.uiShader = shader;
    }

    public void addElement(UIElement element) {
        elements.add(element);
    }
    public void handleInput(float mouseX, float mouseY, boolean isPressed) {
        for (UIElement element : elements) {
            if (element instanceof Button) {
                ((Button) element).handleInput(mouseX, mouseY, isPressed);
            }
        }
    }
    public void render(int windowWidth, int windowHeight) {
        // Настройка ортографической проекции
        projection.setOrtho(0, windowWidth, windowHeight, 0, -1, 1);

        uiShader.bind();
        uiShader.setUniform("projection", projection);

        // --- ОТКЛЮЧАЕМ НАСТРОЙКИ 3D ---
        glDisable(GL_DEPTH_TEST); // Чтобы UI был поверх всего
        glDisable(GL_CULL_FACE);  // ИСПРАВЛЕНИЕ: Отключаем отсечение граней для 2D

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Отрисовка всех элементов
        for (UIElement element : elements) {
            element.render(uiShader);
        }

        // --- ВОЗВРАЩАЕМ НАСТРОЙКИ 3D ---
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);   // ИСПРАВЛЕНИЕ: Возвращаем отсечение для мира
    }
}