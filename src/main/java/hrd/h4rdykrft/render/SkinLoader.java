package hrd.h4rdykrft.render;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SkinLoader {
    public static void ensureSkinExists(String path) {
        File file = new File(path);
        if (file.exists()) return;

        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        // Новые размеры текстуры скина: 64x64
        int w = 64, h = 64;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Заливка фона темным
        g.setColor(new Color(20, 20, 20));
        g.fillRect(0, 0, w, h);

        // Стандартная развертка Minecraft 64x64 (g, u, v, width, height, depth, базовыйОттенок)
        // Голова
        drawBoxTemplate(g, 0, 0, 8, 8, 8, 0);
        // Правая нога (вверху слева в текстуре)
        drawBoxTemplate(g, 0, 16, 4, 12, 4, 40);
        // Туловище
        drawBoxTemplate(g, 16, 16, 8, 12, 4, 80);
        // Правая рука
        drawBoxTemplate(g, 40, 16, 4, 12, 4, 120);

        // Левая нога (в новом стандарте 64x64 находится внизу)
        drawBoxTemplate(g, 16, 48, 4, 12, 4, 160);
        // Левая рука
        drawBoxTemplate(g, 32, 48, 4, 12, 4, 200);

        g.dispose();
        try {
            ImageIO.write(img, "png", file);
            System.out.println("[SkinLoader] Создана текстура 64x64 с пограничной заливкой: " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void drawBoxTemplate(Graphics2D g, int u, int v, int w, int h, int d, int colorShift) {
        // Каждая грань получает свой уникальный, сдвинутый по цветовому спектру тон

        // Top (Верхняя грань)
        g.setColor(new Color((colorShift + 30) % 255, 60, 60));
        g.fillRect(u + d, v, w, d);

        // Bottom (Нижняя грань)
        g.setColor(new Color((colorShift + 60) % 255, 120, 60));
        g.fillRect(u + d + w, v, w, d);

        // Left (Левая грань)
        g.setColor(new Color(60, (colorShift + 90) % 255, 60));
        g.fillRect(u, v + d, d, h);

        // Front (Передняя грань)
        g.setColor(new Color(60, 60, (colorShift + 120) % 255));
        g.fillRect(u + d, v + d, w, h);

        // Right (Правая грань)
        g.setColor(new Color((colorShift + 150) % 255, (colorShift + 150) % 255, 60));
        g.fillRect(u + d + w, v + d, d, h);

        // Back (Задняя грань)
        g.setColor(new Color((colorShift + 180) % 255, 60, (colorShift + 180) % 255));
        g.fillRect(u + d + w + d, v + d, w, h);
    }
}