package hrd.h4rdykrft.item;

/**
 * Стак предметов - конкретный экземпляр предмета с количеством и прочностью
 */
public class ItemStack {
    private final Item item;
    private int count;
    private int durability;

    /**
     * Создать стак предметов
     * @param item тип предмета
     * @param count количество предметов
     */
    public ItemStack(Item item, int count) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be greater than 0");
        }
        this.item = item;
        this.count = Math.min(count, item.getMaxStackSize());
        this.durability = item.getMaxDurability();
    }

    /**
     * Создать стак предметов с определённой прочностью
     */
    public ItemStack(Item item, int count, int durability) {
        this(item, count);
        this.durability = Math.max(0, Math.min(durability, item.getMaxDurability()));
    }

    // === Геттеры ===
    public Item getItem() { return item; }
    public int getCount() { return count; }
    public int getDurability() { return durability; }
    public int getMaxDurability() { return item.getMaxDurability(); }

    /**
     * Получить прочность в процентах (0-100)
     */
    public int getDurabilityPercent() {
        if (item.getMaxDurability() <= 0) {
            return 100;
        }
        return (int) ((durability / (float) item.getMaxDurability()) * 100);
    }

    /**
     * Проверить, сломан ли предмет
     */
    public boolean isBroken() {
        return durability <= 0;
    }

    /**
     * Установить количество предметов
     */
    public void setCount(int count) {
        this.count = Math.max(0, Math.min(count, item.getMaxStackSize()));
    }

    /**
     * Уменьшить прочность на 1
     */
    public void damage() {
        if (item.getMaxDurability() > 0) {
            durability = Math.max(0, durability - 1);
        }
    }

    /**
     * Уменьшить прочность на указанное количество
     */
    public void damage(int amount) {
        if (item.getMaxDurability() > 0) {
            durability = Math.max(0, durability - amount);
        }
    }

    /**
     * Восстановить прочность
     */
    public void repair(int amount) {
        durability = Math.min(item.getMaxDurability(), durability + amount);
    }

    /**
     * Восстановить прочность полностью
     */
    public void repairFully() {
        durability = item.getMaxDurability();
    }

    /**
     * Добавить предметы в стак
     * @return количество предметов, которые не поместились
     */
    public int add(int amount) {
        int canAdd = item.getMaxStackSize() - count;
        int added = Math.min(canAdd, amount);
        count += added;
        return amount - added;
    }

    /**
     * Удалить предметы из стака
     * @return количество действительно удалённых предметов
     */
    public int remove(int amount) {
        int removed = Math.min(amount, count);
        count -= removed;
        return removed;
    }

    /**
     * Проверить, может ли предмет быть объединён с другим
     */
    public boolean canCombineWith(ItemStack other) {
        if (other == null) return false;
        if (other.item.getId() != this.item.getId()) return false;
        // Предметы с прочностью не объединяются в классические стаки
        return item.getMaxDurability() <= 0;
    }

    /**
     * Объединить с другим стаком
     * @return количество предметов, которые не поместились
     */
    public int combine(ItemStack other) {
        if (!canCombineWith(other)) {
            return other.count;
        }
        return add(other.count);
    }

    /**
     * Создать копию стака
     */
    public ItemStack copy() {
        return new ItemStack(item, count, durability);
    }

    /**
     * Проверить, пуст ли стак
     */
    public boolean isEmpty() {
        return count <= 0;
    }

    @Override
    public String toString() {
        String durabilityStr = item.getMaxDurability() > 0
                ? String.format(" [%d/%d]", durability, item.getMaxDurability())
                : "";
        return String.format("ItemStack{%s x%d%s}", item.getName(), count, durabilityStr);
    }
}
