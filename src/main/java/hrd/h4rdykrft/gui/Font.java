package hrd.h4rdykrft.gui;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTBakedChar;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBTruetype.stbtt_BakeFontBitmap;

public class Font {
    public int textureId;
    public STBTTBakedChar.Buffer charData;
    public static final int BITMAP_W = 512;
    public static final int BITMAP_H = 512;

    public Font(String filepath, float fontSize) {
        try {
            // Читаем TTF файл в байтовый буфер
            byte[] ttf = Files.readAllBytes(Paths.get(filepath));
            ByteBuffer ttfBuffer = BufferUtils.createByteBuffer(ttf.length);
            ttfBuffer.put(ttf).flip();

            // Создаем пустой буфер для картинки (1 байт на пиксель)
            ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);

            // Выделяем память под данные символов (ASCII 32..126)
            charData = STBTTBakedChar.malloc(96);

            // Запекаем шрифт в bitmap начиная с 32 символа (пробел)
            stbtt_BakeFontBitmap(ttfBuffer, fontSize, bitmap, BITMAP_W, BITMAP_H, 32, charData);

            // Создаем текстуру в OpenGL
            textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);

            // ИСПРАВЛЕНИЕ: Выравнивание строго ДО загрузки данных в glTexImage2D
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            // Настройка ограничений, чтобы края букв не "мылились" и не двоились
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            // Настройка фильтрации для сглаживания текста
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            // Загружаем данные. Формат GL_RED, так как bitmap хранит только яркость (1 канал)
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, BITMAP_W, BITMAP_H, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);

            // Отвязываем текстуру
            glBindTexture(GL_TEXTURE_2D, 0);

        } catch (Exception e) {
            System.err.println("Не удалось загрузить шрифт: " + filepath);
            e.printStackTrace();
        }
    }

    public void cleanup() {
        glDeleteTextures(textureId);
        if (charData != null) {
            charData.free();
        }
    }
}
