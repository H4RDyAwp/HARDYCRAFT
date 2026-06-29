package hrd.h4rdykrft.player;

import hrd.h4rdykrft.math.AABB;
import hrd.h4rdykrft.render.PlayerModel;
import hrd.h4rdykrft.render.Shader;
import hrd.h4rdykrft.world.World;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;
import static org.lwjgl.glfw.GLFW.*;

public class Player {

    public enum GameMode {
        SURVIVAL, CREATIVE
    }
    public enum CameraMode {
        FIRST_PERSON, THIRD_PERSON
    }

    // Режим игры и полет
    private GameMode gameMode = GameMode.SURVIVAL;
    private CameraMode cameraMode = CameraMode.FIRST_PERSON;
    private boolean isFlying = false;
    private float flySpeedModifier = 2.5f;

    // Идентификация
    private final String uuid;
    private final String username;

    // Трансформ игрока
    private final Vector3f position;
    private final Vector3f velocity = new Vector3f(0.0f);
    private float pitch;
    private float yaw;
    public boolean isLocal;
    // Настройки физики
    private final float normalSpeed = 6.0f;
    private final float crawlSpeed = 1.5f;
    private final float gravity = -28.0f;
    private final float jumpForce = 9f;
    private final float sensitivity = 0.10f;
    private boolean vWasPressed = false;
    private boolean spaceWasPressed = false;
    private float spaceClickTimer = 0.0f;
    // Хитбоксы
    private final float width = 0.6f;
    private final float normalHeight = 1.8f;
    private final float crawlHeight = 0.6f;
    private float currentHeight = normalHeight;
    private final AABB playerBox = new AABB(0, 0, 0, 0, 0, 0);

    // Здоровье
    private float maxHealth = 100.0f;
    private float currentHealth = maxHealth;

    // Переменные для расчета урона от падения
    private float highestYDuringFall;
    private boolean wasGrounded = true;

    // Состояния физики
    private boolean onGround = false;
    private boolean isCrawling = false;

    // Плавная смена позиции камеры
    private float renderEyeHeight = 1.62f;
    private final float cameraLerpSpeed = 14.0f;

    private double lastMouseX = 0, lastMouseY = 0;
    private boolean firstMouse = true;
    private PlayerModel playerModel;
    public Player(String uuid, String username, Vector3f startPosition, boolean isLocal) {
        this.uuid = uuid;
        this.username = username;
        this.position = new Vector3f(startPosition);
        this.pitch = 0f;
        this.yaw = -90f;
        this.playerBox.setPosition(position, width, currentHeight);
        this.isLocal = isLocal;
        this.playerModel = new PlayerModel();
    }

    /**
     * Шаг физики и обработки ввода.
     */
    public void update(long window, float deltaTime, World world) {
        if (isLocal) {
            // ---- 1. СМЕНА РЕЖИМА ИГРЫ (Фикс залипания V) ----
            boolean vPressed = glfwGetKey(window, GLFW_KEY_V) == GLFW_PRESS;
            if (vPressed && !vWasPressed) {
                if (getGameMode() == GameMode.SURVIVAL) {
                    setGameMode(GameMode.CREATIVE);
                } else {
                    setGameMode(GameMode.SURVIVAL);
                    isFlying = false; // Отключаем полет при переходе в выживание
                }
            }
            vWasPressed = vPressed;

            // ---- 2. ВКЛЮЧЕНИЕ / ВЫКЛЮЧЕНИЕ ПОЛЕТА (Двойной пробел) ----
            if (spaceClickTimer > 0) {
                spaceClickTimer -= deltaTime;
            }

            boolean spacePressed = glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS;
            if (spacePressed && !spaceWasPressed && isCreative()) {
                if (spaceClickTimer > 0) {
                    isFlying = !isFlying; // Переключаем режим полета
                    spaceClickTimer = 0;  // Сбрасываем таймер
                } else {
                    spaceClickTimer = 0.3f; // Окно в 300мс на второе нажатие
                }
            }
            spaceWasPressed = spacePressed;

            // ---- 3. СБОР ВВОДА С КЛАВИАТУРЫ ДЛЯ ДВИЖЕНИЯ ----
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

            boolean shiftPressed = glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS;

            // --- УПРАВЛЕНИЕ ПОЛЗАНИЕМ И ПРОВЕРКА ПОТОЛКА ---
            if (!isFlying) {
                if (isBlockInsideHeight(world, normalHeight)) {
                    currentHeight = crawlHeight;
                    isCrawling = true;
                } else {
                    if (shiftPressed) {
                        currentHeight = crawlHeight;
                        isCrawling = true;
                    } else {
                        currentHeight = normalHeight;
                        isCrawling = false;
                    }
                }
            } else {
                currentHeight = normalHeight;
                isCrawling = false;
            }

            float targetEyeHeight = isCrawling ? 0.45f : 1.62f;
            renderEyeHeight += (targetEyeHeight - renderEyeHeight) * cameraLerpSpeed * deltaTime;

            // --- ФИЗИКА И СКОРОСТЬ ---
            float currentSpeed = isCrawling ? crawlSpeed : normalSpeed;
            if (isCreative() && isFlying) {
                currentSpeed *= flySpeedModifier;
            }

            velocity.x = inputDirection.x * currentSpeed;
            velocity.z = inputDirection.z * currentSpeed;

            // --- ВЕРТИКАЛЬНОЕ ДВИЖЕНИЕ (ПОЛЕТ ИЛИ ГРАВИТАЦИЯ) ---
            if (isCreative() && isFlying) {
                velocity.y = 0.0f; // Гравитация отключена
                if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
                    velocity.y = currentSpeed; // Летим вверх
                }
                if (shiftPressed) {
                    velocity.y = -currentSpeed; // Летим вниз
                }
            } else {
                // Обычная гравитация
                velocity.y += gravity * deltaTime;

                // Прыжок
                if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS && onGround && !isCrawling) {
                    velocity.y = jumpForce;
                    onGround = false;
                }
            }

