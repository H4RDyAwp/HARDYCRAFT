package hrd.h4rdykrft.block;

import java.util.HashMap;
import java.util.Map;

public class Block {
    private final byte id;
    private final String name;
    private final int[] textures;

    public Block(int id, String name, int... textureIndices) {
        this.id = (byte) id;
        this.name = name;
        if (textureIndices.length == 1) {
            this.textures = new int[]{textureIndices[0], textureIndices[0], textureIndices[0], textureIndices[0], textureIndices[0], textureIndices[0]};
        } else {
            this.textures = textureIndices;
        }
    }

    public byte getId() { return id; }
    public int getTexture(int face) { return textures[face]; }
}