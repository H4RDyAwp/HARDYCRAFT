package hrd.h4rdykrft.gui;
import hrd.h4rdykrft.gui.Image;
import hrd.h4rdykrft.render.Shader;
import hrd.h4rdykrft.render.InventoryTextureManager;
import hrd.h4rdykrft.block.Blocks;
import hrd.h4rdykrft.item.ItemStack;
import hrd.h4rdykrft.item.ItemType;

public class InventorySlot extends UIElement {
    private ItemStack itemStack;
    private Image imageslot;
    private Image image;

    public InventorySlot(int x, int y, ItemStack itemStack) {
        super(x, y, 50, 50);
        this.itemStack = itemStack;
        this.imageslot = new Image(x, y, 50, 50, "textures/slot.png");
        updateImage();
    }

    public InventorySlot(int x, int y) {
        this(x, y, null);
    }

    private void updateImage() {
        if (itemStack != null && !itemStack.isEmpty()) {
            int itemId = itemStack.getItem().getId();
            
            // Определяем путь в зависимости от типа предмета
            String texturePath;
            if (itemStack.getItem().getType() == ItemType.BLOCK) {
                texturePath = "textures/inventory/blocks/" + itemId + ".png";
            } else {
                texturePath = "textures/inventory/items/" + itemId + ".png";
            }
            
            this.image = new Image(x + 2, y + 2, 46, 46, texturePath);
        } else {
            this.image = null;
        }
    }

    @Override
    public void render(Shader shader) {
        this.imageslot.x = this.x;
        this.imageslot.y = this.y;
        this.imageslot.render(shader);
        
        if (this.image != null) {
            this.image.x = this.x + 2;
            this.image.y = this.y + 2;
            this.image.render(shader);
        }
        
        // Отображение количества предметов (обработка в инвентаре с помощью UI системы)
    }

    // === Геттеры ===
    public ItemStack getItemStack() {
        return itemStack;
    }

    public boolean isEmpty() {
        return itemStack == null || itemStack.isEmpty();
    }

    public int getItemId() {
        return itemStack == null ? 0 : itemStack.getItem().getId();
    }

    public int getCount() {
        return itemStack == null ? 0 : itemStack.getCount();
    }

    public int getDurability() {
        return itemStack == null ? 0 : itemStack.getDurability();
    }

    // === Сеттеры ===
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        updateImage();
    }

    public void clearSlot() {
        this.itemStack = null;
        updateImage();
    }

    /**
     * Добавить предметы в слот
     * @return количество предметов, которые не поместились
     */
    public int addItems(ItemStack stackToAdd) {
        if (isEmpty()) {
            this.itemStack = stackToAdd.copy();
            updateImage();
            return 0;
        }

        if (itemStack.canCombineWith(stackToAdd)) {
            int remaining = itemStack.add(stackToAdd.getCount());
            updateImage();
            return remaining;
        }

        return stackToAdd.getCount();
    }
}