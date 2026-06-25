package hrd.h4rdykrft.render;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Texture {
    public int id;

    public Texture(String path) {
        id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        // GL_NEAREST сохраняет четкость пикселей (для Pixel Art)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        try (MemoryStack stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            ByteBuffer data = STBImage.stbi_load(path, w, h, comp, 4);
            if (data != null) {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w.get(), h.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
                glGenerateMipmap(GL_TEXTURE_2D);
                STBImage.stbi_image_free(data);
            } else {
                throw new RuntimeException("Не удалось загрузить текстуру: " + path);
            }
        }
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }
}