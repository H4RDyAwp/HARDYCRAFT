package hrd.h4rdykrft.math;

import org.joml.Vector3f;

public class AABB {
    public float minX, minY, minZ;
    public float maxX, maxY, maxZ;

    public AABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    // Проверка пересечения текущего хитбокса с другим хитбоксом blockBox
    public boolean intersects(AABB o) {
        return this.maxX > o.minX && this.minX < o.maxX &&
                this.maxY > o.minY && this.minY < o.maxY &&
                this.maxZ > o.minZ && this.minZ < o.maxZ;
    }

    // Смещение хитбокса вслед за движением игрока
    public void setPosition(Vector3f pos, float width, float height) {
        float halfWidth = width / 2.0f;
        this.minX = pos.x - halfWidth;
        this.maxX = pos.x + halfWidth;
        this.minY = pos.y; // Ноги игрока находятся на pos.y
        this.maxY = pos.y + height;
        this.minZ = pos.z - halfWidth;
        this.maxZ = pos.z + halfWidth;
    }
}