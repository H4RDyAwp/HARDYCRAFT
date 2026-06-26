package hrd.h4rdykrft.world;

import hrd.h4rdykrft.math.FastNoiseLite;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    // Пул фоновых потоков для генерации ландшафта и операций ввода-вывода (I/O)
    private final ExecutorService threadPool = Executors.newFixedThreadPool(
            Math.max(1, Runtime.getRuntime().availableProcessors() - 1)
    );
    // Потокобезопасный набор для отслеживания чанков, которые СЕЙЧАС генерируются или загружаются
    private final Set<Long> loadingChunks = ConcurrentHashMap.newKeySet();

    // Директория, в которую будут складываться файлы регионов/чанков
    private final Path saveFolder = Paths.get("world_data");

    public World() {
        noise = new FastNoiseLite();
        noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        noise.SetSeed(14888);
        noise.SetFrequency(0.015f);

        // Инициализируем папку сохранений при запуске
        try {
            Files.createDirectories(saveFolder);
        } catch (IOException e) {
            System.err.println("Не удалось создать папку сохранения мира: " + e.getMessage());
        }
    }

    public void update(float playerX, float playerZ) {
        int playerChunkX = (int) Math.floor(playerX / Chunk.SIZE_X);
        int playerChunkZ = (int) Math.floor(playerZ / Chunk.SIZE_Z);

        int submittedThisFrame = 0;
        int maxSubmissionsPerFrame = 4; // Лимит генераций/загрузок за кадр, чтобы разгрузить CPU

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

                        // Передаем тяжелую задачу в фоновый поток (загрузка с диска или генерация шума)
                        threadPool.submit(() -> {
                            try {
                                Chunk chunk;
                                if (hasSaveFile(cx, cz)) {
                                    chunk = loadChunkFromFile(cx, cz);
                                    if (chunk == null) {
                                        // Восстановление: если файл поврежден, генерируем заново
                                        chunk = new Chunk(cx, cz, noise);
                                    }
                                } else {
                                    chunk = new Chunk(cx, cz, noise);
                                }

                                chunks.put(key, chunk);

                                // Обновляем соседние чанки, чтобы убрать дыры на стыках
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

        // Выгрузка старых чанков с сохранением измененных данных
        Iterator<Map.Entry<Long, Chunk>> iterator = chunks.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Chunk> entry = iterator.next();
            Chunk chunk = entry.getValue();

            if (Math.abs(chunk.chunkX - playerChunkX) > RENDER_DISTANCE + 1 ||
                    Math.abs(chunk.chunkZ - playerChunkZ) > RENDER_DISTANCE + 1) {

                // Чтобы избежать фризов, запись массива байт на жесткий диск выполняется в фоновом потоке
                final Chunk chunkToSave = chunk;
                threadPool.submit(() -> saveChunkToFile(chunkToSave));

                // Очистку OpenGL-ресурсов (VAO/VBO) выполняем сразу в главном потоке (требование графического API)
                chunk.cleanup();
                iterator.remove();
            }
        }
    }

    // --- ВНУТРЕННЯЯ СИСТЕМА СОХРАНЕНИЯ (NIO) ---

    private Path getChunkPath(int cx, int cz) {
        return saveFolder.resolve("chunk_" + cx + "_" + cz + ".bin");
    }

    private boolean hasSaveFile(int cx, int cz) {
        return Files.exists(getChunkPath(cx, cz));
    }



    private Chunk loadChunkFromFile(int cx, int cz) {
        try {
            Path path = getChunkPath(cx, cz);
            byte[] data = Files.readAllBytes(path);
            return new Chunk(cx, cz, data);
        } catch (IOException e) {
            System.err.println("Ошибка при чтении чанка [" + cx + ", " + cz + "]: " + e.getMessage());
            return null;
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
            chunk.isDirty = true; // Маркируем чанк, чтобы пересобрать меш и записать изменения

            // ОПТИМИЗАЦИЯ ГРАНИЦ: Если блок изменен на краю чанка, маркируем соседний чанк как dirty
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

    private void saveChunkToFile(Chunk chunk) {
        // Если в чанке ничего не менялось, просто выходим — файл на диске создавать/перезаписывать не нужно
        if (!chunk.hasChanges) {
            return;
        }

        try {
            Path path = getChunkPath(chunk.chunkX, chunk.chunkZ);
            byte[] data = chunk.getBlocksData();
            Files.write(path, data);

            // Сбрасываем флаг, так как изменения успешно записаны на диск
            chunk.hasChanges = false;
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении чанка [" + chunk.chunkX + ", " + chunk.chunkZ + "]: " + e.getMessage());
        }
    }

    public void shutdown() {
        System.out.println("Начало сохранения измененных чанков перед выходом...");
        threadPool.shutdown(); // Останавливаем принятие новых фоновых задач

        int savedCount = 0;

        for (Chunk chunk : chunks.values()) {
            // Проверяем флаг изменений перед записью на диск
            if (chunk.hasChanges) {
                saveChunkToFile(chunk);
                savedCount++;
            }
            chunk.cleanup(); // Освобождаем OpenGL-память для всех чанков без исключения
        }

        System.out.println("Игра успешно закрыта. Сохранено измененных чанков: " + savedCount);
    }
}