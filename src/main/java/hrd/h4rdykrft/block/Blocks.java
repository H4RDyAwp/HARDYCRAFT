package hrd.h4rdykrft.block;

import java.util.HashMap;
import java.util.Map;

public class Blocks {
    private static final Map<Byte, Block> REGISTRY = new HashMap<>();

    public static final Block AIR   = register(new Block(0, "air", 0));
    public static final Block DIRT  = register(new Block(1, "dirt", 2));
    public static final Block GRASS = register(new Block(2, "grass", 0, 2, 1, 1, 1, 1));
    public static final Block STONE = register(new Block(3, "stone", 3));
    public static final Block GOLD  = register(new Block(4, "gold", 4));

    private static Block register(Block block) {
        REGISTRY.put(block.getId(), block);
        return block;
    }

    public static Block get(byte id) {
        return REGISTRY.getOrDefault(id, AIR);
    }
}