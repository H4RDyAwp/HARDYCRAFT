#version 330 core

in vec2 TexCoords;
out vec4 FragColor;

uniform sampler2D uiTexture;
uniform vec4 color;

// Режим рендера: 0 = Панель, 1 = Текст, 2 = Текстура иконки
uniform int renderMode;

void main() {
    if (renderMode == 0) {
        // РЕЖИМ 0: Сплошной цвет (фоны, панели, рамка выделения слота)
        FragColor = color;
    }
    else if (renderMode == 1) {
        // РЕЖИМ 1: Текст (STB шрифт хранит данные только в красном канале)
        vec4 sampled = vec4(1.0, 1.0, 1.0, texture(uiTexture, TexCoords).r);
        FragColor = color * sampled;
    }
    else if (renderMode == 2) {
        // РЕЖИМ 2: Обычная 2D текстура (для иконок блоков из атласа)
        vec4 texColor = texture(uiTexture, TexCoords);

        // Отбрасываем полностью прозрачные пиксели (чтобы не было артефактов глубины/смешивания)
        if (texColor.a < 0.1) {
            discard;
        }

        FragColor = texColor * color;
    }
}