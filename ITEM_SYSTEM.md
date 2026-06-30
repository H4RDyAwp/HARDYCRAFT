# Система предметов HARDYCRAFT

Полная система управления предметами с поддержкой прочности, урона, эффективности ломания и групп блоков.

## Основные компоненты

### 1. **Item** - Определение предмета
Класс, описывающий тип предмета с его характеристиками:

```java
// Создание предмета через Builder
Item ironPickaxe = new Item.ItemBuilder(103, "Iron Pickaxe", ItemType.TOOL)
    .durability(250)
    .addEfficiency(BlockGroup.STONE, 6.0f)
    .addEfficiency(BlockGroup.METAL, 6.0f)
    .build();

// Геттеры
int id = ironPickaxe.getId();           // ID предмета
String name = ironPickaxe.getName();    // Название
int maxDurability = ironPickaxe.getMaxDurability(); // Макс прочность
float damage = ironPickaxe.getDamage(); // Урон
int maxStack = ironPickaxe.getMaxStackSize(); // Макс количество в стаке
```

#### Типы предметов (ItemType):
- `BLOCK` - Обычный блок (64 в стаке, без прочности)
- `TOOL` - Инструмент (1 в стаке, с прочностью)
- `WEAPON` - Оружие (1 в стаке, с урагом и прочностью)
- `CONSUMABLE` - Потребляемый предмет (может быть съедобным и т.д.)

### 2. **BlockGroup** - Группы блоков
Каждый блок принадлежит к группе, которая определяет время ломания и эффективность инструментов:

```java
// Группы:
BlockGroup.STONE    // Камень, кирпич (1.0s базовое время)
BlockGroup.DIRT     // Земля, трава (0.5s)
BlockGroup.WOOD     // Дерево, доски (1.5s)
BlockGroup.METAL    // Руда металла (2.0s)
BlockGroup.SAND     // Песок, гравий (0.3s)
BlockGroup.GLASS    // Стекло (1.0s)
BlockGroup.PLANT    // Растения, листья (0.2s)
```

Каждая группа имеет базовое время ломания, которое модифицируется эффективностью инструмента.

### 3. **ItemStack** - Стак предметов
Конкретный экземпляр предмета с количеством и прочностью:

```java
// Создание стака
ItemStack stack = new ItemStack(Items.STONE_PICKAXE, 1);
ItemStack dirtStack = new ItemStack(Items.DIRT, 32);

// Геттеры
Item item = stack.getItem();           // Тип предмета
int count = stack.getCount();          // Количество
int durability = stack.getDurability(); // Текущая прочность
int percent = stack.getDurabilityPercent(); // 0-100%
boolean broken = stack.isBroken();     // Сломан ли

// Работа с прочностью
stack.damage();       // -1 прочность
stack.damage(5);      // -5 прочности
stack.repair(10);     // +10 прочности
stack.repairFully();  // Полное восстановление

// Работа с количеством
int remaining = stack.add(5);           // Добавить 5 предметов
int removed = stack.remove(3);          // Убрать 3 предмета
stack.combine(otherStack);              // Объединить два стака

// Копирование
ItemStack copy = stack.copy();
```

### 4. **Items** - Реестр предметов
Содержит все доступные предметы в игре:

```java
// Инструменты
Items.STONE_PICKAXE   // ID: 101
Items.IRON_PICKAXE    // ID: 103
Items.DIAMOND_PICKAXE // ID: 104
Items.WOOD_AXE        // ID: 111
Items.IRON_AXE        // ID: 112
// и другие...

// Оружие
Items.WOODEN_SWORD    // ID: 201
Items.IRON_SWORD      // ID: 203
Items.DIAMOND_SWORD   // ID: 204
// и другие...

// Получение
Item item = Items.getItem(103); // По ID
boolean exists = Items.hasItem(103);
```

### 5. **ToolManager** - Менеджер инструментов
Вспомогательный класс для работы с механикой инструментов:

```java
// Время ломания
float time = ToolManager.getMiningTime(ironPickaxe, stoneBlock);
// Формула: время = базовое_время / эффективность

// Проверка эффективности
boolean effective = ToolManager.canBreakBlockEfficiently(
    ironPickaxe, 
    stoneBlock
); // true если эффективность > 1

// Урон
float damage = ToolManager.getWeaponDamage(diamondSword); // 7.0
float handDamage = ToolManager.getWeaponDamage(null);     // 2.0 (руки)

// Повреждение предметов
ToolManager.damageToolOnBlockBreak(ironPickaxe);  // -1 прочность
ToolManager.damageWeaponOnHit(diamondSword);      // -1 прочность

// Восстановление
ToolManager.repairTool(ironPickaxe, 20); // +20 прочности

// Проверки
boolean broken = ToolManager.isBroken(ironPickaxe);
String durStr = ToolManager.getDurabilityString(ironPickaxe); // "100/250"
int color = ToolManager.getDurabilityColor(ironPickaxe);      // RGB для отображения
```

