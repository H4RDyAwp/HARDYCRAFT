#version 330 core
out vec4 fragColor;

// ИСПРАВЛЕНИЕ: Имя должно строго совпадать с out из вершинного шейдера!
in vec2 TexCoords;

uniform sampler2D uiTexture;
uniform vec4 color;
uniform int isText;

void main() {
    if (isText == 1) {
        // 1. Читаем маску видимости буквы из КРАСНОГО канала (GL_RED)
        float alpha = texture(uiTexture, TexCoords).r;

        // 2. ИСПРАВЛЕНИЕ: Накладываем маску на переданный из Java цвет текста.
        // Берем RGB цвет из uniform color, а альфа-канал умножаем на плотность буквы.
        fragColor = vec4(color.rgb, color.a * alpha);
    } else {
        // Обычный рендер для остальных UI элементов (кнопки, панели)
        fragColor = color;
    }
}
