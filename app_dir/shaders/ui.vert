#version 330 core
layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoords; // Координаты текстуры для букв

out vec2 TexCoords;

uniform mat4 projection;
uniform vec2 offset;

void main() {
    gl_Position = projection * vec4(aPos + offset, 0.0, 1.0);
    TexCoords = aTexCoords;
}