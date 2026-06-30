package hrd.h4rdykrft.block;

/**
 * Группа материала для блоков.
 * Каждой группе соответствует множитель эффективности для инструментов
 */
public enum BlockGroup {
    STONE(1.0f),      // Камень, кирпич
    DIRT(0.5f),       // Земля, трава
    WOOD(1.5f),       // Дерево, доски
    METAL(2.0f),      // Руда металла
    SAND(0.3f),       // Песок, гравий
    GLASS(1.0f),      // Стекло
    PLANT(0.2f);      // Растения, листья

    private final float baseMiningTime;

    BlockGroup(float baseMiningTime) {
        this.baseMiningTime = baseMiningTime;
    }

    public float getBaseMiningTime() {
        return baseMiningTime;
    }
}
