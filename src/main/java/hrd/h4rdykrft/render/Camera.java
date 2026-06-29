package hrd.h4rdykrft.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private final Vector3f position = new Vector3f();
    private float pitch, yaw;

    /**
     * Привязываем камеру к трансформу сущности (например, игрока).
     * Высота глаз игрока в Minecraft обычно ~1.62 блока над его ногами.
     */
    public void update(Vector3f entityPos, float entityPitch, float entityYaw) {
        this.position.set(entityPos.x, entityPos.y, entityPos.z); // Смещение на уровень глаз
        this.pitch = entityPitch;
        this.yaw = entityYaw;
    }

    public Matrix4f getViewMatrix() {
        Vector3f front = new Vector3f();
        front.x = (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        front.y = (float) Math.sin(Math.toRadians(pitch));
        front.z = (float) (Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)));
        front.normalize();

        Vector3f center = new Vector3f(position).add(front);
        return new Matrix4f().lookAt(position, center, new Vector3f(0, 1, 0));
    }

    public Vector3f getPosition() {
        return position;
    }
}