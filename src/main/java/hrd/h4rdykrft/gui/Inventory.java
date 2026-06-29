package hrd.h4rdykrft.gui;

import hrd.h4rdykrft.render.Shader;
import org.lwjgl.glfw.GLFW; // Импортируем GLFW для работы с клавиатурой
import java.util.ArrayList;
import java.util.List;

public class Inventory extends UIElement {
    private List<InventorySlot> slots;
    private int columns;
    private int rows;

    // Переменная для хранения выбранного слота в первом ряду (хотбаре)
    private int selectedSlotIndex = 0;

    // Добавим текстуру рамки выделения, если захотите визуализировать выбор
    private Image selectionBorder;

    public Inventory(int screenWidth, int screenHeight, int columns, int rows) {
        super(0, 0, columns * 52, rows * 52);
        this.columns = columns;
        this.rows = rows;
        this.slots = new ArrayList<>();
        initSlots();
        selectionBorder = new Image(0,0,50,50,"textures/equipped.png");
    }

    private void initSlots() {
        int slotSize = 50;
        int padding = 2;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int slotX = (int) (this.x + col * (slotSize + padding));
                int slotY = (int) (this.y + row * (slotSize + padding));
                slots.add(new InventorySlot(slotX, slotY, 0));
            }
        }
    }

    // Измененный метод update: принимает окно LWJGL и размеры экрана
    public void update(long window, int screenWidth, int screenHeight) {
        // 1. Центрирование и позиционирование
        this.x = (screenWidth - this.width) / 2;
        this.y = screenHeight - this.height - 20;

        int slotSize = 50;
        int padding = 2;
        int slotIndex = 0;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (slotIndex >= slots.size()) break;

                int slotX = (int) (this.x + col * (slotSize + padding));
                int slotY = (int) (this.y + row * (slotSize + padding));

                InventorySlot slot = slots.get(slotIndex);
                slot.x = slotX;
                slot.y = slotY;

                slotIndex++;
            }
        }

        // 2. Обработка ввода LWJGL (GLFW) для первого ряда
        handleHotbarInput(window);
    }

    private void handleHotbarInput(long window) {
        // Проверяем клавиши от 1 до 9
        for (int i = 0; i < 9; i++) {
            // GLFW_KEY_1 имеет код 49, GLFW_KEY_2 — 50 и так далее
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_1 + i) == GLFW.GLFW_PRESS) {
                // Проверяем, что этот слот существует в рамках наших колонок
                if (i < columns) {
                    selectedSlotIndex = i;
                }
            }
        }

        // Обработка клавиши '0' (обычно это 10-й слот, индекс 9)
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_0) == GLFW.GLFW_PRESS) {
            if (9 < columns) {
                selectedSlotIndex = 9;
            }
        }
    }

    // Получить предмет, который сейчас выбран игроком
    public int getSelectedItem() {
        if (selectedSlotIndex < slots.size()) {
            return slots.get(selectedSlotIndex).itemId;
        }
        return 0;
    }

    public int getSelectedSlotIndex() {
        return selectedSlotIndex;
    }

    @Override
    public void render(Shader shader) {
        for (InventorySlot slot : slots) {
            slot.render(shader);
        }

        InventorySlot activeSlot = slots.get(selectedSlotIndex);
        selectionBorder.x = activeSlot.x;
        selectionBorder.y = activeSlot.y;
        selectionBorder.render(shader);
    }
    public boolean addItem(int itemId) {
        for (InventorySlot slot : slots) {
            if (slot.itemId == 0) {
                slot.setItemId(itemId); // Используем метод обновления из прошлого шага
                return true;
            }
        }
        return false;
    }
}
