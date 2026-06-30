package hrd.h4rdykrft.gui;

import hrd.h4rdykrft.render.Shader;
import hrd.h4rdykrft.item.ItemStack;
import org.lwjgl.glfw.GLFW;
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
        selectionBorder = new Image(0, 0, 50, 50, "textures/equipped.png");
    }

    private void initSlots() {
        int slotSize = 50;
        int padding = 2;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int slotX = (int) (this.x + col * (slotSize + padding));
                int slotY = (int) (this.y + row * (slotSize + padding));
                slots.add(new InventorySlot(slotX, slotY));
            }
        }
    }

    @Override
    public void render(Shader shader) {
        for (InventorySlot slot : slots) {
            slot.render(shader);
        }

        // Отображаем рамку выделения для выбранного слота
        if (selectedSlotIndex < slots.size()) {
            InventorySlot selectedSlot = slots.get(selectedSlotIndex);
            selectionBorder.x = selectedSlot.x;
            selectionBorder.y = selectedSlot.y;
            selectionBorder.render(shader);
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

    // === Методы для работы со слотами ===

    /**
     * Добавить предметы в инвентарь
     * @param itemStack стак для добавления
     * @return количество предметов, которые не поместились
     */
    public int addItems(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return 0;
        }

        ItemStack toAdd = itemStack.copy();

        // Сначала ищем слот с такими же предметами
        for (InventorySlot slot : slots) {
            if (!slot.isEmpty() && slot.getItemStack().canCombineWith(toAdd)) {
                toAdd.setCount(slot.addItems(toAdd));
                if (toAdd.isEmpty()) {
                    return 0;
                }
            }
        }

        // Затем ищем пустые слоты
        for (InventorySlot slot : slots) {
            if (slot.isEmpty()) {
                toAdd.setCount(slot.addItems(toAdd));
                if (toAdd.isEmpty()) {
                    return 0;
                }
            }
        }

        return toAdd.getCount();
    }

    /**
     * Получить выбранный стак предметов
     */
    public ItemStack getSelectedItemStack() {
        if (selectedSlotIndex < slots.size()) {
            return slots.get(selectedSlotIndex).getItemStack();
        }
        return null;
    }

    /**
     * Получить стак предметов из слота по индексу
     */
    public ItemStack getItemStackAt(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < slots.size()) {
            return slots.get(slotIndex).getItemStack();
        }
        return null;
    }

    /**
     * Получить ID выбранного предмета
     */
    public int getSelectedItemId() {
        ItemStack selected = getSelectedItemStack();
        return selected == null ? 0 : selected.getItem().getId();
    }

    /**
     * Получить индекс выбранного слота
     */
    public int getSelectedSlotIndex() {
        return selectedSlotIndex;
    }

    /**
     * Установить предмет в слот
     */
    public void setItemStack(int slotIndex, ItemStack itemStack) {
        if (slotIndex >= 0 && slotIndex < slots.size()) {
            slots.get(slotIndex).setItemStack(itemStack);
        }
    }

    /**
     * Очистить слот
     */
    public void clearSlot(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < slots.size()) {
            slots.get(slotIndex).clearSlot();
        }
    }

    /**
     * Получить все слоты
     */
    public List<InventorySlot> getSlots() {
        return new ArrayList<>(slots);
    }

    /**
     * Получить количество слотов
     */
    public int getSlotCount() {
        return slots.size();
    }
}
