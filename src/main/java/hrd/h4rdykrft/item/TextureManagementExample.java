package hrd.h4rdykrft.item;

import hrd.h4rdykrft.render.InventoryTextureManager;
import hrd.h4rdykrft.gui.Inventory;

/**
 * Примеры использования InventoryTextureManager для работы с текстурами
 */
public class TextureManagementExample {

    /**
     * ПРИМЕР 1: Инициализация текстур при загрузке игры
     */
    public static void initializeTexturesExample() {
        System.out.println("Инициализация текстур инвентаря...");
        
        // Загружаем все текстуры блоков
        InventoryTextureManager.loadBlockTexture(0);   // AIR
        InventoryTextureManager.loadBlockTexture(1);   // DIRT
        InventoryTextureManager.loadBlockTexture(2);   // GRASS
        InventoryTextureManager.loadBlockTexture(3);   // STONE
        InventoryTextureManager.loadBlockTexture(4);   // GOLD
        InventoryTextureManager.loadBlockTexture(5);   // OAK_PLANKS
        InventoryTextureManager.loadBlockTexture(6);   // OAK_LOG
        InventoryTextureManager.loadBlockTexture(7);   // GLASS
        
        // Загружаем все текстуры инструментов
        InventoryTextureManager.loadItemTexture(101);  // Stone Pickaxe
        InventoryTextureManager.loadItemTexture(102);  // Wood Pickaxe
        InventoryTextureManager.loadItemTexture(103);  // Iron Pickaxe
        InventoryTextureManager.loadItemTexture(104);  // Diamond Pickaxe
        InventoryTextureManager.loadItemTexture(111);  // Wood Axe
        InventoryTextureManager.loadItemTexture(112);  // Iron Axe
        InventoryTextureManager.loadItemTexture(121);  // Wood Shovel
        InventoryTextureManager.loadItemTexture(122);  // Iron Shovel
        
        // Загружаем все текстуры оружия
        InventoryTextureManager.loadItemTexture(201);  // Wooden Sword
        InventoryTextureManager.loadItemTexture(202);  // Stone Sword
        InventoryTextureManager.loadItemTexture(203);  // Iron Sword
        InventoryTextureManager.loadItemTexture(204);  // Diamond Sword
        
        // Выводим статистику
        System.out.println(InventoryTextureManager.getStats());
    }

    /**
     * ПРИМЕР 2: Работа с кешированием текстур
     */
    public static void textureCachingExample() {
        System.out.println("=== Пример кеширования ===");
        
        // Первая загрузка - загружает текстуру из файла
        int textureId1 = InventoryTextureManager.loadItemTexture(103);
        System.out.println("Первая загрузка, ID: " + textureId1);
        
        // Вторая загрузка - берёт из кеша (быстро)
        int textureId2 = InventoryTextureManager.loadItemTexture(103);
        System.out.println("Вторая загрузка, ID: " + textureId2);
        
        // Проверяем, что это один и тот же объект
        System.out.println("Из кеша: " + (textureId1 == textureId2)); // true
        
        // Получение текстуры без загрузки
        int cachedId = InventoryTextureManager.getItemTextureId(103);
        System.out.println("Кешированный ID: " + cachedId);
    }

    /**
     * ПРИМЕР 3: Загрузка текстур при добавлении нового предмета
     */
    public static void loadCustomItemTextureExample() {
        System.out.println("=== Пример загрузки кастомного предмета ===");
        
        // Создаём новый предмет (ID 150)
        Item customTool = new Item.ItemBuilder(150, "Custom Tool", ItemType.TOOL)
                .durability(500)
                .addEfficiency(hrd.h4rdykrft.block.BlockGroup.STONE, 10.0f)
                .build();
        
        System.out.println("Создан предмет: " + customTool.getName() + " (ID: " + customTool.getId() + ")");
        
        // Загружаем его текстуру
        // Убедитесь, что файл находится в: textures/inventory/items/150.png
        int textureId = InventoryTextureManager.loadItemTexture(150);
        
        if (textureId > 0) {
            System.out.println("Текстура успешно загружена! ID: " + textureId);
        } else {
            System.out.println("ОШИБКА: Текстура не найдена!");
            System.out.println("Убедитесь, что файл находится в: textures/inventory/items/150.png");
        }
    }

