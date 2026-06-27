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

    // Простой контейнер для одновременного возврата ширины и высоты
    public record TextSize(float width, float height) {}

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

    /**
     * Вычисляет ширину и точную высоту текста за один проход по строке.
     */
    public TextSize getStringSize(String text) {
        if (text == null || text.isEmpty()) {
            return new TextSize(0, 0);
        }

        float width = 0;
        float minTop = 0;
        float maxBottom = 0;
        boolean hasValidChars = false;

        for (int i = 0; i < text.length(); i++) {
            int charIndex = text.charAt(i) - 32;

            if (charIndex >= 0 && charIndex < charData.capacity()) {
                STBTTBakedChar bc = charData.get(charIndex);

                // Считаем ширину
                width += bc.xadvance();

                // Считаем вертикальные границы
                if (bc.y0() < minTop) minTop = bc.y0();
                if (bc.y1() > maxBottom) maxBottom = bc.y1();

                hasValidChars = true;
            }
        }

        float height = hasValidChars ? (maxBottom - minTop) : 0;
        return new TextSize(width, height);
    }

    /**
     * Возвращает фиксированную (максимальную) высоту строки шрифта.
     * Рекомендуется для создания кнопок и элементов интерфейса.
     */
    public float getFontHeight() {
        float maxAscent = 0;
        float maxDescent = 0;

        for (int i = 0; i < charData.capacity(); i++) {
            STBTTBakedChar bc = charData.get(i);
            if (bc.y0() < maxAscent) maxAscent = bc.y0();
            if (bc.y1() > maxDescent) maxDescent = bc.y1();
        }

        return Math.abs(maxAscent) + Math.abs(maxDescent);
    }

    public void cleanup() {
        glDeleteTextures(textureId);
        if (charData != null) {
            charData.free();
        }
    }
}