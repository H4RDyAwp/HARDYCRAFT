#version 330 core
in vec2 TexCoord;
in vec3 Normal;

out vec4 FragColor;

uniform sampler2D texture1;
uniform vec3 sunDirection; // НОВОЕ: Направление солнца из World.java

void main() {
    // 1. Получаем цвет самой текстуры
    vec4 texColor = texture(texture1, TexCoord);

    // Пропускаем прозрачные пиксели (например, для листвы)
    if(texColor.a < 0.1) {
        discard;
    }

    // 2. Окружающее освещение (Ambient)
    // Гарантирует, что блоки в тени не будут абсолютно черными
    float ambientStrength = 0.3;
    vec3 ambient = ambientStrength * vec3(1.0, 1.0, 1.0);

    // 3. Направленное освещение (Diffuse)
    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(sunDirection);

    // Логика Солнца и Луны:
    // Если солнце опускается за горизонт (Y < 0), мы инвертируем вектор света,
    // чтобы свет шел с противоположной стороны (от Луны), но делаем его тусклее.
    vec3 actualLightDir;
    float lightIntensity;
    vec3 lightColor;

    if (lightDir.y >= 0.0) {
        // День (Солнце)
        actualLightDir = lightDir;
        lightIntensity = 1.0;

        // Добавляем теплый оранжевый оттенок на закате/рассвете
        if (lightDir.y < 0.2) {
            lightColor = mix(vec3(1.0, 0.6, 0.3), vec3(1.0, 1.0, 1.0), lightDir.y * 5.0);
        } else {
            lightColor = vec3(1.0, 1.0, 1.0);
        }
    } else {
        // Ночь (Луна)
        actualLightDir = -lightDir; // Инвертируем направление
        lightIntensity = 0.25; // Луна светит слабо
        lightColor = vec3(0.6, 0.7, 1.0); // Холодный синеватый лунный свет
    }

    // Вычисляем угол падения света на грань
    float diff = max(dot(norm, actualLightDir), 0.0);
    vec3 diffuse = diff * lightIntensity * lightColor;

    // 4. Финальный цвет = (Тень + Свет) * Текстура
    vec3 result = (ambient + diffuse) * texColor.rgb;

    FragColor = vec4(result, texColor.a);
}