package hrd.h4rdykrft.block;

import java.util.HashMap;
import java.util.Map;

public class Block {
    private final byte id;
    private final String name;
    private final int[] textures;
    public final int invId;
    public final boolean solid;
    private final BlockGroup group;

    public Block(int id, int invid, String name, boolean solid, int... textureIndices) {
        this(id, invid, name, solid, BlockGroup.STONE, textureIndices);
    }

    public Block(int id, int invid, String name, boolean solid, BlockGroup group, int... textureIndices) {
        this.id = (byte) id;
        this.name = name;
        this.invId = invid;
        this.solid = solid;
        this.group = group;

        if (textureIndices.length == 1) {
            this.textures = new int[]{textureIndices[0], textureIndices[0], textureIndices[0], textureIndices[0], textureIndices[0], textureIndices[0]};
        } else {
            this.textures = textureIndices;
        }
    }

    public int getId() { return id; }
    public int getTexture(int face) { return textures[face]; }
    public int getInvId() { return invId; }
    public BlockGroup getGroup() { return group; }
    public String getName() { return name; }
}