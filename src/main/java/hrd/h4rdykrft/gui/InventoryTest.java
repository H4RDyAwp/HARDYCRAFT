package hrd.h4rdykrft.gui;

import hrd.h4rdykrft.item.Item;
import hrd.h4rdykrft.item.ItemStack;
import hrd.h4rdykrft.item.Items;
import hrd.h4rdykrft.block.Blocks;

/**
 * Тестирование всех функций Inventory
 * 
 * Запуск:
 *   java -cp target/classes hrd.h4rdykrft.gui.InventoryTest
 */
public class InventoryTest {
    
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("   ТЕСТИРОВАНИЕ INVENTORY - ВСЕ ФУНКЦИИ");
        System.out.println("═══════════════════════════════════════════════════════\n");
        
        // Создаём инвентарь 9x2 (9 слотов в первом ряду, 2 ряда)
        Inventory inventory = new Inventory(1280, 720, 9, 2);
        
        testAddItemsBlocks(inventory);
        testAddItemsTools(inventory);
        testAddItemsStackLimit(inventory);
        testGetMethods(inventory);
        testSetAndClear(inventory);
        testGetSlots(inventory);
        
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("   ✅ ВСЕ ТЕСТЫ ЗАВЕРШЕНЫ!");
        System.out.println("═══════════════════════════════════════════════════════\n");
    }
    
    /**
     * Тест 1: Добавление блоков (с лимитом стека 64)
     */
    private static void testAddItemsBlocks(Inventory inventory) {
        System.out.println("📝 ТЕСТ 1: Добавление БЛОКОВ (maxStackSize = 64)");
        System.out.println("─────────────────────────────────────────────────────");
        
        // Добавляем 100 блоков STONE
        ItemStack stack100 = new ItemStack(Items.getItem(Blocks.STONE.getInvId()), 100);
        int remaining = inventory.addItems(stack100);
        
        System.out.println("  Добавили: " + stack100);
        System.out.println("  Осталось: " + remaining);
        System.out.println("  Слот 0: " + inventory.getItemStackAt(0));
        System.out.println("  Слот 1: " + inventory.getItemStackAt(1));
        
        if (remaining == 36 && 
            inventory.getItemStackAt(0).getCount() == 64 &&
            inventory.getItemStackAt(1).getCount() == 36) {
            System.out.println("  ✅ PASSED: Блоки распределены правильно\n");
        } else {
            System.out.println("  ❌ FAILED: Неправильное распределение!\n");
        }
    }
    
    /**
     * Тест 2: Добавление инструментов (с лимитом стека 1)
     */
    private static void testAddItemsTools(Inventory inventory) {
        System.out.println("📝 ТЕСТ 2: Добавление ИНСТРУМЕНТОВ (maxStackSize = 1)");
        System.out.println("─────────────────────────────────────────────────────");
        
        // Очищаем инвентарь
        for (int i = 0; i < 18; i++) {
            inventory.clearSlot(i);
        }
        
        // Пытаемся добавить инструмент
        ItemStack tool1 = new ItemStack(Items.IRON_PICKAXE, 1);
        int remaining1 = inventory.addItems(tool1);
        System.out.println("  Добавили: " + tool1);
        System.out.println("  Слот 0: " + inventory.getItemStackAt(0));
        System.out.println("  Осталось: " + remaining1);
        
        if (remaining1 == 0 && inventory.getItemStackAt(0).getCount() == 1) {
            System.out.println("  ✅ PASSED: Инструмент добавлен правильно");
        } else {
            System.out.println("  ❌ FAILED: Инструмент не добавлен!");
        }
        
        // Пытаемся добавить второй инструмент (он не объединится)
        ItemStack tool2 = new ItemStack(Items.STONE_SWORD, 1);
        int remaining2 = inventory.addItems(tool2);
        System.out.println("  Добавили второй инструмент: " + tool2);
        System.out.println("  Слот 1: " + inventory.getItemStackAt(1));
        System.out.println("  Осталось: " + remaining2);
        
        if (remaining2 == 0 && inventory.getItemStackAt(1).getCount() == 1) {
            System.out.println("  ✅ PASSED: Второй инструмент добавлен в отдельный слот\n");
        } else {
            System.out.println("  ❌ FAILED: Второй инструмент не добавлен!\n");
        }
    }
    
    /**
     * Тест 3: Проверка лимита стека (старт с заполненным слотом)
     */
    private static void testAddItemsStackLimit(Inventory inventory) {
        System.out.println("📝 ТЕСТ 3: Лимит СТЕКА (добавление к заполненному)");
        System.out.println("─────────────────────────────────────────────────────");
        
        // Очищаем инвентарь
        for (int i = 0; i < 18; i++) {
            inventory.clearSlot(i);
        }
        
        // Добавляем 30 блоков в слот 0
        ItemStack stack30 = new ItemStack(Items.getItem(Blocks.DIRT.getInvId()), 30);
        inventory.addItems(stack30);
        System.out.println("  Начальное состояние:");
        System.out.println("  Слот 0: " + inventory.getItemStackAt(0) + " (30 блоков)");
        
        // Добавляем ещё 100 блоков (30+100=130, но слот вмещает 64)
        ItemStack stack100 = new ItemStack(Items.getItem(Blocks.DIRT.getInvId()), 100);
        int remaining = inventory.addItems(stack100);
        
        System.out.println("  После добавления 100 блоков:");
        System.out.println("  Слот 0: " + inventory.getItemStackAt(0) + " (должно быть 64)");
        System.out.println("  Слот 1: " + inventory.getItemStackAt(1) + " (должно быть 64)");
        System.out.println("  Осталось: " + remaining + " (должно быть 2)");
        
        if (remaining == 2 &&
            inventory.getItemStackAt(0).getCount() == 64 &&
            inventory.getItemStackAt(1).getCount() == 64) {
            System.out.println("  ✅ PASSED: Стеки распределены правильно\n");
        } else {
            System.out.println("  ❌ FAILED: Неправильное распределение!\n");
        }
    }
    
    /**
     * Тест 4: Тестирование GET методов
     */
    private static void testGetMethods(Inventory inventory) {
        System.out.println("📝 ТЕСТ 4: GET методы");
        System.out.println("─────────────────────────────────────────────────────");
        
        // Очищаем и добавляем предметы
        for (int i = 0; i < 18; i++) {
            inventory.clearSlot(i);
        }
        
        ItemStack stack = new ItemStack(Items.getItem(Blocks.STONE.getInvId()), 32);
        inventory.setItemStack(3, stack);
        
        System.out.println("  Установили STONE x32 в слот 3");
        System.out.println("  getItemStackAt(3): " + inventory.getItemStackAt(3));
        System.out.println("  Слот 3 пуст? " + inventory.getItemStackAt(3).isEmpty());
        System.out.println("  Количество: " + inventory.getItemStackAt(3).getCount());
        System.out.println("  ID: " + inventory.getItemStackAt(3).getItem().getId());
        
        // Выбираем слот 3
        inventory.setItemStack(2, new ItemStack(Items.getItem(Blocks.DIRT.getInvId()), 10));
        inventory.clearSlot(0); // Убедимся что слот 0 пуст
        
        System.out.println("  getSelectedItemStack() (индекс 0): " + inventory.getSelectedItemStack());
        System.out.println("  getSelectedItemId() (индекс 0): " + inventory.getSelectedItemId());
        System.out.println("  getSelectedSlotIndex(): " + inventory.getSelectedSlotIndex());
        
        if (inventory.getSelectedSlotIndex() == 0 && inventory.getSelectedItemId() == 0) {
            System.out.println("  ✅ PASSED: GET методы работают правильно\n");
        } else {
            System.out.println("  ❌ FAILED: GET методы не работают!\n");
        }
    }
    
    /**
     * Тест 5: SET и CLEAR методы
     */
    private static void testSetAndClear(Inventory inventory) {
        System.out.println("📝 ТЕСТ 5: SET и CLEAR методы");
        System.out.println("─────────────────────────────────────────────────────");
        
        // Очищаем
        for (int i = 0; i < 18; i++) {
            inventory.clearSlot(i);
        }
        
        // Устанавливаем предмет
        ItemStack wood = new ItemStack(Items.getItem(Blocks.OAK_PLANKS.getInvId()), 32);
        inventory.setItemStack(5, wood);
        System.out.println("  setItemStack(5, OAK_PLANKS x32)");
        System.out.println("  Слот 5: " + inventory.getItemStackAt(5));
        
        // Очищаем слот
        inventory.clearSlot(5);
        System.out.println("  clearSlot(5)");
        System.out.println("  Слот 5: " + inventory.getItemStackAt(5));
        System.out.println("  Слот 5 пуст? " + inventory.getItemStackAt(5).isEmpty());
        
        if (inventory.getItemStackAt(5).isEmpty()) {
            System.out.println("  ✅ PASSED: SET и CLEAR работают правильно\n");
        } else {
            System.out.println("  ❌ FAILED: CLEAR не работает!\n");
        }
    }
    
    /**
     * Тест 6: getSlots() метод
     */
    private static void testGetSlots(Inventory inventory) {
        System.out.println("📝 ТЕСТ 6: getSlots() метод");
        System.out.println("─────────────────────────────────────────────────────");
        
        java.util.List<InventorySlot> slots = inventory.getSlots();
        System.out.println("  Количество слотов: " + slots.size());
        System.out.println("  Ожидается: 18 (9 колонн x 2 ряда)");
        
        if (slots.size() == 18) {
            System.out.println("  ✅ PASSED: getSlots() возвращает правильное количество\n");
        } else {
            System.out.println("  ❌ FAILED: Неправильное количество слотов!\n");
        }
    }
}
