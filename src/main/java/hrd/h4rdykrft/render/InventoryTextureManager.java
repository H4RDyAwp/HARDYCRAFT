package hrd.h4rdykrft.render;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Менеджер для загрузки текстур инвентаря
 * Поддерживает разделённую структуру: блоки и предметы
 */
public class InventoryTextureManager {
    private static final String BLOCK_TEXTURE_PATH = "textures/inventory/blocks/";
    private static final String ITEM_TEXTURE_PATH = "textures/inventory/items/";
    
    private static final Map<String, Integer> textureCache = new HashMap<>();
    
    /**
     * Загрузить текстуру блока по его ID
     * @param blockId ID блока
     * @return ID OpenGL текстуры (0 если не найдена)
     */
    public static int loadBlockTexture(int blockId) {
        return loadTexture(BLOCK_TEXTURE_PATH + blockId + ".png", "block_" + blockId);
    }
    
    /**
     * Загрузить текстуру предмета по его ID
     * @param itemId ID предмета
     * @return ID OpenGL текстуры (0 если не найдена)
     */
    public static int loadItemTexture(int itemId) {
        return loadTexture(ITEM_TEXTURE_PATH + itemId + ".png", "item_" + itemId);
    }
    
    /**
     * Загрузить текстуру с кешированием
     */
    private static int loadTexture(String path, String cacheKey) {
        // Проверяем кеш
        if (textureCache.containsKey(cacheKey)) {
            return textureCache.get(cacheKey);
        }
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            
            // Загружаем изображение
            ByteBuffer imageData = stbi_load(path, width, height, channels, 4);
            
            if (imageData == null) {
                System.err.println("⚠️  Текстура не найдена: " + path + " - Используется ошибка-текстура");
                int errorTextureId = createErrorTexture();
                textureCache.put(cacheKey, errorTextureId);
                return errorTextureId;
            }
            
            // Создаём OpenGL текстуру
            int textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);
            
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, imageData);
            
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            
            glBindTexture(GL_TEXTURE_2D, 0);
            
            // Освобождаем память
            STBImage.stbi_image_free(imageData);
            
            // Кешируем результат
            textureCache.put(cacheKey, textureId);
            
            System.out.println("✓ Текстура загружена: " + path);
            return textureId;
            
        } catch (Exception e) {
            System.err.println("❌ Ошибка загрузки текстуры " + path + ": " + e.getMessage());
            int errorTextureId = createErrorTexture();
            textureCache.put(cacheKey, errorTextureId);
            return errorTextureId;
        }
    }
    
    /**
     * Создать черно-розовую текстуру ошибки (64x64)
     * Используется как сигнал о том, что текстура не найдена
     */
    private static int createErrorTexture() {
        final int SIZE = 64;
        final int CHECKER_SIZE = 16;
        ByteBuffer imageData = ByteBuffer.allocateDirect(SIZE * SIZE * 4);
        
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                // Определяем цвет в зависимости от позиции (шахматный паттерн)
                boolean isBlackSquare = ((x / CHECKER_SIZE) + (y / CHECKER_SIZE)) % 2 == 0;
                
                if (isBlackSquare) {
                    // Черный: 0, 0, 0, 255
                    imageData.put((byte) 0);      // R
                    imageData.put((byte) 0);      // G
                    imageData.put((byte) 0);      // B
                    imageData.put((byte) 255);    // A
                } else {
                    // Розовый/Магента: 255, 0, 255, 255
                    imageData.put((byte) 255);    // R
                    imageData.put((byte) 0);      // G
                    imageData.put((byte) 255);    // B
                    imageData.put((byte) 255);    // A
                }
            }
        }
        
        imageData.flip();
        
        // Создаём OpenGL текстуру
        int textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, SIZE, SIZE, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageData);
        
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        
        glBindTexture(GL_TEXTURE_2D, 0);
        
        System.out.println("⚠️  Создана черно-розовая текстура ошибки (ID: " + textureId + ")");
        return textureId;
    }
    
    /**
     * Получить ID кешированной текстуры без загрузки
     */
    public static int getBlockTextureId(int blockId) {
        return textureCache.getOrDefault("block_" + blockId, 0);
    }
    
    /**
     * Получить ID кешированной текстуры без загрузки
     */
    public static int getItemTextureId(int itemId) {
        return textureCache.getOrDefault("item_" + itemId, 0);
    }
    
    /**
     * Очистить весь кеш текстур
     */
    public static void clearCache() {
        for (int textureId : textureCache.values()) {
            glDeleteTextures(textureId);
        }
        textureCache.clear();
    }
    
    /**
     * Получить статистику загруженных текстур
     */
    public static String getStats() {
        return String.format("Загруженных текстур инвентаря: %d", textureCache.size());
    }
}
