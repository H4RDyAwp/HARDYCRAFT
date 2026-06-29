package hrd.h4rdykrft.gui;

import hrd.h4rdykrft.player.Player;
import hrd.h4rdykrft.render.Shader;

public class HealthBar extends UIElement {
    private Player player;
    private Panel backgroundPanel;
    private Panel fillPanel;
    private float maxBarWidth;

    public HealthBar(float x, float y, float width, float height, Player player) {
        super(x, y, width, height);
        this.player = player;
        this.maxBarWidth = width;

        // Создаем панели один раз. Координаты и размеры будут обновляться в render.
        this.backgroundPanel = new Panel(x, y, width, height);
        this.backgroundPanel.color = new float[] { 0.15f, 0.15f, 0.15f, 0.8f };

        this.fillPanel = new Panel(x, y, width, height);
        this.fillPanel.color = new float[] { 0.8f, 0.15f, 0.15f, 1.0f };
    }

    @Override
    public void render(Shader uiShader) {
        if (!visible) return;

        // 1. Приведение к float защищает от бага целочисленного деления
        float healthRatio = (float) player.getCurrentHealth() / player.getMaxHealth();

        // 2. Ограничиваем соотношение строго в диапазоне от 0.0 до 1.0
        if (healthRatio < 0.0f) healthRatio = 0.0f;
        if (healthRatio > 1.0f) healthRatio = 1.0f;

        // 3. Рассчитываем текущую ширину
        float currentWidth = maxBarWidth * healthRatio;
        // 4. Динамически привязываем координаты панелей к координатам самого HealthBar
        // Это позволит двигать HealthBar, и фон с заполнением сдвинутся вместе с ним
        this.backgroundPanel.x = this.x;
        this.backgroundPanel.y = this.y;
        this.backgroundPanel.width = this.maxBarWidth; // На случай, если размер UI изменится
        this.backgroundPanel.height = this.height;

        this.fillPanel.x = this.x;
        this.fillPanel.y = this.y;
        this.fillPanel.width = currentWidth;
        this.fillPanel.height = this.height;
        fillPanel.setupMesh();

        // 5. Рендеринг компонентов
        backgroundPanel.render(uiShader);

        fillPanel.render(uiShader);
    }
}