## Примеры использования

### Пример 1: Добавление предмета в инвентарь

```java
// В классе Main или GameLoop
private Inventory inventory;

public void initInventory() {
    inventory = new Inventory(screenWidth, screenHeight, 9, 3);
    
    // Добавляем предметы
    ItemStack pickaxe = new ItemStack(Items.STONE_PICKAXE, 1);
    ItemStack dirt = new ItemStack(Items.DIRT, 32);
    
    inventory.addItems(pickaxe);
    inventory.addItems(dirt);
}
```

### Пример 2: Использование инструмента при ломании блока

```java
// При клике по блоку
ItemStack selectedItem = inventory.getSelectedItemStack();
Block targetBlock = world.getBlockAt(x, y, z);

// Получить время ломания
float miningTime = ToolManager.getMiningTime(selectedItem, targetBlock);

// После ломания блока
if (blockBroken) {
    ToolManager.damageToolOnBlockBreak(selectedItem);
    
    // Если предмет сломан, удалить его
    if (ToolManager.isBroken(selectedItem)) {
        inventory.clearSlot(inventory.getSelectedSlotIndex());
    }
}
```

### Пример 3: Урон при ударе оружием

```java
// При ударе по игроку/моб
ItemStack weapon = inventory.getSelectedItemStack();
float damage = ToolManager.getWeaponDamage(weapon);

enemy.takeDamage((int) damage);

// Повредить оружие
ToolManager.damageWeaponOnHit(weapon);
```

### Пример 4: Создание новых предметов

```java
// В классе Items.java
public static final Item GOLDEN_PICKAXE = register(
    new Item.ItemBuilder(105, "Golden Pickaxe", ItemType.TOOL)
        .durability(32)
        .addEfficiency(BlockGroup.STONE, 12.0f)
        .addEfficiency(BlockGroup.METAL, 12.0f)
        .addEfficiency(BlockGroup.GLASS, 12.0f)
        .build()
);
```

### Пример 5: Создание новой группы блоков

```java
// В BlockGroup.java добавить:
OBSIDIAN(4.0f), // Обсидиан, очень сложный для ломания

// В Blocks.java добавить:
public static final Block OBSIDIAN = register(
    new Block(8, 10, "obsidian", true, BlockGroup.OBSIDIAN, 10)
);
```

## Интеграция с инвентарём

Класс `Inventory` теперь полностью интегрирован с системой предметов:

```java
// Добавить предмет
int remaining = inventory.addItems(new ItemStack(Items.DIRT, 64));
if (remaining > 0) {
    System.out.println("Осталось " + remaining + " предметов");
}

// Получить выбранный предмет
ItemStack selected = inventory.getSelectedItemStack();
if (selected != null) {
    System.out.println(selected.getItem().getName() + " x" + selected.getCount());
}

// Установить предмет в слот
inventory.setItemStack(0, new ItemStack(Items.IRON_SWORD, 1));

// Очистить слот
inventory.clearSlot(5);

// Получить слот по индексу
ItemStack slot = inventory.getItemStackAt(2);
```

## Отображение количества в UI

Количество предметов автоматически отображается в слотах инвентаря:
- Если количество > 1, показывается число в правом нижнем углу слота
- Если количество = 1, число не показывается
- Для предметов с прочностью (инструменты, оружие) количество всегда 1

## Формулы и значения

### Время ломания блока
```
время_ломания = базовое_время_группы / (эффективность_инструмента)
```

Примеры:
- Камень + Кирка из железа: 1.0s / 6.0 = 0.167s
- Камень + Рука: 1.0s / 1.0 = 1.0s (базовое время * 5 за руку)

### Прочность предметов
- Деревянные инструменты: 60 использований
- Каменные инструменты: 131 использование
- Железные инструменты: 250 использований
- Алмазные инструменты: 1561 использование

### Урон оружия
- Рука: 2.0 урона
- Деревянный меч: 4.0 урона
- Каменный меч: 5.0 урона
- Железный меч: 6.0 урона
- Алмазный меч: 7.0 урона

## Требования к текстурам

Для каждого предмета нужна текстура по пути:
```
textures/inventory/{itemId}.png
```

Пример:
- `textures/inventory/101.png` - Текстура каменной кирки
- `textures/inventory/103.png` - Текстура железной кирки
