package hrd.h4rdykrft.item;

import hrd.h4rdykrft.block.Block;
import hrd.h4rdykrft.block.Blocks;
import hrd.h4rdykrft.gui.Inventory;

/**
 * Примеры интеграции системы предметов с Main игроком и миром
 * Добавьте эти методы в класс Main для использования системы
 */
public class IntegrationExample {

    /**
     * ПРИМЕР 1: Инициализация инвентаря с предметами
     * Добавьте этот метод в Main
     */
    public static void initializeInventoryExample(Inventory inventory) {
        // Добавляем инструменты
        ItemStack woodPickaxe = new ItemStack(Items.WOOD_PICKAXE, 1);
        ItemStack ironPickaxe = new ItemStack(Items.IRON_PICKAXE, 1);
        ItemStack woodSword = new ItemStack(Items.WOODEN_SWORD, 1);
        
        // Добавляем блоки
        ItemStack dirt = new ItemStack(Items.getItem(Blocks.DIRT.getInvId()), 64);
        ItemStack stone = new ItemStack(Items.getItem(Blocks.STONE.getInvId()), 32);
        ItemStack wood = new ItemStack(Items.getItem(Blocks.OAK_PLANKS.getInvId()), 32);
        
        // Добавляем в инвентарь
        inventory.addItems(woodPickaxe);
        inventory.addItems(ironPickaxe);
        inventory.addItems(woodSword);
        inventory.addItems(dirt);
        inventory.addItems(stone);
        inventory.addItems(wood);
        
        System.out.println("Инвентарь инициализирован");
    }

    /**
     * ПРИМЕР 2: Обработка ломания блока
     * Добавьте эту логику в Main при обработке клика по блоку
     * 
     * В Main.java в методе обработки левого клика:
     */
    public static void handleBlockBreakExample(
            Inventory inventory,
            Block targetBlock,
            long breakStartTime,
            long currentTime) {
        
        // Получаем выбранный инструмент
        ItemStack selectedTool = inventory.getSelectedItemStack();
        
        // Вычисляем время ломания (в миллисекундах)
        float miningTimeSeconds = ToolManager.getMiningTime(selectedTool, targetBlock);
        long miningTimeMs = (long) (miningTimeSeconds * 50); // 50ms на тик
        
        long timeSinceStart = currentTime - breakStartTime;
        
        // Проверяем, достаточно ли времени прошло
        if (timeSinceStart >= miningTimeMs) {
            // Блок сломан!
            System.out.println("Блок сломан: " + targetBlock.getName());
            
            // Повреждаем инструмент
            ToolManager.damageToolOnBlockBreak(selectedTool);
            
            // Проверяем, не сломан ли инструмент
            if (ToolManager.isBroken(selectedTool)) {
                System.out.println("Инструмент сломался!");
                inventory.clearSlot(inventory.getSelectedSlotIndex());
            } else {
                System.out.println("Прочность: " + ToolManager.getDurabilityString(selectedTool));
            }
            
            // Добавляем дропы (если есть)
            // world.dropBlock(targetBlock, x, y, z);
        } else {
            // Отображаем прогресс ломания (0.0f - 1.0f)
            float progress = (float) timeSinceStart / miningTimeMs;
            // Можно использовать для визуализации прогресс-бара
        }
    }

    /**
     * ПРИМЕР 3: Обработка удара оружием по игроку
     * Добавьте эту логику при ударе по врагу
     */
    public static void handleWeaponAttackExample(
            Inventory inventory,
            Object enemy) { // Object для примера, замените на реальный тип врага
        
        // Получаем выбранное оружие
        ItemStack selectedWeapon = inventory.getSelectedItemStack();
        
        // Вычисляем урон
        float damage = ToolManager.getWeaponDamage(selectedWeapon);
        
        System.out.println("Атака с уроном: " + damage);
        
        // Наносим урон врагу
        // enemy.takeDamage((int) damage);
        
        // Повреждаем оружие
        ToolManager.damageWeaponOnHit(selectedWeapon);
        
        if (ToolManager.isBroken(selectedWeapon)) {
            System.out.println("Оружие сломалось!");
            inventory.clearSlot(inventory.getSelectedSlotIndex());
        }
    }

    /**
     * ПРИМЕР 4: Отображение информации о выбранном предмете в UI
     * Используйте для вывода информации о предмете в HUD
     */
    public static String getItemInfoForDisplay(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return "Рука (урон: 2.0)";
        }
        
        Item itemType = item.getItem();
        StringBuilder info = new StringBuilder();
        
        info.append(itemType.getName());
        info.append(" x").append(item.getCount());
        
