package hrd.h4rdykrft.player;

import hrd.h4rdykrft.math.AABB;
import hrd.h4rdykrft.world.World;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.glfw.GLFW.*;

public class Player {
    private final String uuid;
    private final String username;

    // Трансформ игрока
    private final Vector3f position;
    private final Vector3f velocity = new Vector3f(0.0f); // Текущая скорость игрока
    private float pitch;
    private float yaw;

    // Настройки физики (приближенные к Minecraft)
    private final float speed = 4.317f;       // Скорость бега (блоков в секунду)
    private final float gravity = -28.0f;     // Ускорение свободного падения (блоков/сек^2)
    private final float jumpForce = 8.5f;     // Сила прыжка
    private final float sensitivity = 0.10f;

    // Хитбокс игрока (размеры: ширина 0.6 блока, высота 1.8 блока)
    private final float width = 0.6f;
    private final float height = 1.8f;
    private final AABB playerBox = new AABB(0, 0, 0, 0, 0, 0);

    // Состояния физики
    private boolean onGround = false;

    private double lastMouseX = 0, lastMouseY = 0;
    private boolean firstMouse = true;

    public Player(String uuid, String username, Vector3f startPosition) {
        this.uuid = uuid;
        this.username = username;
        this.position = new Vector3f(startPosition);
        this.pitch = 0f;
        this.yaw = -90f;
        this.playerBox.setPosition(position, width, height);
    }

    /**
     * Шаг физики и обработки ввода. Метод заменяет старый handleKeyboard.
     */
    public void update(long window, float deltaTime, World world) {
        // ---- 1. СБОР ВВОДА С КЛАВИАТУРЫ ----
        Vector3f forward = new Vector3f((float) Math.cos(Math.toRadians(yaw)), 0.0f, (float) Math.sin(Math.toRadians(yaw))).normalize();
        Vector3f right = new Vector3f();
        forward.cross(new Vector3f(0.0f, 1.0f, 0.0f), right).normalize();

        Vector3f inputDirection = new Vector3f(0.0f);
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) inputDirection.add(forward);
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) inputDirection.sub(forward);
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) inputDirection.add(right);
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) inputDirection.sub(right);

        if (inputDirection.lengthSquared() > 0) {
            inputDirection.normalize();
        }

        // Устанавливаем горизонтальную скорость на основе ввода
        velocity.x = inputDirection.x * speed;
        velocity.z = inputDirection.z * speed;

        // Применяем гравитацию к вертикальной скорости
        velocity.y += gravity * deltaTime;

        // Прыжок (доступен только если стоим на земле)
        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS && onGround) {
            velocity.y = jumpForce;
            onGround = false;
        }

        // ---- 2. ПООЧЕРЕДНАЯ ПРОВЕРКА СТОЛКНОВЕНИЙ (AABB) ----

        // Вектор предполагаемого смещения за этот кадр
        float moveX = velocity.x * deltaTime;
        float moveY = velocity.y * deltaTime;
        float moveZ = velocity.z * deltaTime;

        // Получаем список всех твердых блоков вокруг хитбокса игрока
        List<AABB> candidateBlocks = getLocalBlockBoxes(world);

        // --- Ось X ---
        position.x += moveX;
        playerBox.setPosition(position, width, height);
        for (AABB blockBox : candidateBlocks) {
            if (playerBox.intersects(blockBox)) {
                if (moveX > 0) position.x = blockBox.minX - (width / 2.0f) - 0.001f; // Столкновение справа
                else if (moveX < 0) position.x = blockBox.maxX + (width / 2.0f) + 0.001f; // Столкновение слева
                velocity.x = 0;
                playerBox.setPosition(position, width, height);
            }
        }

        // --- Ось Z ---
        position.z += moveZ;
        playerBox.setPosition(position, width, height);
        for (AABB blockBox : candidateBlocks) {
            if (playerBox.intersects(blockBox)) {
                if (moveZ > 0) position.z = blockBox.minZ - (width / 2.0f) - 0.001f; // Столкновение спереди
                else if (moveZ < 0) position.z = blockBox.maxZ + (width / 2.0f) + 0.001f; // Столкновение сзади
                velocity.z = 0;
                playerBox.setPosition(position, width, height);
            }
        }

        // --- Ось Y ---
        onGround = false; // Сбрасываем флаг земли перед проверкой по Y
        position.y += moveY;
        playerBox.setPosition(position, width, height);
        for (AABB blockBox : candidateBlocks) {
            if (playerBox.intersects(blockBox)) {
                if (moveY > 0) {
                    position.y = blockBox.minY - height - 0.001f; // Ударился головой о потолок
                    velocity.y = 0;
                } else if (moveY < 0) {
                    position.y = blockBox.maxY + 0.001f; // Приземлился на блок
                    velocity.y = 0;
                    onGround = true; // Мы на земле!
                }
                playerBox.setPosition(position, width, height);
            }
        }
    }

    /**
     * Собирает хитбоксы всех твердых блоков в радиусе 2-х блоков от игрока.
     */
    private List<AABB> getLocalBlockBoxes(World world) {
        List<AABB> boxes = new ArrayList<>();

        // Диапазон блоков вокруг игрока
        int minBlockX = (int) Math.floor(position.x - (width / 2.0f) - 1.0f);
        int maxBlockX = (int) Math.floor(position.x + (width / 2.0f) + 1.0f);
        int minBlockY = (int) Math.floor(position.y - 1.0f);
        int maxBlockY = (int) Math.floor(position.y + height + 1.0f);
        int minBlockZ = (int) Math.floor(position.z - (width / 2.0f) - 1.0f);
        int maxBlockZ = (int) Math.floor(position.z + (width / 2.0f) + 1.0f);

        for (int x = minBlockX; x <= maxBlockX; x++) {
            for (int y = minBlockY; y <= maxBlockY; y++) {
                for (int z = minBlockZ; z <= maxBlockZ; z++) {
                    byte blockId = world.getBlock(x, y, z);

                    // Предполагаем, что 0 — это воздух. Все остальные блоки считаем твердыми.
                    if (blockId != 0) {
                        boxes.add(new AABB(x, y, z, x + 1.0f, y + 1.0f, z + 1.0f));
                    }
                }
            }
        }
        return boxes;
    }

    /**
     * Обработка движения мыши (без изменений).
     */
    public void handleMouse(double mouseX, double mouseY) {
        if (firstMouse) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            firstMouse = false;
        }
        double offsetX = mouseX - lastMouseX;
        double offsetY = lastMouseY - mouseY;
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        yaw += (float) (offsetX * sensitivity);
        pitch += (float) (offsetY * sensitivity);

        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;
    }

    public Vector3f getDirection() {
        Vector3f direction = new Vector3f();
        direction.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        direction.y = (float) Math.sin(Math.toRadians(pitch));
        direction.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        return direction.normalize();
    }

    // Геттеры и сеттеры
    public Vector3f getPosition() { return position; }
    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }
    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }
    public String getUuid() { return uuid; }
    public String getUsername() { return username; }
}