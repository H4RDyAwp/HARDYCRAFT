package hrd.h4rdykrft.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;

public class ModelPart {
    private int vao, vbo;
    private int vertexCount;

    public float pivotX, pivotY, pivotZ;
    public float rotateX, rotateY, rotateZ;

    public ModelPart(int u, int v, int w, int h, int d, float x1, float y1, float z1, float x2, float y2, float z2) {
        float ts = 1.0f / 64.0f;

        float uStart = u * ts;
        float vStart = v * ts;
        float fW = w * ts;
        float fH = h * ts;
        float fD = d * ts;

        // Вычисляем базовые UV границы для каждой из 6 граней куба
        float t_u1 = uStart + fD;           float t_v1 = vStart;
        float t_u2 = uStart + fD + fW;      float t_v2 = vStart + fD;
        float b_u1 = uStart + fD + fW;      float b_v1 = vStart;
        float b_u2 = uStart + fD + fW + fW; float b_v2 = vStart + fD;

        float l_u1 = uStart;                float l_v1 = vStart + fD;
        float l_u2 = uStart + fD;           float l_v2 = vStart + fD + fH;
        float f_u1 = uStart + fD;           float f_v1 = vStart + fD;
        float f_u2 = uStart + fD + fW;      float f_v2 = vStart + fD + fH;
        float r_u1 = uStart + fD + fW;      float r_v1 = vStart + fD;
        float r_u2 = uStart + fD + fW + fD; float r_v2 = vStart + fD + fH;
        float k_u1 = uStart + fD + fW + fD; float k_v1 = vStart + fD;
        float k_u2 = uStart + fD + fW + fD + fW; float k_v2 = vStart + fD + fH;

        // Массив вершин с полностью перенаправленными UV-координатами для каждой грани
        float[] vertices = new float[] {
                // Передняя грань (Z+) — Инвертированы U-координаты (f_u1 <-> f_u2)
                x1, y1, z2,  f_u2, f_v2,   x2, y1, z2,  f_u1, f_v2,   x2, y2, z2,  f_u1, f_v1,
                x2, y2, z2,  f_u1, f_v1,   x1, y2, z2,  f_u2, f_v1,   x1, y1, z2,  f_u2, f_v2,
                // Задняя грань (Z-) — Инвертированы U-координаты (k_u1 <-> k_u2)
                x2, y1, z1,  k_u2, k_v2,   x1, y1, z1,  k_u1, k_v2,   x1, y2, z1,  k_u1, k_v1,
                x1, y2, z1,  k_u1, k_v1,   x2, y2, z1,  k_u2, k_v1,   x2, y1, z1,  k_u2, k_v2,
                // Левая грань (X-) — Инвертированы U-координаты (l_u1 <-> l_u2)
                x1, y1, z1,  l_u2, l_v2,   x1, y1, z2,  l_u1, l_v2,   x1, y2, z2,  l_u1, l_v1,
                x1, y2, z2,  l_u1, l_v1,   x1, y2, z1,  l_u2, l_v1,   x1, y1, z1,  l_u2, l_v2,
                // Правая грань (X+) — Инвертированы U-координаты (r_u1 <-> r_u2)
                x2, y1, z2,  r_u2, r_v2,   x2, y1, z1,  r_u1, r_v2,   x2, y2, z1,  r_u1, r_v1,
                x2, y2, z1,  r_u1, r_v1,   x2, y2, z2,  r_u2, r_v1,   x2, y1, z2,  r_u2, r_v2,
                // Верхняя грань (Y+) — Исходная (Верная) привязка
                x1, y2, z2,  t_u1, t_v2,   x2, y2, z2,  t_u2, t_v2,   x2, y2, z1,  t_u2, t_v1,
                x2, y2, z1,  t_u2, t_v1,   x1, y2, z1,  t_u1, t_v1,   x1, y2, z2,  t_u1, t_v2,
                // Нижняя грань (Y-) — Исходная (Верная) привязка
                x1, y1, z1,  b_u1, b_v2,   x2, y1, z1,  b_u2, b_v2,   x2, y1, z2,  b_u2, b_v1,
                x2, y1, z2,  b_u2, b_v1,   x1, y1, z2,  b_u1, b_v1,   x1, y1, z1,  b_u1, b_v2
        };

        vertexCount = vertices.length / 5;
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 5 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);

        GL30.glBindVertexArray(0);
    }

    public void render() {
        GL30.glBindVertexArray(vao);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);
        GL30.glBindVertexArray(0);
    }

    public void cleanup() {
        GL15.glDeleteBuffers(vbo);
        GL30.glDeleteVertexArrays(vao);
    }
}
