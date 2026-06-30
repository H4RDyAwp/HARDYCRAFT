package hrd.h4rdykrft.block;

import java.util.HashMap;
import java.util.Map;

public class Blocks {
    private static final Map<Byte, Block> REGISTRY = new HashMap<>();

    public static final Block AIR   = register(new Block(0, 0, "air", true, BlockGroup.STONE, 0));
    public static final Block DIRT  = register(new Block(1, 3, "dirt", true, BlockGroup.DIRT, 2));
    public static final Block GRASS = register(new Block(2, 2, "grass", true, BlockGroup.DIRT, 0, 2, 1, 1, 1, 1));
    public static final Block STONE = register(new Block(3, 4, "stone", true, BlockGroup.STONE, 3));
    public static final Block GOLD  = register(new Block(4, 5, "gold", true, BlockGroup.METAL, 4));
    public static final Block OAK_PLANKS  = register(new Block(5, 6, "oak_planks", true, BlockGroup.WOOD, 5));
    public static final Block OAK_LOG  = register(new Block(6, 7, "oak_log", true, BlockGroup.WOOD, 7, 7, 6, 6, 6, 6));
    public static final Block GLASS  = register(new Block(7, 9, "glass", false, BlockGroup.GLASS, 8));
    
    private static Block register(Block block) {
        REGISTRY.put((byte) block.getId(), block);
        return block;
    }

    public static Block get(byte id) {
        return REGISTRY.getOrDefault(id, AIR);
    }
}