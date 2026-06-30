package hrd.h4rdykrft.item;

import hrd.h4rdykrft.block.BlockGroup;
import java.util.HashMap;
import java.util.Map;

/**
 * Реестр всех доступных предметов в игре
 */
public class Items {
    private static final Map<Integer, Item> ITEMS = new HashMap<>();

    // === БЛОКИ ===
    public static final Item AIR = register(new Item.ItemBuilder(0, "Air", ItemType.BLOCK)
            .maxStackSize(1)
            .build());
    
    public static final Item DIRT = register(new Item.ItemBuilder(3, "Dirt", ItemType.BLOCK)
            .maxStackSize(64)
            .build());
    
    public static final Item GRASS = register(new Item.ItemBuilder(2, "Grass", ItemType.BLOCK)
            .maxStackSize(64)
            .build());
    
    public static final Item STONE = register(new Item.ItemBuilder(4, "Stone", ItemType.BLOCK)
            .maxStackSize(64)
            .build());
    
    public static final Item GOLD = register(new Item.ItemBuilder(5, "Gold Ore", ItemType.BLOCK)
            .maxStackSize(64)
            .build());
    
    public static final Item OAK_PLANKS = register(new Item.ItemBuilder(6, "Oak Planks", ItemType.BLOCK)
            .maxStackSize(64)
            .build());
    
    public static final Item OAK_LOG = register(new Item.ItemBuilder(7, "Oak Log", ItemType.BLOCK)
            .maxStackSize(64)
            .build());
    
    public static final Item GLASS = register(new Item.ItemBuilder(9, "Glass", ItemType.BLOCK)
            .maxStackSize(64)
            .build());

    // === ИНСТРУМЕНТЫ ===
    public static final Item STONE_PICKAXE = register(new Item.ItemBuilder(101, "Stone Pickaxe", ItemType.TOOL)
            .durability(131)
            .addEfficiency(BlockGroup.STONE, 8.0f)
            .addEfficiency(BlockGroup.METAL, 8.0f)
            .addEfficiency(BlockGroup.GLASS, 8.0f)
            .addEfficiency(BlockGroup.DIRT, 1.0f)
            .addEfficiency(BlockGroup.WOOD, 1.0f)
            .addEfficiency(BlockGroup.SAND, 1.0f)
            .addEfficiency(BlockGroup.PLANT, 1.0f)
            .build());

    public static final Item WOOD_PICKAXE = register(new Item.ItemBuilder(102, "Wood Pickaxe", ItemType.TOOL)
            .durability(60)
            .addEfficiency(BlockGroup.STONE, 2.0f)
            .addEfficiency(BlockGroup.METAL, 2.0f)
            .addEfficiency(BlockGroup.DIRT, 1.0f)
            .addEfficiency(BlockGroup.WOOD, 1.0f)
            .addEfficiency(BlockGroup.SAND, 1.0f)
            .addEfficiency(BlockGroup.GLASS, 2.0f)
            .addEfficiency(BlockGroup.PLANT, 1.0f)
            .build());

    public static final Item IRON_PICKAXE = register(new Item.ItemBuilder(103, "Iron Pickaxe", ItemType.TOOL)
            .durability(250)
            .addEfficiency(BlockGroup.STONE, 6.0f)
            .addEfficiency(BlockGroup.METAL, 6.0f)
            .addEfficiency(BlockGroup.GLASS, 6.0f)
            .addEfficiency(BlockGroup.DIRT, 1.0f)
            .addEfficiency(BlockGroup.WOOD, 1.0f)
            .addEfficiency(BlockGroup.SAND, 1.0f)
            .addEfficiency(BlockGroup.PLANT, 1.0f)
            .build());

    public static final Item DIAMOND_PICKAXE = register(new Item.ItemBuilder(104, "Diamond Pickaxe", ItemType.TOOL)
            .durability(1561)
            .addEfficiency(BlockGroup.STONE, 12.0f)
            .addEfficiency(BlockGroup.METAL, 12.0f)
            .addEfficiency(BlockGroup.GLASS, 12.0f)
            .addEfficiency(BlockGroup.DIRT, 1.0f)
            .addEfficiency(BlockGroup.WOOD, 1.0f)
            .addEfficiency(BlockGroup.SAND, 1.0f)
            .addEfficiency(BlockGroup.PLANT, 1.0f)
            .build());