    /**
     * ПРИМЕР 4: Обработка ошибок загрузки текстур
     */
    public static void errorHandlingExample() {
        System.out.println("=== Пример обработки ошибок ===");
        
        // Попытка загрузить несуществующую текстуру
        int textureId = InventoryTextureManager.loadItemTexture(999);
        
        if (textureId == 0) {
            System.out.println("Текстура не найдена (ID: 999)");
            System.out.println("Используем текстуру по умолчанию");
            
            // Или загружаем текстуру по умолчанию
            textureId = InventoryTextureManager.loadItemTexture(101); // Stone Pickaxe
        }
        
        System.out.println("Используем текстуру с ID: " + textureId);
    }

    /**
     * ПРИМЕР 5: Очистка кеша при выходе из игры
     */
    public static void cleanupExample() {
        System.out.println("=== Очистка ресурсов ===");
        
        System.out.println("Было загружено: " + InventoryTextureManager.getStats());
        
        // Очищаем все текстуры перед выходом
        InventoryTextureManager.clearCache();
        
        System.out.println("Кеш очищен");
    }

    /**
     * ПРИМЕР 6: Проверка структуры папок текстур
     */
    public static String getTextureStructureInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== Структура текстур инвентаря ===\n\n");
        
        info.append("БЛОКИ (textures/inventory/blocks/):\n");
        info.append("  0.png  - AIR\n");
        info.append("  1.png  - DIRT\n");
        info.append("  2.png  - GRASS\n");
        info.append("  3.png  - STONE\n");
        info.append("  4.png  - GOLD\n");
        info.append("  5.png  - OAK_PLANKS\n");
        info.append("  6.png  - OAK_LOG\n");
        info.append("  7.png  - GLASS\n\n");
        
        info.append("ИНСТРУМЕНТЫ (textures/inventory/items/):\n");
        info.append("  101.png - Stone Pickaxe\n");
        info.append("  102.png - Wood Pickaxe\n");
        info.append("  103.png - Iron Pickaxe\n");
        info.append("  104.png - Diamond Pickaxe\n");
        info.append("  111.png - Wood Axe\n");
        info.append("  112.png - Iron Axe\n");
        info.append("  121.png - Wood Shovel\n");
        info.append("  122.png - Iron Shovel\n\n");
        
        info.append("ОРУЖИЕ (textures/inventory/items/):\n");
        info.append("  201.png - Wooden Sword\n");
        info.append("  202.png - Stone Sword\n");
        info.append("  203.png - Iron Sword\n");
        info.append("  204.png - Diamond Sword\n");
        
        return info.toString();
    }

    /**
     * ПРИМЕР 7: Интеграция с инвентарём
     */
    public static void inventoryTextureIntegrationExample(Inventory inventory) {
        System.out.println("=== Интеграция с инвентарём ===");
        
        // Добавляем предмет
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE, 1);
        inventory.addItems(pickaxe);
        
        // InventorySlot автоматически загрузит текстуру
        // Путь: textures/inventory/items/103.png
        
        System.out.println("Предмет добавлен в инвентарь");
        System.out.println("Текстура загружена автоматически из: textures/inventory/items/103.png");
    }

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║  Примеры работы с текстурами инвентаря HARDYCRAFT      ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");
        
        System.out.println(getTextureStructureInfo());
        
        System.out.println("\n--- ПРИМЕРЫ КОДА ---\n");
        
        // initializeTexturesExample();
        // textureCachingExample();
        // loadCustomItemTextureExample();
        // errorHandlingExample();
        // cleanupExample();
        
        System.out.println("\nРаскомментируйте нужный пример для его запуска!");
    }
}
