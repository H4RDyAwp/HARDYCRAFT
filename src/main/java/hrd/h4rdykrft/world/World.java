package hrd.h4rdykrft.world;

import hrd.h4rdykrft.math.FastNoiseLite;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class World {
    private final ConcurrentHashMap<Long, Chunk> chunks = new ConcurrentHashMap<>();
    private final int RENDER_DISTANCE = 16;
    private final FastNoiseLite noise;

    // ОПТИМИЗАЦИЯ: Пул фоновых потоков для генерации ландшафта
    private final ExecutorService threadPool = Executors.newFixedThreadPool(
            Math.max(1, Runtime.getRuntime().availableProcessors() - 1)
    );
    // Потокобезопасный набор для отслеживания чанков, которые СЕЙЧАС генерируются
    private final Set<Long> loadingChunks = ConcurrentHashMap.newKeySet();

    public World() {
        noise = new FastNoiseLite();
        noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        noise.SetSeed(1488);
        noise.SetFrequency(0.015f);
    }

    public void update(float playerX, float playerZ) {
        int playerChunkX = (int) Math.floor(playerX / Chunk.SIZE_X);
        int playerChunkZ = (int) Math.floor(playerZ / Chunk.SIZE_Z);

        int submittedThisFrame = 0;
        int maxSubmissionsPerFrame = 4; // Лимит генераций за кадр, чтобы разгрузить CPU

        // Сборка чанков кругами (от центра к периферии)
        for (int r = 0; r <= RENDER_DISTANCE; r++) {
            for (int x = playerChunkX - r; x <= playerChunkX + r; x++) {
                for (int z = playerChunkZ - r; z <= playerChunkZ + r; z++) {
                    // Обрабатываем только внешнее кольцо текущего радиуса r
                    if (Math.abs(x - playerChunkX) != r && Math.abs(z - playerChunkZ) != r) continue;

                    long key = getChunkKey(x, z);
                    if (!chunks.containsKey(key) && !loadingChunks.contains(key)) {
                        if (submittedThisFrame >= maxSubmissionsPerFrame) break;

                        loadingChunks.add(key);
                        final int cx = x;
                        final int cz = z;

                        // Передаем тяжелую задачу генерации в фоновый поток
                        threadPool.submit(() -> {
                            try {
                                Chunk chunk = new Chunk(cx, cz, noise);
                                chunks.put(key, chunk);

                                // Оптимизация швов: обновляем соседние чанки, чтобы убрать дыры на стыках граней
                                markNeighborDirty(cx + 1, cz);
                                markNeighborDirty(cx - 1, cz);
                                markNeighborDirty(cx, cz + 1);
                                markNeighborDirty(cx, cz - 1);
                            } finally {
                                loadingChunks.remove(key);
                            }
                        });

                        submittedThisFrame++;
                    }
                }
                if (submittedThisFrame >= maxSubmissionsPerFrame) break;
            }
            if (submittedThisFrame >= maxSubmissionsPerFrame) break;
        }

        // Выгрузка старых чанков
        Iterator<Map.Entry<Long, Chunk>> iterator = chunks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Chunk> entry = iterator.next();
            Chunk chunk = entry.getValue();

            if (Math.abs(chunk.chunkX - playerChunkX) > RENDER_DISTANCE + 1 ||
                    Math.abs(chunk.chunkZ - playerChunkZ) > RENDER_DISTANCE + 1) {
                chunk.cleanup();
                iterator.remove();
            }
        }
    }
    public void setBlock(int x, int y, int z, byte blockId) {
        if (y < 0 || y >= Chunk.SIZE_Y) return;

        int chunkX = Math.floorDiv(x, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(z, Chunk.SIZE_Z);

        Chunk chunk = chunks.get(getChunkKey(chunkX, chunkZ));
        if (chunk != null) {
            int localX = Math.floorMod(x, Chunk.SIZE_X);
            int localZ = Math.floorMod(z, Chunk.SIZE_Z);

            // Меняем блок в целевом чанке
            chunk.setLocalBlock(localX, y, localZ, blockId);

            // ОПТИМИЗАЦИЯ ГРАНИЦ: Если блок изменен на краю чанка,
            // маркируем соседний чанк как dirty, чтобы обновить смежные грани.
            if (localX == 0) markNeighborDirty(chunkX - 1, chunkZ);
            if (localX == Chunk.SIZE_X - 1) markNeighborDirty(chunkX + 1, chunkZ);
            if (localZ == 0) markNeighborDirty(chunkX, chunkZ - 1);
            if (localZ == Chunk.SIZE_Z - 1) markNeighborDirty(chunkX, chunkZ + 1);
        }
    }

    private void markNeighborDirty(int cx, int cz) {
        Chunk neighbor = chunks.get(getChunkKey(cx, cz));
        if (neighbor != null) {
            neighbor.isDirty = true;
        }
    }

    public byte getBlock(int x, int y, int z) {
        if (y < 0 || y >= Chunk.SIZE_Y) return 0;

        int chunkX = Math.floorDiv(x, Chunk.SIZE_X);
        int chunkZ = Math.floorDiv(z, Chunk.SIZE_Z);

        Chunk chunk = chunks.get(getChunkKey(chunkX, chunkZ));
        if (chunk == null) return 0;

        int localX = Math.floorMod(x, Chunk.SIZE_X);
        int localZ = Math.floorMod(z, Chunk.SIZE_Z);

        return chunk.getLocalBlock(localX, y, localZ);
    }

    public Collection<Chunk> getChunks() {
        return chunks.values();
    }

    private long getChunkKey(int x, int z) {
        return (((long) x) << 32) | (z & 0xffffffffL);
    }

    // Не забудь вызвать при закрытии игры в Main.java
    public void shutdown() {
        threadPool.shutdown();
    }
}