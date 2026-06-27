package hrd.h4rdykrft;

import hrd.h4rdykrft.gui.*;
import hrd.h4rdykrft.render.Camera;
import hrd.h4rdykrft.render.Renderer;
import hrd.h4rdykrft.render.Shader;
import hrd.h4rdykrft.render.Texture;
import hrd.h4rdykrft.world.World;
import hrd.h4rdykrft.player.Player;
import hrd.h4rdykrft.world.RaycastResult;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.joml.Math.floor;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    // Идентификаторы окна и главные модули движка
    private long window;
    private World world;
    private Camera camera;
    private Renderer renderer;
    private Shader shader;
    private Texture atlas;
    private Texture fontTexture;
    private UIManager uiManager;
    private Shader uiShader;
    // Элементы интерфейса для теста
    private boolean isMenuOpen = false; // По умолчанию игра в режиме обзора
    // Переменные для расчета FPS
    private double lastFpsCheckTime = 0;
    private int fpsCount = 0;
    private int currentFps = 0;
    // Новые модули для игрока и взаимодействия с миром
    private Player localPlayer;
    private boolean leftMousePressed = false;
    private boolean rightMousePressed = false;
    private final float MAX_REACH_DISTANCE = 6.0f; // Дистанция работы рук (как в Minecraft)
    private Font uiFont;
    private Label fpsLabel;
    private Label posLabel;
    public void run() {
        init();
        loop();
        if (world != null) {
            world.shutdown();
        }

        // Освобождение ресурсов OpenGL и GLFW
        // Очистка памяти при закрытии игры
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
        System.exit(0);
    }

    private void init() {

        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Не удалось инициализировать GLFW");
        }

        // Настройка параметров окна (OpenGL 3.3 Core Profile)
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        // Создание окна игры
        window = glfwCreateWindow(1280, 720, "H4rdyKrft", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Не удалось создать окно GLFW");
        }

        // Центрирование окна на экране пользователя
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vidmode != null) {
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - 1280) / 2,
                    (vidmode.height() - 720) / 2
            );
        }
        glfwSetFramebufferSizeCallback(window, (windowHandle, width, height) -> {
            glViewport(0, 0, width, height);
        });

        glfwMakeContextCurrent(window);

        glfwShowWindow(window);

        // Критически важно для связки LWJGL и OpenGL функций
        GL.createCapabilities();

        // Настройки рендеринга: включаем буфер глубины и отсечение невидимых задних граней
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        // Блокируем курсор мыши внутри окна (режим "захвата" для шутеров)
        //glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        // Инициализация базовых игровых систем
        world = new World();
        camera = new Camera();
        renderer = new Renderer();
        uiShader = new Shader("shaders/ui.vert", "shaders/ui.frag");
        uiManager = new UIManager(uiShader);

        // Пример добавления элемента

        // Инициализация игрока: передаем UUID, имя и стартовую позицию в мире
        // Координата Y выставлена повыше (120f), чтобы игрок гарантированно заспавнился НАД землей
        localPlayer = new Player("local-player-id", "Steve", new Vector3f(8f, 120f, 22f));

        // Загрузка ресурсов (Замени пути на актуальные для твоей структуры папок, если они отличаются)
        shader = new Shader("shaders/main.vert", "shaders/main.frag");
        atlas = new Texture("textures/atlas.png");
        // Привязываем движение мыши к повороту головы ИГРОКА, а не камеры напрямую
        glfwSetCursorPosCallback(window, (w, xpos, ypos) -> {
            if (!isMenuOpen) {
                localPlayer.handleMouse(xpos, ypos);
            }
        });
        glfwSetKeyCallback(window, (windowHandle, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_E && action == GLFW_PRESS) {
                // Меняем состояние на противоположное
                isMenuOpen = !isMenuOpen;

                if (isMenuOpen) {
                    // Освобождаем мышь для кликов по UI
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                } else {
                    // Возвращаем захват мыши для игры
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                }
            }
        });
        // Загружаем текстуру шрифта (Сетка ASCII 16x16, например классический шрифт Minecraft)
        // Настройка вывода ошибок GLFW
        uiFont = new Font("fonts/minecraft_font.ttf", 24f);

