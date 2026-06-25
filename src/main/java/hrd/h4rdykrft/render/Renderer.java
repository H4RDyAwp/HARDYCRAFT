package hrd.h4rdykrft.render;

import hrd.h4rdykrft.world.Chunk;
import hrd.h4rdykrft.world.World;
import org.joml.Matrix4f;

public class Renderer {

    public void renderWorld(World world, Shader shader) {
        for (Chunk chunk : world.getChunks()) {

            // Если чанк только что создали (или изменили), просим его собрать 3D-модель
            if (chunk.isDirty) {
                chunk.buildMesh(world);
            }

            // Магия OpenGL: мы сдвигаем весь чанк на его законное место в мире.
            // Геометрия внутри чанка локальная (0..15), но на экране она будет там, где нужно.
            Matrix4f model = new Matrix4f().translate(
                    chunk.chunkX * Chunk.SIZE_X,
                    0,
                    chunk.chunkZ * Chunk.SIZE_Z
            );

            shader.setUniform("model", model);
            chunk.render();
        }
    }
}