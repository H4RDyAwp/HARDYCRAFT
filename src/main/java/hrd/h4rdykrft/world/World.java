package hrd.h4rdykrft.world;

import hrd.h4rdykrft.math.FastNoiseLite;
import org.joml.Vector3f;

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
    public static final float DAY_LENGTH = 24000f; // Длина полного дня
    private float timeOfDay = 6000f; // Начинаем с утра (6000)
    private float timeScale = 50.0f; // Скорость течения времени (чем больше, тем быстрее)
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
        timeOfDay += 0.016 * timeScale;

        // Закольцовываем день
        if (timeOfDay >= DAY_LENGTH) {
            timeOfDay %= DAY_LENGTH;
        }
    }
    // --- МЕТОДЫ ДЛЯ СИНХРОНИЗАЦИИ (СЕРВЕР/КЛИЕНТ) ---
    public void setTimeOfDay(float time) {
        this.timeOfDay = time;
    }

    public float getTimeOfDay() {
        return timeOfDay;
    }

    // --- ВЫЧИСЛЕНИЕ ЦВЕТА НЕБА ---
    /**
     * Возвращает цвет неба в зависимости от времени суток.
     * Возвращает Vector3f(R, G, B), где значения от 0.0 до 1.0.
     */
    public Vector3f getSkyColor() {
        float phase = timeOfDay / DAY_LENGTH;

        // Ночь (0.0 - 0.25 и 0.75 - 1.0)
        if (phase < 0.2f || phase > 0.8f) {
            return new Vector3f(0.05f, 0.05f, 0.1f); // Темно-синий
        }
        // Рассвет (0.2 - 0.3)
        else if (phase >= 0.2f && phase < 0.3f) {
            float t = (phase - 0.2f) / 0.1f;
            return new Vector3f(0.05f, 0.05f, 0.1f).lerp(new Vector3f(0.6f, 0.8f, 1.0f), t);
        }
        // Закат (0.7 - 0.8)
        else if (phase > 0.7f && phase <= 0.8f) {
            float t = (phase - 0.7f) / 0.1f;
            return new Vector3f(0.6f, 0.8f, 1.0f).lerp(new Vector3f(0.8f, 0.4f, 0.2f), t)
                    .lerp(new Vector3f(0.05f, 0.05f, 0.1f), t); // Переход через оранжевый в ночь
        }
        // День (0.3 - 0.7)
        else {
            return new Vector3f(0.6f, 0.8f, 1.0f); // Голубое небо
        }
    }

    // --- ВЫЧИСЛЕНИЕ НАПРАВЛЕНИЯ СОЛНЦА (ДЛЯ ШЕЙДЕРОВ) ---
    /**
     * Возвращает нормализованный вектор направления солнца (откуда светит свет).
     */
    public Vector3f getSunDirection() {
        float angle = (timeOfDay / DAY_LENGTH) * (float) Math.PI * 2.0f;
        // Солнце вращается вокруг оси Z (или X, зависит от твоей сцены)
        float x = (float) Math.cos(angle);
        float y = (float) Math.sin(angle);
        float z = 0.2f; // Немного наклоняем, чтобы тени не были идеально ровными
        return new Vector3f(x, y, z).normalize();
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