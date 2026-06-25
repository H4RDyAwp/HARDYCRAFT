package hrd.h4rdykrft.player;

import org.joml.Vector3f;
import static org.lwjgl.glfw.GLFW.*;

public class Player {
    // Сетевые идентификаторы
    private final String uuid;
    private final String username;

    // Трансформ игрока
    private final Vector3f position;
    private float pitch;
    private float yaw;

    // Настройки физики/движения
    private final float speed = 0.15f;
    private final float sensitivity = 0.15f;

    // Состояние мыши (индивидуальное для каждого локального игрока)
    private double lastMouseX = 0, lastMouseY = 0;
    private boolean firstMouse = true;

    public Player(String uuid, String username, Vector3f startPosition) {
        this.uuid = uuid;
        this.username = username;
        this.position = new Vector3f(startPosition);
        this.pitch = 15f;
        this.yaw = -90f;
    }

    /**
     * Обработка нажатий клавиш.
     * В будущем этот метод будет вызываться ТОЛЬКО для локального игрока (тебя).
     * Для удаленных игроков позиция будет перезаписываться координатами из сетевых пакетов.
     */
    public void handleKeyboard(long window) {
        float cosYaw = (float) Math.cos(Math.toRadians(yaw));
        float sinYaw = (float) Math.sin(Math.toRadians(yaw));

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) { position.x += cosYaw * speed; position.z += sinYaw * speed; }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) { position.x -= cosYaw * speed; position.z -= sinYaw * speed; }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) { position.x -= sinYaw * speed; position.z += cosYaw * speed; }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) { position.x += sinYaw * speed; position.z -= cosYaw * speed; }
        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) position.y += speed;
        if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) position.y -= speed;
    }

    /**
     * Обработка движения мыши (поворот головы).
     */
    public void handleMouse(double mouseX, double mouseY) {
        if (firstMouse) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            firstMouse = false;
        }

        double offsetX = mouseX - lastMouseX;
        double offsetY = lastMouseY - mouseY; // Инвертировано, так как Y-координаты экрана идут сверху вниз

        lastMouseX = mouseX;
        lastMouseY = mouseY;

        yaw += (float) (offsetX * sensitivity);
        pitch += (float) (offsetY * sensitivity);

        // Ограничиваем угол обзора вверх/вниз, чтобы не сломать шею
        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;
    }

    /**
     * Возвращает нормализованный вектор направления взгляда игрока.
     */
    public Vector3f getDirection() {
        Vector3f direction = new Vector3f();
        direction.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        direction.y = (float) Math.sin(Math.toRadians(pitch));
        direction.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        return direction.normalize();
    }

    // Геттеры и сеттеры (критически важны для синхронизации по сети)
    public Vector3f getPosition() { return position; }
    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }
    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }
    public String getUuid() { return uuid; }
    public String getUsername() { return username; }
}