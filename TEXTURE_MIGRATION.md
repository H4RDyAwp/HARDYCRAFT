# 📦 Миграция текстур инвентаря

## 🔄 Переход со старой структуры на новую

Если у вас уже были текстуры в старой структуре `textures/inventory/`, вот как их мигрировать:

## Старая структура:
```
textures/inventory/
├── 0.png   - Блоки и предметы вместе
├── 1.png
├── 2.png
├── ...
└── 10.png
```

## Новая структура:
```
textures/inventory/
├── blocks/
│   ├── 0.png
│   ├── 1.png
│   ├── 2.png
│   ├── 3.png
│   ├── 4.png
│   ├── 5.png
│   ├── 6.png
│   └── 7.png
│
└── items/
    ├── 101.png - Stone Pickaxe
    ├── 102.png - Wood Pickaxe
    ├── 103.png - Iron Pickaxe
    ├── 104.png - Diamond Pickaxe
    ├── 111.png - Wood Axe
    ├── 112.png - Iron Axe
    ├── 121.png - Wood Shovel
    ├── 122.png - Iron Shovel
    ├── 201.png - Wooden Sword
    ├── 202.png - Stone Sword
    ├── 203.png - Iron Sword
    └── 204.png - Diamond Sword
```

## 📋 Шаги миграции

### Шаг 1: Определить какие файлы это блоки, какие - предметы

**Блоки (ID 0-7):**
- 0.png → blocks/0.png (AIR)
- 1.png → blocks/1.png (DIRT)
- 2.png → blocks/2.png (GRASS)
- 3.png → blocks/3.png (STONE)
- 4.png → blocks/4.png (GOLD)
- 5.png → blocks/5.png (OAK_PLANKS)
- 6.png → blocks/6.png (OAK_LOG)
- 7.png → blocks/7.png (GLASS)

**Предметы (ID 100+, 200+):**
- Все остальные файлы → items/{ID}.png

### Шаг 2: Скопировать файлы

Используя командную строку:

**Linux/Mac:**
```bash
cd /workspaces/HARDYCRAFT/textures/inventory

# Копируем блоки
cp 0.png blocks/0.png
cp 1.png blocks/1.png
cp 2.png blocks/2.png
cp 3.png blocks/3.png
cp 4.png blocks/4.png
cp 5.png blocks/5.png
cp 6.png blocks/6.png
cp 7.png blocks/7.png

# Копируем предметы (если есть старые ID)
cp 8.png items/101.png   # Stone Pickaxe
cp 9.png items/102.png   # Wood Pickaxe
# и так далее...
```

**Windows (PowerShell):**
```powershell
$source = "textures\inventory\"
$blocks = "textures\inventory\blocks\"
$items = "textures\inventory\items\"

# Копируем блоки
Copy-Item "$source\0.png" "$blocks\0.png"
Copy-Item "$source\1.png" "$blocks\1.png"
# и так далее...
```

**Или просто используя файловый менеджер:**
1. Откройте `/workspaces/HARDYCRAFT/textures/inventory/`
2. Создайте папки `blocks/` и `items/` (если ещё не созданы)
3. Перетащите файлы в соответствующие папки
4. Переименуйте файлы если нужно

### Шаг 3: Проверить результат

Должна получиться структура:
```
textures/inventory/
├── blocks/
│   ├── 0.png
│   ├── 1.png
│   ├── 2.png
│   ├── 3.png
│   ├── 4.png
│   ├── 5.png
│   ├── 6.png
│   └── 7.png
└── items/
    ├── 101.png
    ├── 102.png
    ├── 103.png
    ├── 104.png
    ├── 111.png
    ├── 112.png
    ├── 121.png
    ├── 122.png
    ├── 201.png
    ├── 202.png
    ├── 203.png
    └── 204.png
```

### Шаг 4: Запустить игру

Система автоматически:
- ✅ Обнаружит тип предмета/блока
- ✅ Загрузит текстуру из правильной папки
- ✅ Кеширует для производительности

## 🎯 Таблица соответствия ID

| Стареый файл | Новый путь | Тип | Описание |
|---|---|---|---|
| 0.png | blocks/0.png | Блок | AIR |
| 1.png | blocks/1.png | Блок | DIRT |
| 2.png | blocks/2.png | Блок | GRASS |
| 3.png | blocks/3.png | Блок | STONE |
| 4.png | blocks/4.png | Блок | GOLD |
| 5.png | blocks/5.png | Блок | OAK_PLANKS |
| 6.png | blocks/6.png | Блок | OAK_LOG |
| 7.png | blocks/7.png | Блок | GLASS |
| 8.png | items/101.png | Инструмент | Stone Pickaxe |
| 9.png | items/102.png | Инструмент | Wood Pickaxe |

## ✅ Проверка после миграции

После миграции проверьте:

1. **Все текстуры загружаются**:
   ```
   Текстура загружена: textures/inventory/blocks/3.png
   Текстура загружена: textures/inventory/items/103.png
   ```

2. **Нет ошибок в консоли**:
   ```
   ❌ НЕ должно быть:
   Ошибка загрузки текстуры: textures/inventory/3.png
   ```

3. **Инвентарь отображает предметы**:
   - Блоки в инвентаре отображаются
   - Инструменты отображаются
   - Оружие отображается

## 🗑️ Очистка (опционально)

После успешной миграции вы можете удалить старые файлы:

```bash
# Удалить старые PNG файлы из inventory/
rm /workspaces/HARDYCRAFT/textures/inventory/0.png
rm /workspaces/HARDYCRAFT/textures/inventory/1.png
# и так далее...

# Оставить только папки
# textures/inventory/blocks/
# textures/inventory/items/
```

## 🔧 Автоматическая миграция

Если у вас много файлов, используйте скрипт:

**Linux/Mac:**
```bash
#!/bin/bash
cd textures/inventory

# Перемещаем блоки (ID 0-7)
for i in {0..7}; do
  if [ -f "$i.png" ]; then
    mv "$i.png" "blocks/$i.png"
  fi
done

# Перемещаем остальные как предметы
for file in *.png; do
  if [ -f "$file" ]; then
    mv "$file" "items/$file"
  fi
done

echo "Миграция завершена!"
```

**Windows (PowerShell):**
```powershell
$invPath = "textures\inventory\"
$blocksPath = "textures\inventory\blocks\"
$itemsPath = "textures\inventory\items\"

# Перемещаем блоки (0-7)
for ($i = 0; $i -le 7; $i++) {
  $file = "$invPath$i.png"
  if (Test-Path $file) {
    Move-Item $file "$blocksPath$i.png"
  }
}

# Перемещаем остальные в items
Get-ChildItem "$invPath*.png" | ForEach-Object {
  Move-Item $_.FullName "$itemsPath$($_.Name)"
}

Write-Host "Миграция завершена!"
```

## ⚠️ Важно

- **Резервная копия**: Перед миграцией сделайте резервную копию папки `textures/inventory/`
- **Проверьте ID**: Убедитесь, что ID в старых файлах совпадают с новыми
- **Расширение**: Используйте только `.png` файлы с прозрачностью

## 📝 Заметки

После миграции:
- Код автоматически загрузит текстуры из новых папок
- Старая структура больше не используется
- Система кеширует текстуры для производительности
- Можно добавлять новые предметы просто добавляя файлы

## ✨ Готово!

После миграции ваши текстуры будут:
- ✅ Хорошо организованы
- ✅ Легче искать и редактировать
- ✅ Готовы к расширению
- ✅ Оптимизированы для производительности
