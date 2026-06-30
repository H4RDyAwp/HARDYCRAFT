# Быстрое начало с системой предметов

## 📦 Что было создано?

Полная система управления предметами для HARDYCRAFT с поддержкой:
- ✅ **Прочности** инструментов и оружия (-1 за каждое использование)
- ✅ **Урона** оружия при ударе по врагам
- ✅ **Эффективности** ломания блоков (разная скорость для разных инструментов)
- ✅ **Групп блоков** (камень, дерево, земля, руда и т.д.)
- ✅ **Множителей эффективности** для каждой группы у инструментов
- ✅ **Максимального стакирования** (количество предметов в стаке)
- ✅ **Отображения количества** в инвентаре

## 🚀 Быстрый старт

### Шаг 1: Добавить предметы в инвентарь

```java
// В классе Main
private Inventory inventory;

public void initGame() {
    inventory = new Inventory(screenWidth, screenHeight, 9, 3);
    
    // Добавляем предметы
    ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE, 1);
    ItemStack dirt = new ItemStack(Items.DIRT, 64);
    
    inventory.addItems(pickaxe);
    inventory.addItems(dirt);
}
```

### Шаг 2: Обработка ломания блока

```java
// При клике по блоку
ItemStack tool = inventory.getSelectedItemStack();
Block block = world.getBlockAt(x, y, z);

// Получить время ломания
float miningTime = ToolManager.getMiningTime(tool, block);

// После ломания
if (blockBroken) {
    ToolManager.damageToolOnBlockBreak(tool);
    
    if (ToolManager.isBroken(tool)) {
        inventory.clearSlot(inventory.getSelectedSlotIndex());
    }
}
```

### Шаг 3: Обработка удара оружием

```java
// При ударе по врагу
ItemStack weapon = inventory.getSelectedItemStack();
float damage = ToolManager.getWeaponDamage(weapon);

enemy.takeDamage((int) damage);
ToolManager.damageWeaponOnHit(weapon);
```

## 📂 Структура классов

```
hrd.h4rdykrft.item/
├── Item.java              # Определение типа предмета
├── ItemType.java          # Типы предметов
├── ItemStack.java         # Конкретный экземпляр с прочностью
├── Items.java             # Реестр всех предметов
├── ToolManager.java       # Менеджер инструментов и урона
├── ItemSystemExample.java # Примеры использования
└── IntegrationExample.java# Примеры интеграции с Main

hrd.h4rdykrft.block/
├── Block.java            # Модифицирован (добавлена группа)
├── BlockGroup.java       # Группы блоков
└── Blocks.java          # Модифицирован (добавлены группы)

hrd.h4rdykrft.gui/
├── InventorySlot.java    # Модифицирован (работа с ItemStack)
└── Inventory.java        # Модифицирован (работа с ItemStack)
```

## 🔧 Основные методы

### ItemStack
```java
ItemStack stack = new ItemStack(Items.IRON_PICKAXE, 1);
stack.getItem();              // Тип предмета
stack.getCount();             // Количество
stack.getDurability();        // Текущая прочность
stack.getDurabilityPercent(); // 0-100%
stack.damage();               // -1 прочность
stack.damage(5);              // -5 прочности
stack.repair(10);             // +10 прочности
stack.add(5);                 // Добавить 5 предметов
stack.remove(3);              // Убрать 3 предмета
```

### Inventory
```java
// Добавить предметы
int remaining = inventory.addItems(new ItemStack(Items.DIRT, 64));

// Получить выбранный предмет
ItemStack selected = inventory.getSelectedItemStack();

// Установить предмет в слот
inventory.setItemStack(0, new ItemStack(Items.IRON_SWORD, 1));

// Очистить слот
inventory.clearSlot(5);

// Получить слот по индексу
ItemStack slot = inventory.getItemStackAt(2);
```

### ToolManager
```java
// Время ломания
float time = ToolManager.getMiningTime(pickaxe, stoneBlock);

// Проверка эффективности
boolean effective = ToolManager.canBreakBlockEfficiently(pickaxe, stoneBlock);

// Урон оружия
float damage = ToolManager.getWeaponDamage(diamondSword);

// Повреждение предметов
ToolManager.damageToolOnBlockBreak(pickaxe);
ToolManager.damageWeaponOnHit(sword);

// Строка прочности
String durStr = ToolManager.getDurabilityString(pickaxe); // "100/250"
```

## 📊 Параметры предметов

### Инструменты
| Инструмент | ID | Прочность | Эффективность |
|---|---|---|---|
| Wooden Pickaxe | 102 | 60 | Камень: 2x |
| Stone Pickaxe | 101 | 131 | Камень: 8x |
| Iron Pickaxe | 103 | 250 | Камень: 6x |
| Diamond Pickaxe | 104 | 1561 | Камень: 12x |
| Wood Axe | 111 | 60 | Дерево: 8x |
| Iron Axe | 112 | 250 | Дерево: 12x |
| Wood Shovel | 121 | 60 | Земля: 8x |
| Iron Shovel | 122 | 250 | Земля: 12x |

### Оружие
| Оружие | ID | Прочность | Урон |
|---|---|---|---|
| Wooden Sword | 201 | 60 | 4.0 |
| Stone Sword | 202 | 131 | 5.0 |
| Iron Sword | 203 | 250 | 6.0 |
| Diamond Sword | 204 | 1561 | 7.0 |

### Группы блоков
| Группа | Базовое время | Примеры |
|---|---|---|
| STONE | 1.0s | Камень, кирпич |
| DIRT | 0.5s | Земля, трава |
| WOOD | 1.5s | Дерево, доски |
| METAL | 2.0s | Руда металла |
| SAND | 0.3s | Песок, гравий |
| GLASS | 1.0s | Стекло |
| PLANT | 0.2s | Растения, листья |

## 🎮 Примеры использования

Подробные примеры интеграции находятся в:
- `ItemSystemExample.java` - Примеры использования API
- `IntegrationExample.java` - Примеры интеграции с Main

## 📖 Полная документация

Полная документация по системе предметов находится в файле `ITEM_SYSTEM.md`

## ✨ Особенности

1. **Автоматический стакинг** - Предметы автоматически объединяются в стаки
2. **Множители эффективности** - Разная скорость ломания для разных инструментов
3. **Система прочности** - Инструменты и оружие изнашиваются при использовании
4. **Отображение количества** - Количество предметов показывается в инвентаре
5. **Гибкая система** - Легко добавлять новые предметы и группы блоков

## 🔗 Файлы для изменения

Для интеграции системы в основное приложение:

1. **Main.java** - Инициализация инвентаря и логика ломания блоков
2. **Player.java** - Механика атак и урона
3. **World.java** - Обработка ломания блоков
4. **UIManager.java** - Отображение инвентаря в UI

Смотрите `IntegrationExample.java` для деталей!
