package hrd.h4rdykrft.render;

public class TextureAtlas {
    public static final int ATLAS_SIZE = 512; // Размер картинки в пикселях
    public static final int TILE_SIZE = 16;   // Размер одного тайла
    public static final float STEP = (float) TILE_SIZE / ATLAS_SIZE;

    // Метод возвращает массив из 4 значений: [u1, v1, u2, v2]
    public static float[] getUV(int blockId) {
        int columns = ATLAS_SIZE / TILE_SIZE;
        int x = blockId % columns;
        int y = blockId / columns;

        float u1 = x * STEP;
        float v1 = y * STEP;
        float u2 = u1 + STEP;
        float v2 = v1 + STEP;

        return new float[] { u1, v1, u2, v2 };
    }
}