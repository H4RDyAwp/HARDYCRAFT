package hrd.h4rdykrft.item;

import hrd.h4rdykrft.block.Block;
import hrd.h4rdykrft.block.BlockGroup;

/**
 * Менеджер для работы с инструментами и их характеристиками
 */
public class ToolManager {
    
    /**
     * Получить скорость ломания блока с учётом инструмента
     * @param tool инструмент (может быть null, если блок ломается голой рукой)
     * @param block блок для ломания
     * @return время ломания в тиках (20 тиков = 1 секунда)
     */
    public static float getMiningTime(ItemStack tool, Block block) {
        if (block == null) return 0;
        
        float baseMiningTime = block.getGroup().getBaseMiningTime();
        
        // Если нет инструмента, ломаем с базовой скоростью * 5
        if (tool == null || tool.isEmpty()) {
            return baseMiningTime * 5.0f;
        }
        
        Item item = tool.getItem();
        if (!item.isTool()) {
            // Не инструмент, ломаем медленно
            return baseMiningTime * 5.0f;
        }
        
        // Получаем множитель эффективности для этой группы
        float efficiency = item.getEfficiencyMultiplier(block.getGroup());
        
        // Формула: время = базовое_время / эффективность
        // Меньше времени = быстрее ломается
        return baseMiningTime / efficiency;
    }

    /**
     * Проверить, может ли инструмент ломать блок эффективно
     * (возвращает true, если эффективность > 1)
     */
    public static boolean canBreakBlockEfficiently(ItemStack tool, Block block) {
        if (block == null || tool == null || tool.isEmpty()) {
            return false;
        }
        
        Item item = tool.getItem();
        if (!item.isTool()) {
            return false;
        }
        
        return item.getEfficiencyMultiplier(block.getGroup()) > 1.0f;
    }

    /**
     * Получить урон оружия
     */
    public static float getWeaponDamage(ItemStack weapon) {
        if (weapon == null || weapon.isEmpty()) {
            return 2.0f; // Базовый урон голой рукой
        }
        
        Item item = weapon.getItem();
        if (item.isWeapon()) {
            return item.getDamage();
        }
        
        return 2.0f;
    }

    /**
     * Нанести урон инструменту при ломании блока
     */
    public static void damageToolOnBlockBreak(ItemStack tool) {
        if (tool != null && !tool.isEmpty()) {
            tool.damage();
        }
    }

    /**
     * Нанести урон оружию при ударе
     */
    public static void damageWeaponOnHit(ItemStack weapon) {
        if (weapon != null && !weapon.isEmpty()) {
            weapon.damage();
        }
    }

    /**
     * Восстановить прочность инструмента
     */
    public static void repairTool(ItemStack tool, int repairAmount) {
        if (tool != null && !tool.isEmpty()) {
            tool.repair(repairAmount);
        }
    }

    /**
     * Проверить, сломан ли инструмент/оружие
     */
    public static boolean isBroken(ItemStack item) {
        return item == null || item.isEmpty() || item.isBroken();
    }

    /**
     * Получить строковое представление прочности для отображения
     */
    public static String getDurabilityString(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return "";
        }
        
        Item itemType = item.getItem();
        if (itemType.getMaxDurability() <= 0) {
            return "";
        }
        
        return String.format("%d/%d", item.getDurability(), item.getMaxDurability());
    }

    /**
     * Получить цвет для отображения прочности (RGB)
     * 0 = красный (мало), 100 = зелёный (много)
     */
    public static int getDurabilityColor(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return 0xFF00FF00; // Зелёный
        }
        
        int percent = item.getDurabilityPercent();
        
        if (percent > 75) {
            return 0xFF00FF00; // Зелёный
        } else if (percent > 50) {
            return 0xFFFFFF00; // Жёлтый
        } else if (percent > 25) {
            return 0xFFFF8000; // Оранжевый
        } else {
            return 0xFFFF0000; // Красный
        }
    }
}
