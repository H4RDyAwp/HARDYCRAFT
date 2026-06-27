#version 330 core
in vec2 TexCoord;
in vec3 Normal;

out vec4 FragColor;

uniform sampler2D texture1;

void main() {
    vec4 texColor = texture(texture1, TexCoord);
    if(texColor.a < 0.1) {
        discard;
    }

    // Нормализуем вектор нормали на случай погрешностей интерполяции
    vec3 norm = normalize(Normal);

    // Задаем базовый рассеянный свет (чтобы в тени не было полной темноты)
    float ambient = 0.8;

    // Имитируем источник света сверху-сбоку (типичное солнце: x=0.3, y=1.0, z=0.2)
    vec3 lightDir = normalize(vec3(0.3, 1.0, 0.2));

    // Вычисляем коэффициент освещенности (диффузное отражение Ламберта)
    // clamp ограничивает значения от 0.0 до 1.0
    float diff = clamp(dot(norm, lightDir), 0.0, 1.0);

    // Объединяем фоновый и направленный свет (максимум равен 1.0)
    float lighting = clamp(ambient + diff, 0.0, 1.0);

    // Умножаем текстуру на полученный коэффициент освещения
    vec3 result = texColor.rgb * lighting;

    FragColor = vec4(result, texColor.a);
}