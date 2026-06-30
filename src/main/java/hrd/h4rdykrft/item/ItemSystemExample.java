package hrd.h4rdykrft.item;

/**
 * Пример использования системы предметов
 */
public class ItemSystemExample {
    
    /**
     * Пример создания инвентаря и добавления предметов
     */
    public static void inventoryExample() {
        // Создаём стаки предметов
        ItemStack stonePickaxe = new ItemStack(Items.STONE_PICKAXE, 1);
        ItemStack woodSword = new ItemStack(Items.WOODEN_SWORD, 1);
        ItemStack dirt = new ItemStack(Items.DIRT, 32);
        ItemStack moreNDirt = new ItemStack(Items.DIRT, 40);
        
        System.out.println("Создано: " + stonePickaxe);
        System.out.println("Создано: " + woodSword);
        System.out.println("Создано: " + dirt);
        
        // Тест повреждения
        stonePickaxe.damage();
        System.out.println("Кирка после урона: " + stonePickaxe);
        System.out.println("Процент прочности: " + stonePickaxe.getDurabilityPercent() + "%");
        
        // Тест объединения стаков
        int remaining = dirt.add(moreNDirt.getCount());
        System.out.println("Земля после объединения: " + dirt);
        System.out.println("Осталось предметов: " + remaining);
    }
    
    /**
     * Пример использования эффективности инструментов
     */
    public static void toolEfficiencyExample() {
        ItemStack ironPickaxe = new ItemStack(Items.IRON_PICKAXE, 1);
        ItemStack ironAxe = new ItemStack(Items.IRON_AXE, 1);
        
        // Ломание разных блоков
        float stoneTime = ToolManager.getMiningTime(ironPickaxe, hrd.h4rdykrft.block.Blocks.STONE);
        float woodTime = ToolManager.getMiningTime(ironAxe, hrd.h4rdykrft.block.Blocks.OAK_LOG);
        float dirtTime = ToolManager.getMiningTime(ironPickaxe, hrd.h4rdykrft.block.Blocks.DIRT);
        
        System.out.println("Время ломания камня кирой: " + stoneTime + " тиков");
        System.out.println("Время ломания бревна топором: " + woodTime + " тиков");
        System.out.println("Время ломания земли кирой: " + dirtTime + " тиков");
        
        // Проверка эффективности
        boolean canBreakStone = ToolManager.canBreakBlockEfficiently(ironPickaxe, hrd.h4rdykrft.block.Blocks.STONE);
        boolean canBreakWood = ToolManager.canBreakBlockEfficiently(ironPickaxe, hrd.h4rdykrft.block.Blocks.OAK_LOG);
        
        System.out.println("Кирка эффективна на камне: " + canBreakStone);
        System.out.println("Кирка эффективна на дереве: " + canBreakWood);
    }
    
    /**
     * Пример использования урона оружия
     */
    public static void weaponDamageExample() {
        ItemStack diamondSword = new ItemStack(Items.DIAMOND_SWORD, 1);
        ItemStack emptyHand = null;
        
        float swordDamage = ToolManager.getWeaponDamage(diamondSword);
        float handDamage = ToolManager.getWeaponDamage(emptyHand);
        
        System.out.println("Урон алмазного меча: " + swordDamage);
        System.out.println("Урон голой рукой: " + handDamage);
        
        // Тест повреждения при ударе
        ToolManager.damageWeaponOnHit(diamondSword);
        System.out.println("Меч после удара: " + diamondSword);
    }
    
    /**
     * Пример использования групп блоков
     */
    public static void blockGroupExample() {
        hrd.h4rdykrft.block.Block stone = hrd.h4rdykrft.block.Blocks.STONE;
        hrd.h4rdykrft.block.Block wood = hrd.h4rdykrft.block.Blocks.OAK_LOG;
        
        System.out.println("Группа камня: " + stone.getGroup());
        System.out.println("Группа дерева: " + wood.getGroup());
        System.out.println("Базовое время ломания камня: " + stone.getGroup().getBaseMiningTime());
        System.out.println("Базовое время ломания дерева: " + wood.getGroup().getBaseMiningTime());
    }
    
    public static void main(String[] args) {
        System.out.println("=== Пример системы предметов ===\n");
        
        System.out.println("--- Инвентарь ---");
        inventoryExample();
        
        System.out.println("\n--- Эффективность инструментов ---");
        toolEfficiencyExample();
        
        System.out.println("\n--- Урон оружия ---");
        weaponDamageExample();
        
        System.out.println("\n--- Группы блоков ---");
        blockGroupExample();
    }
}