// Создаем текст в координатах X=10, Y=10 (левый верхний угол)
        fpsLabel = new Label(uiFont, 10, 10);
        fpsLabel.color = new float[]{1.0f, 1.0f, 1.0f, 1.0f}; // Сделаем текст желтым для контраста
        uiManager.addElement(fpsLabel);
        posLabel = new Label(uiFont, 10, 28);
        posLabel.color = new float[]{1.0f, 1.0f, 1.0f, 1.0f}; // Сделаем текст желтым для контраста
        uiManager.addElement(posLabel);
        Button testButton = new Button(10, 56, 100, 30, uiFont, "Tp back", () -> {
            localPlayer.setPosition(new Vector3f(0,512,0));
        });
        uiManager.addElement(testButton);
    }

    private void loop() {
        // Устанавливаем цвет очистки экрана (светло-голубое небо)
        glClearColor(0.5f, 0.7f, 1.0f, 1.0f);

        // Массивы для динамического получения текущего размера окна
        int[] width = new int[1];
        int[] height = new int[1];
        Panel panel = new Panel(50, 50, 5, 5);

        panel.color = new float[]{1f, 1f, 1f, 0.6f}; // Темно-серый полупрозрачный
        uiManager.addElement(panel);
        while (!glfwWindowShouldClose(window)) {
            // ИСПРАВЛЕНИЕ 1: Сначала получаем актуальные размеры окна
            glfwGetWindowSize(window, width, height);

            // ИСПРАВЛЕНИЕ 2: Теперь считаем центр экрана корректно
            panel.x = (float) ((double) width[0] / 2 - 2.5);
            panel.y = (float) ((double) height[0] / 2 - 2.5);

            // Очищаем буфер кадра и глубины
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // 1. ФИЗИКА И ДВИЖЕНИЕ: Игрок считывает нажатия клавиатуры
            localPlayer.update(window,0.016f,world);

            // 2. ОБНОВЛЕНИЕ КАМЕРЫ: Камера жестко встает на позицию глаз игрока
            camera.update(localPlayer.getPosition(), localPlayer.getPitch(), localPlayer.getYaw());

            // 3. ГЕНЕРАЦИЯ МИРА: Подгружаем/удаляем чанки вокруг текущей позиции игрока
            world.update(localPlayer.getPosition().x, localPlayer.getPosition().z);

            // 4. СЕТКА И ВЗАИМОДЕЙСТВИЕ: Считываем клики мыши
            boolean currentLeft = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;
            boolean currentRight = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS;

            if (currentLeft || currentRight) {
                // Пускаем луч из глаз камеры по вектору взгляда игрока
                RaycastResult ray = RaycastResult.trace(
                        world,
                        camera.getPosition(),
                        localPlayer.getDirection(),
                        MAX_REACH_DISTANCE
                );

                if (ray.hit) {
                    if (currentLeft && !leftMousePressed) {
                        // ЛКМ: Ломаем блок (заменяем его id на AIR — 0)
                        world.setBlock(ray.blockX, ray.blockY, ray.blockZ, (byte) 0);
                    }
                    else if (currentRight && !rightMousePressed) {
                        // ПКМ: Ставим блок (например, Камень — ID 3)
                        int playerX = (int) Math.floor(localPlayer.getPosition().x);
                        int playerY = (int) Math.floor(localPlayer.getPosition().y);
                        int playerZ = (int) Math.floor(localPlayer.getPosition().z);

                        // Проверка коллизии: не ставим блок туда, где сейчас ноги (playerY) или голова (playerY + 1) игрока
                        if (!(ray.airX == playerX && (ray.airY == playerY || ray.airY == playerY + 1) && ray.airZ == playerZ)) {
                            world.setBlock(ray.airX, ray.airY, ray.airZ, (byte) 3);
                        }
                    }
                }
            }

            // Сохраняем состояние кнопок, чтобы одиночный клик не превращался в "пулеметное" уничтожение блоков
            leftMousePressed = currentLeft;
            rightMousePressed = currentRight;
            double[] mouseX = new double[1];
            double[] mouseY = new double[1];
            glfwGetCursorPos(window, mouseX, mouseY);

            boolean isMousePressed = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;

// Передаем данные в менеджер интерфейса
            uiManager.handleInput((float) mouseX[0], (float) mouseY[0], isMousePressed);
            // 5. МАТЕМАТИКА КАМЕРЫ И РЕНДЕРИНГ
            glfwGetWindowSize(window, width, height);
            float aspect = height[0] > 0 ? (float) width[0] / height[0] : 1.0f;

            // Матрица перспективной проекции (Угол обзора 70 градусов)
            Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(70.0f), aspect, 0.1f, 1000.0f);

            // Матрица вида (трансформирует мир относительно положения камеры)
            Matrix4f view = camera.getViewMatrix();
            double currentTime = glfwGetTime();
            fpsCount++;
            if (currentTime - lastFpsCheckTime >= 1.0) { // Каждую секунду
                currentFps = fpsCount;
                fpsCount = 0;
                lastFpsCheckTime = currentTime;

                // Динамически обновляем текст
                fpsLabel.setText("FPS: " + currentFps);
            }
            posLabel.setText("X:" + floor(localPlayer.getPosition().x) + " Y" + floor(localPlayer.getPosition().y) + " Z" + floor(localPlayer.getPosition().z));
            // Активация шейдерной программы
            // Вычисляем deltaTime (разницу во времени между кадрами)


            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);


            shader.bind();

            // Загрузка матриц трансформации в шейдер (в Uniform-переменные)
            shader.setUniform("projection", projection);
            shader.setUniform("view", view);
            // Активация и привязка текстурного атласа блоков
            glActiveTexture(GL_TEXTURE0);
            atlas.bind();
            shader.setUniform("texture1", 0);
            // Получаем координаты курсора и состояние левой кнопки мыши

            // Отрисовка геометрии мира
            renderer.renderWorld(world, shader);
            uiManager.render(width[0],height[0]);
            // Смена графических буферов (вывод кадра на экран) и обработка системных событий ОС
            glfwSwapBuffers(window);
            glfwPollEvents();
        }

    }

    public static void main(String[] args) {
        // Точка входа приложения
        new Main().run();
    }
}