            float moveX = velocity.x * deltaTime;
            float moveY = velocity.y * deltaTime;
            float moveZ = velocity.z * deltaTime;

            List<AABB> candidateBlocks = getLocalBlockBoxes(world);

            // ---- 4. ПООЧЕРЕДНАЯ ПРОВЕРКА СТОЛКНОВЕНИЙ (AABB) ----

            // --- Ось X ---
            float oldX = position.x;
            position.x += moveX;
            playerBox.setPosition(position, width, currentHeight);

            for (AABB blockBox : candidateBlocks) {
                if (playerBox.intersects(blockBox)) {
                    if (moveX > 0) position.x = blockBox.minX - (width / 2.0f) - 0.001f;
                    else if (moveX < 0) position.x = blockBox.maxX + (width / 2.0f) + 0.001f;
                    velocity.x = 0;
                    playerBox.setPosition(position, width, currentHeight);
                }
            }

            if (isCrawling && onGround && !isFlying) {
                if (!isPositionSupported(world)) {
                    position.x = oldX;
                    velocity.x = 0;
                    playerBox.setPosition(position, width, currentHeight);
                }
            }

            // --- Ось Z ---
            float oldZ = position.z;
            position.z += moveZ;
            playerBox.setPosition(position, width, currentHeight);

            for (AABB blockBox : candidateBlocks) {
                if (playerBox.intersects(blockBox)) {
                    if (moveZ > 0) position.z = blockBox.minZ - (width / 2.0f) - 0.001f;
                    else if (moveZ < 0) position.z = blockBox.maxZ + (width / 2.0f) + 0.001f;
                    velocity.z = 0;
                    playerBox.setPosition(position, width, currentHeight);
                }
            }

            if (isCrawling && onGround && !isFlying) {
                if (!isPositionSupported(world)) {
                    position.z = oldZ;
                    velocity.z = 0;
                    playerBox.setPosition(position, width, currentHeight);
                }
            }

            // --- Ось Y ---
            onGround = false;
            position.y += moveY;
            playerBox.setPosition(position, width, currentHeight);

            for (AABB blockBox : candidateBlocks) {
                if (playerBox.intersects(blockBox)) {
                    if (moveY > 0) {
                        position.y = blockBox.minY - currentHeight - 0.001f;
                        velocity.y = 0;
                    } else if (moveY < 0) {
                        position.y = blockBox.maxY + 0.001f;
                        velocity.y = 0;
                        onGround = true;
                        if (isFlying) isFlying = false; // Приземление отключает полет
                    }
                    playerBox.setPosition(position, width, currentHeight);
                }
            }

