package hrd.h4rdykrft.item;

import hrd.h4rdykrft.block.BlockGroup;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс предмета с параметрами прочности, урона и эффективности ломания блоков
 */
public class Item {
    private final int id;
    private final String name;
    private final ItemType type;
    private final int maxDurability;
    private final float damage;
    private final int maxStackSize;
    
    // Множители эффективности для каждой группы блоков
    private final Map<BlockGroup, Float> efficiencyMultipliers;

    private Item(ItemBuilder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.type = builder.type;
        this.maxDurability = builder.maxDurability;
        this.damage = builder.damage;
        this.maxStackSize = builder.maxStackSize;
        this.efficiencyMultipliers = new HashMap<>(builder.efficiencyMultipliers);
    }

    // === Геттеры ===
    public int getId() { return id; }
    public String getName() { return name; }
    public ItemType getType() { return type; }
    public int getMaxDurability() { return maxDurability; }
    public float getDamage() { return damage; }
    public int getMaxStackSize() { return maxStackSize; }

    /**
     * Получить множитель эффективности для группы блоков
     * Если множитель не задан, возвращает 1.0f (базовая скорость)
     */
    public float getEfficiencyMultiplier(BlockGroup group) {
        return efficiencyMultipliers.getOrDefault(group, 1.0f);
    }

    /**
     * Проверить, является ли предмет инструментом
     */
    public boolean isTool() {
        return type == ItemType.TOOL;
    }

    /**
     * Проверить, является ли предмет оружием
     */
    public boolean isWeapon() {
        return type == ItemType.WEAPON;
    }

    /**
     * Получить все множители эффективности
     */
    public Map<BlockGroup, Float> getAllEfficiencyMultipliers() {
        return new HashMap<>(efficiencyMultipliers);
    }

    // === Builder для создания предметов ===
    public static class ItemBuilder {
        private final int id;
        private final String name;
        private final ItemType type;
        private int maxDurability;
        private float damage;
        private int maxStackSize;
        private final Map<BlockGroup, Float> efficiencyMultipliers;

        public ItemBuilder(int id, String name, ItemType type) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.maxDurability = 0;
            this.damage = 0;
            this.maxStackSize = 64;
            this.efficiencyMultipliers = new HashMap<>();

            // Установка значений по умолчанию для инструментов
            if (type == ItemType.TOOL) {
                this.maxDurability = 100;
            }
            // Установка значений по умолчанию для оружия
            else if (type == ItemType.WEAPON) {
                this.maxDurability = 100;
                this.damage = 5.0f;
            }
            // Блоки обычно не имеют прочности
            else if (type == ItemType.BLOCK) {
                this.maxStackSize = 64;
            }
        }

        public ItemBuilder durability(int maxDurability) {
            this.maxDurability = maxDurability;
            return this;
        }

        public ItemBuilder damage(float damage) {
            this.damage = damage;
            return this;
        }

        public ItemBuilder maxStackSize(int maxStackSize) {
            this.maxStackSize = maxStackSize;
            return this;
        }

        /**
         * Добавить множитель эффективности для группы блоков
         */
        public ItemBuilder addEfficiency(BlockGroup group, float multiplier) {
            this.efficiencyMultipliers.put(group, multiplier);
            return this;
        }

        /**
         * Установить одинаковый множитель эффективности для всех групп
         */
        public ItemBuilder setDefaultEfficiency(float multiplier) {
            for (BlockGroup group : BlockGroup.values()) {
                this.efficiencyMultipliers.put(group, multiplier);
            }
            return this;
        }

        public Item build() {
            return new Item(this);
        }
    }

    @Override
    public String toString() {
        return String.format("Item{id=%d, name='%s', type=%s, maxDurability=%d, damage=%.1f, maxStackSize=%d}",
                id, name, type, maxDurability, damage, maxStackSize);
    }
}
