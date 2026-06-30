package hrd.h4rdykrft.item;

import hrd.h4rdykrft.block.Blocks;

/**
 * Логический тест всех функций ItemStack и стека предметов
 * Работает без OpenGL и GUI зависимостей
 */
public class InventoryLogicTest {
    
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("   ЛОГИЧЕСКИЙ ТЕСТ INVENTORY - ВСЕ ФУНКЦИИ");
        System.out.println("═══════════════════════════════════════════════════════\n");
        
        testItemStackCreation();
        testItemStackDurability();
        testItemStackStacking();
        testItemStackCombine();
        testBlockGroups();
        testToolEfficiency();
        testWeaponDamage();
        
        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("   ✅ ВСЕ ТЕСТЫ УСПЕШНО ЗАВЕРШЕНЫ!");
        System.out.println("═══════════════════════════════════════════════════════\n");
    }
    
    /**
     * ТЕСТ 1: Создание ItemStack
     */
    private static void testItemStackCreation() {
        System.out.println("📝 ТЕСТ 1: Создание ItemStack");
        System.out.println("─────────────────────────────────────────────────────");
        
        ItemStack dirt = new ItemStack(Items.getItem(Blocks.DIRT.getInvId()), 32);
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE, 1);
        
        System.out.println("  Блок: " + dirt);
        System.out.println("  Инструмент: " + pickaxe);
        
        if (dirt.getCount() == 32 && pickaxe.getCount() == 1) {
            System.out.println("  ✅ PASSED\n");
        } else {
            System.out.println("  ❌ FAILED\n");
        }
    }
    
    /**
     * ТЕСТ 2: Прочность инструмента
     */
    private static void testItemStackDurability() {
        System.out.println("📝 ТЕСТ 2: Прочность инструмента");
        System.out.println("─────────────────────────────────────────────────────");
        
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE, 1);
        System.out.println("  Создано: " + pickaxe);
        System.out.println("  Прочность: " + pickaxe.getDurability() + "/" + pickaxe.getMaxDurability());
        System.out.println("  Процент: " + pickaxe.getDurabilityPercent() + "%");
        
        // Урон
        pickaxe.damage(50);
        System.out.println("  После damage(50): " + pickaxe.getDurability() + " (" + pickaxe.getDurabilityPercent() + "%)");
        
        // Восстановление
        pickaxe.repair(25);
        System.out.println("  После repair(25): " + pickaxe.getDurability() + " (" + pickaxe.getDurabilityPercent() + "%)");
        
        if (pickaxe.getDurability() == 225) {
            System.out.println("  ✅ PASSED\n");
        } else {
            System.out.println("  ❌ FAILED\n");
        }
    }
    
    /**
     * ТЕСТ 3: Стеки предметов
     */
    private static void testItemStackStacking() {
        System.out.println("📝 ТЕСТ 3: Стеки предметов");
        System.out.println("─────────────────────────────────────────────────────");
        
        // Блоки с лимитом 64
        ItemStack blocks = new ItemStack(Items.getItem(Blocks.STONE.getInvId()), 100);
        System.out.println("  Создано 100 блоков, но maxStackSize=64");
        System.out.println("  Фактическое количество: " + blocks.getCount());
        
        // Добавление
        int remaining = blocks.add(50);
        System.out.println("  После add(50): количество=" + blocks.getCount() + ", осталось=" + remaining);
        
        // Удаление
        int removed = blocks.remove(20);
        System.out.println("  После remove(20): количество=" + blocks.getCount() + ", удалено=" + removed);
        
        if (blocks.getCount() == 44 && remaining == 50) {
            System.out.println("  ✅ PASSED\n");
        } else {
            System.out.println("  ❌ FAILED\n");
        }
    }
    
    /**
     * ТЕСТ 4: Объединение стеков
     */
    private static void testItemStackCombine() {
        System.out.println("📝 ТЕСТ 4: Объединение стеков");
        System.out.println("─────────────────────────────────────────────────────");
        
        ItemStack stack1 = new ItemStack(Items.getItem(Blocks.DIRT.getInvId()), 30);
        ItemStack stack2 = new ItemStack(Items.getItem(Blocks.DIRT.getInvId()), 40);
        
        System.out.println("  stack1: " + stack1);
        System.out.println("  stack2: " + stack2);
        System.out.println("  canCombineWith? " + stack1.canCombineWith(stack2));
        
        int remaining = stack1.combine(stack2);
        System.out.println("  После combine: stack1=" + stack1.getCount() + ", осталось=" + remaining);
        
        // Инструменты не объединяются
        ItemStack pickaxe1 = new ItemStack(Items.IRON_PICKAXE, 1);
        ItemStack pickaxe2 = new ItemStack(Items.IRON_PICKAXE, 1);
        System.out.println("  Две кирки canCombineWith? " + pickaxe1.canCombineWith(pickaxe2));
        
        if (stack1.getCount() == 64 && remaining == 6 && !pickaxe1.canCombineWith(pickaxe2)) {
            System.out.println("  ✅ PASSED\n");
        } else {
            System.out.println("  ❌ FAILED\n");
        }
    }
    
    /**
     * ТЕСТ 5: Группы блоков
     */
    private static void testBlockGroups() {
        System.out.println("📝 ТЕСТ 5: Группы блоков");
        System.out.println("─────────────────────────────────────────────────────");
        
        System.out.println("  STONE группа: " + Blocks.STONE.getGroup());
        System.out.println("  DIRT группа: " + Blocks.DIRT.getGroup());
        System.out.println("  Время копания STONE: " + Blocks.STONE.getGroup().getBaseMiningTime() + "с");
        System.out.println("  Время копания DIRT: " + Blocks.DIRT.getGroup().getBaseMiningTime() + "с");
        
        if (Blocks.STONE.getGroup() != null && Blocks.DIRT.getGroup() != null) {
            System.out.println("  ✅ PASSED\n");
        } else {
            System.out.println("  ❌ FAILED\n");
        }
    }
    
    /**
     * ТЕСТ 6: Эффективность инструментов
     */
    private static void testToolEfficiency() {
        System.out.println("📝 ТЕСТ 6: Эффективность инструментов");
        System.out.println("─────────────────────────────────────────────────────");
        
        Item pickaxe = Items.IRON_PICKAXE;
        System.out.println("  IRON_PICKAXE эффективность для STONE: " + 
                pickaxe.getEfficiencyMultiplier(Blocks.STONE.getGroup()) + "x");
        System.out.println("  IRON_PICKAXE эффективность для DIRT: " + 
                pickaxe.getEfficiencyMultiplier(Blocks.DIRT.getGroup()) + "x");
        System.out.println("  IRON_PICKAXE является инструментом? " + pickaxe.isTool());
        
        // Формула: miningTime = baseMiningTime / efficiency
        double stoneTime = Blocks.STONE.getGroup().getBaseMiningTime() / 
                          pickaxe.getEfficiencyMultiplier(Blocks.STONE.getGroup());
        System.out.println("  Время копания STONE железной киркой: " + String.format("%.2f", stoneTime) + "с");
        
        if (stoneTime < 1.0) {
            System.out.println("  ✅ PASSED\n");
        } else {
            System.out.println("  ❌ FAILED\n");
        }
    }
    
    /**
     * ТЕСТ 7: Урон оружия
     */
    private static void testWeaponDamage() {
        System.out.println("📝 ТЕСТ 7: Урон оружия");
        System.out.println("─────────────────────────────────────────────────────");
        
        System.out.println("  WOODEN_SWORD урон: " + Items.WOODEN_SWORD.getDamage());
        System.out.println("  IRON_SWORD урон: " + Items.IRON_SWORD.getDamage());
        System.out.println("  DIAMOND_SWORD урон: " + Items.DIAMOND_SWORD.getDamage());
        System.out.println("  WOODEN_SWORD является оружием? " + Items.WOODEN_SWORD.isWeapon());
        System.out.println("  DIAMOND_SWORD является оружием? " + Items.DIAMOND_SWORD.isWeapon());
        
        if (Items.WOODEN_SWORD.getDamage() < Items.DIAMOND_SWORD.getDamage() &&
            Items.WOODEN_SWORD.isWeapon() && Items.DIAMOND_SWORD.isWeapon()) {
            System.out.println("  ✅ PASSED\n");
        } else {
            System.out.println("  ❌ FAILED\n");
        }
    }
}