    public static final Item WOOD_AXE = register(new Item.ItemBuilder(111, "Wood Axe", ItemType.TOOL)
            .durability(60)
            .addEfficiency(BlockGroup.WOOD, 8.0f)
            .addEfficiency(BlockGroup.PLANT, 8.0f)
            .addEfficiency(BlockGroup.STONE, 1.0f)
            .addEfficiency(BlockGroup.METAL, 1.0f)
            .addEfficiency(BlockGroup.DIRT, 1.0f)
            .addEfficiency(BlockGroup.SAND, 1.0f)
            .addEfficiency(BlockGroup.GLASS, 1.0f)
            .build());

    public static final Item IRON_AXE = register(new Item.ItemBuilder(112, "Iron Axe", ItemType.TOOL)
            .durability(250)
            .addEfficiency(BlockGroup.WOOD, 12.0f)
            .addEfficiency(BlockGroup.PLANT, 12.0f)
            .addEfficiency(BlockGroup.STONE, 1.0f)
            .addEfficiency(BlockGroup.METAL, 1.0f)
            .addEfficiency(BlockGroup.DIRT, 1.0f)
            .addEfficiency(BlockGroup.SAND, 1.0f)
            .addEfficiency(BlockGroup.GLASS, 1.0f)
            .build());

    public static final Item WOOD_SHOVEL = register(new Item.ItemBuilder(121, "Wood Shovel", ItemType.TOOL)
            .durability(60)
            .addEfficiency(BlockGroup.DIRT, 8.0f)
            .addEfficiency(BlockGroup.SAND, 8.0f)
            .addEfficiency(BlockGroup.STONE, 1.0f)
            .addEfficiency(BlockGroup.METAL, 1.0f)
            .addEfficiency(BlockGroup.WOOD, 1.0f)
            .addEfficiency(BlockGroup.GLASS, 1.0f)
            .addEfficiency(BlockGroup.PLANT, 1.0f)
            .build());

    public static final Item IRON_SHOVEL = register(new Item.ItemBuilder(122, "Iron Shovel", ItemType.TOOL)
            .durability(250)
            .addEfficiency(BlockGroup.DIRT, 12.0f)
            .addEfficiency(BlockGroup.SAND, 12.0f)
            .addEfficiency(BlockGroup.STONE, 1.0f)
            .addEfficiency(BlockGroup.METAL, 1.0f)
            .addEfficiency(BlockGroup.WOOD, 1.0f)
            .addEfficiency(BlockGroup.GLASS, 1.0f)
            .addEfficiency(BlockGroup.PLANT, 1.0f)
            .build());

    // === ОРУЖИЕ ===
    public static final Item WOODEN_SWORD = register(new Item.ItemBuilder(201, "Wooden Sword", ItemType.WEAPON)
            .durability(60)
            .damage(4.0f)
            .build());

    public static final Item STONE_SWORD = register(new Item.ItemBuilder(202, "Stone Sword", ItemType.WEAPON)
            .durability(131)
            .damage(5.0f)
            .build());

    public static final Item IRON_SWORD = register(new Item.ItemBuilder(203, "Iron Sword", ItemType.WEAPON)
            .durability(250)
            .damage(6.0f)
            .build());

    public static final Item DIAMOND_SWORD = register(new Item.ItemBuilder(204, "Diamond Sword", ItemType.WEAPON)
            .durability(1561)
            .damage(7.0f)
            .build());

    /**
     * Зарегистрировать предмет в реестре
     */
    private static <T extends Item> T register(T item) {
        if (ITEMS.containsKey(item.getId())) {
            throw new IllegalArgumentException("Item with ID " + item.getId() + " already exists!");
        }
        ITEMS.put(item.getId(), item);
        return item;
    }

    /**
     * Получить предмет по ID
     */
    public static Item getItem(int id) {
        return ITEMS.get(id);
    }

    /**
     * Получить все предметы
     */
    public static Map<Integer, Item> getAllItems() {
        return new HashMap<>(ITEMS);
    }

    /**
     * Проверить, существует ли предмет с таким ID
     */
    public static boolean hasItem(int id) {
        return ITEMS.containsKey(id);
    }
}