        // Добавляем информацию о прочности если есть
        if (itemType.getMaxDurability() > 0) {
            info.append(" [").append(item.getDurability())
                    .append("/").append(itemType.getMaxDurability()).append("]");
        }
        
        // Добавляем информацию об уроне для оружия
        if (itemType.isWeapon()) {
            info.append(" (урон: ").append(itemType.getDamage()).append(")");
        }
        
        return info.toString();
    }

    /**
     * ПРИМЕР 5: Сбор дропа предмета
     * Используйте при взаимодействии с дропом на земле
     */
    public static int pickupDroppedItem(Inventory inventory, ItemStack droppedItem) {
        // Пытаемся добавить дроп в инвентарь
        int remaining = inventory.addItems(droppedItem);
        
        if (remaining == 0) {
            System.out.println("Предмет поднят полностью");
        } else {
            System.out.println("Инвентарь переполнен. Осталось: " + remaining + " предметов");
        }
        
        return remaining;
    }

    /**
     * ПРИМЕР 6: Создание нового инструмента (кастомизация)
     * Используйте для добавления новых инструментов в Items
     */
    public static Item createCustomPickaxe() {
        return new Item.ItemBuilder(
                150,
                "Custom Pickaxe",
                ItemType.TOOL
        )
                .durability(500)  // Очень прочная
                .addEfficiency(hrd.h4rdykrft.block.BlockGroup.STONE, 10.0f)
                .addEfficiency(hrd.h4rdykrft.block.BlockGroup.METAL, 10.0f)
                .addEfficiency(hrd.h4rdykrft.block.BlockGroup.GLASS, 10.0f)
                .addEfficiency(hrd.h4rdykrft.block.BlockGroup.DIRT, 2.0f)
                .build();
    }

    /**
     * ПРИМЕР 7: Проверка эффективности инструмента при выборе
     * Используйте для вывода информации о блоке под курсором
     */
    public static String getBlockInfoForDisplay(Block block, ItemStack selectedTool) {
        if (block == null) return "";
        
        StringBuilder info = new StringBuilder();
        info.append("Блок: ").append(block.getName());
        info.append(" (группа: ").append(block.getGroup().toString()).append(")");
        
        if (selectedTool != null && !selectedTool.isEmpty()) {
            Item tool = selectedTool.getItem();
            if (tool.isTool()) {
                float efficiency = tool.getEfficiencyMultiplier(block.getGroup());
                float miningTime = ToolManager.getMiningTime(selectedTool, block);
                
                boolean isEffective = efficiency > 1.0f;
                info.append(" | Эффективность: ").append(efficiency);
                info.append(" | Время: ").append(String.format("%.1f", miningTime)).append("s");
                info.append(" | ").append(isEffective ? "✓ Эффективно" : "✗ Неэффективно");
            }
        }
        
        return info.toString();
    }

    /**
     * ПРИМЕР 8: Восстановление предмета
     * Используйте для механики восстановления прочности
     */
    public static void repairToolExample(Inventory inventory, int slotIndex, int repairAmount) {
        ItemStack item = inventory.getItemStackAt(slotIndex);
        
        if (item != null && item.getItem().getMaxDurability() > 0) {
            int oldDurability = item.getDurability();
            ToolManager.repairTool(item, repairAmount);
            int newDurability = item.getDurability();
            
            System.out.println("Восстановлено: " + oldDurability + " -> " + newDurability);
        }
    }

    /**
     * ПРИМЕР 9: Проверка наличия предмета в инвентаре
     */
    public static boolean hasItem(Inventory inventory, int itemId, int minCount) {
        int totalCount = 0;
        
        for (hrd.h4rdykrft.gui.InventorySlot slot : inventory.getSlots()) {
            ItemStack stack = slot.getItemStack();
            if (stack != null && stack.getItem().getId() == itemId) {
                totalCount += stack.getCount();
                if (totalCount >= minCount) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * ПРИМЕР 10: Удаление предметов из инвентаря
     */
    public static int removeItems(Inventory inventory, int itemId, int countToRemove) {
        int removed = 0;
        
        for (hrd.h4rdykrft.gui.InventorySlot slot : inventory.getSlots()) {
            ItemStack stack = slot.getItemStack();
            if (stack != null && stack.getItem().getId() == itemId) {
                int removed_from_slot = stack.remove(Math.min(countToRemove - removed, stack.getCount()));
                removed += removed_from_slot;
                
                if (stack.isEmpty()) {
                    slot.clearSlot();
                }
                
                if (removed >= countToRemove) {
                    break;
                }
            }
        }
        
        return removed;
    }
}
