package hrd.h4rdykrft.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public class PlayerModel {
    private ModelPart head, torso, rightArm, leftArm, rightLeg, leftLeg;
    private int textureId;
    private float animTime = 0.0f;

    public PlayerModel() {
        String skinPath = "textures/skin.png";
        SkinLoader.ensureSkinExists(skinPath);

        // ВНИМАНИЕ: Замени на вызов своего загрузчика текстур из движка, например Texture.load(path)
        this.textureId = new Texture("textures/skin.png").id; // Заглушка, подставь свой ID загруженной текстуры skinPath

        float s = 1.0f / 16.0f; // Коэффициент масштаба Minecraft (1px = 1/16 блока)

        // Инициализация по каноничным UV и пиксельным размерам Minecraft:
        // Конструктор: (u, v, w, h, d,  local_x1, local_y1, local_z1,  local_x2, local_y2, local_z2)
        head = new ModelPart(0, 0, 8, 8, 8, -4*s, 0, -4*s, 4*s, 8*s, 4*s);
        head.pivotX = 0; head.pivotY = 24 * s; head.pivotZ = 0;

        torso = new ModelPart(16, 16, 8, 12, 4, -4*s, -12*s, -2*s, 4*s, 0, 2*s);
        torso.pivotX = 0; torso.pivotY = 24 * s; torso.pivotZ = 0;

        rightArm = new ModelPart(40, 16, 4, 12, 4, -4*s, -12*s, -2*s, 0, 0, 2*s);
        rightArm.pivotX = -4 * s; rightArm.pivotY = 24 * s; rightArm.pivotZ = 0;

        // В классической схеме 64x32 левые конечности берут текстуру правых
        leftArm = new ModelPart(40, 16, 4, 12, 4, 0, -12*s, -2*s, 4*s, 0, 2*s);
        leftArm.pivotX = 4 * s; leftArm.pivotY = 24 * s; leftArm.pivotZ = 0;

        rightLeg = new ModelPart(0, 16, 4, 12, 4, -2*s, -12*s, -2*s, 2*s, 0, 2*s);
        rightLeg.pivotX = -2 * s; rightLeg.pivotY = 12 * s; rightLeg.pivotZ = 0;

        leftLeg = new ModelPart(0, 16, 4, 12, 4, -2*s, -12*s, -2*s, 2*s, 0, 2*s);
        leftLeg.pivotX = 2 * s; leftLeg.pivotY = 12 * s; leftLeg.pivotZ = 0;
    }

    public void update(boolean isMoving, float deltaTime) {
        if (isMoving) {
            animTime += deltaTime * 12.0f; // Скорость махов ногами/руками
            float swing = (float) Math.sin(animTime) * 0.65f; // Амплитуда махов

            rightArm.rotateX = swing;
            leftArm.rotateX = -swing;
            rightLeg.rotateX = -swing;
            leftLeg.rotateX = swing;
        } else {
            // Плавное затухание анимации в позу покоя (Idle)
            animTime = 0;
            rightArm.rotateX += (0 - rightArm.rotateX) * 0.15f;
            leftArm.rotateX += (0 - leftArm.rotateX) * 0.15f;
            rightLeg.rotateX += (0 - rightLeg.rotateX) * 0.15f;
            leftLeg.rotateX += (0 - leftLeg.rotateX) * 0.15f;
        }
    }

    public void render(Matrix4f view, Matrix4f proj, Shader shader, Vector3f pos, float yaw) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        shader.bind();
        shader.setUniform("projection", proj);
        shader.setUniform("view", view);

        // Рендерим каждую деталь относительно позиции игрока
        renderPart(head, shader, pos, yaw);
        renderPart(torso, shader, pos, yaw);
        renderPart(rightArm, shader, pos, yaw);
        renderPart(leftArm, shader, pos, yaw);
        renderPart(rightLeg, shader, pos, yaw);
        renderPart(leftLeg, shader, pos, yaw);
    }

    private void renderPart(ModelPart part, Shader shader, Vector3f pos, float yaw) {
        Matrix4f model = new Matrix4f();
        model.identity();

        // 1. Позиция в мире
        model.translate(pos);
        // 2. Поворот тела игрока
        model.rotateY((float) Math.toRadians(-yaw + 90)); // Корректируй базовый угол под ориентацию твоего мира
        // 3. Перенос в точку сустава (Pivot)
        model.translate(part.pivotX, part.pivotY, part.pivotZ);

        // 4. Вращение сустава конечности
        if (part.rotateZ != 0) model.rotateZ(part.rotateZ);
        if (part.rotateY != 0) model.rotateY(part.rotateY);
        if (part.rotateX != 0) model.rotateX(part.rotateX);

        shader.setUniform("model", model);
        part.render();
    }

    public void cleanup() {
        head.cleanup(); torso.cleanup();
        rightArm.cleanup(); leftArm.cleanup();
        rightLeg.cleanup(); leftLeg.cleanup();
    }
}