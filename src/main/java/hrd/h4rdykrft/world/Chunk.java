package hrd.h4rdykrft.world;

import hrd.h4rdykrft.block.Block;
import hrd.h4rdykrft.block.Blocks;
import hrd.h4rdykrft.math.FastNoiseLite;
import org.lwjgl.system.MemoryUtil;
import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL33.*;

public class Chunk {
    public static final int SIZE_X = 16, SIZE_Y = 512, SIZE_Z = 8;

    private static final int ATLAS_SIZE = 512;
    private static final int TILE_SIZE = 16;
    private static final float STEP = (float) TILE_SIZE / ATLAS_SIZE;

    // ОПТИМИЗАЦИЯ: Один глобальный буфер для построения мешей на главном потоке (убирает GC и нативные аллокации)
    private static final FloatBuffer meshBuffer = MemoryUtil.memAllocFloat(1200000);

    public final int chunkX, chunkZ;

    // ОПТИМИЗАЦИЯ: Одномерный массив вместо трехмерного для идеального Cache Locality
    private final byte[] voxels = new byte[SIZE_X * SIZE_Y * SIZE_Z];

    private int vao, vbo, vertexCount;
    public boolean isDirty = true;

    public Chunk(int chunkX, int chunkZ, FastNoiseLite noise) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        generateTerrain(noise);
    }

    // Индексация подстроена под порядок обхода в циклах (y - внутренний)
    private int getVoxelIndex(int x, int y, int z) {
        return (x * SIZE_Z + z) * SIZE_Y + y;
    }

    private void generateTerrain(FastNoiseLite noise) {
        for (int x = 0; x < SIZE_X; x++) {
            for (int z = 0; z < SIZE_Z; z++) {
                float gx = chunkX * SIZE_X + x;
                float gz = chunkZ * SIZE_Z + z;

                int h = (int) (20 + noise.GetNoise(gx, gz) * 15);
                h = Math.max(1, Math.min(h, SIZE_Y - 1));

                for (int y = 0; y < SIZE_Y; y++) {
                    int idx = getVoxelIndex(x, y, z);
                    if (y == h - 1) {
                        voxels[idx] = Blocks.GRASS.getId();
                    } else if (y < h - 1) {
                        voxels[idx] = (y < h - 5) ? Blocks.STONE.getId() : Blocks.DIRT.getId();
                    } else {
                        voxels[idx] = Blocks.AIR.getId();
                    }
                }
            }
        }
    }

    /**
     * ОПТИМИЗАЦИЯ: Проверка блока.
     * Если координаты лежат внутри текущего чанка, мы берем блок напрямую из массива,
     * полностью пропуская тяжелый поиск по ConcurrentHashMap в World.
     */
    private byte getBlockAt(World world, int x, int y, int z) {
        if (y < 0 || y >= SIZE_Y) return 0;
        if (x >= 0 && x < SIZE_X && z >= 0 && z < SIZE_Z) {
            return voxels[getVoxelIndex(x, y, z)];
        }
        // Только если вышли за границы чанка — запрашиваем мир
        return world.getBlock(chunkX * SIZE_X + x, y, chunkZ * SIZE_Z + z);
    }

    public void buildMesh(World world) {
        meshBuffer.clear(); // Сбрасываем позицию общего статического буфера
        vertexCount = 0;

        for (int x = 0; x < SIZE_X; x++) {
            for (int z = 0; z < SIZE_Z; z++) {
                for (int y = 0; y < SIZE_Y; y++) {
                    byte blockId = voxels[getVoxelIndex(x, y, z)];
                    if (blockId == 0) continue;

                    // Вызываем оптимизированную локальную проверку соседей
                    // Top
                    if (getBlockAt(world, x, y + 1, z) == 0)
                        addFace(meshBuffer, x, y, z, getUV(blockId, 0), 0);

                    // Bottom
                    if (getBlockAt(world, x, y - 1, z) == 0)
                        addFace(meshBuffer, x, y, z, getUV(blockId, 1), 1);

                    // Front
                    if (getBlockAt(world, x, y, z + 1) == 0)
                        addFace(meshBuffer, x, y, z, getUV(blockId, 2), 2);

                    // Back
                    if (getBlockAt(world, x, y, z - 1) == 0)
                        addFace(meshBuffer, x, y, z, getUV(blockId, 3), 3);

                    // Right
                    if (getBlockAt(world, x + 1, y, z) == 0)
                        addFace(meshBuffer, x, y, z, getUV(blockId, 4), 4);

                    // Left
                    if (getBlockAt(world, x - 1, y, z) == 0)
                        addFace(meshBuffer, x, y, z, getUV(blockId, 5), 5);
                }
            }
        }
        meshBuffer.flip();

        if (vao == 0) {
            vao = glGenVertexArrays();
            vbo = glGenBuffers();
        }
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, meshBuffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        isDirty = false;
    }

    private float[] getUV(byte blockId, int face) {
        Block block = Blocks.get(blockId);
        int textureIndex = block.getTexture(face);

        int columns = ATLAS_SIZE / TILE_SIZE;
        int texX = textureIndex % columns;
        int texY = textureIndex / columns;

        float u1 = texX * STEP;
        float v1 = texY * STEP;
        float u2 = u1 + STEP;
        float v2 = v1 + STEP;

        return new float[]{u1, v1, u2, v2};
    }

    private void addFace(FloatBuffer buffer, float x, float y, float z, float[] uv, int face) {
        float u1 = uv[0], v1 = uv[1], u2 = uv[2], v2 = uv[3];

        if (face == 0) { // Top
            addVertex(buffer, x, y+1, z, u1, v1); addVertex(buffer, x, y+1, z+1, u1, v2); addVertex(buffer, x+1, y+1, z, u2, v1);
            addVertex(buffer, x, y+1, z+1, u1, v2); addVertex(buffer, x+1, y+1, z+1, u2, v2); addVertex(buffer, x+1, y+1, z, u2, v1);
        }
        else if (face == 1) { // Bottom
            addVertex(buffer, x, y, z, u1, v1); addVertex(buffer, x+1, y, z, u2, v1); addVertex(buffer, x, y, z+1, u1, v2);
            addVertex(buffer, x+1, y, z, u2, v1); addVertex(buffer, x+1, y, z+1, u2, v2); addVertex(buffer, x, y, z+1, u1, v2);
        }
        else if (face == 2) { // Front
            addVertex(buffer, x, y, z+1, u1, v2); addVertex(buffer, x+1, y, z+1, u2, v2); addVertex(buffer, x+1, y+1, z+1, u2, v1);
            addVertex(buffer, x, y, z+1, u1, v2); addVertex(buffer, x+1, y+1, z+1, u2, v1); addVertex(buffer, x, y+1, z+1, u1, v1);
        }
        else if (face == 3) { // Back
            addVertex(buffer, x, y, z, u2, v2); addVertex(buffer, x, y+1, z, u2, v1); addVertex(buffer, x+1, y+1, z, u1, v1);
            addVertex(buffer, x, y, z, u2, v2); addVertex(buffer, x+1, y+1, z, u1, v1); addVertex(buffer, x+1, y, z, u1, v2);
        }
        else if (face == 4) { // Right
            addVertex(buffer, x+1, y, z, u1, v2); addVertex(buffer, x+1, y+1, z, u1, v1); addVertex(buffer, x+1, y+1, z+1, u2, v1);
            addVertex(buffer, x+1, y, z, u1, v2); addVertex(buffer, x+1, y+1, z+1, u2, v1); addVertex(buffer, x+1, y, z+1, u2, v2);
        }
        else { // Left
            addVertex(buffer, x, y, z, u2, v2); addVertex(buffer, x, y, z+1, u1, v2); addVertex(buffer, x, y+1, z, u2, v1);
            addVertex(buffer, x, y, z+1, u1, v2); addVertex(buffer, x, y+1, z+1, u1, v1); addVertex(buffer, x, y+1, z, u2, v1);
        }
    }

    private void addVertex(FloatBuffer buffer, float x, float y, float z, float u, float v) {
        buffer.put(x).put(y).put(z);
        buffer.put(u).put(v);
        vertexCount++;
    }

    public byte getLocalBlock(int x, int y, int z) {
        if (x < 0 || x >= SIZE_X || y < 0 || y >= SIZE_Y || z < 0 || z >= SIZE_Z) return 0;
        return voxels[getVoxelIndex(x, y, z)];
    }
    public void setLocalBlock(int x, int y, int z, byte id) {
        if (x >= 0 && x < SIZE_X && y >= 0 && y < SIZE_Y && z >= 0 && z < SIZE_Z) {

            // ВМЕСТО voxels[x][y][z] = id;
            // ИСПОЛЬЗУЕМ одномерную индексацию:
            voxels[getVoxelIndex(x, y, z)] = id;

            isDirty = true;
        }
    }
    public void render() {
        if (vertexCount == 0) return;
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
    }

    public void cleanup() {
        if (vao != 0) glDeleteVertexArrays(vao);
        if (vbo != 0) glDeleteBuffers(vbo);
    }
}