            // ---- 5. УРОН ОТ ПАДЕНИЯ ----
            if (onGround) {
                if (!wasGrounded && !isCreative()) {
                    float fallDistance = highestYDuringFall - this.position.y;
                    if (fallDistance > 5.0f) {
                        float damage = (fallDistance - 3.0f) * 3f;
                        takeDamage(damage);
                    }
                }
                highestYDuringFall = this.position.y;
            } else {
                if (this.position.y > highestYDuringFall || isCreative() || isFlying) {
                    highestYDuringFall = this.position.y;
                }
            }
            wasGrounded = onGround;
        }
        playerModel.update((Math.abs(velocity.x) > 0.01f || Math.abs(velocity.z) > 0.01f),deltaTime);


    }
    public void render(Matrix4f view, Matrix4f proj, Shader shader){
        playerModel.render(view,proj,shader,position,yaw);
    }

    private boolean isPositionSupported(World world) {
        AABB checkBox = new AABB(
                position.x - width / 2.0f, position.y - 0.05f, position.z - width / 2.0f,
                position.x + width / 2.0f, position.y, position.z + width / 2.0f
        );
        for (AABB blockBox : getLocalBlockBoxes(world)) {
            if (checkBox.intersects(blockBox)) return true;
        }
        return false;
    }

    private boolean isBlockInsideHeight(World world, float heightToCheck) {
        AABB checkHeightBox = new AABB(
                position.x - width / 2.0f, position.y, position.z - width / 2.0f,
                position.x + width / 2.0f, position.y + heightToCheck, position.z + width / 2.0f
        );
        for (AABB blockBox : getLocalBlockBoxes(world)) {
            if (checkHeightBox.intersects(blockBox)) return true;
        }
        return false;
    }

    private List<AABB> getLocalBlockBoxes(World world) {
        List<AABB> boxes = new ArrayList<>();
        int minBlockX = (int) Math.floor(position.x - (width / 2.0f) - 1.0f);
        int maxBlockX = (int) Math.floor(position.x + (width / 2.0f) + 1.0f);
        int minBlockY = (int) Math.floor(position.y - 1.0f);
        int maxBlockY = (int) Math.floor(position.y + normalHeight + 1.0f);
        int minBlockZ = (int) Math.floor(position.z - (width / 2.0f) - 1.0f);
        int maxBlockZ = (int) Math.floor(position.z + (width / 2.0f) + 1.0f);

        for (int x = minBlockX; x <= maxBlockX; x++) {
            for (int y = minBlockY; y <= maxBlockY; y++) {
                for (int z = minBlockZ; z <= maxBlockZ; z++) {
                    byte blockId = world.getBlock(x, y, z);
                    if (blockId != 0) {
                        boxes.add(new AABB(x, y, z, x + 1.0f, y + 1.0f, z + 1.0f));
                    }
                }
            }
        }
        return boxes;
    }

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

    public Vector3f getEyePosition() {
        return new Vector3f(position.x, position.y + renderEyeHeight, position.z);
    }

    public Vector3f getDirection() {
        Vector3f direction = new Vector3f();
        direction.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        direction.y = (float) Math.sin(Math.toRadians(pitch));
        direction.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        return direction.normalize();
    }

    // --- СИСТЕМА УРОНА ---
    public void takeDamage(float amount) {
        if (isCreative()) return;
        if (amount <= 0 || currentHealth <= 0) return;

        currentHealth -= amount;
        if (currentHealth < 0) currentHealth = 0;

        System.out.println("Игрок получил урон: " + amount + " HP. Осталось: " + currentHealth);

        if (currentHealth <= 0) {
            respawn();
        }
    }

    public void respawn() {
        System.out.println("Игрок погиб!");
        this.currentHealth = maxHealth;
        this.position.set(0, 30, 0);
        this.velocity.set(0, 0, 0);
    }

    // --- ГЕТТЕРЫ И СЕТТЕРЫ ---
    public float getCurrentHealth() { return currentHealth; }
    public float getMaxHealth() { return maxHealth; }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
        if (gameMode == GameMode.CREATIVE) {
            this.currentHealth = maxHealth;
        } else {
            this.isFlying = false;
        }
    }

    public GameMode getGameMode() { return gameMode; }
    public boolean isCreative() { return gameMode == GameMode.CREATIVE; }

    public boolean isFlying() { return isFlying; }
    public void setFlying(boolean flying) { this.isFlying = flying; }

    public Vector3f getPosition() { return position; }
    public void setPosition(Vector3f pos) { position.x = pos.x; position.y = pos.y; position.z = pos.z; }

    public float getPitch() { return pitch; }
    public void setPitch(float pitch) { this.pitch = pitch; }

    public float getYaw() { return yaw; }
    public void setYaw(float yaw) { this.yaw = yaw; }

    public String getUuid() { return uuid; }
    public String getUsername() { return username; }
    public boolean isCrawling() { return isCrawling; }
}