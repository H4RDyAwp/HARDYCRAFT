#version 330 core
layout (location = 0) in vec3 aPos;

uniform mat4 lightSpaceMatrix; // Матрица проекции + вида Солнца
uniform mat4 model;            // Позиция чанка

void main() {
    gl_Position = lightSpaceMatrix * model * vec4(aPos, 1.0);
}