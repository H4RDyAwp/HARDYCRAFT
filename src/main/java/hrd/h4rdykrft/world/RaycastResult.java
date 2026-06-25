package hrd.h4rdykrft.world;

import org.joml.Vector3f;

public class RaycastResult {
    public boolean hit;
    // Координаты блока, в который врезался луч (для ломания)
    public int blockX, blockY, blockZ;
    // Координаты блока прямо ПЕРЕД точкой столкновения (для установки)
    public int airX, airY, airZ;

    public static RaycastResult trace(World world, Vector3f start, Vector3f direction, float maxDistance) {
        RaycastResult result = new RaycastResult();
        Vector3f currentPos = new Vector3f(start);
        float stepSize = 0.05f; // Размер шага луча. Чем меньше, тем точнее выбор

        int lastAirX = (int) Math.floor(currentPos.x);
        int lastAirY = (int) Math.floor(currentPos.y);
        int lastAirZ = (int) Math.floor(currentPos.z);

        for (float length = 0; length < maxDistance; length += stepSize) {
            // Сдвигаем точку луча вперед
            currentPos.add(direction.x * stepSize, direction.y * stepSize, direction.z * stepSize);

            int x = (int) Math.floor(currentPos.x);
            int y = (int) Math.floor(currentPos.y);
            int z = (int) Math.floor(currentPos.z);

            byte blockId = world.getBlock(x, y, z);

            if (blockId != 0) { // Наткнулись на любой блок, кроме воздуха (AIR)
                result.hit = true;
                result.blockX = x;
                result.blockY = y;
                result.blockZ = z;
                result.airX = lastAirX;
                result.airY = lastAirY;
                result.airZ = lastAirZ;
                return result;
            }

            // Запоминаем текущую позицию воздуха перед следующим шагом в твердь
            lastAirX = x;
            lastAirY = y;
            lastAirZ = z;
        }

        result.hit = false;
        return result;
    }
}