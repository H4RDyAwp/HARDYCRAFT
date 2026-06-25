package hrd.h4rdykrft.gui;

import hrd.h4rdykrft.render.Shader;

public abstract class UIElement {
    public float x, y, width, height;
    public boolean visible = true;

    public UIElement(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render(Shader shader);
}